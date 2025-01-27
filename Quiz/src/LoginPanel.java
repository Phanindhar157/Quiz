import javax.swing.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginPanel extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginPanel() {
        setTitle("Login Panel");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(20, 30, 80, 25);
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(100, 30, 160, 25);
        add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 70, 80, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(100, 70, 160, 25);
        add(passwordField);

        loginButton = new JButton("Login");
        loginButton.setBounds(100, 110, 80, 30);
        add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();
                if (validateLogin(username, password)) {
                    // Successful login, open QuizPanel
                    new QuizPanel(username);  // Pass username to QuizPanel
                    dispose();  // Close the login window
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid credentials, please try again.");
                }
            }
        });

        setVisible(true);
    }

    // Method to validate user credentials
    private boolean validateLogin(String username, String password) {
        boolean isValid = false;
        Connection connection = DatabaseConnection.getConnection();
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            isValid = resultSet.next();  // If a record exists, login is valid
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isValid;
    }

    public static void main(String[] args) {
        new LoginPanel();
    }
}
