import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class GUI extends JFrame {
    private SqlInjectionDemo sqlDemo;
    private JComboBox<String> modeSelector;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextArea outputArea;
    private JButton loginButton;
    private JButton getUserButton;
    private JButton getAllUsersButton;
    private JButton clearOutputButton;

    public GUI() {
        initializeUI();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            sqlDemo = new SqlInjectionDemo();
            sqlDemo.setOutputListener(this::appendOutput);
            try {
                sqlDemo.createTable();
                sqlDemo.addDummyData();
                appendOutput("Datenbank initialisiert mit Dummy-Daten.\n");
            } catch (SQLException e) {
                appendOutput("Datenbank existiert bereits.\n");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Datenbankverbindung fehlgeschlagen:\n" + e.getMessage(),
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initializeUI() {
        setTitle("SQL Injection Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top panel - Mode selection
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel - Input fields and buttons
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel - Output area
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Modus"));

        JLabel modeLabel = new JLabel("Abfrage-Modus:");
        modeSelector = new JComboBox<>(new String[]{"Safe (PreparedStatement)", "Unsafe (SQL-Injection anfällig)"});
        modeSelector.setPreferredSize(new Dimension(250, 25));

        panel.add(modeLabel);
        panel.add(modeSelector);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Eingabe"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Benutzername:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Passwort:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        // Buttons panel
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        panel.add(createButtonPanel(), gbc);

        // Example inputs panel
        gbc.gridy = 3;
        panel.add(createExamplesPanel(), gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> performLogin());

        getUserButton = new JButton("Benutzer abrufen");
        getUserButton.addActionListener(e -> getUser());

        getAllUsersButton = new JButton("Alle Benutzer anzeigen");
        getAllUsersButton.addActionListener(e -> getAllUsers());

        clearOutputButton = new JButton("Ausgabe löschen");
        clearOutputButton.addActionListener(e -> outputArea.setText(""));

        panel.add(loginButton);
        panel.add(getUserButton);
        panel.add(getAllUsersButton);
        panel.add(clearOutputButton);

        return panel;
    }

    private JPanel createExamplesPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(new TitledBorder("SQL-Injection Beispiele"));

        JTextField example1 = new JTextField("Beispiel 1: Username = alice' OR '1'='1  (alle Benutzer)");
        JTextField example2 = new JTextField("Beispiel 2: Username = admin'--  Password = beliebig  (Login ohne Passwort)");
        JTextField example3 = new JTextField("Beispiel 3: Username = admin'; DROP TABLE users;--  (Tabelle löschen)");

        example1.setEditable(false);
        example2.setEditable(false);
        example3.setEditable(false);

        example1.setFont(new Font("Monospaced", Font.PLAIN, 11));
        example2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        example3.setFont(new Font("Monospaced", Font.PLAIN, 11));

        panel.add(example1);
        panel.add(example2);
        panel.add(example3);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Ausgabe"));

        outputArea = new JTextArea(10, 60);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Bitte Benutzername und Passwort eingeben.",
                    "Fehlende Eingabe",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isSafe = modeSelector.getSelectedIndex() == 0;
        appendOutput("\n========================================\n");
        appendOutput("Login-Versuch mit Modus: " + modeSelector.getSelectedItem() + "\n");
        appendOutput("Benutzername: " + username + "\n");
        appendOutput("========================================\n");

        try {
            if (isSafe) {
                sqlDemo.safeLogin(username, password);
            } else {
                sqlDemo.vulnerableLogin(username, password);
            }
        } catch (SQLException e) {
            appendOutput("SQL Fehler: " + e.getMessage() + "\n");
        }
    }

    private void getUser() {
        String username = usernameField.getText();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Bitte Benutzername eingeben.",
                    "Fehlende Eingabe",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isSafe = modeSelector.getSelectedIndex() == 0;
        appendOutput("\n========================================\n");
        appendOutput("Benutzer-Abfrage mit Modus: " + modeSelector.getSelectedItem() + "\n");
        appendOutput("Benutzername: " + username + "\n");
        appendOutput("========================================\n");

        try {
            if (isSafe) {
                sqlDemo.getUserByUsernameSafe(username);
            } else {
                sqlDemo.getUserByUsernameUnsafe(username);
            }
        } catch (SQLException e) {
            appendOutput("SQL Fehler: " + e.getMessage() + "\n");
        }
    }

    private void getAllUsers() {
        appendOutput("\n========================================\n");
        appendOutput("Alle Benutzer abrufen\n");
        appendOutput("========================================\n");

        try {
            sqlDemo.getAllUsers();
        } catch (SQLException e) {
            appendOutput("SQL Fehler: " + e.getMessage() + "\n");
        }
    }

    private void appendOutput(String text) {
        outputArea.append(text);
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    @Override
    public void dispose() {
        try {
            if (sqlDemo != null) {
                sqlDemo.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}
