import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

interface PlanStore {
    List<Plan> load() throws IOException;
    void save(List<Plan> plans) throws IOException;
}

record PropertiesFile(Path path) {
    Properties read() throws IOException {
        var values = new Properties();
        try (var input = Files.newInputStream(path)) { values.load(input); }
        return values;
    }

    void replace(Properties values) throws IOException {
        Path temp = Files.createTempFile(path.toAbsolutePath().getParent(), "planner-", ".tmp");
        try {
            try (var output = Files.newOutputStream(temp)) { values.store(output, null); }
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}

record PropertiesStoreAdapter(PropertiesFile file) implements PlanStore {
    public List<Plan> load() throws IOException {
        var plans = new ArrayList<Plan>();
        if (Files.notExists(file.path())) return plans;
        var values = file.read();
        try {
            int count = Integer.parseInt(values.getProperty("count"));
            for (int i = 0; i < count; i++) {
                String prefix = i + ".";
                String extra = values.getProperty(prefix + "service");
                plans.add(new Plan(Integer.parseInt(values.getProperty(prefix + "id")),
                    new Pet(values.getProperty(prefix + "pet")),
                    new Procedure(values.getProperty(prefix + "procedure"),
                        Integer.parseInt(values.getProperty(prefix + "base"))),
                    extra.isEmpty() ? null : new Service(extra, Integer.parseInt(values.getProperty(prefix + "extra"))),
                    values.getProperty(prefix + "strategy"),
                    LocalDate.parse(values.getProperty(prefix + "requested")),
                    LocalDate.parse(values.getProperty(prefix + "due")),
                    Integer.parseInt(values.getProperty(prefix + "minutes")),
                    Boolean.parseBoolean(values.getProperty(prefix + "done"))));
            }
        } catch (RuntimeException error) {
            throw new IOException("Invalid planner data file.", error);
        }
        return plans;
    }

    public void save(List<Plan> plans) throws IOException {
        var values = new Properties();
        values.setProperty("count", String.valueOf(plans.size()));
        for (int i = 0; i < plans.size(); i++) {
            var plan = plans.get(i);
            String prefix = i + ".";
            values.setProperty(prefix + "id", String.valueOf(plan.id()));
            values.setProperty(prefix + "pet", plan.pet().name());
            values.setProperty(prefix + "procedure", plan.procedure().name());
            values.setProperty(prefix + "base", String.valueOf(plan.procedure().minutes()));
            values.setProperty(prefix + "service", plan.service() == null ? "" : plan.service().name());
            values.setProperty(prefix + "extra", String.valueOf(plan.service() == null ? 0 : plan.service().minutes()));
            values.setProperty(prefix + "strategy", plan.strategy());
            values.setProperty(prefix + "requested", plan.requested().toString());
            values.setProperty(prefix + "due", plan.due().toString());
            values.setProperty(prefix + "minutes", String.valueOf(plan.minutes()));
            values.setProperty(prefix + "done", String.valueOf(plan.done()));
        }
        file.replace(values);
    }
}
//
