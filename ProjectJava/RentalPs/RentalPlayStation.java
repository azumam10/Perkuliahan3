import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class RentalPlayStation extends JFrame {
    // Deklarasi komponen GUI dan koneksi database
    private JTextField tfNamaPelanggan, tfKontak, tfLamaSewa;
    private JComboBox<String> cbKonsol;
    private JTable table;
    private DefaultTableModel model;
    private Connection conn;

    public RentalPlayStation() {
        // Membuat koneksi ke database MySQL
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/rental_ps", "root", "rootpassword");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Koneksi Database Gagal: " + e.getMessage());
            System.exit(0);
        }

        // Konfigurasi dasar frame
        setTitle("Rental PlayStation");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        // Membuat dan mengatur posisi komponen GUI untuk nama pelanggan
        JLabel lblNama = new JLabel("Nama Pelanggan:");
        lblNama.setBounds(20, 20, 120, 25);
        add(lblNama);

        tfNamaPelanggan = new JTextField();
        tfNamaPelanggan.setBounds(150, 20, 200, 25);
        add(tfNamaPelanggan);

        // Komponen untuk kontak pelanggan
        JLabel lblKontak = new JLabel("Kontak:");
        lblKontak.setBounds(20, 60, 120, 25);
        add(lblKontak);

        tfKontak = new JTextField();
        tfKontak.setBounds(150, 60, 200, 25);
        add(tfKontak);

        // Komponen untuk pemilihan konsol
        JLabel lblKonsol = new JLabel("Konsol:");
        lblKonsol.setBounds(20, 100, 120, 25);
        add(lblKonsol);

        cbKonsol = new JComboBox<>();
        cbKonsol.setBounds(150, 100, 200, 25);
        add(cbKonsol);
        loadKonsol(); // Memuat data konsol dari database

        // Komponen untuk lama sewa
        JLabel lblLamaSewa = new JLabel("Lama Sewa (jam):");
        lblLamaSewa.setBounds(20, 140, 120, 25);
        add(lblLamaSewa);

        tfLamaSewa = new JTextField();
        tfLamaSewa.setBounds(150, 140, 200, 25);
        add(tfLamaSewa);

        // Tombol-tombol untuk aksi CRUD
        JButton btnSimpan = new JButton("Simpan");
        btnSimpan.setBounds(150, 180, 100, 25);
        add(btnSimpan);

        JButton btnUpdate = new JButton("Update");
        btnUpdate.setBounds(260, 180, 100, 25);
        add(btnUpdate);

        JButton btnDelete = new JButton("Delete");
        btnDelete.setBounds(370, 180, 100, 25);
        add(btnDelete);

        // Menambahkan action listener untuk setiap tombol
        btnSimpan.addActionListener(e -> simpanTransaksi());
        btnUpdate.addActionListener(e -> updateTransaksi());
        btnDelete.addActionListener(e -> deleteTransaksi());

        // Membuat tabel untuk menampilkan data
        model = new DefaultTableModel(new String[]{"ID", "Nama Pelanggan", "Konsol", "Lama Sewa", "Total Harga"}, 0);
        table = new JTable(model);
        JScrollPane spTable = new JScrollPane(table);
        spTable.setBounds(20, 220, 750, 300);
        add(spTable);

        loadTransaksi(); // Memuat data transaksi dari database
    }

    // Method untuk memuat data konsol ke combobox
    private void loadKonsol() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM konsol");
            while (rs.next()) {
                cbKonsol.addItem(rs.getString("id_konsol") + " - " + rs.getString("nama_konsol") + " (" + rs.getDouble("harga_per_jam") + "/jam)");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal Memuat Data Konsol: " + e.getMessage());
        }
    }

    // Method untuk menyimpan transaksi baru
    private void simpanTransaksi() {
        try {
            // Mengambil data dari form
            String nama = tfNamaPelanggan.getText();
            String kontak = tfKontak.getText();
            int lamaSewa = Integer.parseInt(tfLamaSewa.getText());
            String konsol = (String) cbKonsol.getSelectedItem();

            // Validasi input
            if (nama.isEmpty() || kontak.isEmpty() || konsol == null) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
                return;
            }

            int idKonsol = Integer.parseInt(konsol.split(" - ")[0]);

            // Menyimpan data pelanggan
            PreparedStatement psPelanggan = conn.prepareStatement("INSERT INTO pelanggan (nama_pelanggan, kontak_pelanggan) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            psPelanggan.setString(1, nama);
            psPelanggan.setString(2, kontak);
            psPelanggan.executeUpdate();
            ResultSet rsPelanggan = psPelanggan.getGeneratedKeys();
            rsPelanggan.next();
            int idPelanggan = rsPelanggan.getInt(1);

            // Menghitung total harga
            PreparedStatement psKonsol = conn.prepareStatement("SELECT harga_per_jam FROM konsol WHERE id_konsol = ?");
            psKonsol.setInt(1, idKonsol);
            ResultSet rsKonsol = psKonsol.executeQuery();
            rsKonsol.next();
            double hargaPerJam = rsKonsol.getDouble(1);
            double totalHarga = hargaPerJam * lamaSewa;

            // Menyimpan data transaksi
            PreparedStatement psTransaksi = conn.prepareStatement("INSERT INTO transaksi (id_pelanggan, id_konsol, lama_sewa, total_harga) VALUES (?, ?, ?, ?)");
            psTransaksi.setInt(1, idPelanggan);
            psTransaksi.setInt(2, idKonsol);
            psTransaksi.setInt(3, lamaSewa);
            psTransaksi.setDouble(4, totalHarga);
            psTransaksi.executeUpdate();

            JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan!");
            loadTransaksi(); // Memperbarui tabel
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Menyimpan Transaksi: " + e.getMessage());
        }
    }

    // Method untuk mengupdate transaksi yang ada
    private void updateTransaksi() {
        try {
            // Validasi pemilihan baris
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Pilih transaksi yang ingin diupdate!");
                return;
            }

            // Mengambil data dari form
            int idTransaksi = (int) model.getValueAt(selectedRow, 0);
            String nama = tfNamaPelanggan.getText();
            String kontak = tfKontak.getText();
            int lamaSewa = Integer.parseInt(tfLamaSewa.getText());
            String konsol = (String) cbKonsol.getSelectedItem();

            // Validasi input
            if (nama.isEmpty() || kontak.isEmpty() || konsol == null) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
                return;
            }

            int idKonsol = Integer.parseInt(konsol.split(" - ")[0]);

            // Update data pelanggan
            PreparedStatement psPelanggan = conn.prepareStatement("UPDATE pelanggan SET nama_pelanggan = ?, kontak_pelanggan = ? WHERE id_pelanggan = (SELECT id_pelanggan FROM transaksi WHERE id_transaksi = ?)");
            psPelanggan.setString(1, nama);
            psPelanggan.setString(2, kontak);
            psPelanggan.setInt(3, idTransaksi);
            psPelanggan.executeUpdate();

            // Menghitung ulang total harga
            PreparedStatement psKonsol = conn.prepareStatement("SELECT harga_per_jam FROM konsol WHERE id_konsol = ?");
            psKonsol.setInt(1, idKonsol);
            ResultSet rsKonsol = psKonsol.executeQuery();
            rsKonsol.next();
            double hargaPerJam = rsKonsol.getDouble(1);
            double totalHarga = hargaPerJam * lamaSewa;

            // Update data transaksi
            PreparedStatement psTransaksi = conn.prepareStatement("UPDATE transaksi SET id_konsol = ?, lama_sewa = ?, total_harga = ? WHERE id_transaksi = ?");
            psTransaksi.setInt(1, idKonsol);
            psTransaksi.setInt(2, lamaSewa);
            psTransaksi.setDouble(3, totalHarga);
            psTransaksi.setInt(4, idTransaksi);
            psTransaksi.executeUpdate();

            JOptionPane.showMessageDialog(this, "Transaksi berhasil diupdate!");
            loadTransaksi(); // Memperbarui tabel
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Mengupdate Transaksi: " + e.getMessage());
        }
    }

    // Method untuk menghapus transaksi
    private void deleteTransaksi() {
        try {
            // Validasi pemilihan baris
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Pilih transaksi yang ingin dihapus!");
                return;
            }

            int idTransaksi = (int) model.getValueAt(selectedRow, 0);

            // Menghapus data transaksi
            PreparedStatement psTransaksi = conn.prepareStatement("DELETE FROM transaksi WHERE id_transaksi = ?");
            psTransaksi.setInt(1, idTransaksi);
            psTransaksi.executeUpdate();

            JOptionPane.showMessageDialog(this, "Transaksi berhasil dihapus!");
            loadTransaksi(); // Memperbarui tabel
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Menghapus Transaksi: " + e.getMessage());
        }
    }

    // Method untuk memuat data transaksi ke tabel
    private void loadTransaksi() {
        model.setRowCount(0); // Mengosongkan tabel
        try {
            Statement stmt = conn.createStatement();
            // Query untuk mengambil data transaksi dengan join ke tabel pelanggan dan konsol
            ResultSet rs = stmt.executeQuery("SELECT t.id_transaksi, p.nama_pelanggan, k.nama_konsol, t.lama_sewa, t.total_harga " +
                    "FROM transaksi t JOIN pelanggan p ON t.id_pelanggan = p.id_pelanggan " +
                    "JOIN konsol k ON t.id_konsol = k.id_konsol");
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getDouble(5)});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal Memuat Data Transaksi: " + e.getMessage());
        }
    }

    // Method main untuk menjalankan aplikasi
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RentalPlayStation().setVisible(true));
    }
}