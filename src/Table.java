import java.util.*;

/**
 * A single table: its columns, their declared types, an optional index, and
 * the query operations ({@code INSERT}, {@code PRINT}, {@code DELETE},
 * {@code GENERATE ... INDEX}, {@code JOIN}) that operate on them.
 */
public class Table {

    private String tableName;

    // CREATE <tableName> <N> <colType1> ... <colTypeN> <colName1> ... <colNameN>
    private ArrayList<String> columnNames;
    private ArrayList<String> columnTypes;
    private HashMap<String,Column> table;

    // An index over a single column, created by GENERATE ... INDEX (bst or hash)
    private Index index;
    private String indexColumnName;

    private int height;

    public Table(String tableName, String[] colTypes, String[] colNames) {
        this.tableName = tableName;

        columnNames = new ArrayList<>();
        columnTypes = new ArrayList<>();

        columnNames.addAll(Arrays.asList(colNames));
        columnTypes.addAll(Arrays.asList(colTypes));

        table = new HashMap<>();

        for (int i = 0 ; i < colNames.length; i++) {
            String colName = columnNames.get(i);
            table.putIfAbsent(colName, new Column(TableEntry.parseType(colTypes[i])));
        }

        height = 0;
    }

    /**
     * Reads {@code numberOfRows} lines of space-separated values from
     * {@code scanner} and appends each as a new row to this table.
     */
    public void insertInto(Scanner scanner, int numberOfRows) {
        int rowsAdded = 0;
        scanner.nextLine();

        for (int i = 0; i < numberOfRows; i++) {
            String[] data = scanner.nextLine().split(" ");
            Row r = new Row();
            r.create(columnTypes, data);
            height++;
            rowsAdded++;
            for (int j = 0; j < r.getRow().size(); j++) {
                String colName = columnNames.get(j);
                table.get(colName).getData().add(r.getRow().get(j));
            }
            if (index != null) {
                updateIndex(r);
            }
        }
        System.out.printf("Added %d rows to %s from position %d to %d\n", numberOfRows, tableName, height-rowsAdded, height-1);
    }

    public void printFrom(ArrayList<String> colsToPrint, int numColumns, boolean quiet) {
        // The height of a column and also the entire table
        int printedRows = 0;

        // Gets the column objects that I want to print
        ArrayList<Column> whatToPrint = whatToPrint(colsToPrint,numColumns);
        if (whatToPrint == null) {
            return;
        }

        // Print the headers of the columns
        printHeaders(colsToPrint);

        // specialized print method "ALL"
        for (int i = 0; i < height; i++) {
            for (Column col : whatToPrint) {
                if (!quiet) {
                    System.out.print(col.getData().get(i) + " ");
                }
            }
            System.out.println();
            printedRows++;
        }
        System.out.println("Printed " + printedRows + " matching rows from " + tableName);
    }

    /**
     * Prints {@code colsToPrint} for every row where
     * {@code cmp.compare(value, val) == 0} in {@code whereColumn}. If an
     * index exists on {@code whereColumn}, it is used to look up matching
     * rows directly; otherwise every row is scanned and tested.
     */
    public void printFrom(ArrayList<String> colsToPrint, int numColumns,
                          Comparator<TableEntry> cmp, TableEntry val, String whereColumn, boolean quiet) {
        Column test = table.get(whereColumn);
        // Number of rows in the table
        int rowCount = test.getData().size();
        int printedRows = 0;

        // The columns to print, in the requested order
        ArrayList<Column> whatToPrint = whatToPrint(colsToPrint,numColumns);
        if (whatToPrint == null) {
            return;
        }

        ArrayList<Integer> whereColInRow = new ArrayList<>();
        for (String k: colsToPrint) {
            whereColInRow.add(columnNames.indexOf(k));
        }

        printHeaders(colsToPrint);

        if (index != null && Objects.equals(whereColumn, indexColumnName)) {
            ArrayList<Integer> indicesWhereMatch = index.lookup(cmp, val);
            for (Integer whereMatch : indicesWhereMatch) {
                Row r = test.getData().get(whereMatch).row;
                for (Integer j : whereColInRow) {
                    if (!quiet) {
                        System.out.print(r.getRow().get(j) + " ");
                    }
                }
                printedRows++;
                System.out.println();
            }
        }else {
            // Fall back to a full scan when there is no index on whereColumn
            for (int i = 0; i < rowCount; i++) {
                if (cmp.compare(test.getData().get(i), val) == 0) {
                    for (Column c : whatToPrint) {
                        if (!quiet) {
                            System.out.print(c.getData().get(i) + " ");
                        }
                    }
                    printedRows++;
                    System.out.println();
                }
            }
        }

        System.out.println("Printed " + printedRows + " matching rows from " + tableName);
    }

    /**
     * Deletes every row where {@code cmp.compare(value, val) == 0} in
     * {@code columnName}. Matching rows are removed from highest index to
     * lowest so that earlier removals don't shift the positions of rows
     * still pending deletion. If an index exists, it is rebuilt afterward
     * to reflect the new row positions.
     */
    public void deleteFrom(String columnName, Comparator<TableEntry> cmp, TableEntry val) {
        ArrayList<Integer> rowIndicesToDelete = new ArrayList<>();
        if (!columnNames.contains(columnName)) {
            System.out.printf("Error: %s does not name a column in %s\n", columnName, tableName);
            return;
        }
        Column c = table.get(columnName);
        if (index == null || !Objects.equals(indexColumnName, columnName)) {
            for (int i = 0; i < c.getData().size(); i++) {
                if (cmp.compare(c.getData().get(i), val) == 0) {
                    rowIndicesToDelete.add(i);
                }
            }
        } else {
            rowIndicesToDelete = index.lookup(cmp, val);
            // Sort so rows can be removed from highest index to lowest
            Collections.sort(rowIndicesToDelete);
        }

        // Backward iteration keeps the remaining indices valid during removal
        for (int i = rowIndicesToDelete.size() - 1; i >= 0; i--) {
            int rowIndex = rowIndicesToDelete.get(i);
            for (Column col : table.values()) {
                col.getData().remove(rowIndex);
            }
            height--;
        }

        if (index != null) {
            index.delete(table.get(indexColumnName));
        }

        System.out.printf("Deleted %d rows from %s\n", rowIndicesToDelete.size(), tableName);
    }

    /**
     * Builds an index of type {@code "bst"} ({@link BSTIndex}) or
     * {@code "hash"} ({@link HashIndex}) on {@code colName}, populated from
     * the column's current rows. A table has at most one index at a time;
     * creating a new one replaces any previous index.
     */
    public void generateIndex(String indexType, String colName) {
        if (!columnNames.contains(colName)) {
            System.out.printf("Error: %s does not name a column in %s\n", colName, tableName);
            return;
        }

        indexColumnName = colName;
        Column c = table.get(colName);

        if (indexType.equals("bst")) {
            index = new BSTIndex();
        }else if (indexType.equals("hash")){
            index = new HashIndex();
        }else {
            System.out.printf("Error: unknown index type '%s'\n", indexType);
            return;
        }

        for (int i = 0; i < height; i++){
            TableEntry te = c.getData().get(i);
            index.insert(te, i);
        }
        System.out.printf("Created %s index for table %s on column %s\n",indexType, tableName ,colName);

    }

    /**
     * Joins this table to {@code t2} on {@code cmp.compare(value1, value2) == 0}
     * between {@code colName1} (this table) and {@code colName2} ({@code t2}),
     * printing {@code colsToPrint} for each matching pair of rows.
     * {@code colsFromTable1.get(i)} indicates whether {@code colsToPrint.get(i)}
     * comes from this table or from {@code t2}.
     *
     * <p>If either table has an index on its join column, that index is used
     * to look up matches directly; otherwise every pair of rows is compared
     * in a nested loop.
     */
    public void join(Table t2,
                     Comparator<TableEntry> cmp,
                     String colName1, String colName2,
                     ArrayList<String> colsToPrint, ArrayList<Boolean> colsFromTable1,
                     boolean quiet) {
        ArrayList<Integer> colsToPrintIndices = new ArrayList<>();
        for(int i = 0; i < colsFromTable1.size(); i++) {
            ArrayList<String> findIdx = colsFromTable1.get(i) ? columnNames : t2.columnNames;
            colsToPrintIndices.add(findIdx.indexOf(colsToPrint.get(i)));
        }

        Column t1Col = table.get(colName1);
        Column t2Col = t2.table.get(colName2);
        if (t1Col == null) {
            System.out.printf("Error: %s does not name a column in %s\n", colName1, tableName);
            return;
        } else if (t2Col == null) {
            System.out.printf("Error: %s does not name a column in %s\n", colName2, t2.tableName);
            return;
        }
        int numPrints= 0;
        printHeaders(colsToPrint);

        if (index != null && Objects.equals(indexColumnName, colName1)) {
            // This table has the index - scan t2 and look up matches here
            for (TableEntry te2 : t2Col.getData()) {
                ArrayList<Integer> matchingRows = index.lookup(cmp, te2);
                for (int rowIdx : matchingRows) {
                    if (!quiet) {
                        printFromTwoRows(t1Col.getData().get(rowIdx).row, t2, te2.row, colsToPrintIndices, colsFromTable1);
                    }
                    numPrints++;
                }
            }
        } else if (t2.index != null && Objects.equals(t2.indexColumnName, colName2)) {
            // t2 has the index - scan this table and look up matches in t2
            for (TableEntry te1 : t1Col.getData()) {
                ArrayList<Integer> matchingRows = t2.index.lookup(cmp, te1);
                for (int rowIdx : matchingRows) {
                    if (!quiet) {
                        printFromTwoRows(te1.row, t2, t2Col.getData().get(rowIdx).row, colsToPrintIndices, colsFromTable1);
                    }
                    numPrints++;
                }
            }
        }else {
            // Neither side is indexed on the join column - compare every pair
            for (TableEntry te1: t1Col.getData()) {
                for (TableEntry te2: t2Col.getData()) {
                    if (cmp.compare(te1, te2) == 0) {
                        if (!quiet) {
                            printFromTwoRows(te1.row, t2, te2.row, colsToPrintIndices, colsFromTable1);
                        }
                        numPrints++;
                    }
                }
            }
        }
        System.out.printf("Printed %d rows from joining %s to %s\n", numPrints, tableName, t2.tableName);
    }

    // ---- HELPER METHODS ----
    private void printFromTwoRows(Row table1row, Table t2, Row table2row, ArrayList<Integer> colsToPrintIndices, ArrayList<Boolean> colsFromTable1) {
        for (int i = 0; i < colsFromTable1.size(); i++) {
            if (colsFromTable1.get(i)) {
                System.out.print(table1row.getRow().get(colsToPrintIndices.get(i)) + " ");
            } else {
                System.out.print(table2row.getRow().get(colsToPrintIndices.get(i)) + " ");
            }
        }
        System.out.println();
    }

    /** Records the newly inserted row's value in the column index, if one exists. */
    private void updateIndex(Row r) {
        int idx = columnNames.indexOf(indexColumnName);
        index.insert(r.getRow().get(idx), height-1);
    }

    private ArrayList<Column> whatToPrint(ArrayList<String> colsToPrint, int numColumns) {
        ArrayList<Column> whatToPrint = new ArrayList<>();
        for (int i = 0; i < numColumns; i++) {
            String columnName = colsToPrint.get(i);
            if (!columnNames.contains(columnName)) {
                System.out.printf("Error: %s does not name a column in %s\n", columnName, tableName);
                return null;
            }
            whatToPrint.add(table.get(columnName));
        }
        return whatToPrint;
    }

    private static void printHeaders(ArrayList<String> colsToPrint) {
        for (String name : colsToPrint) {
            System.out.print(name + " ");
        }
        System.out.println();
    }

    public String getTableName() {
        return tableName;
    }

    public String getNamesRow() {
        return String.join(" ", columnNames);
    }

    public TableEntry.Type getColType(String colName) {
        Column c = table.get(colName);
        if (c == null) return null;
        return c.getColumnType();
    }

    public HashMap<String, Column> getTable() {
        return table;
    }
}
