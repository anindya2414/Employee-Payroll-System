import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

// === Main Class with GUI ===
public class PayrollSystemGUI extends JFrame {
    private HashMap<Integer, Employee> employees = new HashMap<>();
    private final String FILE_NAME = "employees_data.ser";

    private JTextField idField, nameField, salaryField, taxRateField;
    private JTextArea displayArea;

    public PayrollSystemGUI() {
        setTitle("Employee Payroll System");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        idField = new JTextField();
        nameField = new JTextField();
        salaryField = new JTextField();
        taxRateField = new JTextField();

        inputPanel.add(new JLabel("Employee ID:"));
        inputPanel.add(idField);
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Salary:"));
        inputPanel.add(salaryField);
        inputPanel.add(new JLabel("Tax Rate (e.g., 0.1):"));
        inputPanel.add(taxRateField);

        JButton addButton = new JButton("Add Employee");
        addButton.addActionListener(e -> addEmployee());
        inputPanel.add(addButton);

        JButton leaveButton = new JButton("Mark Leave");
        leaveButton.addActionListener(e -> markLeave());
        inputPanel.add(leaveButton);

        add(inputPanel, BorderLayout.NORTH);

        // Display Area
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        // Bottom Buttons
        JPanel bottomPanel = new JPanel();
        JButton viewButton = new JButton("View Employees");
        viewButton.addActionListener(e -> displayEmployees());
        JButton payrollButton = new JButton("Process Payroll");
        payrollButton.addActionListener(e -> processPayroll());
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveData());

        bottomPanel.add(viewButton);
        bottomPanel.add(payrollButton);
        bottomPanel.add(saveButton);

        add(bottomPanel, BorderLayout.SOUTH);

        loadData();
        setVisible(true);
    }

    // === Add Employee ===
    private void addEmployee() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            double salary = Double.parseDouble(salaryField.getText().trim());
            double taxRate = Double.parseDouble(taxRateField.getText().trim());

            String[] options = {"Full-Time", "Part-Time"};
            int type = JOptionPane.showOptionDialog(this, "Select Employee Type:", "Employee Type",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            Employee emp;
            if (type == 0) {
                emp = new FullTimeEmployee(id, name, salary, taxRate);
            } else if (type == 1) {
                emp = new PartTimeEmployee(id, name, salary, taxRate);
            } else {
                displayArea.setText("Employee type not selected. Cancelled.\n");
                return;
            }

            employees.put(id, emp);
            displayArea.setText("Employee added successfully.\n");
            saveData(); // Save automatically
        } catch (Exception e) {
            displayArea.setText("Error: " + e.getMessage());
        }
    }

    // === Mark Leave ===
    private void markLeave() {
        String input = JOptionPane.showInputDialog(this, "Enter Employee ID:");
        if (input != null) {
            try {
                int id = Integer.parseInt(input);
                Employee emp = employees.get(id);
                if (emp != null) {
                    emp.markLeave();
                    displayArea.setText("Leave marked for " + emp.name + ".\n");
                    saveData();
                } else {
                    displayArea.setText("Employee not found.\n");
                }
            } catch (NumberFormatException ex) {
                displayArea.setText("Invalid ID.\n");
            }
        }
    }

    // === Process Payroll ===
    private void processPayroll() {
        for (Employee e : employees.values()) {
            e.updateNetSalary();
        }
        displayArea.setText("Payroll processed for all employees.\n");
    }

    // === View All Employees ===
    private void displayEmployees() {
        StringBuilder sb = new StringBuilder();
        for (Employee e : employees.values()) {
            sb.append(e).append("\n");
        }
        displayArea.setText(sb.toString());
    }

    // === Save Data to File ===
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(employees);
            displayArea.setText("Data saved successfully.\n");
        } catch (IOException e) {
            displayArea.setText("Save failed: " + e.getMessage());
        }
    }

    // === Load Data from File ===
    private void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            employees = (HashMap<Integer, Employee>) ois.readObject();
            displayArea.setText("Data loaded.\n");
        } catch (Exception e) {
            employees = new HashMap<>();
            displayArea.setText("No previous data found. Starting fresh.\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PayrollSystemGUI::new);
    }
}

// === Parent Class ===
class Employee implements Serializable {
    int id;
    String name;
    double salary;
    double taxRate;
    int leavesTaken;
    double netSalary;

    public Employee(int id, String name, double salary, double taxRate) {
        this.id = id;
        this.name = name;
        this.salary = salary;
        this.taxRate = taxRate;
        this.leavesTaken = 0;
        updateNetSalary();
    }

    public void markLeave() {
        leavesTaken++;
        updateNetSalary();
    }

    public void updateNetSalary() {
        double leaveDeduction = leavesTaken * 100;
        double tax = salary * taxRate;
        netSalary = salary - tax - leaveDeduction;
        if (netSalary < 0) netSalary = 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " - ID: " + id +
                ", Name: " + name +
                ", Salary: " + salary +
                ", Tax Rate: " + taxRate +
                ", Leaves: " + leavesTaken +
                ", Net Salary: " + netSalary;
    }
}

// === Child Class: Full-Time Employee ===
class FullTimeEmployee extends Employee {
    public FullTimeEmployee(int id, String name, double salary, double taxRate) {
        super(id, name, salary, taxRate);
    }

    @Override
    public void updateNetSalary() {
        double leaveDeduction = leavesTaken * 150;
        double tax = salary * taxRate;
        netSalary = salary - tax - leaveDeduction;
        if (netSalary < 0) netSalary = 0;
    }
}

// === Child Class: Part-Time Employee ===
class PartTimeEmployee extends Employee {
    public PartTimeEmployee(int id, String name, double salary, double taxRate) {
        super(id, name, salary, taxRate);
    }

    @Override
    public void updateNetSalary() {
        double leaveDeduction = leavesTaken * 50;
        double tax = salary * taxRate;
        netSalary = salary - tax - leaveDeduction;
        if (netSalary < 0) netSalary = 0;
    }
}
