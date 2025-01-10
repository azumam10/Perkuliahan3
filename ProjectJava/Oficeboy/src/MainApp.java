import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        String username = JOptionPane.showInputDialog("Masukkan Username:");
        String password = JOptionPane.showInputDialog("Masukkan Password:");

        if (User.authenticate(username, password)) {
            SwingUtilities.invokeLater(InventoryApp::new);
        } else {
            JOptionPane.showMessageDialog(null, "Login gagal! Username atau password salah.");
        }
    }
}
