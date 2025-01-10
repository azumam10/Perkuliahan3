import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.sql.*;

public class InventoryApp {
    // JDBC Variables
    private static final String DB_URL = "jdbc:mysql://localhost:3306/inventory";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "rootpassword";

    // Swing Components
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtItemName, txtCategory, txtQuantity;
    private JButton btnAdd, btnUpdate, btnDelete;

    public InventoryApp() {
        frame = new JFrame("Inventory Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);

        // Layout and Components
        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel lblItemName = new JLabel("Item Name:");
        lblItemName.setBounds(20, 20, 100, 25);
        panel.add(lblItemName);

        txtItemName = new JTextField();
        txtItemName.setBounds(120, 20, 150, 25);
        panel.add(txtItemName);

        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setBounds(20, 60, 100, 25);
        panel.add(lblCategory);

        txtCategory = new JTextField();
        txtCategory.setBounds(120, 60, 150, 25);
        panel.add(txtCategory);

        JLabel lblQuantity = new JLabel("Quantity:");
        lblQuantity.setBounds(20, 100, 100, 25);
        panel.add(lblQuantity);

        txtQuantity = new JTextField();
        txtQuantity.setBounds(120, 100, 150, 25);
        panel.add(txtQuantity);

        btnAdd = new JButton("Add");
        btnAdd.setBounds(20, 140, 75, 25);
        panel.add(btnAdd);

        btnUpdate = new JButton("Update");
        btnUpdate.setBounds(100, 140, 85, 25);
        panel.add(btnUpdate);

        btnDelete = new JButton("Delete");
        btnDelete.setBounds(195, 140, 85, 25);
        panel.add(btnDelete);

        tableModel = new DefaultTableModel(new String[]{"ID", "Item Name", "Category", "Quantity"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 180, 640, 250);
        panel.add(scrollPane);

        frame.add(panel);

        // Load Data
        loadData();

        // Event Handlers
        btnAdd.addActionListener(e -> addItem());
        btnUpdate.addActionListener(e -> updateItem());
        btnDelete.addActionListener(e -> deleteItem());

        frame.setVisible(true);
    }

    private void loadData() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM inventory_items")) {

            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("item_name"), rs.getString("category"), rs.getInt("quantity")});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error loading data: " + ex.getMessage());
        }
    }

    private void addItem() {
        String itemName = txtItemName.getText();
        String category = txtCategory.getText();
        String quantityText = txtQuantity.getText();

        if (itemName.isEmpty() || category.isEmpty() || quantityText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill all fields.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO inventory_items (item_name, category, quantity) VALUES (?, ?, ?)");) {

            pstmt.setString(1, itemName);
            pstmt.setString(2, category);
            pstmt.setInt(3, Integer.parseInt(quantityText));
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Item added successfully.");
            loadData();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error adding item: " + ex.getMessage());
        }
    }

    private void updateItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an item to update.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String itemName = txtItemName.getText();
        String category = txtCategory.getText();
        String quantityText = txtQuantity.getText();

        if (itemName.isEmpty() || category.isEmpty() || quantityText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill all fields.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("UPDATE inventory_items SET item_name = ?, category = ?, quantity = ? WHERE id = ?")) {

            pstmt.setString(1, itemName);
            pstmt.setString(2, category);
            pstmt.setInt(3, Integer.parseInt(quantityText));
            pstmt.setInt(4, id);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Item updated successfully.");
            loadData();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error updating item: " + ex.getMessage());
        }
    }

    private void deleteItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an item to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM inventory_items WHERE id = ?")) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Item deleted successfully.");
            loadData();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error deleting item: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InventoryApp());
    }
}
