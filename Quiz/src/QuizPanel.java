import javax.swing.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import javax.swing.Timer;

public class QuizPanel extends JFrame {
    private JTextArea questionArea;
    private JRadioButton[] optionButtons;
    private ButtonGroup optionGroup;
    private JButton nextButton, downloadScoreButton, emailScoreButton;
    private JLabel timerLabel;
    private ArrayList<String> questions;
    private ArrayList<ArrayList<String>> options;
    private ArrayList<String> correctAnswers;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String username;  // Username of quiz taker for scorecard
    private Timer timer;
    private int timeRemaining;

    // Constructor
    public QuizPanel(String username) {
        this.username = username;
        setTitle("Quiz Panel");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        questions = getQuestionsFromDatabase();
        options = getOptionsFromDatabase();
        correctAnswers = getCorrectAnswersFromDatabase();

        questionArea = new JTextArea();
        questionArea.setBounds(20, 20, 450, 100);
        questionArea.setEditable(false);
        add(questionArea);

        // Timer label
        timerLabel = new JLabel("Time Remaining: ");
        timerLabel.setBounds(300, 10, 150, 30);
        add(timerLabel);

        // Options as radio buttons
        optionButtons = new JRadioButton[4];  // Assuming 4 options per question
        optionGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setBounds(20, 150 + (i * 30), 450, 30);
            optionGroup.add(optionButtons[i]);
            add(optionButtons[i]);
        }

        nextButton = new JButton("Next");
        nextButton.setBounds(200, 300, 100, 30);
        add(nextButton);

        downloadScoreButton = new JButton("Download Scorecard");
        downloadScoreButton.setBounds(150, 350, 180, 30);
        downloadScoreButton.setVisible(false);  // Initially hidden
        add(downloadScoreButton);

        emailScoreButton = new JButton("Email Scorecard");
        emailScoreButton.setBounds(150, 400, 180, 30);
        emailScoreButton.setVisible(false);  // Initially hidden
        add(emailScoreButton);

        displayQuestion(currentQuestionIndex);

        // Timer: total time for all questions (e.g., 300 sec for 10 questions)
        timeRemaining = questions.size() * 30;  // 30 seconds per question
        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeRemaining--;
                timerLabel.setText("Time Remaining: " + timeRemaining + " sec");
                if (timeRemaining <= 0) {
                    ((Timer) e.getSource()).stop();
                    JOptionPane.showMessageDialog(null, "Time's up! Your score: " + score);
                    endQuiz();
                }
            }
        });
        timer.start();

        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkAnswer();
                currentQuestionIndex++;
                if (currentQuestionIndex < questions.size()) {
                    displayQuestion(currentQuestionIndex);
                } else {
                    JOptionPane.showMessageDialog(null, "Quiz completed! Your score: " + score);
                    endQuiz();
                }
            }
        });

        downloadScoreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downloadScorecard();
            }
        });

        emailScoreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String email = JOptionPane.showInputDialog("phanindhar157@gmail.com:");
                if (email != null && !email.isEmpty()) {
                    sendScorecardByEmail(email);
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter a valid email address.");
                }
            }
        });

        setVisible(true);
    }

    // Method to retrieve questions from the database
    private ArrayList<String> getQuestionsFromDatabase() {
        ArrayList<String> questionList = new ArrayList<>();
        Connection connection = DatabaseConnection.getConnection();
        String query = "SELECT question_text FROM questions";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                questionList.add(resultSet.getString("question_text"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questionList;
    }

    // Method to retrieve options from the database
    private ArrayList<ArrayList<String>> getOptionsFromDatabase() {
        ArrayList<ArrayList<String>> optionList = new ArrayList<>();
        Connection connection = DatabaseConnection.getConnection();
        String query = "SELECT option_a, option_b, option_c, option_d FROM questions";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                ArrayList<String> options = new ArrayList<>();
                options.add(resultSet.getString("option_a"));
                options.add(resultSet.getString("option_b"));
                options.add(resultSet.getString("option_c"));
                options.add(resultSet.getString("option_d"));
                optionList.add(options);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return optionList;
    }

    // Method to retrieve correct answers from the database
    private ArrayList<String> getCorrectAnswersFromDatabase() {
        ArrayList<String> answerList = new ArrayList<>();
        Connection connection = DatabaseConnection.getConnection();
        String query = "SELECT correct_option FROM questions";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                answerList.add(resultSet.getString("correct_option"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answerList;
    }

    // Method to display the current question and options
    private void displayQuestion(int index) {
        questionArea.setText(questions.get(index));
        ArrayList<String> currentOptions = options.get(index);
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].setText(currentOptions.get(i));
            optionButtons[i].setSelected(false);
        }
    }

    // Method to check the selected answer
    private void checkAnswer() {
        String selectedOption = "";
        for (JRadioButton optionButton : optionButtons) {
            if (optionButton.isSelected()) {
                selectedOption = optionButton.getText();
            }
        }
        if (selectedOption.equals(correctAnswers.get(currentQuestionIndex))) {
            score++;
        }
    }

    // Method to end the quiz and show the scorecard options
    private void endQuiz() {
        downloadScoreButton.setVisible(true);
        emailScoreButton.setVisible(true);
        nextButton.setEnabled(false);  // Disable next button after completion
        timer.stop();  // Stop the timer
    }

    // Method to download the scorecard
    private void downloadScorecard() {
        try (FileWriter writer = new FileWriter(username + "_scorecard.txt")) {
            writer.write("Quiz Scorecard for " + username + "\n");
            writer.write("Total Questions: " + questions.size() + "\n");
            writer.write("Correct Answers: " + score + "\n");
            writer.write("Your Score: " + score + "/" + questions.size());
            JOptionPane.showMessageDialog(null, "Scorecard downloaded successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to send the scorecard by email using JavaMail API
    private void sendScorecardByEmail(String recipientEmail) {
        // Assuming you have set up SMTP server details
        String senderEmail = "sm2813@srmist.edu.in";  // Replace with your email
        String senderPassword = "9246558262Phani,";  // Replace with your email password
        String subject = "Quiz Scorecard for " + username;
        String body = "Quiz Scorecard\n" +
                      "Total Questions: " + questions.size() + "\n" +
                      "Correct Answers: " + score + "\n" +
                      "Your Score: " + score + "/" + questions.size();

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            JOptionPane.showMessageDialog(null, "Scorecard sent successfully to " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // Main method to run the quiz panel
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new QuizPanel("User1");  // Example username
        });
    }
}
