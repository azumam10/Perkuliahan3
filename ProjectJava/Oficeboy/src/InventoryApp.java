import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryApp extends JFrame {
    private JTextField tfName, tfStock;
    private DefaultTableModel tableModel;

    public InventoryApp() {
        setTitle("Aplikasi Inventaris Barang");
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.add(new JLabel("Nama Barang:"));
        tfName = new JTextField();
        inputPanel.add(tfName);

        inputPanel.add(new JLabel("Jumlah Stok:"));
        tfStock = new JTextField();
        inputPanel.add(tfStock);

        JButton btnAdd = new JButton("Tambah");
        inputPanel.add(btnAdd);

        add(inputPanel, BorderLayout.NORTH);

        // Tabel Data
        tableModel = new DefaultTableModel(new String[]{"ID", "Nama Barang", "Stok"}, 0);
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnAdd.addActionListener(e -> addItem());
        loadItems();

        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void addItem() {
        String name = tfName.getText();
        int stock = Integer.parseInt(tfStock.getText());
        String query = "INSERT INTO inventory (name, stock) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setInt(2, stock);
            stmt.executeUpdate();
            loadItems();
            JOptionPane.showMessageDialog(this, "Barang berhasil ditambahkan!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadItems() {
        tableModel.setRowCount(0);
        String query = "SELECT * FROM inventory";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("stock")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InventoryApp::new);
    }
}
