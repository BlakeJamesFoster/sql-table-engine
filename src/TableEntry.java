import java.util.Objects;

/**
 * A single typed cell value in a table.
 *
 * <p>Every cell is one of {@link Type#Bool}, {@link Type#Double},
 * {@link Type#Int}, or {@link Type#String}, represented by the
 * corresponding nested subclass. Each subclass implements
 * {@link #compareTo} by comparing its own data; comparing two entries of
 * different types throws {@link ClassCastException}.
 *
 * <p>{@link #stringToTE} parses a raw token (from INSERT or a WHERE/JOIN
 * value) into the right subclass given a column's {@link Type}, and
 * {@link #parseType} converts a CREATE type name ("int", "double", "bool",
 * "string") into that {@link Type}.
 */
public abstract class TableEntry implements Comparable<TableEntry> {

    protected Row row;

    public void setRow(Row row) {
        this.row = row;
    }

    public Row getRow() {
        return row;
    }

    /**
     * Parses {@code next} into a {@link TableEntry} of the given column
     * type, e.g. for an INSERT value or a WHERE/JOIN comparison value.
     */
    public static TableEntry stringToTE(String next, Type colType) {
        switch (colType) {
            case Bool:
                BoolTableEntry b = new BoolTableEntry();
                b.setData(Boolean.parseBoolean(next));
                return b;
            case Double:
                DoubleTableEntry d =  new DoubleTableEntry();
                d.setData(Double.parseDouble(next));
                return d;
            case Int:
                IntTableEntry i = new IntTableEntry();
                i.setData(Integer.parseInt(next));
                return i;
            case String:
                StringTableEntry s = new StringTableEntry();
                s.setData(next);
                return s;
            default:
                throw new IllegalArgumentException("Error: Unknown " + colType);
        }
    }

    /**
     * Converts a column type name from CREATE (e.g. "int", "double", "bool",
     * "string") into the corresponding {@link Type} enum value, or null if
     * the name isn't a recognized type.
     */
    public static Type parseType(String typeName) {
        switch (typeName) {
            case "bool":
                return Type.Bool;
            case "double":
                return Type.Double;
            case "int":
                return Type.Int;
            case "string":
                return Type.String;
            default:
                return null;
        }
    }

    public enum Type {
        Bool,
        Double,
        Int,
        String
    }

    public abstract Type getType();

    public abstract TableEntry copy();

    public abstract String toString();

    public abstract Object getData();


    public static class BoolTableEntry extends TableEntry {
        private Boolean data;

        public BoolTableEntry() {
            this.data = null;
        }

        @Override
        public Type getType() {
            return Type.Bool;
        }

        @Override
        public TableEntry copy() {
            BoolTableEntry copy = new BoolTableEntry();
            copy.data = this.data;
            return copy;
        }

        @Override
        public String toString() {
            return data.toString();
        }

        @Override
        public Object getData() {
            return data;
        }

        public boolean isData() {
            return data;
        }

        public void setData(boolean b) {
            this.data = b;
        }

        @Override
        public int compareTo(TableEntry o) {
            if (o.getType() != Type.Bool) {
                throw new ClassCastException();
            }
            BoolTableEntry ob = (BoolTableEntry) o;
            if (data) {
                if (ob.data) {
                    // both true
                    return 0;
                }else {
                    //data = true
                    //ob.data = false
                    return 1;
                }
            }else {
                if (!ob.data) {
                    // both false
                    return 0;
                }else {
                    //data = false
                    //ob.data = true
                    return -1;
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            BoolTableEntry other = (BoolTableEntry) obj;
            return Objects.equals(this.data, other.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }

    } // BoolTable Entry

    public static class IntTableEntry extends TableEntry {
        private Integer data;

        public IntTableEntry() {
            this.data = null;
        }

        @Override
        public TableEntry copy() {
            IntTableEntry copy = new IntTableEntry();
            copy.data = this.data;
            return copy;
        }

        @Override
        public String toString() {
            return data.toString();
        }

        public Integer getData() {
            return data;
        }

        public void setData(Integer data) {
            this.data = data;
        }

        @Override
        public Type getType() {
            return Type.Int;
        }

        @Override
        public int compareTo(TableEntry o) {
            if (o.getType() != Type.Int) {
                throw new ClassCastException();
            }
            IntTableEntry ob = (IntTableEntry) o;

            return data - ob.data;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            IntTableEntry other = (IntTableEntry) obj;
            return Objects.equals(this.data, other.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }

    } // IntTable Entry

    public static class DoubleTableEntry extends TableEntry {
        private Double data;

        public DoubleTableEntry() {
            this.data = null    ;
        }

        @Override
        public TableEntry copy() {
            DoubleTableEntry copy = new DoubleTableEntry();
            copy.data = this.data;
            return copy;
        }

        @Override
        public String toString() {
            return data.toString();
        }


        public Double getData() {
            return data;
        }

        public void setData(Double data) {
            this.data = data;
        }

        @Override
        public Type getType() {
            return Type.Double;
        }

        @Override
        public int compareTo(TableEntry o) {
            if (o.getType() != Type.Double) {
                throw new ClassCastException();
            }
            DoubleTableEntry ob = (DoubleTableEntry) o;

            return (int) (data - ob.data);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            DoubleTableEntry other = (DoubleTableEntry) obj;
            return Objects.equals(this.data, other.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }
    } // DoubleTable Entry

    public static class StringTableEntry extends TableEntry {
        private String data;

        public StringTableEntry() {
            this.data = null;
        }

        @Override
        public TableEntry copy() {
            StringTableEntry copy = new StringTableEntry();
            copy.data = this.data;
            return copy;
        }

        @Override
        public String toString() {
            return data;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        @Override
        public Type getType() {
            return Type.String;
        }

        @Override
        public int compareTo(TableEntry o) {
            if (o.getType() != Type.String) {
                throw new ClassCastException();
            }
            StringTableEntry ob = (StringTableEntry) o;

            int min = Math.min(ob.data.length(), data.length());

            for (int i = 0; i < min; i++ ) {
                if ((data.charAt(i) != ob.data.charAt(i))) {
                    // Compare using the characters' Unicode values
                    return data.charAt(i) - ob.data.charAt(i);
                }
            }
            // The common prefix is identical, so the shorter string sorts first
            return data.length() - ob.data.length();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            StringTableEntry other = (StringTableEntry) obj;
            return Objects.equals(this.data, other.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }
    } // StringTable Entry
}//TableEntry
