# Task Reminder 2.0

Aplikasi Android untuk mengelola dan mengingatkan tugas, dengan **dua mode terpisah**:

- **Personal Mode** — 100% offline, tanpa login, data tersimpan lokal (Room).
- **Team Mode** — kolaborasi tim dengan login, data tersinkron realtime antar anggota lewat Firebase.

Versi 2.0 adalah **penulisan ulang total** dari project sebelumnya, berfokus pada arsitektur yang bersih dan mudah dikembangkan (MVVM + Repository), bukan SQLite/Singleton manual yang rapuh.

---

## ✨ Fitur

| # | Fitur | Personal | Team |
|---|---|:---:|:---:|
| 01 | Kelola tugas (CRUD) + prioritas | ✅ | ✅ |
| 02 | Riwayat aktivitas otomatis | ✅ | ✅ (menyebut nama anggota) |
| 03 | Pencarian & filter (judul / prioritas / terlambat) | ✅ | ✅ |
| 05 | Catatan manual (notes) per tugas | ✅ | ✅ |
| 06 | Penanda visual prioritas tinggi | ✅ | ✅ |
| 07 | Notifikasi | ✅ reminder deadline (AlarmManager) | ✅ perubahan anggota (client-side) |
| 08 | Auto-overdue (dihitung, tidak disimpan) | ✅ | ✅ |
| 04 | Manajemen tim (akun, buat/join via kode, role owner/member) | — | ✅ |

---

## 🛠️ Tech Stack

| Item | Detail |
|---|---|
| Bahasa / UI | **Java** + Android Views (XML) + Material Design 3 |
| Arsitektur | **MVVM** (Model–View–ViewModel) + **Repository pattern** |
| Persistensi lokal | **Room** (Personal Mode) |
| Cloud (Team Mode) | **Firebase Auth** + **Cloud Firestore** (offline persistence bawaan) |
| Background | `AlarmManager` (reminder), **WorkManager** (sync notif Team), `BroadcastReceiver` |
| Reaktif | LiveData |
| Build | AGP 8.6.1, Gradle 8.7, JDK 21 (sourceCompatibility 17) |
| SDK | compileSdk 35, minSdk 23, targetSdk 35 |
| Testing | JUnit 4, Mockito, `androidx.arch.core` testing |

> **Tanpa Jetpack Compose / Kotlin** dan **tanpa FCM / Cloud Functions** — sengaja, demi kurva belajar terkendali dan **gratis tanpa kartu** (lihat [Keputusan Desain](#-keputusan-desain-penting)).

---

## 🏛️ Arsitektur

Alur lapisan dipaksa secara struktural (lewat constructor & factory), bukan sekadar konvensi:

```
Activity / Fragment (UI)
        │  observe LiveData  ·  panggil method ViewModel
        ▼
     ViewModel
        │  satu-satunya yang boleh memanggil Repository
        ▼
    Repository  ◄── satu-satunya jalur resmi ke sumber data
        │
   ┌────┴─────┐
   ▼          ▼
  Room      Firestore
 (Personal)  (Team Mode)
```

- **UI tidak pernah** menyentuh Repository/DAO/Firestore langsung.
- Logic per-layar di ViewModel (tidak bergantung lifecycle Android → mudah di-unit-test).
- Room memvalidasi query SQL saat **compile**, bukan saat runtime.

---

## 📁 Struktur Project

```
app/src/main/java/com/example/taskreminder2
├── data/
│   ├── local/          Room: entity (Task, Course, TaskLog), dao, AppDatabase
│   ├── model/          POJO Firestore (Team, Member, TeamTask, TeamTaskLog, ...)
│   ├── remote/         wrapper Firebase (dipakai Repository)
│   └── repository/     TaskRepository, TaskLogRepository, AuthRepository,
│                       TeamRepository, TeamTaskRepository  (jalur data resmi)
├── ui/
│   ├── tasklist/       Beranda + form tugas Personal
│   ├── taskdetail/     detail + riwayat + catatan Personal
│   ├── team/           Login/Register, TeamHome, tugas/detail/form Team, Kelola Team
│   └── BaseToolbarActivity, TaskViewBinder, ...  (helper UI bersama)
├── notification/       AlarmReceiver, BootReceiver, NotificationHelper,
│                       ReminderScheduler, TeamSyncWorker (WorkManager)
└── util/               OverdueChecker, TaskStatus, LogType, DateTimeFormatter
```

---

## ✅ Prasyarat

- **Android Studio** (terbaru) + **Android SDK** (platform 35)
- **JDK 21** (atau JBR bawaan Android Studio)
- Akun **Google** untuk membuat project Firebase (gratis, plan Spark)
- Emulator/device Android (minSdk 23 / Android 6.0+)

---

## 🚀 Setup & Build

### 1. Clone & buka
```bash
git clone <repo-url>
```
Buka folder di Android Studio (biarkan Gradle sync).

### 2. Konfigurasi Firebase (untuk Team Mode)

> Personal Mode jalan **tanpa** langkah ini. Firebase hanya dibutuhkan untuk Team Mode.

1. Buat project di [Firebase Console](https://console.firebase.google.com) — **biarkan plan Spark (gratis), jangan upgrade Blaze, tidak perlu kartu.**
2. Tambah app Android dengan package name **`com.example.taskreminder2`**.
3. Unduh **`google-services.json`** → letakkan di `app/google-services.json`.
4. Aktifkan **Authentication → Email/Password**.
5. Buat **Firestore Database** (mulai test mode untuk development; lokasi disarankan `asia-southeast2` Jakarta).

> `google-services.json` di-`.gitignore` (berisi kunci spesifik project) — bagikan terpisah, bukan lewat git.

### 3. Build
```bash
./gradlew assembleDebug
```

---

## ▶️ Menjalankan

Jalankan dari Android Studio (tombol Run) ke emulator/device, atau:
```bash
./gradlew installDebug
```

---

## 🧪 Testing

Unit test (JVM, tanpa emulator):
```bash
./gradlew testDebugUnitTest
```
Mencakup `OverdueChecker`, `TaskStatus`, `TaskRepository` (auto-log & diff), `TaskListViewModel` & `TeamTaskViewModel` (search/filter). **29 test, semua lulus.**

---

## 💡 Keputusan Desain Penting

- **Gratis tanpa kartu** — tidak memakai Cloud Functions/FCM (yang butuh plan Blaze + kartu). Notifikasi Team dibangkitkan **client-side**: snapshot listener saat app hidup + WorkManager (poll) saat app tertutup. Konsekuensi: notif saat app tertutup tertunda ~15–30 menit; reminder deadline tetap tepat waktu via AlarmManager.
- **Team Mode tanpa Room/SyncEngine** — sinkronisasi offline↔online sepenuhnya diserahkan ke **offline persistence bawaan Firestore**. Filter Team Mode dikerjakan di memori atas data dari listener.
- **Auto-overdue dihitung, bukan disimpan** — status terlambat dihitung saat dibutuhkan dari `(deadline, status, sekarang)`, tidak ada kolom `isOverdue` (menghindari data basi & loop penulisan).
- **Dua ruang data terpisah permanen** — tidak ada migrasi data Personal ↔ Team.
- **Last-write-wins** untuk konflik Team Mode (per-field otomatis oleh Firestore), cukup untuk skala tim kecil.

---

## 📌 Status

Seluruh fitur (Fitur-01 s.d. 08, Personal & Team) **selesai** beserta notifikasi kedua mode dan unit test. Aplikasi juga telah dirombak ke tema visual "Emerald Sand".

Sisa: uji manual multi-device (edit bersamaan, offline→online) yang membutuhkan beberapa device + Firebase nyata.

---

## 📄 Catatan

Project tugas kuliah. Package `com.example.taskreminder2` (placeholder) perlu diganti sebelum publikasi ke Play Store.
