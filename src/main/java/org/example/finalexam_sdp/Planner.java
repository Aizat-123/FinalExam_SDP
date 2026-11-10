package org.example.finalexam_sdp;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class Planner {
    static final Map<String, Procedure> PROCEDURES = Map.of(
        "Bath", new Procedure("Bath", 30), "Checkup", new Procedure("Checkup", 25),
        "Nail trim", new Procedure("Nail trim", 15));
    static final Map<String, Service> SERVICES = Map.of(
        "Brushing", new Service("Brushing", 10), "Ear cleaning", new Service("Ear cleaning", 5));
    static final Map<String, SchedulingStrategy> STRATEGIES = Map.of(
        "Exact date", new ExactDate(), "Next weekday", new NextWeekday());

    private final PlanStore store;
    private final PlanEvents events = new PlanEvents();
    private final ReminderBoard board = new ReminderBoard();
    private List<Plan> plans;
    private int nextId;

    Planner(PlanStore store) throws IOException {
        this.store = store;
        plans = store.load();
        nextId = plans.stream().mapToInt(Plan::id).max().orElse(0) + 1;
        events.subscribe(board);
        for (var plan : plans) events.publish(new PlanEvent("loaded", plan));
    }

    synchronized void configure(int id, PlanInput input) throws IOException {
        var procedure = PROCEDURES.get(input.procedure());
        var service = SERVICES.get(input.service());
        var strategy = STRATEGIES.get(input.strategy());
        if (procedure == null) throw new IllegalArgumentException("Choose a valid procedure.");
        if (!input.service().isEmpty() && service == null)
            throw new IllegalArgumentException("Choose a valid extra service.");
        if (strategy == null) throw new IllegalArgumentException("Choose a valid scheduling method.");
        LocalDate date;
        try {
            date = LocalDate.parse(input.date());
        } catch (DateTimeParseException error) {
            throw new IllegalArgumentException("Enter a valid date.", error);
        }
        if (date.getYear() < 2000 || date.getYear() > 2099)
            throw new IllegalArgumentException("Enter a date between 2000 and 2099.");
        Care care = service == null ? procedure : new WithService(procedure, service);
        var plan = new PlanBuilder().forPet(input.pet()).withCare(procedure, service, care.duration())
            .scheduled(input.strategy(), date, strategy.schedule(date)).build();
        var updated = new ArrayList<>(plans);
        if (id == 0) {
            plan = plan.withStatus(nextId, false);
            updated.add(plan);
            save(updated, new PlanEvent("created", plan));
            nextId++;
        } else {
            int index = indexOf(id);
            if (plans.get(index).done()) throw new IllegalArgumentException("Completed plans cannot be edited.");
            plan = plan.withStatus(id, false);
            updated.set(index, plan);
            save(updated, new PlanEvent("updated", plan));
        }
    }

    synchronized void change(int id, String action) throws IOException {
        int index = indexOf(id);
        var updated = new ArrayList<>(plans);
        var plan = updated.get(index);
        switch (action) {
            case "complete" -> {
                if (plan.done()) throw new IllegalArgumentException("Plan is already completed.");
                plan = plan.withStatus(id, true);
                updated.set(index, plan);
                save(updated, new PlanEvent("completed", plan));
            }
            case "delete" -> {
                updated.remove(index);
                save(updated, new PlanEvent("deleted", plan));
            }
            default -> throw new IllegalArgumentException("Unknown action.");
        }
    }

    private int indexOf(int id) {
        for (int i = 0; i < plans.size(); i++)
            if (plans.get(i).id() == id) return i;
        throw new IllegalArgumentException("Plan does not exist.");
    }

    private void save(List<Plan> updated, PlanEvent event) throws IOException {
        store.save(updated);
        plans = updated;
        events.publish(event);
    }

    record View(List<Plan> plans, List<Plan> reminders) {}

    synchronized View view(LocalDate date) {
        return new View(List.copyOf(plans), board.dueBy(date.plusDays(1)));
    }

    synchronized String summary() {
        var result = new StringBuilder("Pet Care Planner\n");
        for (var plan : plans) {
            result.append(plan.pet().name()).append(" | ").append(plan.procedure().name());
            if (plan.service() != null) result.append(" + ").append(plan.service().name());
            result.append(" | ").append(plan.due()).append(" | ").append(plan.minutes())
                .append(" min | ").append(plan.done() ? "Completed" : "Pending").append('\n');
        }
        return result.toString();
    }
}
