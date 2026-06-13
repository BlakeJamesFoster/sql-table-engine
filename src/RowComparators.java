import java.util.Comparator;

/**
 * Filter predicates used by {@code WHERE}, {@code DELETE FROM}, and
 * {@code JOIN} to test a {@link TableEntry} against a target value.
 *
 * <p>These are {@link Comparator}s in name only: {@link Comparator#compare}
 * returns {@code 0} when the first argument satisfies the predicate against
 * the second, and a nonzero value otherwise. This lets {@link Table} and
 * the {@link Index} implementations treat "=", "&lt;", and "&gt;" uniformly
 * as a single {@code compare(entry, value) == 0} match check, without
 * branching on which operator was requested.
 */
public class RowComparators {

    /** Matches when the two entries are equal, per {@link TableEntry#compareTo}. */
    public static class EqualsComparator implements Comparator<TableEntry> {

        @Override
        public int compare(TableEntry o1, TableEntry o2) {
            return o1.compareTo(o2);
        }
    }

    /** Matches when {@code o1} is strictly less than {@code o2}. */
    public static class LTComparator implements Comparator<TableEntry> {

        @Override
        public int compare(TableEntry o1, TableEntry o2) {
            if (o1.compareTo(o2) < 0) {
                return 0;
            }
            return 1;
        }
    }

    /** Matches when {@code o1} is strictly greater than {@code o2}. */
    public static class GTComparator implements Comparator<TableEntry> {

        @Override
        public int compare(TableEntry o1, TableEntry o2) {
            if (o1.compareTo(o2) > 0) {
                return 0;
            }
            return 1;
        }
    }

}
