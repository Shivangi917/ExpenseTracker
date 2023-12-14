import javax.swing.*;

public abstract class ExpensesTracker {
    // Method to display a panel in a frame and hide the current frame
    protected void showPage(JFrame currentFrame, JPanel panelToShow, String title) {
        JFrame frame = createFrame(panelToShow, title);
        hideAndDisplayFrames(currentFrame, frame);
    }

    // Method to display a panel in a new frame
    protected void showFrame(JPanel panelToShow, String title) {
        JFrame frame = createFrame(panelToShow, title);
        displayFrame(frame);
    }

    // Helper method to create a frame with common settings
    private JFrame createFrame(JPanel panel, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setSize(700, 500);
        return frame;
    }

    // Helper method to display a frame
    private void displayFrame(JFrame frame) {
        frame.setVisible(true);
    }

    // Helper method to hide current frame and display a new frame
    private void hideAndDisplayFrames(JFrame currentFrame, JFrame newFrame) {
        currentFrame.setVisible(false);
        displayFrame(newFrame);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoggingPage loggingPage = new LoggingPage();
            loggingPage.showFrame(loggingPage.getRootPanel(), "Signup/Login");
        });
    }
}
