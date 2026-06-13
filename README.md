# SQL Table Engine

A small in-memory database written in Java, driven by a line-oriented
command language called **SluQL**. It supports typed tables, filtered
queries, joins between tables, and pluggable BST/hash indexes - all
through a REPL that reads commands from standard input.

This was the third project of a data structures course sequence, cleaned
up (naming, comments, Javadoc) to the same standard as the other projects
in this portfolio, with no change in behavior.

## Overview

```bash
printf "CREATE pets 2 string bool name happy\nINSERT INTO pets 2 ROWS\nFido true\nMittens false\nPRINT FROM pets 2 name happy ALL\nGENERATE FOR pets bst INDEX ON name\nDELETE FROM pets WHERE happy = false\nPRINT FROM pets 2 name happy ALL\nQUIT\n" | java -cp "out:lib/java-getopt-1.0.14.jar" Main
```

```
% New table pets with column(s) name happy created
% Added 2 rows to pets from position 0 to 1
% name happy
Fido true
Mittens false
Printed 2 matching rows from pets
% Created bst index for table pets on column name
% Deleted 1 rows from pets
% name happy
Fido true
Printed 1 matching rows from pets
% Goodbye, SLU!
```

## Concepts & skills demonstrated

- **Type system via polymorphism** - `TableEntry` is an abstract typed
  cell value with four subclasses (`Bool`/`Int`/`Double`/`String`), each
  implementing its own `compareTo`, `equals`, and `hashCode`.
- **Strategy pattern for filtering** - `RowComparators` provides `=`, `<`,
  and `>` as match predicates: `compare(entry, value) == 0` means "matches",
  so `Table` and the indexes can apply any operator the same way.
- **Pluggable indexing** - the `Index` interface has two implementations,
  `BSTIndex` (a `TreeMap`) and `HashIndex` (a `HashMap`), chosen at runtime
  by `GENERATE ... INDEX`.
- **Index-aware query execution** - `PRINT`, `DELETE`, and `JOIN` use an
  index when one exists on the relevant column, falling back to a full
  table scan otherwise.
- **getopt CLI parsing** - `Options` uses GNU `getopt` for `-h/--help` and
  `-q/--quiet`.
- **REPL/line-oriented command parsing** - `Main` reads one command at a
  time from a shared `Scanner`, dispatching on the first token.

## Project structure

```
src/
├── Main.java            entry point / REPL loop
├── Options.java          command-line flag parsing (-h/--help, -q/--quiet)
├── Database.java         top-level table registry
├── Table.java             table storage and query operations
├── Column.java            typed column storage
├── Row.java               parses one line of INSERT data into typed values
├── TableEntry.java        abstract typed cell value (Bool/Int/Double/String)
├── RowComparators.java    =, <, > filter predicates
├── Index.java             index interface
├── BSTIndex.java          TreeMap-based index
└── HashIndex.java         HashMap-based index
lib/
└── java-getopt-1.0.14.jar
samples/
└── test-1..15-table-commands.txt   sample SluQL scripts
```

## Building & running

Requires a JDK (developed against Java 17+) and the bundled `java-getopt`
library.

```bash
# Compile
javac -d out -cp lib/java-getopt-1.0.14.jar src/*.java

# Run a sample script
java -cp "out:lib/java-getopt-1.0.14.jar" Main -q < samples/test-1-table-commands.txt
```

On Windows, use `;` instead of `:` in the classpath.

### Flags

| Flag | Long form | Effect |
|------|-----------|--------|
| `-q` | `--quiet` | Suppress row data in `PRINT`/`JOIN` output, printing only headers and summary lines. |
| `-h` | `--help` | Print usage information and exit. |

## Command reference

Each line of input is one command. Table and column names are
case-sensitive and may not contain spaces.

- `# <comment>` - a comment line; ignored.
- `CREATE <tablename> <N> <coltype1> ... <coltypeN> <colname1> ... <colnameN>`
  Creates a table with `N` columns. Types are `int`, `double`, `bool`, or `string`.
- `INSERT INTO <tablename> <N> ROWS` followed by `N` lines of
  space-separated values - appends `N` rows to the table.
- `DELETE FROM <tablename> WHERE <colname> <OP> <value>` - deletes every
  row where the column matches `<value>` under `OP` (`=`, `<`, or `>`).
- `PRINT FROM <tablename> <N> <colname1> ... <colnameN> ALL` - prints the
  given columns for every row.
- `PRINT FROM <tablename> <N> <colname1> ... <colnameN> WHERE <colname> <OP> <value>` -
  prints the given columns only for rows matching the condition.
- `GENERATE FOR <tablename> <bst|hash> INDEX ON <colname>` - builds an
  index on a column to speed up later `PRINT`/`DELETE`/`JOIN` operations.
- `JOIN <table1> AND <table2> WHERE <col1> = <col2> AND PRINT <N> <colname1> <1|2> ... <colnameN> <1|2>` -
  prints `N` columns (each tagged `1` or `2` for which table it comes from)
  for every pair of rows where `<col1>` (in `table1`) equals `<col2>` (in `table2`).
- `REMOVE <tablename>` - deletes a table from the database.
- `QUIT` - exits the program.

### Sample data

`samples/` contains 15 SluQL scripts exercising the full command set,
including both index types, all three comparators, joins on string and
int columns, and edge cases like deleting from an empty table and
referencing nonexistent tables or columns.

## Credits

[`java-getopt`](http://www.urbanophile.com/arenn/hacking/getopt/) is a
third-party library used for command-line argument parsing.
