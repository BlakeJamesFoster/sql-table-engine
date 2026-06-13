import java.util.ArrayList;

/**
 * A single row of typed cell values, in column order.
 *
 * <p>{@link #create} parses one line of INSERT data into the
 * {@link TableEntry} subclass matching each column's declared type, and
 * links each entry back to this row via {@link TableEntry#setRow}.
 */
public class Row {

    private ArrayList<TableEntry> row;

    public Row() {
        row = new ArrayList<>();
    }// constructor

    /**
     * Parses {@code data} into typed entries according to {@code cellTypes}
     * (one type name per column, e.g. "bool"/"int"/"double"/"string") and
     * appends them to this row.
     */
    public void create(ArrayList<String> cellTypes, String[] data) {
        assert(cellTypes.size() == data.length);
        for (int i = 0; i < cellTypes.size(); i++) {
            switch (cellTypes.get(i)) {
                case "bool" -> {
                    TableEntry.BoolTableEntry bool = new TableEntry.BoolTableEntry();
                    bool.setData(Boolean.parseBoolean(data[i]));
                    row.add(bool);
                    bool.setRow(this);
                }
                case "int" -> {
                    TableEntry.IntTableEntry num = new TableEntry.IntTableEntry();
                    num.setData(Integer.parseInt(data[i]));
                    row.add(num);
                    num.setRow(this);
                }
                case "string" -> {
                    TableEntry.StringTableEntry str = new TableEntry.StringTableEntry();
                    str.setData(data[i]);
                    row.add(str);
                    str.setRow(this);
                }
                case "double" -> {
                    TableEntry.DoubleTableEntry num = new TableEntry.DoubleTableEntry();
                    num.setData(Double.parseDouble(data[i]));
                    row.add(num);
                    num.setRow(this);
                }
                default -> System.out.printf("%s is not a valid type", cellTypes.get(i));
            }
        }
    }//create

    public ArrayList<TableEntry> getRow() {
        return row;
    }

    @Override
    public String toString() {
        return "Row{" + row + '}';
    }
}//class
