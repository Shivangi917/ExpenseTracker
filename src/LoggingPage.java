import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoggingPage extends ExpensesTracker{
    private JPanel rootPanel;
    private JTextField tfUserName;
    private String username;
    private JPasswordField tfPassword;
    private JButton loginButton;
    private JButton signUpButton;
    public JPanel getRootPanel() {
        return rootPanel;
    }
    public LoggingPage() {
        // Inside your ActionListener for the Login Button
        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SignUp signUp = new SignUp();
                JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(rootPanel);
                showPage(currentFrame, signUp.getRootPanel(), "Login Page");
            }
        });

        loginButton.addActionListener(e -> {
            username = tfUserName.getText();
            char[] passwordChars = tfPassword.getPassword();
            String password = new String(passwordChars);

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(rootPanel, "Enter valid username or password", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/login", "root", "Password");

                    // Check if username exists in the database
                    String checkQuery = "SELECT * FROM user WHERE username=?";
                    PreparedStatement checkStatement = con.prepareStatement(checkQuery);
                    checkStatement.setString(1, username);
                    ResultSet resultSet = checkStatement.executeQuery();

                    if (resultSet.next()) {
                        // Username exists, validate password
                        String storedPassword = resultSet.getString("password");
                        if (password.equals(storedPassword)) {
                            // Passwords match, login successful
                            ExpensesLogging expensesLogging = new ExpensesLogging(username); // Pass username to ExpensesLogging
                            expensesLogging.loadUserExpenses(username);
                            JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(rootPanel);
                            showPage(currentFrame, expensesLogging.getRootPanel(), "Expense Logging for " + username);
                        } else {
                            // Incorrect password
                            JOptionPane.showMessageDialog(rootPanel, "Incorrect password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        // Username doesn't exist
                        JOptionPane.showMessageDialog(rootPanel, "Username does not exist", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }

                    resultSet.close();
                    checkStatement.close();
                    con.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(rootPanel, "Error while accessing user data", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }


    public static void main(String[] args) {
        LoggingPage loggingPage = new LoggingPage();
        loggingPage.showFrame(loggingPage.getRootPanel(), "Signup/Login");
    }
}
