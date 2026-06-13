import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/** A {@link HashMap}-backed {@link Index}, offering O(1) lookups for exact-match queries. */
public class HashIndex implements Index {
    private HashMap<TableEntry, ArrayList<Integer>> map = new HashMap<>();

    @Override
    public void insert(TableEntry key, int rowIndex) {
        map.putIfAbsent(key, new ArrayList<>());
        map.get(key).add(rowIndex);
    }


    @Override
    public ArrayList<Integer> lookup(Comparator<TableEntry> cmp, TableEntry val) {
        ArrayList<Integer> matchingRowIndices = new ArrayList<>();
        for (TableEntry key: map.keySet()) {
            if (cmp.compare(key, val) == 0) {
                matchingRowIndices.addAll(map.get(key));
            }
        }
        return matchingRowIndices;
    }

    @Override
    public void delete(Column column) {
        map.clear();

        for (int i = 0; i < column.getData().size(); i++) {
            insert(column.getData().get(i), i);
        }
    }
}
