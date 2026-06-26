/**
 * Wrapper sumber data remote — Firebase Auth & Firestore mentah (Team Mode).
 *
 * <p>Hanya dipakai oleh Repository Team Mode, tidak pernah diakses langsung
 * dari ViewModel/UI. Offline & antrian pending-write ditangani cache bawaan
 * Firestore — tidak ada SyncEngine manual (lihat blueprint Bagian 5).</p>
 */
package com.example.taskreminder2.data.remote;
