import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

record PlanEvent(String action, Plan plan) {}

interface PlanObserver {
    void changed(PlanEvent event);
}

class PlanEvents {
    private final List<PlanObserver> observers = new ArrayList<>();

    void subscribe(PlanObserver observer) { observers.add(observer); }

    void publish(PlanEvent event) {
        for (var observer : observers) observer.changed(event);
    }
}

class ReminderBoard implements PlanObserver {
    private final HashMap<Integer, Plan> pending = new HashMap<>();

    public void changed(PlanEvent event) {
        var plan = event.plan();
        if (plan.done() || event.action().equals("deleted")) pending.remove(plan.id());
        else pending.put(plan.id(), plan);
    }

    List<Plan> dueBy(LocalDate date) {
        return pending.values().stream().filter(plan -> !plan.due().isAfter(date))
            .sorted(Comparator.comparing(Plan::due).thenComparingInt(Plan::id)).toList();
    }
}
