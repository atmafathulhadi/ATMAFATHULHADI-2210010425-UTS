
# Aplikasi Catatan Harian (ACH)

**Tugas UTS**  
Nama: Atma Fathul Hadi  
NPM: 2210010425  

## 1. Deskripsi Program
Program ini adalah aplikasi GUI berbasis Java untuk mengelola catatan harian. Pengguna dapat menambahkan, mengubah, menghapus, mencari catatan, serta melakukan impor dan ekspor data. Aplikasi ini juga menampilkan waktu secara real-time dan menggunakan database SQLite sebagai tempat penyimpanan data.

## 2. Komponen GUI
Aplikasi ini dibuat menggunakan komponen GUI berikut:
- **JFrame**: Sebagai kerangka utama aplikasi.
- **JPanel**: Wadah komponen GUI.
- **JLabel**: Label teks untuk elemen seperti "Judul", "Tanggal", dll.
- **JTextField**: Input untuk judul catatan.
- **JTextArea**: Input dan tampilan isi catatan.
- **JButton**: Tombol untuk berbagai aksi (Simpan, Ubah, Hapus, Cari, Impor, Ekspor, Keluar).
- **JTable**: Menampilkan daftar catatan.
- **JFileChooser**: Memilih file untuk impor atau ekspor data.
- **Timer**: Menampilkan jam real-time.

## 3. Fitur Program
1. **Menambah Catatan**: Pengguna dapat menambahkan catatan baru dengan judul dan isi.
2. **Mengubah Catatan**: Catatan yang ada dapat diperbarui.
3. **Menghapus Catatan**: Catatan tertentu dapat dihapus.
4. **Mencari Catatan**: Pencarian catatan berdasarkan judul.
5. **Impor Data**: Mengimpor catatan dari file.
6. **Ekspor Data**: Mengekspor semua catatan ke file.
7. **Tampilan Real-Time Jam**: Menampilkan waktu saat ini.

### Kode Terkait:
#### Menambah Catatan
```java
private void s() {
    String title = judul.getText();
    String content = jTextArea1.getText();
    if (title.isEmpty() || content.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Judul dan isi tidak boleh kosong.");
        return;
    }
    try (PreparedStatement pstmt = conn.prepareStatement(
        "INSERT INTO catatan (judul, isi, tanggal) VALUES (?, ?, ?)")) {
        pstmt.setString(1, title);
        pstmt.setString(2, content);
        pstmt.setDate(3, Date.valueOf(LocalDate.now()));
        pstmt.executeUpdate();
        JOptionPane.showMessageDialog(this, "Catatan berhasil disimpan.");
        data();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
```

#### Menghapus Catatan
```java
private void h() {
    int selectedRow = jTable1.getSelectedRow();
    if (selectedRow != -1) {
        int id = (int) model.getValueAt(selectedRow, 0);
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM catatan WHERE id = ?")) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Catatan berhasil dihapus.");
            data();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

## 4. Struktur Database
Aplikasi ini menggunakan database SQLite dengan tabel berikut:
- **Nama Tabel**: `catatan`
- **Kolom**:
  - `id` (INTEGER, PRIMARY KEY, AUTOINCREMENT)
  - `tanggal` (DATE)
  - `judul` (TEXT)
  - `isi` (TEXT)

## 5. Cara Menjalankan Program
1. Pastikan database `C.db` tersedia di direktori kerja aplikasi.
2. Buka aplikasi di IDE seperti NetBeans atau Eclipse.
3. Jalankan aplikasi.
4. Gunakan fitur GUI untuk menambah, mengubah, menghapus, mencari catatan, atau melakukan impor dan ekspor data.
5. Klik "Keluar" untuk menutup aplikasi.

## 6. Indikator Penilaian

| No  | Komponen         |  Persentase  |
| :-: | ---------------- |   :-----:    |
|  1  | Komponen GUI     |    20%       |
|  2  | Logika Program   |    25%       |
|  3  | Kesesuaian UI    |    15%       |
|  4  | Pengelolaan DB   |    20%       |
|  5  | Memenuhi Fitur   |    20%       |
|     | **TOTAL**        | 100%         |

## 7. Contoh Tampilan Program
Tampilan aplikasi mencakup tabel daftar catatan, input untuk judul dan isi, serta tombol-tombol untuk mengelola catatan.
![S.png](https://github.com/atmafathulhadi/ATMAFATHULHADI-2210010425-UTS/blob/main/S.png)
---

Aplikasi ini dirancang untuk mempermudah pengelolaan catatan harian dengan tampilan antarmuka yang sederhana dan fitur lengkap.
