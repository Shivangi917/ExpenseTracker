import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ExpensesLogging extends ExpensesTracker{
    private JPanel rootPanel;
    private JTextField tfDate;
    private JTextField tfAmount;
    private JButton addCategoryButton;
    private JTable dataTable;
    private JButton addButton;
    private JButton deleteButton;
    private DefaultTableModel tableModel;
    private int uniqueID = 1;
    private JTextField tfTotalExpenses;
    private JButton logOutButton;
    private JComboBox cBCategory;
    private JTextField tfAddCategory;
    private JButton setBudgetButton;
    private JButton showBudgetButton;
    private JButton graphButton;
    final String username;
    final Map<String, Double> budgetMap = new HashMap<>();
    private Connection con;
    void loadUserExpenses(String username) {
        try {
            String selectQuery = "SELECT * FROM expenses WHERE username=?";
            PreparedStatement selectStatement = con.prepareStatement(selectQuery);
            selectStatement.setString(1, username);
            ResultSet resultSet = selectStatement.executeQuery();

            // Populate the JTable or handle retrieved records
            while (resultSet.next()) {
                int uniqueId = resultSet.getInt("uniqueId");
                String date = resultSet.getString("date");
                double amount = resultSet.getDouble("amount");
                String category = resultSet.getString("category");
                double budget = resultSet.getDouble("budgetPoint"); // Ensure 'budget point' is the correct column name
                Object[] rowData = {uniqueId, date, amount, category, budget};
                tableModel.addRow(rowData);
            }

            resultSet.close();
            selectStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle retrieval errors
        }
    }
    public JPanel getRootPanel() {
        return rootPanel;
    }
    public ExpensesLogging(String username) {
        this.username = username;
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/login", "root", "Password");
        } catch (Exception e) {
            e.printStackTrace();
            // Handle connection errors
        }
        createTable();
        graphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Map<String, Double> userBudgets = loadUserBudgets(username);
                createPieChart(userBudgets);
            }
        });
        showBudgetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Logic to retrieve and display budgets when the button is clicked
                    Map<String, Double> userBudgets = loadUserBudgets(username);

                    StringBuilder message = new StringBuilder();
                    for (Map.Entry<String, Double> entry : userBudgets.entrySet()) {
                        message.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
                    }

                    JOptionPane.showMessageDialog(rootPanel, message.toString(), "Budgets", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(rootPanel, "Error retrieving budgets", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        setBudgetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Show a dialog box to select a category and set the budget
                int itemCount = cBCategory.getItemCount();
                String[] comboBoxItems = new String[itemCount];
                for (int i = 0; i < itemCount; i++) {
                    comboBoxItems[i] = cBCategory.getItemAt(i).toString();
                }
                // Show a dialog box to select a category and set the budget
                String selectedCategory = (String) JOptionPane.showInputDialog(
                        rootPanel,
                        "Select Category:",
                        "Set Budget",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        comboBoxItems,
                        null);

                if (selectedCategory != null) { // If a category is selected
                    String budgetStr = JOptionPane.showInputDialog(rootPanel, "Enter Budget for " + selectedCategory + ":");
                    if (budgetStr != null && !budgetStr.isEmpty()) {
                        try {
                            double budgetAmount = Double.parseDouble(budgetStr);
                            // Set the budget for the selected category
                            if (budgetMap.containsKey(selectedCategory)) {
                                int confirmation = JOptionPane.showConfirmDialog(
                                        rootPanel,
                                        "A budget for this category already exists. Do you want to update it?",
                                        "Confirm Update",
                                        JOptionPane.YES_NO_OPTION
                                );

                                if (confirmation == JOptionPane.YES_OPTION) {
                                    // Update the budget for the selected category
                                    updateBudget(username, selectedCategory, budgetAmount);
                                    JOptionPane.showMessageDialog(rootPanel, "Budget for " + selectedCategory + " updated successfully.");
                                }
                            } else {
                                // If the budget doesn't exist, simply set it
                                setBudget(selectedCategory, budgetAmount);
                                insertBudget(username, selectedCategory, budgetAmount);
                                JOptionPane.showMessageDialog(rootPanel, "Budget for " + selectedCategory + " set successfully.");
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(rootPanel, "Invalid Budget Amount.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(rootPanel, "Please enter a valid budget amount.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        addCategoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String addCategory = tfAddCategory.getText();
                if(categoryExists(addCategory, cBCategory)) {
                    JOptionPane.showMessageDialog(rootPanel, "The item exists. Please select from the category.");
                } else if(addCategory.matches("^\\s*$")) {
                    JOptionPane.showMessageDialog(rootPanel, "Please Enter Input");
                    tfAddCategory.setText("");
                }else {
                    cBCategory.addItem(addCategory);
                    tfAddCategory.setText("");
                    sortComboBoxItems(cBCategory);
                }
            }
        });
        logOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoggingPage loggingPage = new LoggingPage();
                JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(rootPanel);
                showPage(currentFrame, loggingPage.getRootPanel(), "Login Page");
            }
        });
        tfDate.setText("dd/mm/yyyy");
        tfDate.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tfDate.getText().equals("dd/mm/yyyy")) {
                    tfDate.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (tfDate.getText().isEmpty()) {
                    tfDate.setText("dd/mm/yyyy");
                }
            }
        });
        // action listener to add button
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String date = tfDate.getText();
                if(!isValidDate(date)) {
                    JOptionPane.showMessageDialog(rootPanel, "Please enter date in dd/mm/yyyy format.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String formattedDate = formatDate(date);
                String amountText = tfAmount.getText();
                double amount;
                // check whether it is in numeric format or not
                try {
                    amount = Double.parseDouble(amountText);
                } catch(NumberFormatException ex) {
                    JOptionPane.showMessageDialog(rootPanel, "Invalid Amount Format.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String category = (String)cBCategory.getSelectedItem();
                double budgetAmount = budgetMap.containsKey(category) ? budgetMap.get(category) : 0.0;
                double totalCategoryExpenses = calculateTotalCategoryExpenses(category);
                double budgetPoint = budgetAmount - totalCategoryExpenses - amount;
                Object[] rowData = {uniqueID++, date, amount, category, budgetPoint};
                if(budgetPoint < 0) {
                    JOptionPane.showMessageDialog(rootPanel, "You have exceeded your budget on  " + category + " !");
                } else if(budgetPoint <= 100) {
                    JOptionPane.showMessageDialog(rootPanel, "Please control your expenditure because you are about to exceed your budget on " + category + " !");
                }
                insertExpenseRecord(username, uniqueID, formattedDate, amount, category, budgetPoint);
                // Add budget point to rowData
                rowData = Arrays.copyOf(rowData, rowData.length + 1);
                rowData[rowData.length - 1] = budgetPoint;

                tableModel.addRow(rowData);
                tfDate.setText("dd/mm/yyyy");
                tfAmount.setText("");
                // Calculate total expenses initially
                updateTotalExpenses();
            }
        });
        // action listener to delete button
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String uniqueIDToDelete = JOptionPane.showInputDialog(rootPanel, "Enter Unique ID to be deleted: ");
                boolean dataFound = false;
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String UniqueID = tableModel.getValueAt(i, 0).toString();

                    if (UniqueID.equals(uniqueIDToDelete)) {
                        tableModel.removeRow(i);
                        dataFound = true;
                        reassignUniqueIDs();
                        uniqueID--;

                        try {
                            String deleteQuery = "DELETE FROM expenses WHERE username = ? AND uniqueId = ?";
                            PreparedStatement deleteStatement = con.prepareStatement(deleteQuery);
                            deleteStatement.setString(1, username);
                            deleteStatement.setString(2, uniqueIDToDelete);
                            deleteStatement.executeUpdate();
                            deleteStatement.close();

                            // Calculate total expenses initially
                            updateTotalExpenses();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            throw new RuntimeException(ex);
                            // Handle the SQL exception as needed
                        }
                        break; // Break once the row is deleted
                    }
                }
                if (!dataFound) {
                    JOptionPane.showMessageDialog(rootPanel, "Data does not exist.", "Data Not Found", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        // add mouse listener to update any data
        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = dataTable.getSelectedRow();
                int column = dataTable.getSelectedColumn();
                int id = (int) tableModel.getValueAt(row, 0);

                if(row != -1 && column != -1) {
                    String columnName = dataTable.getColumnName(column);
                    if (columnName.equals("Amount")) { // Check if it's the amount column
                        String newValue = JOptionPane.showInputDialog(rootPanel, "Enter new value:");
                        if (newValue != null) {
                            try {
                                double amount = Double.parseDouble(newValue); // Parse the new value
                                tableModel.setValueAt(amount, row, column); // Set the amount in the table

                                updateTotalExpenses(); // Update total expenses after updating the amount
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(rootPanel, "Invalid Amount Format.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                            double updatedAmount = (double) tableModel.getValueAt(row, column);

                            // Construct the SQL UPDATE statement
                            try {
                                String updateQuery = "UPDATE expenses SET amount = ? WHERE id = ?";
                                PreparedStatement updateStatement = con.prepareStatement(updateQuery);
                                updateStatement.setDouble(1, updatedAmount);
                                updateStatement.setInt(2, (int) tableModel.getValueAt(row, 0)); // Assuming the ID is in the first column
                                updateStatement.executeUpdate();
                                updateStatement.close();

                                // Update total expenses after updating the amount
                                updateTotalExpenses();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(rootPanel, "Failed to update amount in the database.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else if (columnName.equals("Category")) {
                        String newCategory = JOptionPane.showInputDialog(rootPanel, "Enter new value:");
                        if (newCategory != null) {
                            tableModel.setValueAt(newCategory, row, column);
                            if (categoryExists(newCategory, cBCategory)) {
                                cBCategory.addItem(newCategory);
                                sortComboBoxItems(cBCategory);
                            } else {
                                JOptionPane.showMessageDialog(rootPanel, "Category input is empty", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        String updatedCategory = (String) tableModel.getValueAt(row, column);
                        // Construct and execute the UPDATE query for Category
                        try {
                            String updateCategoryQuery = "UPDATE expenses SET category = ? WHERE id = ?";
                            PreparedStatement updateCategoryStatement = con.prepareStatement(updateCategoryQuery);
                            updateCategoryStatement.setString(1, updatedCategory);
                            updateCategoryStatement.setInt(2, id);
                            updateCategoryStatement.executeUpdate();
                            updateCategoryStatement.close();
                            updateTotalExpenses();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(rootPanel, "Failed to update category in the database.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else if (columnName.equals("Date")) {
                        String newDate = JOptionPane.showInputDialog(rootPanel, "Enter new Date:");
                        if (!isValidDate(newDate)) {
                            JOptionPane.showMessageDialog(rootPanel, "Please enter date in dd/mm/yyyy format.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        } else {
                            String formattedDate = formatDate(newDate);
                            tableModel.setValueAt(formattedDate, row, column);
                        }
                        String updatedDate = (String) tableModel.getValueAt(row, column);
                        // Construct and execute the UPDATE query for Date
                        try {
                            String updateDateQuery = "UPDATE expenses SET date = ? WHERE id = ?";
                            PreparedStatement updateDateStatement = con.prepareStatement(updateDateQuery);
                            updateDateStatement.setString(1, updatedDate);
                            updateDateStatement.setInt(2, id);
                            updateDateStatement.executeUpdate();
                            updateDateStatement.close();
                            // No specific total expenses update needed for date change
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(rootPanel, "Failed to update date in the database.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(rootPanel, "Cannot change it", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                        // For other columns, let the default behavior handle them
                        // For instance, allow editing other non-numeric columns directly
                        super.mouseClicked(e);
                }
            }
        });
    }
    private void reassignUniqueIDs() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(i + 1, i, 0); // Update unique IDs sequentially
        }
    }
    private boolean isValidDate(String date) {
        String[] dateSplit = date.split("/");
        if (dateSplit.length != 3) {
            return false; // Ensure the date has three parts: day, month, year
        }

        int day, month, year;
        try {
            day = Integer.parseInt(dateSplit[0]);
            month = Integer.parseInt(dateSplit[1]);
            year = Integer.parseInt(dateSplit[2]);
        } catch (NumberFormatException e) {
            return false; // If any part is not a number, it's invalid
        }

        if (month < 1 || month > 12) {
            return false; // Invalid month
        }

        if (day < 1 || day > getMaxDaysInMonth(month, year)) {
            return false; // Invalid day for the given month
        }

        // Check for future or very old dates (adjust the range as needed)
        int currentYear = java.time.LocalDate.now().getYear();
        if (year < 1900 || year > currentYear) {
            return false;
        }

        return true; // If none of the conditions fail, it's a valid date
    }
    private int getMaxDaysInMonth(int month, int year) {
        if (month == 2) {
            return (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28;
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            return 30;
        }
        return 31;
    }
    private String formatDate(String date) {
        String dateSplit[] = date.split("/");
        int day = Integer.parseInt(dateSplit[0]);
        int month = Integer.parseInt(dateSplit[1]);
        int year = Integer.parseInt(dateSplit[2]);
        // Format the date to dd/mm/yyyy
        return String.format("%02d/%02d/%04d", day, month, year);
    }
    private void createTable() {
        tableModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Unique ID", "Date", "Amount", "Category", "Budget point"}
        );
        dataTable.setModel(tableModel);
        tfTotalExpenses.setEditable(false); // Make the total expenses field non-editable
    }
    private boolean categoryExists(String addCategory, JComboBox<String> cBCategory) {
        for (int i = 0; i < cBCategory.getItemCount(); i++) {
            if (addCategory.equalsIgnoreCase(cBCategory.getItemAt(i))) {
                return true;
            }
        }

        return false;
    }
    private void setBudget(String category, double budgetAmount) {
        budgetMap.put(category, budgetAmount);
    }
    private void sortComboBoxItems(JComboBox<String> comboBox) {
        int itemCount = comboBox.getItemCount();
        String[] items = new String[itemCount];

        // Retrieve all items into an array
        for (int i =0; i < itemCount; i++) {
            items[i] = comboBox.getItemAt(i);
        }

        // Sort the array
        Arrays.sort(items);

        // Clear the JComboBox
        comboBox.removeAllItems();

        // Add the sorted items back to the JComboBox
        for (String item : items) {
            comboBox.addItem(item);
        }
    }
    private void insertExpenseRecord(String username, int uniqueID, String date, double amount, String category, double budgetPoint) {
        try {
            String insertQuery = "INSERT INTO expenses (username, uniqueId, date, amount, category, budgetPoint) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStatement = con.prepareStatement(insertQuery);
            insertStatement.setString(1, username);
            insertStatement.setInt(2, uniqueID);
            insertStatement.setString(3, date);
            insertStatement.setDouble(4, amount);
            insertStatement.setString(5, category);
            insertStatement.setDouble(6, budgetPoint);
            insertStatement.executeUpdate();
            insertStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle insertion errors
        }
    }
    private void insertBudget(String username, String category, double budgetAmount) {
        try {
            String insertBudgetQuery = "INSERT INTO budgets (username, category, budget) VALUES (?, ?, ?)";
            PreparedStatement insertBudgetStatement = con.prepareStatement(insertBudgetQuery);
            insertBudgetStatement.setString(1, username);
            insertBudgetStatement.setString(2, category);
            insertBudgetStatement.setDouble(3, budgetAmount);
            insertBudgetStatement.executeUpdate();
            insertBudgetStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle insertion errors
        }
    }
    private void updateBudget(String username, String category, double newBudgetAmount) {
        try {
            String updateBudgetQuery = "UPDATE budgets SET budget = ? WHERE username = ? AND category = ?";
            PreparedStatement updateBudgetStatement = con.prepareStatement(updateBudgetQuery);
            updateBudgetStatement.setDouble(1, newBudgetAmount);
            updateBudgetStatement.setString(2, username);
            updateBudgetStatement.setString(3, category);
            updateBudgetStatement.executeUpdate();
            updateBudgetStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle update errors
        }
    }
    private void updateTotalExpenses() {
        double totalExpenses = 0.0;

        // Calculate the total amount from the tableModel
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            totalExpenses += (double) tableModel.getValueAt(i, 2);
        }

        tfTotalExpenses.setText(String.valueOf(totalExpenses));
    }
    private double calculateTotalCategoryExpenses(String category) {
        double totalExpenses = 0.0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String rowCategory = (String) tableModel.getValueAt(i, 3); // Assuming category is at index 3 in rowData
            if (rowCategory.equals(category)) {
                totalExpenses += (double) tableModel.getValueAt(i, 2); // Assuming amount is at index 2 in rowData
            }
        }
        return totalExpenses;
    }
    private void createPieChart(Map<String, Double> userBudgets) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        // Add data to the dataset
        for (Map.Entry<String, Double> entry : userBudgets.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        // Create the chart
        JFreeChart chart = ChartFactory.createPieChart(
                "Total Amount by Category",  // Chart title
                dataset,                     // Dataset
                true,                        // Include legend
                true,
                false
        );

        // Display chart in a JFrame
        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame frame = new JFrame("Pie Chart");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(chartPanel, BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
    private Map<String, Double> loadUserBudgets(String username) {
        Map<String, Double> userBudgets = new HashMap<>();

        try {
            String selectBudgetQuery = "SELECT category, budget FROM budgets WHERE username=?";
            PreparedStatement selectBudgetStatement = con.prepareStatement(selectBudgetQuery);
            selectBudgetStatement.setString(1, username);
            ResultSet budgetResultSet = selectBudgetStatement.executeQuery();

            while (budgetResultSet.next()) {
                String category = budgetResultSet.getString("category");
                double budgetAmount = budgetResultSet.getDouble("budget");
                userBudgets.put(category, budgetAmount);
            }

            budgetResultSet.close();
            selectBudgetStatement.close();
            System.out.println("Retrieved Budgets:");
            for (Map.Entry<String, Double> entry : userBudgets.entrySet()) {
                System.out.println("Category: " + entry.getKey() + ", Budget: " + entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle retrieval errors
        }

        return userBudgets;
    }

    public static void main(String[] args) {}
}