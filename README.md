**Employee Management System**
This project is an Employee Management System developed using Java Swing, MySQL, and JDBC. It provides a user-friendly interface for managing employee records, attendance, and departmental information.

**Features**
  Add, update, and delete employee records
  Manage attendance records
  Organize employees by department
  User-friendly graphical interface

**Technologies Used**
  Java Swing for the graphical user interface
  MySQL for the database
  JDBC for database connectivity

**Installation**
  1)Clone the repository:
    git clone https://github.com/RohitBorde0809/Employee_Management_System.git
  
  2)Navigate to the project directory:
    cd Employee_Management_System
  
  3)Set up the MySQL database:
    Create a database named employee_management.
    Import the provided SQL file to set up the necessary tables.
  
  4)Update the database connection details in MySQLSwingApp.java:
    String url = "jdbc:mysql://localhost:3306/employee_management";
    String username = "your_username";
    String password = "your_password";
  
  5)Compile and run the application:
    javac MySQLSwingApp.java
    java MySQLSwingApp
    
**Usage**
  Launch the application and use the graphical interface to manage employee records, attendance, and departments.
  Use the provided buttons and forms to add, update, and delete records.

**Contributing**
  Contributions are welcome! Please fork the repository and create a pull request with your changes.
