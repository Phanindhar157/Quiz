import javax.swing.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminPanel extends JFrame {
    private JTextArea questionArea;
    private JButton addQuestionButton, deleteQuestionButton;
    
    public AdminPanel() {
        setTitle("Admin Panel");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        questionArea = new JTextArea();
        questionArea.setBounds(20, 20, 350, 100);
        add(questionArea);

        addQuestionButton = new JButton("Add Question");
        addQuestionButton.setBounds(20, 150, 150, 30);
        add(addQuestionButton);

        deleteQuestionButton = new JButton("Delete Question");
        deleteQuestionButton.setBounds(200, 150, 150, 30);
        add(deleteQuestionButton);

        addQuestionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String question = questionArea.getText();
                String optionA = JOptionPane.showInputDialog("Enter Option A:");
                String optionB = JOptionPane.showInputDialog("Enter Option B:");
                String optionC = JOptionPane.showInputDialog("Enter Option C:");
                String optionD = JOptionPane.showInputDialog("Enter Option D:");
                String correctOption = JOptionPane.showInputDialog("Enter Correct Option (A/B/C/D):");

                if (!question.isEmpty() && correctOption.matches("[A-Da-d]")) {
                    saveQuestionToDatabase(question, optionA, optionB, optionC, optionD, correctOption.toUpperCase());
                    questionArea.setText("");
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter a valid question and correct option.");
                }
            }
        });

        deleteQuestionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String questionId = JOptionPane.showInputDialog("Enter Question ID to delete:");
                deleteQuestionFromDatabase(questionId);
            }
        });

        setVisible(true);
    }

    // Method to save questions into the database
    private void saveQuestionToDatabase(String question, String optionA, String optionB, String optionC, String optionD, String correctOption) {
        Connection connection = DatabaseConnection.getConnection();
        String query = "INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, question);
            statement.setString(2, optionA);
            statement.setString(3, optionB);
            statement.setString(4, optionC);
            statement.setString(5, optionD);
            statement.setString(6, correctOption);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(null, "Question added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to delete questions from the database
    private void deleteQuestionFromDatabase(String questionId) {
        Connection connection = DatabaseConnection.getConnection();
        String query = "DELETE FROM questions WHERE question_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, questionId);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Question deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Question ID not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new AdminPanel();
    }
}
