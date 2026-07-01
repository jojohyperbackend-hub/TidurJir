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
