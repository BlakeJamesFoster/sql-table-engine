import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Entry point for the SluQL REPL: reads one command per prompt from stdin
 * and dispatches on its first character to the corresponding
 * {@link Table}/{@link Database} operation.
 */
public class Main {
    public static Scanner in = new Scanner(System.in);
    public static Database database = new Database();

    public static void main(String[] args) {
        Options options = new Options(args);
        Table table;
        String tableName;
        String cmd;
        String colName;
        String op;
        Comparator<TableEntry> cmp;
        TableEntry val;

        while (true) {
            cmd = prompt();
            switch (cmd.charAt(0)) {
                case 'J':
                    //JOIN pets AND 256class WHERE Name = person AND PRINT 3 Name 1 emotion 2 likes_dogs? 1
                    String tableName1 = in.next();
                    if (tableNameError(tableName1)) { break; }
                    Table table1 = database.getTables().get(tableName1);

                    in.next(); // skip 'AND'

                    String tableName2 = in.next();
                    if (tableNameError(tableName2)) { break; }
                    Table table2 = database.getTables().get(tableName2);

                    in.next(); // skip 'WHERE'

                    String colName1 = in.next();
                    if (!table1.getTable().containsKey(colName1)) {
                        System.out.printf("Error: %s does not name a column in %s\n", colName1, tableName1);
                        in.nextLine();
                        break;
                    }
                    in.next(); // skip '='
                    String colName2 = in.next();
                    if (!table2.getTable().containsKey(colName2)) {
                        System.out.printf("Error: %s does not name a column in %s\n", colName2, tableName2);
                        in.nextLine();
                        break;
                    }

                    in.next(); // skip 'AND'
                    in.next(); // skip 'PRINT'

                    int numCols = in.nextInt();

                    ArrayList<String> colsToPrint = new ArrayList<>(numCols);
                    ArrayList<Boolean> colsFromTable1 = new ArrayList<>(numCols);

                    for (int i = 0; i < numCols; i++) {
                        colsToPrint.add(in.next());
                        int tableNum = in.nextInt();
                        if (tableNum == 1) {
                            colsFromTable1.add(true);
                        }else {
                            colsFromTable1.add(false);
                        }

                    }

                    Comparator<TableEntry> joinComparator = new RowComparators.EqualsComparator();

                    table1.join(table2, joinComparator, colName1, colName2, colsToPrint, colsFromTable1, options.isQuiet());
                    break;
                case 'G' :
                    //Syntax: GENERATE FOR <tablename> <indextype> INDEX ON <colname>
                    in.next();
                    tableName = in.next();
                    if (tableNameError(tableName)) { break; }
                    String indexType = in.next();
                    in.next();
                    in.next();
                    colName = in.next();
                    table = database.getTables().get(tableName);
                    table.generateIndex(indexType, colName);
                    break;
                case 'D' :
                    // DELETE FROM
                    in.next();
                    tableName = in.next();
                    if (tableNameError(tableName)) { break; }
                    table = database.getTables().get(tableName);
                    in.next();
                    colName = in.next();
                    op = in.next();
                    cmp = switch (op) {
                        case "=" -> new RowComparators.EqualsComparator();
                        case "<" -> new RowComparators.LTComparator();
                        default -> new RowComparators.GTComparator();
                    };

                    if(table.getColType(colName) == null) {
                        System.out.printf("Error: %s does not name a column in %s\n", colName, tableName);
                        break;
                    }

                    val = TableEntry.stringToTE(in.next(), table.getColType(colName));
                    table.deleteFrom(colName,cmp,val);
                    break;
                case 'P':
                    //Syntax: PRINT FROM<tablename><N><print_colname1><print_colname2> ...
                    //<print_colnameN> [WHERE <colname> <OP> <value> | ALL ]
                    in.next();

                    tableName = in.next();
                    if (tableNameError(tableName)) { break; }

                    table = database.getTables().get(tableName);

                    int noCol = in.nextInt();
                    ArrayList<String> columnsToPrint = new ArrayList<>();
                    boolean error = false;
                    for (int i = 0; i <noCol; i++) {
                        colName = in.next();
                        if (table.getTable().get(colName) != null)
                            columnsToPrint.add(colName);
                        else {
                            System.out.printf("Error: %s does not name a column in %s\n",colName, tableName);
                            in.nextLine();
                            error = true;
                            break;
                        }
                    }
                    if (error) { break; }

                    String conditional = in.next();

                    if (conditional.equals("ALL")) {
                        table.printFrom(columnsToPrint, noCol,options.isQuiet());
                    }else {
                        colName = in.next();
                        op = in.next();
                        if (table.getColType(colName) == null) {
                            System.out.printf("Error: %s does not name a column in %s\n",colName, tableName);
                            in.nextLine();
                            break;
                        }
                        TableEntry.Type type = table.getColType(colName);
                        val = TableEntry.stringToTE(in.next(), type);
                        cmp = switch (op) {
                            case "=" -> new RowComparators.EqualsComparator();
                            case "<" -> new RowComparators.LTComparator();
                            default -> new RowComparators.GTComparator();
                        };
                        table.printFrom(columnsToPrint,noCol,cmp,val,colName, options.isQuiet());
                    }
                    break;
                case 'I':
                    // SYNTAX: INSERT INTO <tablename> <N> ROWS

                    //skip INTO
                    in.next();

                    //gather <tablename>
                    tableName = in.next();

                    if (tableNameError(tableName)) { break; }

                    database.getTables().get(tableName).insertInto(in, in.nextInt());
                    break;
                case 'C':
                    // Syntax: CREATE <tablename> <N> <coltype1> <coltype2> ...<coltypeN> <colname1>
                    // <colname2> ... <colnameN>
                    tableName = in.next();

                    int numberOfColums = in.nextInt();
                    String[] colTypesAndNames = in.nextLine().substring(1).split(" ");
                    String[] colTypes = Arrays.copyOfRange(colTypesAndNames,0, numberOfColums);
                    String[] colNames = Arrays.copyOfRange(colTypesAndNames,numberOfColums, colTypesAndNames.length);

                    if (database.getTables().containsKey(tableName)) {
                        System.out.printf("Error: Cannot create already existing table %s\n",tableName);
                        break;
                    }

                    table = new Table(tableName, colTypes, colNames);
                    database.addTable(table);
                    System.out.println("New table " + tableName + " with column(s) " + table.getNamesRow() + " created");
                    break;
                case 'R':
                    // REMOVE <tablename>
                    //remove
                    tableName = in.next();
                    // cant use helper function because the line is done
                    if (!database.getTables().containsKey(tableName)) {
                        System.out.printf("Error: %s does not name a table in the database\n", tableName);
                        break;
                    }
                    database.getTables().remove(tableName);
                    System.out.println("Table " + tableName + " deleted");
                    break;
                case 'Q':
                    //Quit
                    System.out.println("Goodbye, SLU!");
                    System.exit(0);
                    break;
                case '#':
                    in.nextLine();
                    break;
                default:
                    System.out.println("Error: unrecognized command");
                    in.nextLine();
            }//switch
        }// while
    }// main

    // ------  Helper Functions -------

    /**
     * Checks whether {@code tableName} names a table in the database, printing
     * an error and consuming the rest of the line if it does not.
     * @param tableName a name of any table in the database (case-sensitive)
     * @return true if the table is not found in the database; false if it is found
     */
    public static boolean tableNameError(String tableName) {
        if (!database.getTables().containsKey(tableName)) {
            System.out.printf("Error: %s is not a name of a table in the database\n",tableName);
            in.nextLine();
            return true;
        }
        return false;
    }

    private static String prompt() {
        System.out.print("% ");
        return in.next();
    }
}
