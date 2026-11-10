import org.example.finalexam_sdp.Main;
import org.example.finalexam_sdp.Planner;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

public class PlannerTest {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("planner-test-");
        Path data = directory.resolve("test.properties");
        try {
            testPlanner(data);
            testWeb(directory.resolve("web.properties"));
        } finally {
            Files.deleteIfExists(directory.resolve("web.properties"));
            Files.deleteIfExists(data);
            Files.deleteIfExists(directory);
        }
        System.out.println("All 10 scenarios passed.");
    }

    private static void testPlanner(Path data) throws Exception {
        var store = new PropertiesStoreAdapter(new PropertiesFile(data));
        var planner = new Planner(store);
        var now = LocalDate.of(2024, 1, 7);
        var input = new PlanInput("Milo", "Bath", "Brushing", "Exact date", "2024-01-06");

        planner.configure(0, input);
        var first = planner.view(now).plans().getFirst();
        check(first.minutes() == 40 && first.due().toString().equals("2024-01-06"), "Create with extra");

        planner.configure(first.id(), new PlanInput("Milo", "Bath", "", "Next weekday", "2024-01-06"));
        var updated = planner.view(now).plans().getFirst();
        check(updated.id() == first.id() && updated.minutes() == 30
            && updated.due().toString().equals("2024-01-08"), "Switch strategy and edit");

        var reopened = new Planner(store).view(now);
        check(reopened.plans().equals(java.util.List.of(updated)) && reopened.reminders().size() == 1,
            "Reload and remind");

        planner.change(first.id(), "complete");
        check(planner.view(now).plans().getFirst().done() && planner.view(now).reminders().isEmpty()
            && planner.summary().contains("Milo | Bath | 2024-01-08 | 30 min | Completed"), "Completion event");

        reject(() -> planner.configure(0, new PlanInput(" ", "Bath", "", "Exact date", "2024-01-06")),
            "Pet name", "Blank pet");
        reject(() -> planner.configure(0, new PlanInput("Milo", "Bath", "", "Exact date", "2024-02-30")),
            "valid date", "Invalid date");
        reject(() -> planner.configure(0, new PlanInput("Milo", "Bath", "Unknown", "Exact date", "2024-01-06")),
            "valid extra", "Unknown extra");
        reject(() -> planner.configure(first.id(), input), "Completed plans", "Completed edit rejected");
        if (planner.view(now).plans().size() != 1) throw new AssertionError("Invalid edits changed the plan count.");

        planner.change(first.id(), "delete");
        check(planner.view(now).plans().isEmpty(), "Delete");
    }

    private static void testWeb(Path data) throws Exception {
        var planner = new Planner(new PropertiesStoreAdapter(new PropertiesFile(data)));
        var server = Main.start(planner, new Dashboard(Files.readString(Path.of("index.html"))), 0);
        try {
            var client = HttpClient.newHttpClient();
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            String form = "id=0&pet=Milo&procedure=Bath&service=Brushing&strategy=Exact+date&date=2024-01-06";
            var save = HttpRequest.newBuilder(URI.create(base + "/save"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form)).build();
            var saved = client.send(save, HttpResponse.BodyHandlers.ofString());
            var page = get(client, base + "/");
            var summary = get(client, base + "/summary");
            check(saved.statusCode() == 303 && page.statusCode() == 200
                && page.body().contains("Milo · Bath + Brushing") && page.body().contains("<strong>Milo</strong>")
                && summary.statusCode() == 200
                && summary.headers().firstValue("Content-Disposition").orElse("").startsWith("attachment")
                && summary.body().contains("40 min | Pending"), "Web save, display and export");
        } finally {
            server.stop(0);
        }
    }

    private static HttpResponse<String> get(HttpClient client, String url) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        System.out.println("PASS " + name);
    }

    private static void reject(Operation operation, String message, String name) throws Exception {
        try {
            operation.run();
            throw new AssertionError(name + " accepted invalid data");
        } catch (IllegalArgumentException error) {
            check(error.getMessage().contains(message), name);
        }
    }

    interface Operation { void run() throws Exception; }
}
