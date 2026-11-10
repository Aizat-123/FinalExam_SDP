import org.example.finalexam_sdp.Planner;

import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

record Dashboard(String template) {
    private static final Pattern SLOT = Pattern.compile("@@([A-Z_]+)@@");

    String render(Planner.View view, int id, PlanInput input, String error) {
        var reminders = new StringBuilder();
        for (var plan : view.reminders())
            reminders.append("<li><strong>").append(escape(plan.pet().name())).append("</strong> · ")
                .append(escape(plan.procedure().name())).append(" · ").append(plan.due()).append("</li>");
        var plans = new StringBuilder();
        for (var plan : view.plans()) plans.append(card(plan));
        var slots = Map.ofEntries(
            Map.entry("ERROR", error.isEmpty() ? "" : "<section class='error' role='alert'>" + escape(error) + "</section>"),
            Map.entry("TITLE", id == 0 ? "New care plan" : "Edit care plan"),
            Map.entry("ID", String.valueOf(id)),
            Map.entry("PET", escape(input.pet())),
            Map.entry("DATE", escape(input.date())),
            Map.entry("PROCEDURES", options(Planner.PROCEDURES.keySet(), input.procedure(), "Choose a procedure")),
            Map.entry("SERVICES", options(Planner.SERVICES.keySet(), input.service(), "None")),
            Map.entry("STRATEGIES", options(Planner.STRATEGIES.keySet(), input.strategy(), "")),
            Map.entry("CANCEL", id == 0 ? "" : "<a href='/'>Cancel edit</a>"),
            Map.entry("REMINDERS", reminders.isEmpty() ? "<li>No care due yet.</li>" : reminders.toString()),
            Map.entry("PLANS", plans.isEmpty() ? "<p>No saved plans yet.</p>" : plans.toString()));
        return SLOT.matcher(template).replaceAll(match -> Matcher.quoteReplacement(slots.get(match.group(1))));
    }

    private static String card(Plan plan) {
        String extra = plan.service() == null ? "" : " + " + escape(plan.service().name());
        String status = plan.done() ? "Completed" : "Pending";
        String edit = plan.done() ? "" : "<a href='/?edit=" + plan.id() + "'>Edit plan</a>";
        String complete = plan.done() ? "" : "<button name='action' value='complete'>Mark complete</button>";
        return """
            <article class="plan"><strong>%s · %s%s</strong><p>%s · %d min · %s</p>
            <div class="actions">%s<form class="actions" method="post" action="/change">
            <input type="hidden" name="id" value="%d">
            %s<button name="action" value="delete" onclick="return confirm('Delete this care plan?')">Delete</button>
            </form></div></article>
            """.formatted(escape(plan.pet().name()), escape(plan.procedure().name()), extra, plan.due(),
                plan.minutes(), status, edit, plan.id(), complete);
    }

    private static String options(Set<String> names, String selected, String placeholder) {
        var result = new StringBuilder();
        if (!placeholder.isEmpty()) result.append("<option value=''>").append(placeholder).append("</option>");
        for (String name : new TreeSet<>(names))
            result.append("<option").append(name.equals(selected) ? " selected" : "").append(">")
                .append(escape(name)).append("</option>");
        return result.toString();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&#39;");
    }
}

