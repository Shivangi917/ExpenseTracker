import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SignUp extends ExpensesTracker{
    private JTextField tfUserName;
    private JPasswordField tfPassword;
    private JPasswordField tfConfirmPassword;
    private JButton registerButton;
    private JPanel rootPanel;
    private JButton backToLoginButton;
    public JPanel getRootPanel() {
        return rootPanel;
    }

    public SignUp() {
        backToLoginButton.addActionListener(e -> {
            LoggingPage loggingPage = new LoggingPage();
            JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(rootPanel);
            showPage(currentFrame, loggingPage.getRootPanel(), "Login Page");
        });
        registerButton.addActionListener(e -> {
            String username = tfUserName.getText();
            char[] password = tfPassword.getPassword();
            char[] confirmPassword = tfConfirmPassword.getPassword();

            String passwordStr = new String(password);
            String confirmPasswordStr = new String(confirmPassword);

            if (username.isEmpty() || passwordStr.isEmpty() || confirmPasswordStr.isEmpty()) {
                JOptionPane.showMessageDialog(rootPanel, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!passwordStr.equals(confirmPasswordStr)) {
                JOptionPane.showMessageDialog(rootPanel, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/login", "root", "Zhong_1edoc");
                    String checkQuery = "SELECT * FROM user WHERE username=?";
                    PreparedStatement checkStatement = con.prepareStatement(checkQuery);
                    checkStatement.setString(1, username);
                    ResultSet resultSet = checkStatement.executeQuery();

                    if (resultSet.next()) {
                        JOptionPane.showMessageDialog(rootPanel, "Username already exists. Please choose a different one.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        String insertQuery = "INSERT INTO user (username, password) VALUES (?, ?)";
                        PreparedStatement insertStatement = con.prepareStatement(insertQuery);
                        insertStatement.setString(1, username);
                        insertStatement.setString(2, passwordStr);

                        insertStatement.executeUpdate();
                        insertStatement.close();

                        JOptionPane.showMessageDialog(rootPanel, "Registration successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        ExpensesLogging expensesLogging = new ExpensesLogging(username);
                        expensesLogging.loadUserExpenses(username);
                        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(rootPanel);
                        showPage(currentFrame, expensesLogging.getRootPanel(), "Expense Logging");
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
        SignUp signUp = new SignUp();
        signUp.showFrame(signUp.getRootPanel(), "SignUp");
    }
}
