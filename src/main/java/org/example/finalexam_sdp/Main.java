package org.example.finalexam_sdp;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length > 1) throw new IllegalArgumentException("Usage: java -cp out Main [data-file]");
        Path data = Path.of(args.length == 0 ? "planner.properties" : args[0]);
        if (Files.notExists(data)) Files.copy(Path.of("sample.properties"), data);
        var planner = new Planner(new PropertiesStoreAdapter(new PropertiesFile(data)));
        var dashboard = new Dashboard(Files.readString(Path.of("index.html")));
        var server = start(planner, dashboard, 8080);
        System.out.println("Open http://127.0.0.1:" + server.getAddress().getPort());
    }

    static HttpServer start(Planner planner, Dashboard dashboard, int port) throws IOException {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", request -> {
            try (request) { handle(request, planner, dashboard); }
        });
        server.start();
        return server;
    }

    private static void handle(HttpExchange request, Planner planner, Dashboard dashboard) throws IOException {
        String route = request.getRequestMethod() + " " + request.getRequestURI().getPath();
        LocalDate today = LocalDate.now();
        PlanInput input = new PlanInput("", "", "", "Exact date", today.toString());
        int id = 0;
        try {
            switch (route) {
                case "GET /" -> {
                    String edit = fields(request.getRequestURI().getRawQuery()).get("edit");
                    if (edit != null) {
                        int editId = Integer.parseInt(edit);
                        var plan = planner.view(today).plans().stream()
                                .filter(item -> item.id() == editId && !item.done()).findFirst().orElseThrow(
                                        () -> new IllegalArgumentException("Editable plan not found."));
                        id = editId;
                        input = new PlanInput(plan.pet().name(), plan.procedure().name(),
                                plan.service() == null ? "" : plan.service().name(), plan.strategy(),
                                plan.requested().toString());
                    }
                    page(request, dashboard, planner, 200, id, input, "");
                }
                case "POST /save", "POST /change" -> {
                    var bytes = request.getRequestBody().readNBytes(8193);
                    if (bytes.length > 8192) throw new IllegalArgumentException("Form is too large.");
                    var form = fields(new String(bytes, StandardCharsets.UTF_8));
                    if (route.equals("POST /save")) {
                        input = new PlanInput(value(form, "pet"), value(form, "procedure"),
                                value(form, "service"), value(form, "strategy"), value(form, "date"));
                        id = Integer.parseInt(value(form, "id"));
                        if (id < 0) throw new IllegalArgumentException("Invalid plan ID.");
                        planner.configure(id, input);
                    } else {
                        planner.change(Integer.parseInt(value(form, "id")), value(form, "action"));
                    }
                    request.getResponseHeaders().set("Location", "/");
                    request.sendResponseHeaders(303, -1);
                }
                case "GET /summary" -> {
                    request.getResponseHeaders().set("Content-Disposition", "attachment; filename=care-summary.txt");
                    send(request, 200, "text/plain", planner.summary());
                }
                default -> send(request, 404, "text/plain", "Page not found.");
            }
        } catch (NumberFormatException error) {
            page(request, dashboard, planner, 400, 0, input, "Invalid plan ID.");
        } catch (IllegalArgumentException error) {
            page(request, dashboard, planner, 400, Math.max(0, id), input, error.getMessage());
        } catch (IOException error) {
            System.err.println(error.getMessage());
            if (request.getResponseCode() < 0)
                page(request, dashboard, planner, 500, id, input, "Could not save changes.");
        }
    }

    private static String value(Map<String, String> fields, String name) {
        return fields.getOrDefault(name, "");
    }

    private static Map<String, String> fields(String encoded) {
        var result = new HashMap<String, String>();
        if (encoded == null || encoded.isEmpty()) return result;
        for (String pair : encoded.split("&")) {
            String[] parts = pair.split("=", 2);
            result.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                    parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "");
        }
        return result;
    }

    private static void page(HttpExchange request, Dashboard dashboard, Planner planner, int status,
                             int id, PlanInput input, String error) throws IOException {
        send(request, status, "text/html", dashboard.render(planner.view(LocalDate.now()), id, input, error));
    }

    private static void send(HttpExchange request, int status, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        request.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        request.sendResponseHeaders(status, bytes.length);
        request.getResponseBody().write(bytes);
    }
}