import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.sql.*;

/*
javac -cp ".;mysql-connector-j-8.0.32.jar" *.java
java -cp ".;mysql-connector-j-8.0.32.jar" Main
 */

public class Main extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CA-2";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; 

    // Add these color constants at the class level
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);    // Steel Blue
    private static final Color SECONDARY_COLOR = new Color(176, 196, 222); // Light Steel Blue
    private static final Color BUTTON_HOVER_COLOR = new Color(100, 149, 237); // Cornflower Blue
    private static final Color TEXT_COLOR = Color.WHITE;

    public Main() {
        setTitle("Employee Management System");
        setSize(600, 400);  // Increased size for better visibility
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Use JPanel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, w, h, SECONDARY_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));  // horizontal and vertical gaps of 20
        
        // Create main buttons
        JButton employeeButton = new JButton("Employee");
        JButton departmentButton = new JButton("Department");
        JButton attendanceButton = new JButton("Attendance");
        
        // Set preferred size for buttons
        Dimension buttonSize = new Dimension(120, 40);  // width: 120px, height: 40px
        employeeButton.setPreferredSize(buttonSize);
        departmentButton.setPreferredSize(buttonSize);
        attendanceButton.setPreferredSize(buttonSize);
        
        // Style the buttons
        Font buttonFont = new Font("Arial", Font.BOLD, 14);  // Reduced font size
        employeeButton.setFont(buttonFont);
        departmentButton.setFont(buttonFont);
        attendanceButton.setFont(buttonFont);
        
        // Add action listeners
        employeeButton.addActionListener(e -> {
            // Create and show the Emp window
            new Emp();
        });
        departmentButton.addActionListener(e -> {
            new Dept();  // This creates a new instance of Dept class
        });
        attendanceButton.addActionListener(e -> {
            new Attendance();
        });
        
        // Add buttons to frame
        mainPanel.add(employeeButton);
        mainPanel.add(departmentButton);
        mainPanel.add(attendanceButton);
        
        // Add mainPanel to frame
        add(mainPanel);
        
        // Center the frame on screen
        setLocationRelativeTo(null);
    }
    
    private void showEmployeeMenu() {
        JFrame employeeFrame = new JFrame("Employee Management");
        employeeFrame.setSize(250, 300);  // Reduced frame size
        
        // Change to FlowLayout for better control
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton viewButton = new JButton("View Employees");
        JButton addButton = new JButton("Add Employee");
        JButton updateButton = new JButton("Update Employee");
        JButton deleteButton = new JButton("Delete Employee");
        JButton avgSalaryButton = new JButton("Department Average Salary");
        
        // Set size and style for all buttons
        Dimension buttonSize = new Dimension(180, 30);  // smaller width and height
        Font buttonFont = new Font("Arial", Font.PLAIN, 12);  // smaller font
        
        // Apply size and style to each button
        JButton[] buttons = {viewButton, addButton, updateButton, deleteButton, avgSalaryButton};
        for (JButton button : buttons) {
            button.setPreferredSize(buttonSize);
            button.setFont(buttonFont);
            panel.add(button);  // Add each button to the panel
        }
        
        // Add action listeners
        viewButton.addActionListener(e -> viewEmployees());
        addButton.addActionListener(e -> showAddEmployeeForm());
        updateButton.addActionListener(e -> showUpdateEmployeeForm());
        deleteButton.addActionListener(e -> showDeleteEmployeeForm());
        avgSalaryButton.addActionListener(e -> dep_AverageSalary());
        
        employeeFrame.add(panel);
        employeeFrame.setLocationRelativeTo(null);
        employeeFrame.setVisible(true);
    }
    
    private void showDepartmentMenu() {
        JFrame departmentFrame = new JFrame("Department Management");
        departmentFrame.setSize(300, 300);
        departmentFrame.setLayout(new GridLayout(4, 1, 5, 5));
        
        JButton viewButton = new JButton("View Departments");
        JButton addButton = new JButton("Add Department");
        JButton updateButton = new JButton("Update Department");
        JButton deleteButton = new JButton("Delete Department");
        
        viewButton.addActionListener(e -> viewDepartments());
        addButton.addActionListener(e -> showAddDepartmentForm());
        updateButton.addActionListener(e -> showUpdateDepartmentForm());
        deleteButton.addActionListener(e -> showDeleteDepartmentForm());
        
        departmentFrame.add(viewButton);
        departmentFrame.add(addButton);
        departmentFrame.add(updateButton);
        departmentFrame.add(deleteButton);
        
        departmentFrame.setLocationRelativeTo(null);
        departmentFrame.setVisible(true);
    }

    private void viewEmployees() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Employee")) {
    
            JTextArea textArea = new JTextArea();
            while (rs.next()) {
                textArea.append(rs.getString("emp_id") + ": " +
                        rs.getString("name") + " " +
                        rs.getString("gender") + " - " +
                        rs.getString("phNo") + " " +
                        rs.getString("doj") + " " +
                        rs.getString("salary") + " - " +
                        rs.getString("dep_id") + "\n");  // Display dep_id
            }
            showResultFrame("Employee List", textArea);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void showAddEmployeeForm() {
        JFrame addFrame = new JFrame("Add Employee");
        addFrame.setSize(300, 500);
        addFrame.setLayout(new GridLayout(8, 2, 10, 10));
        
        // Employee ID will be auto-generated by the database
        addFrame.add(new JLabel("emp_id:"));
        JTextField empIdField = new JTextField("Auto-generated");
        empIdField.setEditable(false);
        addFrame.add(empIdField);
        
        // Name field
        addFrame.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        addFrame.add(nameField);
        
        // Gender field
        addFrame.add(new JLabel("Gender:"));
        JTextField genderField = new JTextField();
        addFrame.add(genderField);
        
        // Phone Number field
        addFrame.add(new JLabel("Phone Number:"));
        JTextField phNoField = new JTextField();
        addFrame.add(phNoField);
        
        // Date of Joining field
        addFrame.add(new JLabel("Date of Joining:"));
        JTextField dojField = new JTextField();
        addFrame.add(dojField);
        
        // Salary field
        addFrame.add(new JLabel("Salary:"));
        JTextField salaryField = new JTextField();
        addFrame.add(salaryField);
        
        // Department ID field (ComboBox) with a null option
        addFrame.add(new JLabel("Department ID:"));
        JComboBox<Integer> depIdComboBox = new JComboBox<>();
        depIdComboBox.addItem(null);  // Add null option for department ID
        loadDepartmentIds(depIdComboBox);  // Assuming you have a method to load department IDs
        addFrame.add(depIdComboBox);
        
        // Add Button
        JButton addButton = new JButton("Add");
        addFrame.add(addButton);
        
        // Action listener for the Add button
        addButton.addActionListener(e -> {
            String name = nameField.getText();
            String gender = genderField.getText();
            String phNo = phNoField.getText();
            String doj = dojField.getText();
            String salary = salaryField.getText();
            Integer depId = (Integer) depIdComboBox.getSelectedItem();
            
            // Insert employee into the database (auto-incrementing emp_id)
            addEmployeeToDatabase(name, gender, phNo, doj, salary, depId);
            addFrame.dispose();
        });
        
        addFrame.setVisible(true);
    }
    private void addEmployeeToDatabase(String name, String gender, String phNo, String doj, String salary, Integer depId) {
        // Example JDBC code to insert into the database
        String sql = "INSERT INTO Employee (name, gender, phNo, doj, salary, dep_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, gender);
            pstmt.setString(3, phNo);
            pstmt.setString(4, doj);
            pstmt.setString(5, salary);
            
            // If depId is null, use null in the SQL statement
            if (depId != null) {
                pstmt.setInt(6, depId);
            } else {
                pstmt.setNull(6, java.sql.Types.INTEGER);  // Set dep_id as NULL if no department is selected
            }
    
            int rowsAffected = pstmt.executeUpdate();
    
            if (rowsAffected > 0) {
                // Successfully inserted, show success message
                JOptionPane.showMessageDialog(null, "Employee added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // If no rows are affected, show failure message
                JOptionPane.showMessageDialog(null, "Failed to add employee.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error message on exception
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showUpdateEmployeeForm() {
        JFrame updateFrame = new JFrame("Update Employee");
        updateFrame.setSize(300, 500);  // Increased size to add more fields
        updateFrame.setLayout(new GridLayout(8, 2));  // Adjusted grid layout to fit new fields
        
        // Employee selection combo box (no manual emp_id input)
        updateFrame.add(new JLabel("Select Employee:"));
        JComboBox<String> empComboBox = new JComboBox<>();
        loadEmployeeNames(empComboBox);  // Assuming a method to load employee names or identifiers
        updateFrame.add(empComboBox);
        
        // Name input
        updateFrame.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        updateFrame.add(nameField);
        
        // Gender input
        updateFrame.add(new JLabel("Gender:"));
        JTextField genderField = new JTextField();
        updateFrame.add(genderField);
        
        // Phone Number input
        updateFrame.add(new JLabel("Phone Number:"));
        JTextField phNoField = new JTextField();
        updateFrame.add(phNoField);
        
        // Salary input
        updateFrame.add(new JLabel("Salary:"));
        JTextField salaryField = new JTextField();
        updateFrame.add(salaryField);
        
        // Adding department selection (dropdown) with a null option
        updateFrame.add(new JLabel("Department ID:"));
        JComboBox<Integer> depIdComboBox = new JComboBox<>();
        depIdComboBox.addItem(null);  // Add null as an option for department
        loadDepartmentIds(depIdComboBox);  // Assuming you have a method to load department IDs
        updateFrame.add(depIdComboBox);
        
        // Update Button
        JButton updateButton = new JButton("Update");
        updateFrame.add(updateButton);
    
        updateButton.addActionListener(e -> {
            String selectedEmployee = (String) empComboBox.getSelectedItem();
            
            if (selectedEmployee == null) {
                JOptionPane.showMessageDialog(updateFrame, "Please select an employee!");
                return;
            }
    
            // Assuming employee name contains emp_id and is in the format "emp_id - name"
            String empId = selectedEmployee.split(" - ")[0];
            String name = nameField.getText();
            String gender = genderField.getText();
            String phNo = phNoField.getText();
            String salary = salaryField.getText();
            Integer depId = (Integer) depIdComboBox.getSelectedItem();  // Get selected department ID
    
            // Validation: Ensure all fields are filled out
            if (name.isEmpty() || gender.isEmpty() || phNo.isEmpty() || salary.isEmpty()) {
                JOptionPane.showMessageDialog(updateFrame, "All fields must be filled out!");
                return;
            }
    
            try {
                // Attempt to parse salary to ensure it is a valid number
                Double.parseDouble(salary);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(updateFrame, "Salary must be a valid number.");
                return;
            }
    
            updateEmployee(empId, name, gender, phNo, salary, depId);  // Pass the new data to the update method
            updateFrame.dispose();
        });
    
        updateFrame.setVisible(true);
    }
    
    private void loadEmployeeNames(JComboBox<String> empComboBox) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT emp_id, name FROM Employee")) {
            
            while (rs.next()) {
                String empId = rs.getString("emp_id");
                String name = rs.getString("name");
                empComboBox.addItem(empId + " - " + name);  // Store emp_id and name together for easy reference
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadDepartmentIds(JComboBox<Integer> depIdComboBox) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT dep_id FROM Department")) {
            
            while (rs.next()) {
                depIdComboBox.addItem(rs.getInt("dep_id"));  // Add department ID to the combo box
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void updateEmployee(String empId, String name, String gender, String phNo, String salary, Integer depId) {
        String sql = "UPDATE Employee SET name = ?, gender = ?, phNo = ?, salary = ?, dep_id = ? WHERE emp_id = ?";
    
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            pstmt.setString(1, name);  // Set the new name
            pstmt.setString(2, gender);  // Set the new gender
            pstmt.setString(3, phNo);  // Set the new phone number
            pstmt.setString(4, salary);  // Set the new salary
            pstmt.setObject(5, depId, java.sql.Types.INTEGER);  // Set the department ID (can be null)
            pstmt.setString(6, empId);  // Set the employee ID for the employee to be updated
    
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Employee updated successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "Employee with the given ID not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error updating employee: " + e.getMessage());
        }
    }
    

    


    private void showDeleteEmployeeForm() {
        JFrame deleteFrame = new JFrame("Delete Employee");
        deleteFrame.setSize(300, 150);
        deleteFrame.setLayout(new GridLayout(2, 2));

        deleteFrame.add(new JLabel("emp_id:"));
        JTextField empIdField = new JTextField();
        deleteFrame.add(empIdField);

        JButton deleteButton = new JButton("Delete");
        deleteFrame.add(deleteButton);
        
        deleteButton.addActionListener(e -> {
            String empId = empIdField.getText();
            deleteEmployee(empId);
            deleteFrame.dispose();
        });
        
        deleteFrame.setVisible(true);
    }

    private void deleteEmployee(String empId) {
        String sql = "DELETE FROM Employee WHERE emp_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Employee deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void dep_AverageSalary() {
        String sql = "SELECT dep_id, AVG(salary) AS avg_salary FROM Employee GROUP BY dep_id";
    
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
    
            // JTextArea to display results
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false); // Make the text area non-editable
    
            while (rs.next()) {
                int depId = rs.getInt("dep_id");
                double avgSalary = rs.getDouble("avg_salary");
    
                textArea.append("Department ID: " + depId + " - Average Salary: " + avgSalary + "\n");
            }
    
            showResultFrame("Average Salary by Department", textArea);
    
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error executing advanced query.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    

    private void showResultFrame(String title, JTextArea textArea) {
        JFrame resultFrame = new JFrame(title);
        resultFrame.setSize(400, 300);
        resultFrame.add(new JScrollPane(textArea));
        resultFrame.setVisible(true);
    }

    private void viewDepartments() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Department")) {
    
            JTextArea textArea = new JTextArea();
            while (rs.next()) {
                textArea.append(rs.getString("dep_id") + ": " +
                        rs.getString("name") + " - " +
                        rs.getString("location") + "\n");
            }
            showResultFrame("Department List", textArea);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAddDepartmentForm() {
        JFrame addFrame = new JFrame("Add Department");
        addFrame.setSize(300, 200);
        addFrame.setLayout(new GridLayout(4, 2));
        
        addFrame.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        addFrame.add(nameField);
        
        addFrame.add(new JLabel("Location:"));
        JTextField locationField = new JTextField();
        addFrame.add(locationField);
        
        JButton addButton = new JButton("Add");
        addFrame.add(addButton);
        
        addButton.addActionListener(e -> {
            String name = nameField.getText();
            String location = locationField.getText();
            addDepartmentToDatabase(name, location);
            addFrame.dispose();
        });
        
        addFrame.setVisible(true);
    }

    private void addDepartmentToDatabase(String name, String location) {
        String sql = "INSERT INTO Department (name, location) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, location);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Department added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to add department.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showUpdateDepartmentForm() {
        JFrame updateFrame = new JFrame("Update Department");
        updateFrame.setSize(300, 200);
        updateFrame.setLayout(new GridLayout(4, 2));
        
        // Department selection combo box
        updateFrame.add(new JLabel("Select Department:"));
        JComboBox<String> depComboBox = new JComboBox<>();
        loadDepartmentNames(depComboBox);
        updateFrame.add(depComboBox);
        
        // Name input
        updateFrame.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        updateFrame.add(nameField);
        
        // Location input
        updateFrame.add(new JLabel("Location:"));
        JTextField locationField = new JTextField();
        updateFrame.add(locationField);
        
        // Update Button
        JButton updateButton = new JButton("Update");
        updateFrame.add(updateButton);
        
        updateButton.addActionListener(e -> {
            String selectedDepartment = (String) depComboBox.getSelectedItem();
            if (selectedDepartment == null) {
                JOptionPane.showMessageDialog(updateFrame, "Please select a department!");
                return;
            }
            
            String depId = selectedDepartment.split(" - ")[0];
            String name = nameField.getText();
            String location = locationField.getText();
            
            updateDepartment(depId, name, location);
            updateFrame.dispose();
        });
        
        updateFrame.setVisible(true);
    }
    
    private void loadDepartmentNames(JComboBox<String> depComboBox) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT dep_id, name FROM Department")) {
            
            while (rs.next()) {
                String depId = rs.getString("dep_id");
                String name = rs.getString("name");
                depComboBox.addItem(depId + " - " + name);  // Store dep_id and name together for easy reference
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDepartment(String depId, String name, String location) {
        String sql = "UPDATE Department SET name = ?, location = ? WHERE dep_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, location);
            pstmt.setString(3, depId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Department updated successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "Department with the given ID not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error updating department: " + e.getMessage());
        }
    }

    private void showDeleteDepartmentForm() {
        JFrame deleteFrame = new JFrame("Delete Department");
        deleteFrame.setSize(300, 150);
        deleteFrame.setLayout(new GridLayout(2, 2));

        deleteFrame.add(new JLabel("Select Department:"));
        JComboBox<String> depComboBox = new JComboBox<>();
        loadDepartmentNames(depComboBox);  // Reuse existing method to load department names
        deleteFrame.add(depComboBox);

        JButton deleteButton = new JButton("Delete");
        deleteFrame.add(deleteButton);
        
        deleteButton.addActionListener(e -> {
            String selectedDepartment = (String) depComboBox.getSelectedItem();
            if (selectedDepartment != null) {
                String depId = selectedDepartment.split(" - ")[0];  // Extract department ID
                deleteDepartment(depId);
                deleteFrame.dispose();
            } else {
                JOptionPane.showMessageDialog(deleteFrame, "Please select a department!");
            }
        });
        
        deleteFrame.setVisible(true);
    }

    private void deleteDepartment(String depId) {
        String sql = "DELETE FROM Department WHERE dep_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, depId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Department deleted successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "Department not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error deleting department: " + e.getMessage());
        }
    }

    private void showMonthlyAttendanceReport() {
        JFrame reportFrame = new JFrame("Monthly Attendance Report");
        reportFrame.setSize(400, 150);
        reportFrame.setLayout(new GridLayout(3, 2, 10, 10));

        // Month selection
        reportFrame.add(new JLabel("Select Month (1-12):"));
        JComboBox<Integer> monthComboBox = new JComboBox<>();
        for (int i = 1; i <= 12; i++) {
            monthComboBox.addItem(i);
        }
        reportFrame.add(monthComboBox);

        // Year input
        reportFrame.add(new JLabel("Enter Year:"));
        JTextField yearField = new JTextField();
        reportFrame.add(yearField);

        JButton generateButton = new JButton("Generate Report");
        reportFrame.add(generateButton);

        generateButton.addActionListener(e -> {
            try {
                int month = (Integer) monthComboBox.getSelectedItem();
                int year = Integer.parseInt(yearField.getText());
                generateMonthlyReport(month, year);
                reportFrame.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(reportFrame, "Please enter a valid year!");
            }
        });

        reportFrame.setLocationRelativeTo(null);
        reportFrame.setVisible(true);
    }

    private void generateMonthlyReport(int month, int year) {
        String sql = """
            SELECT 
                e.emp_id,
                e.name,
                d.name as department,
                COUNT(a.date) as total_days,
                SUM(CASE WHEN a.status = 'present' THEN 1 ELSE 0 END) as present_days,
                SUM(CASE WHEN a.status = 'absent' THEN 1 ELSE 0 END) as absent_days,
                SUM(CASE WHEN a.status = 'leave' THEN 1 ELSE 0 END) as leave_days
            FROM 
                Employee e
                LEFT JOIN Attendance a ON e.emp_id = a.emp_id 
                    AND MONTH(a.date) = ? 
                    AND YEAR(a.date) = ?
                LEFT JOIN Department d ON e.dep_id = d.dep_id
            GROUP BY 
                e.emp_id, e.name, d.name
            ORDER BY 
                e.emp_id
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, month);
            pstmt.setInt(2, year);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Create text area for report
                JTextArea reportArea = new JTextArea();
                reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

                // Add report header
                reportArea.append(String.format("Monthly Attendance Report - %d/%d\n", month, year));
                reportArea.append("=".repeat(60) + "\n");
                reportArea.append(String.format("%-6s %-15s %-15s %-10s %-10s %-10s %-10s\n",
                        "ID", "Name", "Department", "Total", "Present", "Absent", "Leave"));
                reportArea.append("-".repeat(60) + "\n");

                // Add data rows
                while (rs.next()) {
                    String empId = rs.getString("emp_id");
                    String name = rs.getString("name");
                    String dept = rs.getString("department");
                    int totalDays = rs.getInt("total_days");
                    int presentDays = rs.getInt("present_days");
                    int absentDays = rs.getInt("absent_days");
                    int leaveDays = rs.getInt("leave_days");

                    reportArea.append(String.format("%-6s %-15s %-15s %-10d %-10d %-10d %-10d\n",
                            empId, 
                            truncateString(name, 15),
                            truncateString(dept, 15),
                            totalDays,
                            presentDays,
                            absentDays,
                            leaveDays));
                }

                // Show report in a scrollable frame
                JFrame resultFrame = new JFrame("Attendance Report");
                resultFrame.setSize(600, 400);
                JScrollPane scrollPane = new JScrollPane(reportArea);
                resultFrame.add(scrollPane);
                resultFrame.setLocationRelativeTo(null);
                resultFrame.setVisible(true);

            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating report: " + e.getMessage());
        }
    }

    private String truncateString(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length) : str;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main mainFrame = new Main();
            mainFrame.setVisible(true);
        });
    }
}