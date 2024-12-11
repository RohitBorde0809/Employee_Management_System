import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class Emp {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/CA-2";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";  // Use your password if set

    public Emp() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Employee Management System");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(600, 400);
            
            JPanel panel = new JPanel();
            JButton viewButton = new JButton("View Employees");
            JButton addButton = new JButton("Add Employee");
            JButton updateButton = new JButton("Update Employee");
            JButton deleteButton = new JButton("Delete Employee");
            
            viewButton.addActionListener(e -> viewEmployees(frame));
            addButton.addActionListener(e -> showAddEmployeeForm(frame));
            updateButton.addActionListener(e -> showUpdateEmployeeForm(frame));
            deleteButton.addActionListener(e -> showDeleteEmployeeForm(frame));
            
            panel.add(viewButton);
            panel.add(addButton);
            panel.add(updateButton);
            panel.add(deleteButton);
            
            frame.add(panel);
            frame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        new Emp();
    }

    // View all employees
    private static void viewEmployees(JFrame parent) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT e.emp_id, e.name, e.email, e.phone_number, " +
                 "e.doj, e.salary, d.dept_name " +
                 "FROM Employee e " +
                 "JOIN Department d ON e.dept_id = d.dept_id")) {

            Vector<String> columnNames = new Vector<>();
            columnNames.add("Employee ID");
            columnNames.add("Name");
            columnNames.add("Email");
            columnNames.add("Phone No.");
            columnNames.add("Date of Joining");
            columnNames.add("Salary");
            columnNames.add("Department");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("emp_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone_number"));
                row.add(rs.getDate("doj"));
                row.add(rs.getDouble("salary"));
                row.add(rs.getString("dept_name"));
                data.add(row);
            }

            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);
            JFrame resultFrame = new JFrame("Employee List");
            resultFrame.setSize(800, 400);
            resultFrame.add(scrollPane);
            resultFrame.setVisible(true);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error retrieving employees: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Show form to add an employee
    private static void showAddEmployeeForm(JFrame parent) {
        JFrame addFrame = new JFrame("Add Employee");
        addFrame.setSize(400, 400);
        addFrame.setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField[] fields = new JTextField[6];
        String[] labels = {
            "Name *", 
            "Email *",
            "Phone No.",
            "DOJ (YYYY-MM-DD) *",
            "Salary *",
            "Department ID *"
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = i;
            gbc.gridx = 0;
            gbc.weightx = 0.2;
            formPanel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.8;
            fields[i] = new JTextField(20);
            formPanel.add(fields[i], gbc);
        }

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton addButton = new JButton("Add Employee");
        JButton cancelButton = new JButton("Cancel");
        
        // Style buttons
        addButton.setPreferredSize(new Dimension(120, 30));
        cancelButton.setPreferredSize(new Dimension(120, 30));
        
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        
        // Add components to main panel
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        addFrame.add(mainPanel);
        
        // Button listeners
        addButton.addActionListener(e -> {
            // Validate required fields
            if (fields[0].getText().trim().isEmpty() || 
                fields[1].getText().trim().isEmpty() || 
                fields[3].getText().trim().isEmpty() || 
                fields[4].getText().trim().isEmpty() ||
                fields[5].getText().trim().isEmpty()) {
                
                JOptionPane.showMessageDialog(addFrame,
                    "Please fill in all required fields (marked with *)",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Add employee
            addEmployee(
                fields[0].getText().trim(),  // name
                fields[1].getText().trim(),  // email
                fields[2].getText().trim(),  // phone
                fields[3].getText().trim(),  // doj
                fields[4].getText().trim(),  // salary
                fields[5].getText().trim()   // department_id
            );
            addFrame.dispose();
        });
        
        cancelButton.addActionListener(e -> addFrame.dispose());
        
        // Final frame setup
        addFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addFrame.pack();
        addFrame.setVisible(true);
    }

    // Add an employee to the database
    private static void addEmployee(String name, String email, String phone, 
                              String doj, String salary, String deptId) {
        String sql = "INSERT INTO Employee (emp_id, name, email, phone_number, " +
                     "doj, salary, dept_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String empId = generateNextEmpId();
            pstmt.setString(1, empId);
            pstmt.setString(2, name);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            pstmt.setDate(5, Date.valueOf(doj));
            pstmt.setDouble(6, Double.parseDouble(salary));
            pstmt.setString(7, deptId);
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Employee added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error adding employee: " + e.getMessage());
        }
    }

    // Add method to generate next employee ID
    private static String generateNextEmpId() {
        String nextEmpId = "EMP101"; // Default if no employees exist
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT emp_id FROM Employee ORDER BY emp_id DESC LIMIT 1")) {
            
            if (rs.next()) {
                String lastEmpId = rs.getString("emp_id");
                int lastNumber = Integer.parseInt(lastEmpId.substring(3));
                nextEmpId = String.format("EMP%03d", lastNumber + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nextEmpId;
    }

    // Show form to update an employee
    private static void showUpdateEmployeeForm(JFrame parent) {
        JFrame updateFrame = new JFrame("Update Employee");
        updateFrame.setSize(400, 400);
        updateFrame.setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Search Panel
        JPanel searchPanel = new JPanel(new GridBagLayout());
        GridBagConstraints searchGbc = new GridBagConstraints();
        searchGbc.fill = GridBagConstraints.HORIZONTAL;
        searchGbc.insets = new Insets(5, 5, 5, 5);

        JLabel searchLabel = new JLabel("Employee ID *");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(100, 30));

        searchGbc.gridx = 0; searchGbc.gridy = 0; searchGbc.weightx = 0.2;
        searchPanel.add(searchLabel, searchGbc);
        searchGbc.gridx = 1; searchGbc.weightx = 0.6;
        searchPanel.add(searchField, searchGbc);
        searchGbc.gridx = 2; searchGbc.weightx = 0.2;
        searchPanel.add(searchButton, searchGbc);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField[] fields = new JTextField[6];
        String[] labels = {
            "Name *",
            "Email *",
            "Phone No.",
            "DOJ (YYYY-MM-DD) *",
            "Salary *",
            "Department ID *"
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = i;
            
            // Label
            gbc.gridx = 0;
            gbc.weightx = 0.2;
            formPanel.add(new JLabel(labels[i]), gbc);
            
            // TextField
            gbc.gridx = 1;
            gbc.weightx = 0.8;
            fields[i] = new JTextField(20);
            fields[i].setEnabled(false); // Initially disabled
            formPanel.add(fields[i], gbc);
        }

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton updateButton = new JButton("Update Employee");
        JButton cancelButton = new JButton("Cancel");

        // Style buttons
        updateButton.setPreferredSize(new Dimension(120, 30));
        cancelButton.setPreferredSize(new Dimension(120, 30));
        updateButton.setEnabled(false); // Initially disabled

        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);

        // Add components to main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(new JSeparator(), BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        updateFrame.add(mainPanel);

        // Search button listener
        searchButton.addActionListener(e -> {
            String empId = searchField.getText().trim().toUpperCase();
            if (empId.isEmpty()) {
                JOptionPane.showMessageDialog(updateFrame,
                    "Please enter an Employee ID",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!empId.matches("EMP\\d{3}")) {
                JOptionPane.showMessageDialog(updateFrame,
                    "Please enter a valid Employee ID (Format: EMPxxx, e.g., EMP101)",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            fetchEmployeeDetails(empId, fields);
            updateButton.setEnabled(true);
        });

        // Update button listener
        updateButton.addActionListener(e -> {
            String empId = searchField.getText().trim();
            
            // Validate required fields
            if (fields[0].getText().trim().isEmpty() || 
                fields[1].getText().trim().isEmpty() || 
                fields[3].getText().trim().isEmpty() || 
                fields[4].getText().trim().isEmpty() ||
                fields[5].getText().trim().isEmpty()) {
                
                JOptionPane.showMessageDialog(updateFrame,
                    "Please fill in all required fields (marked with *)",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate salary format
            try {
                Double.parseDouble(fields[4].getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(updateFrame,
                    "Please enter a valid number for salary",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update employee
            updateEmployee(
                empId,
                fields[0].getText().trim(),  // name
                fields[1].getText().trim(),  // email
                fields[2].getText().trim(),  // phone
                fields[3].getText().trim(),  // doj
                fields[4].getText().trim(),  // salary
                fields[5].getText().trim()   // dept_id
            );
            updateFrame.dispose();
        });

        cancelButton.addActionListener(e -> updateFrame.dispose());

        // Final frame setup
        updateFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        updateFrame.pack();
        updateFrame.setVisible(true);
    }

    // Add this method to fetch employee details
    private static void fetchEmployeeDetails(String empId, JTextField[] fields) {
        String sql = "SELECT e.*, d.dept_name FROM Employee e " +
                     "JOIN Department d ON e.dept_id = d.dept_id " +
                     "WHERE e.emp_id = ?";
                 
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, empId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Updated to use correct column names
                fields[0].setText(rs.getString("name"));        // Changed from first_name
                fields[1].setText(rs.getString("email"));
                fields[2].setText(rs.getString("phone_number"));
                fields[3].setText(rs.getDate("doj").toString());
                fields[4].setText(String.valueOf(rs.getDouble("salary")));
                fields[5].setText(rs.getString("dept_id"));
                
                // Enable fields for editing
                for (JTextField field : fields) {
                    field.setEnabled(true);
                }
            } else {
                JOptionPane.showMessageDialog(null,
                    "Employee not found",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error fetching employee details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Update the updateEmployee method
    private static void updateEmployee(String empId, String name, String email, 
                                     String phone, String doj, String salary, String deptId) {
        String sql = "UPDATE Employee SET name=?, email=?, phone_number=?, " +
                     "doj=?, salary=?, dept_id=? WHERE emp_id=?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setDate(4, Date.valueOf(doj));
            pstmt.setDouble(5, Double.parseDouble(salary));
            pstmt.setString(6, deptId);
            pstmt.setString(7, empId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, 
                    "Employee updated successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error updating employee: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Show form to delete an employee
    private static void showDeleteEmployeeForm(JFrame parent) {
        JFrame deleteFrame = new JFrame("Delete Employee");
        deleteFrame.setSize(400, 400);
        deleteFrame.setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Search Panel
        JPanel searchPanel = new JPanel(new GridBagLayout());
        GridBagConstraints searchGbc = new GridBagConstraints();
        searchGbc.fill = GridBagConstraints.HORIZONTAL;
        searchGbc.insets = new Insets(5, 5, 5, 5);

        JLabel searchLabel = new JLabel("Employee ID *");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(100, 30));

        searchGbc.gridx = 0; searchGbc.gridy = 0; searchGbc.weightx = 0.2;
        searchPanel.add(searchLabel, searchGbc);
        searchGbc.gridx = 1; searchGbc.weightx = 0.6;
        searchPanel.add(searchField, searchGbc);
        searchGbc.gridx = 2; searchGbc.weightx = 0.2;
        searchPanel.add(searchButton, searchGbc);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField[] fields = new JTextField[6];
        String[] labels = {
            "Name",
            "Email",
            "Phone No.",
            "DOJ",
            "Salary",
            "Department ID"
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = i;
            
            // Label
            gbc.gridx = 0;
            gbc.weightx = 0.2;
            formPanel.add(new JLabel(labels[i]), gbc);
            
            // TextField
            gbc.gridx = 1;
            gbc.weightx = 0.8;
            fields[i] = new JTextField(20);
            fields[i].setEnabled(false); // Always disabled - read only
            formPanel.add(fields[i], gbc);
        }

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton deleteButton = new JButton("Delete Employee");
        JButton cancelButton = new JButton("Cancel");

        // Style buttons
        deleteButton.setPreferredSize(new Dimension(120, 30));
        cancelButton.setPreferredSize(new Dimension(120, 30));
        deleteButton.setEnabled(false); // Initially disabled

        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);

        // Add components to main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(new JSeparator(), BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        deleteFrame.add(mainPanel);

        // Search button listener
        searchButton.addActionListener(e -> {
            String empId = searchField.getText().trim().toUpperCase();
            if (empId.isEmpty()) {
                JOptionPane.showMessageDialog(deleteFrame,
                    "Please enter an Employee ID",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!empId.matches("EMP\\d{3}")) {
                JOptionPane.showMessageDialog(deleteFrame,
                    "Please enter a valid Employee ID (Format: EMPxxx, e.g., EMP101)",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (fetchEmployeeDetailsForDelete(empId, fields)) {
                deleteButton.setEnabled(true);
            }
        });

        // Delete button listener
        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(deleteFrame,
                "Are you sure you want to delete this employee?\n\n" +
                "Employee Details:\n" +
                "ID: " + searchField.getText().trim() + "\n" +
                "Name: " + fields[0].getText() + "\n" +
                "Email: " + fields[1].getText() + "\n" +
                "Phone: " + fields[2].getText() + "\n" +
                "DOJ: " + fields[3].getText() + "\n" +
                "Salary: " + fields[4].getText() + "\n" +
                "Department ID: " + fields[5].getText(),
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                deleteEmployee(searchField.getText().trim());
                deleteFrame.dispose();
            }
        });

        cancelButton.addActionListener(e -> deleteFrame.dispose());

        // Final frame setup
        deleteFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        deleteFrame.pack();
        deleteFrame.setVisible(true);
    }

    // Update fetchEmployeeDetailsForDelete method
    private static boolean fetchEmployeeDetailsForDelete(String empId, JTextField[] fields) {
        String sql = "SELECT e.*, d.dept_name FROM Employee e " +
                     "JOIN Department d ON e.dept_id = d.dept_id " +
                     "WHERE e.emp_id = ?";
                 
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, empId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                fields[0].setText(rs.getString("name"));
                fields[1].setText(rs.getString("email"));
                fields[2].setText(rs.getString("phone_number"));
                fields[3].setText(rs.getDate("doj").toString());
                fields[4].setText(String.valueOf(rs.getDouble("salary")));
                fields[5].setText(rs.getString("dept_id"));
                return true;
            } else {
                JOptionPane.showMessageDialog(null,
                    "Employee not found",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error fetching employee details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Update the deleteEmployee method
    private static void deleteEmployee(String empId) {
        String sql = "DELETE FROM Employee WHERE emp_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, empId);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, 
                    "Employee deleted successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, 
                    "Employee not found!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error deleting employee: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
