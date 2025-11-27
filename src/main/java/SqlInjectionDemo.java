import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
Neue Datenbank mit dem Namen "SqlInjectionDemo"

DROP TABLE IF EXISTS users;

CREATE TABLE `users` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(100) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `users` (`username`, `password`, `created_at`) VALUES
      ('alice', SHA2('alicePass123', 256), '2025-01-01 10:00:00'),
      ('bob',   SHA2('B0b!Secure',     256), '2025-02-15 12:30:00'),
      ('carla', SHA2('c4rl4_pw',       256), '2025-03-20 09:15:00'),
      ('admin', SHA2('admin',      256), '2025-04-01 00:00:00');

 */

interface OutputListener {
    void onOutput(String message);
}



public class SqlInjectionDemo {
    private final String url;
    private final String user;
    private final String password;
    private final Connection connection;
    private OutputListener outputListener;

    public SqlInjectionDemo() throws SQLException {
        this.url = "jdbc:h2:mem:SqlInjectionDemo;DB_CLOSE_DELAY=-1;ALLOW_LITERALS=ALL";
        this.user = "sa";
        this.password = "";
        this.connection = DriverManager.getConnection(url, user, password);
    }

    public void setOutputListener(OutputListener listener) {
        this.outputListener = listener;
    }

    private void print(String message) {
        if (outputListener != null) {
            outputListener.onOutput(message);
        } else {
            System.out.println(message);
        }
    }

    public void createTable() throws SQLException {
        String sql = "CREATE TABLE users (\n" +
                "  id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "  username VARCHAR(255) NOT NULL,\n" +
                "  password VARCHAR(255) NOT NULL,\n" +
                "  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP\n" +
                ");";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    public void addDummyData() throws SQLException {
        String sql = "INSERT INTO users (username, password, created_at)\n" +
                "VALUES \n" +
                " ('alice', HASH('SHA256', STRINGTOUTF8('alicePass123'), 1), '2025-01-01 10:00:00'),\n" +
                "('bob',   HASH('SHA256', STRINGTOUTF8('B0b!Secure'), 1), '2025-02-15 12:30:00'),\n" +
                "('carla', HASH('SHA256', STRINGTOUTF8('c4rl4_pw'), 1), '2025-03-20 09:15:00'),\n" +
                "('admin', HASH('SHA256', STRINGTOUTF8('admin'), 1), '2025-04-01 00:00:00');";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int rowsAdded = stmt.executeUpdate();
            print("Rows added: " + rowsAdded);
        }
    }

    public void getAllUsers() throws SQLException {
        String sql = "SELECT * FROM users";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                print("User: " + rs.getString("username") + ", " + rs.getString("password"));
            }
        }
    }


    /**
     * Sichere und performantere Variante mit PreparedStatement
     * @param userInput
     * @throws SQLException
     */
    public void getUserByUsernameSafe(String userInput) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userInput);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                print("User: " + rs.getString("username"));
            }
        }
    }

    /**
     * Unsichere Variante mit der Sicherheitslücke "SQL-Injection"
     * @param userInput
     * @throws SQLException
     */
    public void getUserByUsernameUnsafe(String userInput) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = '" + userInput + "'"; // die einfachen Hochkommata führen am Ende zu einem SQL-Syntax-Fehler
        print(">>> Auszuführendes unsicheres Statement: \"" + sql + "\"");
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                print("User: " + rs.getString("username"));
            }
        }
    }

    /**
     * Unsichere Variante: String-Konkatenation -> SQL-Injection möglich: xxx' OR 1='1'; -- '
     *
     * @param username
     * @param password
     * @throws SQLException
     */
    public void vulnerableLogin(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = '" + username + "' AND password = HASH('SHA256', STRINGTOUTF8('" + password + "'), 1)";
        print("Ausgeführte SQL: " + sql);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                print("Login erfolgreich mit username: " + username);
            } else {
                print("Login fehlgeschlagen");
            }
        }
    }

    /**
     * Sichere Variante: PreparedStatement verhindert SQL-Injection
     *
     * @param username
     * @param password
     * @throws SQLException
     */
    public void safeLogin(String username, String password) throws SQLException  {
        String sql = "SELECT * FROM users WHERE username = ? AND password = HASH('SHA256', STRINGTOUTF8(?), 1)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            print("PreparedStatement wird ausgeführt (Parameter gebunden).");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    print("Login erfolgreich mit username: " + username);
                } else {
                    print("Login fehlgeschlagen");
                }
            }
        }
    }

    /**
     * Verbindung schließen
     */
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
