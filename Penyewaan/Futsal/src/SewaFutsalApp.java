import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SewaFutsalApp {
    private static Connection connect() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/sewa_futsal", "root", "rootpassword");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Sewa Lapangan Futsal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        JLabel lblNamaTim = new JLabel("Nama Tim:");
        JTextField txtNamaTim = new JTextField();

        JLabel lblLapangan = new JLabel("Nomor Lapangan:");
        JTextField txtLapangan = new JTextField();

        JLabel lblWaktu = new JLabel("Waktu Sewa (HH:MM):");
        JTextField txtWaktu = new JTextField();

        JLabel lblDurasi = new JLabel("Durasi (jam):");
        JTextField txtDurasi = new JTextField();

        JButton btnSave = new JButton("Simpan");
        JButton btnView = new JButton("Lihat Data");

        panel.add(lblNamaTim);
        panel.add(txtNamaTim);
        panel.add(lblLapangan);
        panel.add(txtLapangan);
        panel.add(lblWaktu);
        panel.add(txtWaktu);
        panel.add(lblDurasi);
        panel.add(txtDurasi);
        panel.add(btnSave);
        panel.add(btnView);

        frame.add(panel);

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String namaTim = txtNamaTim.getText();
                String lapangan = txtLapangan.getText();
                String waktu = txtWaktu.getText();
                String durasi = txtDurasi.getText();

                if (namaTim.isEmpty() || lapangan.isEmpty() || waktu.isEmpty() || durasi.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Semua field harus diisi.");
                    return;
                }

                int nomorLapangan;
                try {
                    nomorLapangan = Integer.parseInt(lapangan);
                    if (nomorLapangan < 1 || nomorLapangan > 5) {
                        JOptionPane.showMessageDialog(frame, "Nomor lapangan harus antara 1 hingga 5.");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Nomor lapangan harus berupa angka.");
                    return;
                }

                try (Connection conn = connect();
                     PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM sewa WHERE nomor_lapangan = ?");
                     PreparedStatement ps = conn.prepareStatement("INSERT INTO sewa (nama_tim, nomor_lapangan, waktu_sewa, durasi) VALUES (?, ?, ?, ?)");) {

                    // Check if the lapangan is already booked
                    checkStmt.setInt(1, nomorLapangan);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(frame, "Lapangan " + nomorLapangan + " sudah penuh. Hapus data terlebih dahulu untuk memesan.");
                        return;
                    }

                    // Insert data
                    ps.setString(1, namaTim);
                    ps.setInt(2, nomorLapangan);
                    ps.setString(3, waktu);
                    ps.setInt(4, Integer.parseInt(durasi));
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(frame, "Data berhasil disimpan.");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            }
        });

        btnView.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame viewFrame = new JFrame("Data Penyewaan");
                viewFrame.setSize(500, 300);

                String[] columnNames = {"ID", "Nama Tim", "Nomor Lapangan", "Waktu Sewa", "Durasi"};
                DefaultTableModel model = new DefaultTableModel(columnNames, 0);
                JTable table = new JTable(model);

                try (Connection conn = connect();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM sewa");) {
                    while (rs.next()) {
                        model.addRow(new Object[]{rs.getInt("id"), rs.getString("nama_tim"), rs.getString("nomor_lapangan"), rs.getString("waktu_sewa"), rs.getInt("durasi")});
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(viewFrame, "Error: " + ex.getMessage());
                }

                viewFrame.add(new JScrollPane(table));
                viewFrame.setVisible(true);
            }
        });

        frame.setVisible(true);
    }
}
