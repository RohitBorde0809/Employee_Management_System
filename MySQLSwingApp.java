import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class MySQLSwingApp {
    // MySQL Database credentials
    static final String DB_URL = "jdbc:mysql://localhost:3306/testdb";  // Replace with your database name
    static final String USER = "root";  // Replace with your MySQL username
    static final String PASS = "root";      // Replace with your MySQL password

    // GUI components
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                MySQLSwingApp window = new MySQLSwingApp();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public MySQLSwingApp() {
        // Initialize the frame
        frame = new JFrame("MySQL Swing Application");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        // Set up table to display database data
        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Email"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Add button to fetch data from MySQL database
        JButton btnLoadData = new JButton("Load Data");
        btnLoadData.addActionListener(e -> loadDataFromDatabase());
        frame.getContentPane().add(btnLoadData, BorderLayout.SOUTH);
    }

    // Method to fetch data from the MySQL database
    private void loadDataFromDatabase() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // Step 1: Connect to MySQL database
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();

            // Step 2: Execute SQL query
            String sql = "SELECT id, name, email FROM users";
            rs = stmt.executeQuery(sql);

            // Step 3: Clear existing rows in the table
            tableModel.setRowCount(0);

            // Step 4: Process the result set and add rows to the table
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                tableModel.addRow(new Object[]{id, name, email});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error fetching data: " + e.getMessage());
        } finally {
            // Clean up the resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
