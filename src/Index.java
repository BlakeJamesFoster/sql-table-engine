import java.util.ArrayList;
import java.util.Comparator;

/**
 * An index over a single column, mapping cell values to the row indices
 * where they occur. Built by {@code GENERATE ... INDEX} and consulted by
 * {@link Table} to speed up {@code PRINT}, {@code DELETE}, and
 * {@code JOIN} when the queried column is the indexed one.
 */
public interface Index {

    /** Records that {@code key} occurs at {@code rowIndex} in the indexed column. */
    void insert(TableEntry key, int rowIndex);

    /**
     * Returns the row indices whose indexed value matches {@code val}
     * according to {@code cmp}, i.e. {@code cmp.compare(key, val) == 0}
     * (see {@link RowComparators}).
     */
    ArrayList<Integer> lookup(Comparator<TableEntry> cmp, TableEntry val);

    /** Rebuilds the index from {@code column}'s current contents, e.g. after rows are deleted. */
    void delete(Column column);
}
