import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

/** A {@link TreeMap}-backed {@link Index}, keeping indexed values in sorted order. */
public class BSTIndex implements Index{
    TreeMap<TableEntry, ArrayList<Integer>> tree = new TreeMap<>();

    @Override
    public void insert(TableEntry key, int rowIndex) {
        tree.putIfAbsent(key, new ArrayList<>());
        tree.get(key).add(rowIndex);
    }

    /**
     * @param cmp the match predicate, applied as {@code cmp.compare(key, val) == 0}
     * @param val the value to match against each indexed key
     * @return row indices of every entry whose key matches {@code val}
     */
    @Override
    public ArrayList<Integer> lookup(Comparator<TableEntry> cmp, TableEntry val) {
        ArrayList<Integer> matchingRowIndices = new ArrayList<>();
        for (TableEntry key: tree.keySet()) {
            if (cmp.compare(key, val) == 0) {
                matchingRowIndices.addAll(tree.get(key));
            }
        }
        return matchingRowIndices;
    }

    @Override
    public void delete(Column column) {
        tree.clear();

        for (int i = 0; i < column.getData().size(); i++) {
            insert(column.getData().get(i), i);
        }
    }
}
