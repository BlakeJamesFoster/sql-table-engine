import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/** Parses command-line flags (`-h`/`--help`, `-q`/`--quiet`) for the SluQL REPL. */
public class Options {
    private boolean quiet;

    public Options(String[] args) {
        LongOpt[] longOpts = {
                new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
                new LongOpt("quiet", LongOpt.NO_ARGUMENT, null, 'q'),
        };

        Getopt g = new Getopt("SluQL", args, "hq", longOpts);

        int choice;

        while ((choice = g.getopt()) != -1) {
            switch (choice) {
                case 'h':
                    printHelp();
                    System.exit(0);
                    break;
                case 'q':
                    quiet = true;
                    break;
                default:
                    System.err.println("Not a valid input");
                    System.exit(1);
            }
        }
    }

    public static void printHelp() {
        System.out.println("SLU-QL Help");
        System.out.println("-----------");

        System.out.println("\nCREATE TABLE:");
        System.out.println("CREATE <tablename> <N> <coltype1> ... <coltypeN> <colname1> ... <colnameN>");
        System.out.println("Types: int, double, bool, string");

        System.out.println("\nINSERT:");
        System.out.println("INSERT INTO <tablename> <N> ROWS");
        System.out.println("<value1> <value2> ...");
        System.out.println("...");

        System.out.println("\nDELETE:");
        System.out.println("DELETE FROM <tablename> WHERE <colname> <OP> <value>");
        System.out.println("OP: =  <  >");

        System.out.println("\nGENERATE INDEX:");
        System.out.println("GENERATE FOR <tablename> <hash|bst> INDEX ON <colname>");

        System.out.println("\nPRINT:");
        System.out.println("PRINT FROM <tablename> <N> <colname1> ... <colnameN> WHERE <colname> <OP> <value>");
        System.out.println("OR:");
        System.out.println("PRINT FROM <tablename> <N> <colname1> ... <colnameN> ALL");

        System.out.println("\nJOIN:");
        System.out.println("JOIN <table1> AND <table2> WHERE <col1> = <col2>");
        System.out.println("AND PRINT <N> <colname1> <1|2> ...");

        System.out.println("\nREMOVE:");
        System.out.println("REMOVE <tablename>");

        System.out.println("\nNOTES:");
        System.out.println("- Commands are case sensitive");
        System.out.println("- No spaces in table/column names");
        System.out.println("- Strings must be single tokens (use underscores)");
    }

    public boolean isQuiet() {
        return quiet;
    }
}
