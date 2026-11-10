import java.time.DayOfWeek;
import java.time.LocalDate;

interface SchedulingStrategy {
    LocalDate schedule(LocalDate date);
}

class ExactDate implements SchedulingStrategy {
    public LocalDate schedule(LocalDate date) { return date; }
}

class NextWeekday implements SchedulingStrategy {
    public LocalDate schedule(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY)
            date = date.plusDays(1);
        return date;
    }
}