import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try {
            SqlInjectionDemo sqlInjectionDemo = new SqlInjectionDemo();

            try { // Falls nicht vorhanden wird die Tabelle mit Dummy-Daten erstellt
                sqlInjectionDemo.createTable();
                sqlInjectionDemo.addDummyData();
            } catch(SQLException e){
                System.out.println(e.getMessage());
            }

            System.out.println("***************** getAllUsers *******************");
            System.out.println("*** SELECT * FROM users");
            sqlInjectionDemo.getAllUsers();

            System.out.println("**************** getUserByUsernameSafe ********************");

            System.out.println("*** Safe Abfrage - bob:");
            sqlInjectionDemo.getUserByUsernameSafe("bob");

            System.out.println("*** Safe Abfrage - \"alice' OR '1'='1\":");
            sqlInjectionDemo.getUserByUsernameSafe("alice' OR '1'='1");

            System.out.println("***************** getUserByUsernameUnsafe *******************");
            String sqlInjection1 = "alice' OR '1'='1"; // man beachte das fehlende einfache Hochkomma am Anfang und Ende des SQL-Injection-Strings
            System.out.println("*** Unsafe Abfrage: \"" + sqlInjection1 + "\"");
            // SELECT * FROM users WHERE username = 'alice' OR '1'='1'
            sqlInjectionDemo.getUserByUsernameUnsafe(sqlInjection1);
            // Ergebnis: Alle Benutzer werden zurückgegeben, weil die Bedingung immer wahr ist.


            Scanner scanner = new Scanner(System.in);
            System.out.println("***************** login *******************");
            System.out.print("Benutzer: ");
            String username = scanner.nextLine();
            System.out.print("Passwort: ");
            String password = scanner.nextLine();

            System.out.println("\n--- Unsichere Abfrage (anfällig für SQL-Injection) ---");
            sqlInjectionDemo.vulnerableLogin(username, password);

            System.out.println("\n--- Sichere Abfrage (PreparedStatement) ---");
            sqlInjectionDemo.safeLogin(username, password);

            System.out.println("************************************");
            String sqlInjection3 = "admin'; DROP TABLE users;"; // man beachte das fehlende einfache Hochkomma am Anfang und Ende des SQL-Injection-Strings
            System.out.println("*** Noch gefährlicher: \"" + sqlInjection3 + "\"");
            sqlInjectionDemo.getUserByUsernameUnsafe(sqlInjection3);
            // Führt zum Löschen der Tabelle (wenn mehrere Statements erlaubt sind).
            // Mehrere Statements sind standardmäßig nicht erlaubt. Dazu würde es folgende Verbindungs-URL benötigen:
            // "jdbc:mysql://localhost:3306/SqlInjectionDemo?allowMultiQueries=true"

            sqlInjectionDemo.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
}
