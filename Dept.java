import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class Dept {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CA-2";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        new Dept();
    }

    public Dept() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Department Management System");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(600, 400);
            
            JPanel panel = new JPanel();
            JButton viewButton = new JButton("View Departments");
            JButton updateButton = new JButton("Update Department");
            JButton addButton = new JButton("Add Department");
            JButton deleteButton = new JButton("Delete Department");
            
            updateButton.addActionListener(e -> showUpdateDepartmentForm(frame));
            viewButton.addActionListener(e -> showViewDepartments(frame));
            addButton.addActionListener(e -> showAddDepartmentForm(frame));
            deleteButton.addActionListener(e -> showDeleteDepartmentForm(frame));
            
            panel.add(viewButton);
            panel.add(updateButton);
            panel.add(addButton);
            panel.add(deleteButton);
            frame.add(panel);
            frame.setVisible(true);
        });
    }

    private static void saveDepartment(String name, String deptId) {
        String sql = "INSERT INTO Department (dept_id, dept_name) VALUES (?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, deptId);
            pstmt.setString(2, name);
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Department added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error adding department: " + e.getMessage());
        }
    }

    private static boolean fetchDepartmentDetails(String deptId, JTextField nameField) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Department WHERE dept_id = ?")) {
            
            pstmt.setString(1, deptId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("dept_name"));
                return true;
            } else {
                JOptionPane.showMessageDialog(null,
                    "Department not found",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error fetching department details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private static void updateDepartment(String deptId, String name) {
        String sql = "UPDATE Department SET dept_name = ? WHERE dept_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, deptId);
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null,
                "Department updated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error updating department: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showAddDepartmentForm(JFrame parentFrame) {
        JDialog dialog = new JDialog(parentFrame, "Add Department", true);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(parentFrame);

        dialog.add(new JLabel("Department ID:"));
        JTextField deptIdField = new JTextField();
        dialog.add(deptIdField);

        dialog.add(new JLabel("Department Name:"));
        JTextField nameField = new JTextField();
        dialog.add(nameField);

        dialog.add(new JLabel(""));
        JButton saveButton = new JButton("Save");
        dialog.add(saveButton);

        saveButton.addActionListener(e -> {
            String name = nameField.getText();
            String deptId = deptIdField.getText().trim();
            
            if (deptId.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Department ID cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            saveDepartment(name, deptId);
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    private static void showUpdateDepartmentForm(JFrame parentFrame) {
        JDialog dialog = new JDialog(parentFrame, "Update Department", true);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(parentFrame);

        dialog.add(new JLabel("Department ID:"));
        JTextField idField = new JTextField();
        dialog.add(idField);

        dialog.add(new JLabel("Department Name:"));
        JTextField nameField = new JTextField();
        dialog.add(nameField);

        JButton fetchButton = new JButton("Fetch");
        JButton updateButton = new JButton("Update");
        dialog.add(fetchButton);
        dialog.add(updateButton);

        fetchButton.addActionListener(e -> {
            try {
                String deptId = idField.getText();
                fetchDepartmentDetails(deptId, nameField);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid Department ID format", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        updateButton.addActionListener(e -> {
            String deptId = idField.getText().trim();
            if (deptId.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Department ID cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String name = nameField.getText();

            updateDepartment(deptId, name);
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    private static void showDeleteDepartmentForm(JFrame parentFrame) {
        JDialog dialog = new JDialog(parentFrame, "Delete Department", true);
        dialog.setLayout(new GridLayout(2, 2, 10, 10));
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(parentFrame);

        dialog.add(new JLabel("Department ID:"));
        JTextField idField = new JTextField();
        dialog.add(idField);

        JButton deleteButton = new JButton("Delete");
        dialog.add(deleteButton);

        deleteButton.addActionListener(e -> {
            String deptId = idField.getText().trim();
            if (deptId.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Department ID cannot be empty", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            deleteDepartment(deptId);
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    private static void deleteDepartment(String deptId) {
        String sql = "DELETE FROM Department WHERE dept_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, deptId);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null,
                    "Department deleted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                    "Department not found",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error deleting department: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showViewDepartments(JFrame parentFrame) {
        JDialog dialog = new JDialog(parentFrame, "View Departments", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(parentFrame);

        String[] columnNames = {"ID", "Name"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Department")) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("dept_id"),
                    rs.getString("dept_name")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error fetching departments: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }

        dialog.add(new JScrollPane(table));
        dialog.setVisible(true);
    }

    // Add this method to show departments with their manager details
    private static void showDepartmentsWithManagers(JFrame parentFrame) {
        String sql = """
            SELECT d.dept_id, d.dept_name, d.location, 
                   COUNT(e.emp_id) as employee_count
            FROM Department d
            LEFT JOIN Employee e ON e.dept_id = d.dept_id
            GROUP BY d.dept_id, d.dept_name, d.location
            """;

        String[] columnNames = {
            "Dept ID", "Department", "Location", "Employee Count"
        };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("dept_id"),
                    rs.getString("dept_name"),
                    rs.getString("location"),
                    rs.getInt("employee_count")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, 
                "Error fetching department details: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }

        JDialog dialog = new JDialog(parentFrame, "Departments with Managers", true);
        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.add(new JScrollPane(table));
        dialog.setVisible(true);
    }

    // Add this method to show department statistics
    private static void showDepartmentStats(JFrame parentFrame) {
        String sql = """
            SELECT d.dept_name,
                   COUNT(e.emp_id) as employee_count,
                   MIN(e.salary) as min_salary,
                   MAX(e.salary) as max_salary,
                   AVG(e.salary) as avg_salary,
                   SUM(e.salary) as total_salary
            FROM department d
            LEFT JOIN employee e ON e.dept_id = d.dept_id
            GROUP BY d.dept_id, d.dept_name
            ORDER BY employee_count DESC
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            StringBuilder report = new StringBuilder();
            report.append("Department Statistics:\n\n");

            while (rs.next()) {
                report.append("Department: ").append(rs.getString("dept_name"))
                      .append("\n----------------------------\n")
                      .append("Employee Count: ").append(rs.getInt("employee_count"))
                      .append("\nMinimum Salary: $").append(String.format("%.2f", rs.getDouble("min_salary")))
                      .append("\nMaximum Salary: $").append(String.format("%.2f", rs.getDouble("max_salary")))
                      .append("\nAverage Salary: $").append(String.format("%.2f", rs.getDouble("avg_salary")))
                      .append("\nTotal Salary: $").append(String.format("%.2f", rs.getDouble("total_salary")))
                      .append("\n\n");
            }

            JTextArea textArea = new JTextArea(report.toString());
            textArea.setEditable(false);
            
            JDialog dialog = new JDialog(parentFrame, "Department Statistics", true);
            dialog.add(new JScrollPane(textArea));
            dialog.setSize(400, 500);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, 
                "Error generating statistics: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
