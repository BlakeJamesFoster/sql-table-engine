import java.util.HashMap;

/** The top-level registry of tables, keyed by table name. */
public class Database {
    private HashMap<String, Table> tables;

    public Database() {
        tables = new HashMap<>();
    }

    public  void addTable(Table t) {
        tables.put(t.getTableName(), t);
    }

    public HashMap<String, Table> getTables() {
        return tables;
    }
}
