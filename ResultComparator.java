import java.util.Comparator;

public class ResultComparator implements Comparator<SchedulingResult> {

    @Override
    public int compare(SchedulingResult o1, SchedulingResult o2) {
        return o1.getPid()-o2.getPid();
    }
}
