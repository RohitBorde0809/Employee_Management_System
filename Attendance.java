import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.io.PrintWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import java.io.File;

public class Attendance extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CA-2";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public Attendance() {
        setTitle("Attendance Management");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(4, 1, 10, 10));
        
        JButton markAttendanceButton = new JButton("Mark Attendance");
        JButton viewAttendanceButton = new JButton("View Attendance");
        JButton reportButton = new JButton("Attendance Report");
        
        markAttendanceButton.addActionListener(e -> showMarkAttendanceForm());
        viewAttendanceButton.addActionListener(e -> viewAttendance());
        reportButton.addActionListener(e -> generateReport());
        
        mainPanel.add(markAttendanceButton);
        mainPanel.add(viewAttendanceButton);
        mainPanel.add(reportButton);
        
        add(mainPanel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void showMarkAttendanceForm() {
        JFrame markFrame = new JFrame("Mark Attendance");
        markFrame.setSize(400, 300);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        
        // Employee selection
        JLabel empLabel = new JLabel("Select Employee:");
        JComboBox<String> employeeCombo = new JComboBox<>();
        loadEmployees(employeeCombo);
        
        // Status selection
        JLabel statusLabel = new JLabel("Status:");
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Present", "Absent", "Late"});
        
        // Date (current date)
        JLabel dateLabel = new JLabel("Date: " + LocalDate.now().toString());
        
        // Add components
        formPanel.add(empLabel);
        formPanel.add(employeeCombo);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(statusLabel);
        formPanel.add(statusCombo);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(dateLabel);
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton submitButton = new JButton("Submit");
        JButton cancelButton = new JButton("Cancel");
        
        submitButton.addActionListener(e -> {
            String selectedEmp = (String) employeeCombo.getSelectedItem();
            if (selectedEmp != null) {
                String empId = selectedEmp.split(" - ")[0];
                String status = (String) statusCombo.getSelectedItem();
                markAttendance(empId, status);
                markFrame.dispose();
            }
        });
        
        cancelButton.addActionListener(e -> markFrame.dispose());
        
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        markFrame.add(mainPanel);
        markFrame.setLocationRelativeTo(null);
        markFrame.setVisible(true);
    }

    private void viewAttendance() {
        String[] columnNames = {"Date", "Employee", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT a.date, e.name, a.status FROM Attendance a " +
                "JOIN Employee e ON a.emp_id = e.emp_id " +
                "ORDER BY a.date DESC")) {
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getDate("date"),
                    rs.getString("name"),
                    rs.getString("status")
                });
            }
            
            JTable table = new JTable(model);
            showResultFrame("Attendance Records", new JScrollPane(table));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void generateReport() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT e.name, e.emp_id, " +
                "COUNT(CASE WHEN a.status = 'Present' THEN 1 END) as present_count, " +
                "COUNT(CASE WHEN a.status = 'Absent' THEN 1 END) as absent_count, " +
                "COUNT(CASE WHEN a.status = 'Late' THEN 1 END) as late_count " +
                "FROM Employee e " +
                "LEFT JOIN Attendance a ON e.emp_id = a.emp_id " +
                "GROUP BY e.emp_id")) {
            
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            
            while (rs.next()) {
                textArea.append(String.format("%s (%s)\nPresent: %d, Absent: %d, Late: %d\n\n",
                    rs.getString("name"),
                    rs.getString("emp_id"),
                    rs.getInt("present_count"),
                    rs.getInt("absent_count"),
                    rs.getInt("late_count")));
            }
            
            showResultFrame("Attendance Report", new JScrollPane(textArea));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadEmployees(JComboBox<String> combo) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT emp_id, name FROM Employee")) {
            
            while (rs.next()) {
                combo.addItem(rs.getString("emp_id") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void markAttendance(String empId, String status) {
        // First check if attendance already exists
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Check existing attendance
            String checkSql = "SELECT status FROM Attendance WHERE emp_id = ? AND date = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, empId);
                checkStmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    // Attendance already exists, ask user if they want to update
                    int response = JOptionPane.showConfirmDialog(this,
                        "Attendance already marked for this employee today. Do you want to update it?",
                        "Attendance Exists",
                        JOptionPane.YES_NO_OPTION);
                    
                    if (response == JOptionPane.YES_OPTION) {
                        // Update existing attendance
                        String updateSql = "UPDATE Attendance SET status = ? WHERE emp_id = ? AND date = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, status);
                            updateStmt.setString(2, empId);
                            updateStmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                            updateStmt.executeUpdate();
                            JOptionPane.showMessageDialog(this, "Attendance updated successfully!");
                        }
                    }
                } else {
                    // Insert new attendance
                    String insertSql = "INSERT INTO Attendance (emp_id, date, status) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, empId);
                        insertStmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                        insertStmt.setString(3, status);
                        insertStmt.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Attendance marked successfully!");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error marking attendance: " + e.getMessage());
        }
    }

    private void showResultFrame(String title, JComponent component) {
        JFrame frame = new JFrame(title);
        frame.setSize(400, 300);
        frame.add(new JScrollPane(component));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Attendance();
        });
    }
}