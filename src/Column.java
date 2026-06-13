import java.util.ArrayList;

/** A single table column: its declared {@link TableEntry.Type} plus the cell values for every row. */
public class Column {
    private ArrayList<TableEntry> entries = new ArrayList<>();
    private TableEntry.Type columnType;

    public Column(TableEntry.Type columnType) {
        this.columnType = columnType;
    }

    public void add(TableEntry entry) {
        entries.add(entry);
    }

    public ArrayList<TableEntry> getData() {
        return entries;
    }

    public TableEntry.Type getColumnType() {
        return columnType;
    }
}
