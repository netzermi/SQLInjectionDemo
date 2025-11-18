import java.sql.*;

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


public class SqlInjectionDemo {
    private final String url;
    private final String user;
    private final String password;
    private final Connection connection;

    public SqlInjectionDemo() throws SQLException {
        this.url = "jdbc:mysql://localhost:3306/jdbcdemo?allowMultiQueries=true";
        this.user = "user";
        this.password = "12345";
        this.connection = DriverManager.getConnection(url, user, password);
    }

    public void createTable() throws SQLException {
        String sql = "CREATE TABLE `users` (\n" +
                "  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                "  `username` varchar(255) NOT NULL,\n" +
                "  `password` varchar(255) NOT NULL,\n" +
                "  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP\n" +
                ");";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    public void addDummyData() throws SQLException {
        String sql = "INSERT INTO `users` (`username`, `password`, `created_at`)\n" +
                "VALUES \n" +
                " ('alice', SHA2('alicePass123', 256), '2025-01-01 10:00:00'),\n" +
                "('bob',   SHA2('B0b!Secure',     256), '2025-02-15 12:30:00'),\n" +
                "('carla', SHA2('c4rl4_pw',       256), '2025-03-20 09:15:00'),\n" +
                "('admin', SHA2('admin',      256), '2025-04-01 00:00:00');";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int rowsAdded = stmt.executeUpdate();
            System.out.println("Rows added: " + rowsAdded);
        }
    }

    public void getAllUsers() throws SQLException {
        String sql = "SELECT * FROM users";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("User: " + rs.getString("username") + ", " + rs.getString("password"));
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
                System.out.println("User: " + rs.getString("username"));
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
        System.out.println(">>> Auszuführendes unsicheres Statement: \"" + sql + "\"");
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                System.out.println("User: " + rs.getString("username"));
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
        String sql = "SELECT * FROM `users` WHERE `username` = '" + username + "' AND `password` = SHA2('" + password + "', 256)";
        System.out.println("Ausgeführte SQL: " + sql);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("Login erfolgreich mit username: " + username);
            } else {
                System.out.println("Login fehlgeschlagen");
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
        String sql = "SELECT * FROM `users` WHERE `username` = ? AND `password` = SHA2(?, 256)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            System.out.println("PreparedStatement wird ausgeführt (Parameter gebunden).");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Login erfolgreich mit username: " + username);
                } else {
                    System.out.println("Login fehlgeschlagen");
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
