
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ACH extends javax.swing.JFrame {

    private Connection conn;  // Menyimpan koneksi ke database
    private DefaultTableModel model;  // Model untuk tabel yang digunakan untuk menampilkan data

    public ACH() {
        initComponents();  // Memanggil metode yang dihasilkan oleh GUI designer untuk inisialisasi komponen
        model = new DefaultTableModel(new Object[][]{}, new String[]{"ID", "JUDUL", "TANGGAL"});
        jTable1.setModel(model);  // Menetapkan model pada jTable1
        konekdb();  // Menghubungkan aplikasi dengan database
        data();  // Memuat data awal ke dalam tabel
        setupActions();  // Mengatur listener untuk tombol dan aksi

        // Menyembunyikan kolom ID di jTable1
        jTable1.getColumnModel().getColumn(0).setMinWidth(0);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(0);
        jTable1.getColumnModel().getColumn(0).setWidth(0);

        startClock();  // Memulai timer untuk update jam setiap detik

        // Listener untuk tombol pencarian
        bcari.addActionListener(e -> {
            String keyword = cari.getText().trim();
            if (keyword.isEmpty()) {
                data();  // Menampilkan semua data jika kata kunci kosong
            } else {
                cari(keyword);  // Menampilkan data berdasarkan kata kunci pencarian
            }
        });
    }

    // Fungsi untuk memulai timer yang memperbarui label jam setiap detik
    private void startClock() {
        Timer timer = new Timer(1000, e -> updateClock());  // Timer dengan interval 1000ms (1 detik)
        timer.start();  // Memulai timer
    }

    // Fungsi untuk memperbarui label jam dengan waktu saat ini
    private void updateClock() {
        LocalTime currentTime = LocalTime.now();  // Mendapatkan waktu saat ini
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");  // Format untuk jam dan menit
        jam.setText(currentTime.format(formatter));  // Menampilkan waktu dalam format "HH:mm"
    }

    // Fungsi untuk menghubungkan aplikasi ke database
    private void konekdb() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:C.db");  // Koneksi ke database SQLite
            if (conn != null) {
                System.out.println("Database connected.");
                createTableIfNotExists();  // Memastikan tabel catatan ada
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fungsi untuk memuat data catatan dari database ke dalam tabel
    private void data() {
        model.setRowCount(0);  // Menghapus data lama yang ada di tabel
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM catatan");  // Query untuk mengambil semua catatan
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), // ID catatan
                    rs.getString("judul"), // Judul catatan
                    rs.getDate("tanggal") // Tanggal catatan
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fungsi untuk membuat tabel catatan jika belum ada
    private void createTableIfNotExists() {
        try (Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS catatan (id INTEGER PRIMARY KEY AUTOINCREMENT, tanggal DATE, judul TEXT, isi TEXT)";
            stmt.executeUpdate(sql);  // Eksekusi perintah untuk membuat tabel jika belum ada
            System.out.println("Table catatan verified/created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fungsi untuk menyiapkan aksi listener pada tombol-tombol
    private void setupActions() {
        // Menghapus listener yang ada sebelum menambah listener baru pada tombol simpan
        for (ActionListener al : simpan.getActionListeners()) {
            simpan.removeActionListener(al);
        }
        simpan.addActionListener(evt -> s());  // Menambahkan aksi untuk tombol simpan
        ubah.addActionListener(evt -> u());  // Aksi untuk tombol ubah
        hapus.addActionListener(evt -> h());  // Aksi untuk tombol hapus
        impor.addActionListener(evt -> i());  // Aksi untuk tombol impor
        ekspor.addActionListener(evt -> e());  // Aksi untuk tombol ekspor
        keluar.addActionListener(evt -> System.exit(0));  // Aksi untuk tombol keluar

        // Listener untuk klik pada tabel
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleTableClick();  // Menangani klik pada tabel
            }
        });
    }

    // Fungsi untuk menangani klik pada tabel dan memuat catatan berdasarkan ID yang dipilih
    private void handleTableClick() {
        int row = jTable1.getSelectedRow();
        System.out.println("Row selected: " + row);  // Debugging baris yang dipilih
        if (row != -1) {
            int id = (int) model.getValueAt(row, 0);  // Mengambil ID catatan dari baris yang dipilih
            muat(id);  // Memuat catatan berdasarkan ID yang dipilih
        }
    }

    // Fungsi untuk memuat catatan berdasarkan ID
    private void muat(int id) {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT judul, isi FROM catatan WHERE id = ?")) {
            pstmt.setInt(1, id);  // Mengatur ID catatan
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Memastikan komponen judul dan jTextArea1 tidak null
                if (judul != null && jTextArea1 != null) {
                    judul.setText(rs.getString("judul"));  // Menampilkan judul catatan
                    jTextArea1.setText(rs.getString("isi"));  // Menampilkan isi catatan
                } else {
                    System.out.println("Text components are not properly initialized.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fungsi untuk menyimpan catatan baru
    private boolean isSaving = false;  // Variabel untuk memastikan tidak ada simpanan ganda

    private void s() {
        System.out.println("Memulai proses simpan...");
        if (isSaving) {
            System.out.println("Proses simpan sedang berlangsung. Batalkan simpan.");
            return;
        }
        isSaving = true;
        simpan.setEnabled(false);  // Menonaktifkan tombol simpan saat proses berlangsung

        String title = judul.getText();  // Mengambil judul dari jTextField judul
        String content = jTextArea1.getText();  // Mengambil isi dari jTextArea

        if (title.isEmpty() || content.isEmpty()) {
            System.out.println("Title or content is empty!");
            isSaving = false;
            simpan.setEnabled(true);  // Mengaktifkan kembali tombol simpan jika data kosong
            return;
        }

        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO catatan (judul, isi, tanggal) VALUES (?, ?, ?)")) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setDate(3, Date.valueOf(LocalDate.now()));  // Menyimpan tanggal saat ini
            pstmt.executeUpdate();  // Menyimpan data ke database
            System.out.println("Catatan disimpan.");
            data();  // Memuat ulang data setelah simpan
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Proses simpan selesai.");
            isSaving = false;  // Mengatur ulang status simpan
            simpan.setEnabled(true);  // Mengaktifkan kembali tombol simpan
        }
    }

    // Fungsi untuk mengubah catatan
    private void u() {
        int selectedRow = jTable1.getSelectedRow();  // Mendapatkan baris yang dipilih
        if (selectedRow != -1) {
            int id = (int) model.getValueAt(selectedRow, 0);  // Mengambil ID dari catatan yang dipilih
            String newTitle = judul.getText();
            String newText = jTextArea1.getText();

            try (PreparedStatement pstmt = conn.prepareStatement("UPDATE catatan SET judul = ?, isi = ? WHERE id = ?")) {
                pstmt.setString(1, newTitle);
                pstmt.setString(2, newText);
                pstmt.setInt(3, id);
                pstmt.executeUpdate();  // Menyimpan perubahan ke database
                JOptionPane.showMessageDialog(this, "Catatan diperbarui.");
                data();  // Memuat ulang data setelah perubahan
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Fungsi untuk menghapus catatan
    private void h() {
        int selectedRow = jTable1.getSelectedRow();  // Mendapatkan baris yang dipilih
        if (selectedRow != -1) {
            int id = (int) model.getValueAt(selectedRow, 0);  // Mengambil ID dari catatan yang dipilih

            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM catatan WHERE id = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();  // Menghapus catatan dari database
                JOptionPane.showMessageDialog(this, "Catatan dihapus.");
                data();  // Memuat ulang data setelah penghapusan
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Fungsi untuk mencari catatan berdasarkan kata kunci
    private void cari(String keyword) {
        model.setRowCount(0);  // Menghapus data yang ada di tabel
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM catatan WHERE judul LIKE ?")) {
            pstmt.setString(1, "%" + keyword + "%");  // Pencarian dengan wildcard
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), // ID catatan
                    rs.getString("judul"), // Judul catatan
                    rs.getDate("tanggal") // Tanggal catatan
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fungsi untuk impor catatan
// Fungsi untuk impor catatan
    private void i() {
        JFileChooser chooser = new JFileChooser(); // Membuka file chooser untuk memilih file
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { // Jika file dipilih
            File file = chooser.getSelectedFile(); // Mendapatkan file yang dipilih
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) { // Membaca file dengan BufferedReader
                String line;
                while ((line = reader.readLine()) != null) { // Membaca setiap baris dalam file
                    String[] parts = line.split(","); // Memisahkan data dengan tanda koma
                    if (parts.length == 2) { // Jika data terdiri dari dua bagian (judul, isi)
                        try (PreparedStatement pstmt = conn.prepareStatement(
                                "INSERT INTO catatan (tanggal, judul, isi) VALUES (?, ?, ?)")) { // Query untuk menyimpan data
                            pstmt.setDate(1, Date.valueOf(LocalDate.now())); // Mengisi kolom tanggal dengan tanggal sekarang
                            pstmt.setString(2, parts[0]); // Mengisi kolom judul
                            pstmt.setString(3, parts[1]); // Mengisi kolom isi
                            pstmt.executeUpdate(); // Menjalankan query untuk menyimpan data
                        }
                    }
                }
                JOptionPane.showMessageDialog(this, "Catatan diimpor."); // Menampilkan pesan jika impor berhasil
                data(); // Memuat ulang data ke tabel setelah impor
            } catch (IOException | SQLException e) { // Menangkap kesalahan yang terjadi saat membaca atau menyimpan
                e.printStackTrace(); // Menampilkan error stack trace
            }
        }
    }

    // Fungsi untuk ekspor catatan
    private void e() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Query semua catatan dari database
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT judul, isi FROM catatan")) {

                    while (rs.next()) {
                        String title = rs.getString("judul"); // Judul catatan
                        String content = rs.getString("isi"); // Isi catatan

                        // Tulis ke file dalam format: "judul,isi"
                        writer.write(title + "," + content);
                        writer.newLine();
                    }
                }
                JOptionPane.showMessageDialog(this, "Catatan berhasil diekspor.");
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel = new javax.swing.JPanel();
        simpan = new javax.swing.JButton();
        ubah = new javax.swing.JButton();
        hapus = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        impor = new javax.swing.JButton();
        ekspor = new javax.swing.JButton();
        keluar = new javax.swing.JButton();
        judul = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        jScrollPane = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jam = new javax.swing.JLabel();
        cari = new javax.swing.JTextField();
        bcari = new javax.swing.JButton();
        BG = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(34, 62, 98));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("APLIKASI CATATAN HARIAN");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(360, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(343, 343, 343))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel.setBackground(new java.awt.Color(0, 102, 102));
        jPanel.setLayout(null);

        simpan.setBackground(new java.awt.Color(0, 255, 0));
        simpan.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        simpan.setText("SIMPAN");
        simpan.setToolTipText("");
        simpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simpanActionPerformed(evt);
            }
        });
        jPanel.add(simpan);
        simpan.setBounds(185, 308, 87, 29);

        ubah.setBackground(new java.awt.Color(153, 255, 0));
        ubah.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        ubah.setText("UBAH");
        jPanel.add(ubah);
        ubah.setBounds(286, 308, 73, 29);

        hapus.setBackground(new java.awt.Color(255, 51, 51));
        hapus.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        hapus.setText("HAPUS");
        hapus.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel.add(hapus);
        hapus.setBounds(610, 350, 80, 30);

        jTable1.setBackground(new java.awt.Color(204, 255, 204));
        jTable1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jTable1.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "JUDUL", "TANGGAL"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        jPanel.add(jScrollPane2);
        jScrollPane2.setBounds(485, 109, 291, 181);

        impor.setBackground(new java.awt.Color(204, 255, 102));
        impor.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        impor.setText("IMPOR");
        impor.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel.add(impor);
        impor.setBounds(610, 310, 75, 30);

        ekspor.setBackground(new java.awt.Color(204, 255, 102));
        ekspor.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        ekspor.setText("EKSPOR");
        ekspor.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel.add(ekspor);
        ekspor.setBounds(700, 310, 80, 30);

        keluar.setBackground(new java.awt.Color(255, 51, 51));
        keluar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        keluar.setText("KELUAR");
        keluar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        keluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keluarActionPerformed(evt);
            }
        });
        jPanel.add(keluar);
        keluar.setBounds(700, 350, 80, 30);

        judul.setBackground(new java.awt.Color(204, 255, 204));
        judul.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        judul.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                judulActionPerformed(evt);
            }
        });
        jPanel.add(judul);
        judul.setBounds(185, 70, 122, 24);

        jScrollPane3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jScrollPane3.setViewportBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jTextArea1.setBackground(new java.awt.Color(204, 255, 204));
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jTextArea1.setRows(5);
        jScrollPane.setViewportView(jTextArea1);

        jScrollPane3.setViewportView(jScrollPane);

        jPanel.add(jScrollPane3);
        jScrollPane3.setBounds(185, 109, 268, 181);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("JUDUL");
        jPanel.add(jLabel2);
        jLabel2.setBounds(121, 67, 56, 25);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setText("ISI");
        jPanel.add(jLabel3);
        jLabel3.setBounds(121, 109, 22, 25);

        jam.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jam.setForeground(new java.awt.Color(255, 255, 255));
        jam.setText("JAM");
        jPanel.add(jam);
        jam.setBounds(442, 28, 60, 25);

        cari.setBackground(new java.awt.Color(204, 255, 204));
        cari.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jPanel.add(cari);
        cari.setBounds(554, 70, 149, 24);

        bcari.setBackground(new java.awt.Color(102, 255, 102));
        bcari.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        bcari.setText("CARI");
        jPanel.add(bcari);
        bcari.setBounds(715, 69, 65, 29);

        BG.setIcon(new javax.swing.ImageIcon("C:\\Users\\atmaf\\Downloads\\PBO SUPP\\u.jpg")); // NOI18N
        jPanel.add(BG);
        BG.setBounds(0, 0, 950, 440);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void simpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simpanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_simpanActionPerformed

    private void keluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keluarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_keluarActionPerformed

    private void judulActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_judulActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_judulActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ACH.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ACH.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ACH.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ACH.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ACH().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel BG;
    private javax.swing.JButton bcari;
    private javax.swing.JTextField cari;
    private javax.swing.JButton ekspor;
    private javax.swing.JButton hapus;
    private javax.swing.JButton impor;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel jam;
    private javax.swing.JTextField judul;
    private javax.swing.JButton keluar;
    private javax.swing.JButton simpan;
    private javax.swing.JButton ubah;
    // End of variables declaration//GEN-END:variables
}
