# TidurJir

TidurJir adalah aplikasi Android sederhana untuk mencatat dan memantau pola tidur serta kondisi mental (mood) harian pengguna secara luring. Semua data disimpan sepenuhnya di dalam penyimpanan lokal perangkat Anda tanpa melibatkan koneksi internet atau server eksternal.

## Fitur Utama

1. **Sleep & Mood Log (CRUD Lokal)**
   - **Create**: Menambahkan catatan tidur baru yang mencakup jam tidur, jam bangun, kualitas tidur (skala 1–5), tingkat mood (skala 1–5), serta catatan tambahan.
   - **Read**: Menampilkan daftar riwayat log tidur yang diurutkan dari yang terbaru (berdasarkan waktu pembuatan log secara kronologis terbalik).
   - **Update**: Memperbarui informasi pada log tidur yang telah tersimpan sebelumnya.
   - **Delete**: Menghapus catatan tidur dari penyimpanan database lokal secara permanen.

2. **Insight Dashboard (Statistik Deterministik Non-AI)**
   - Menyajikan perhitungan matematika murni seperti durasi rata-rata tidur dan rata-rata kondisi mood.
   - Menampilkan peringatan berbasis aturan statis (rule-based warning) untuk pola tidur yang kurang ideal.
   - Menyajikan diagram korelasi visual sederhana antara durasi tidur dan kondisi mood pengguna tanpa menggunakan teknologi AI atau model generatif.

## Skenario Penggunaan Nyata (Real-World Use Cases)

Berikut adalah beberapa skenario riil bagaimana pengguna memanfaatkan **TidurJir** dalam kehidupan sehari-hari:

### 1. Pelacakan Fleksibel untuk Pekerja Shift Malam (Shift Worker)
*   **Kasus**: Budi bekerja sebagai petugas keamanan shift malam. Jam tidurnya sangat tidak beraturan, sering kali ia harus tidur di pagi hari (misalnya pukul 08:00 pagi) hingga siang hari (pukul 15:00 sore).
*   **Aksi di Aplikasi**: Budi dapat mencatat jam tidur dan bangunnya secara bebas dan sebebas-bebasnya kapan saja tanpa batasan tanggal masa depan yang kaku.
*   **Manfaat**: Riwayat tidur Budi tetap tercatat dengan akurat meskipun pola tidurnya terbalik dari orang biasa, tanpa adanya peringatan eror validasi waktu yang mengganggu.

### 2. Analisis Korelasi Kurang Tidur & Mood (Pekerja Kantoran / Mahasiswa)
*   **Kasus**: Rara merasa akhir-akhir ini emosinya tidak stabil dan sering merasa cemas (mood rendah). Ia mencurigai hal ini disebabkan oleh kebiasaan begadang mengerjakan proyek.
*   **Aksi di Aplikasi**: Setiap bangun tidur, Rara meluangkan waktu 10 detik untuk mengisi log jam tidur, jam bangun, kualitas tidur, tingkat mood (1-5), dan menambahkan catatan kecil seperti *"Begadang push rank"* atau *"Lembur deadline project"*.
*   **Manfaat**: Setelah satu minggu, Rara membuka **Insight Dashboard** dan melihat korelasi nyata dalam bentuk angka deterministik bahwa durasi tidur di bawah 5 jam selalu berujung pada tingkat Mood berskala 1 atau 2. Ini menjadi bukti konkret bagi Rara untuk memperbaiki jadwal tidurnya.

### 3. Pemulihan Penderita Insomnia (Insomnia Recovery)
*   **Kasus**: Andi sedang menjalani terapi mandiri untuk memulihkan gangguan insomnianya dengan mencoba teknik tidur baru dan ingin melihat perkembangannya secara objektif.
*   **Aksi di Aplikasi**: Andi memperbarui log tidurnya setiap hari. Jika dia salah memasukkan data karena mengantuk, dia dapat langsung melakukan **Update** (edit) atau **Delete** log tersebut secara instan melalui daftar riwayat.
*   **Manfaat**: Dengan penyimpanan database lokal Room, Andi merasa tenang karena seluruh data kesehatan tidurnya bersifat 100% privat di dalam ponselnya sendiri, tanpa takut bocor ke internet atau server cloud manapun.

## Tech Stack

| Layer / Komponen | Teknologi |
| --- | --- |
| **Sistem Operasi** | Android |
| **Bahasa Pemrograman** | Kotlin |
| **Framework UI** | Jetpack Compose (Material Design 3) |
| **State Management** | ViewModel + StateFlow (MVVM) |
| **Local Database** | Room (SQLite) |
| **Navigasi** | Navigation Compose |
| **Waktu & Penulisan Tanggal** | java.util.Calendar / SimpleDateFormat |

## Struktur Folder Project

```text
app/src/main/java/com/example/
│
├── data/
│   ├── SleepLog.kt          # Entity - Model data kelas representasi tabel Room
│   ├── SleepDao.kt          # DAO - Kontrak query CRUD Room
│   ├── SleepDatabase.kt     # Database - Singleton instance untuk database SQLite lokal
│   └── SleepRepository.kt   # Repository - Abstraksi akses data serta validasi logis
│
├── ui/
│   ├── SleepListScreen.kt   # Screen - Tampilan daftar log riwayat dan dashboard statistik
│   ├── SleepFormScreen.kt   # Screen - Formulir input tambah/edit log tidur dan mood
│   ├── SleepViewModel.kt    # ViewModel - Pengelola state UI dan perantara repositori
│   └── DateTimeHelper.kt    # Helper - Utilitas penanganan waktu dan format lokal
│
└── MainActivity.kt          # Entry Point & Navigasi Utama Aplikasi
```

## Flow Aplikasi & Alur Data

Alur data berjalan secara reaktif satu arah (Unidirectional Data Flow) untuk memastikan integritas data lokal:

```text
  [ User Input di SleepFormScreen ]
                 │
                 ▼
          [ SleepViewModel ]  ── (Set state isSaving = true, validasi awal UI)
                 │
                 ▼
   [ SleepRepository.insertLog() ]  ── (Validasi sekunder integritas data)
                 │
                 ▼
       [ SleepDao.insert() ]
                 │
                 ▼
        [ SQLite Database ] (Room)
                 │
                 ▼ (Mengalirkan data terbaru secara otomatis)
     [ SleepDao.getAllLogs() ]  ── (Mengembalikan Flow<List<SleepLog>>)
                 │
                 ▼
         [ SleepViewModel ]  ── (StateFlow.collectAsState())
                 │
                 ▼
   [ SleepListScreen Auto-Refresh ]
```

## Prasyarat (Requirements)

- **Android Studio**: Ladybug / Koala atau versi yang lebih baru (mendukung Kotlin DSL dan Gradle versi terbaru)
- **Minimum SDK (minSdk)**: API 24 (Android 7.0)
- **Target SDK (targetSdk)**: API 36
- **Kotlin Version**: [ISI SESUAI PROJECT]
- **Gradle Version**: [ISI SESUAI PROJECT]

## Cara Clone & Menjalankan Project

Ikuti langkah-langkah berikut untuk menjalankan project TidurJir di komputer lokal Anda:

1. Clone repositori project ini menggunakan Git:
   ```bash
   git clone https://github.com/jojohyperbackend-hub/TidurJir.git
   ```
2. Buka aplikasi **Android Studio**.
3. Pilih opsi **Open an Existing Project** dan arahkan ke direktori hasil clone di atas.
4. Tunggu hingga proses sinkronisasi Gradle (**Gradle Sync**) selesai dilakukan secara otomatis.
5. Hubungkan perangkat Android fisik atau jalankan Android Virtual Device (Emulator).
6. Klik tombol **Run** (ikon segitiga hijau) atau gunakan shortcut `Shift + F10` untuk mengompilasi dan memasang aplikasi ke perangkat.

## Batasan & Ruang Lingkup (Scope Lock)

Aplikasi TidurJir dirancang dengan prinsip kesederhanaan dan keamanan data tingkat tinggi. Oleh karena itu, aplikasi ini memiliki beberapa batasan mutlak berikut:
- **TIDAK menggunakan AI/LLM**: Seluruh statistik, diagram korelasi, dan badge informasi diperoleh dari kalkulasi matematis murni (agregasi deterministik data lokal). Tidak ada generator teks otomatis, saran pintar generatif, atau chatbot di dalam aplikasi ini.
- **TIDAK ada Backend**: Aplikasi ini bekerja 100% luring (fully offline) dan tidak membutuhkan koneksi internet atau server database pihak ketiga.
- **TIDAK ada Sistem Auth**: Tidak diperlukan pendaftaran akun, pembuatan username, atau kata sandi. Data tersimpan aman di sandbox lokal aplikasi perangkat masing-masing pengguna.
