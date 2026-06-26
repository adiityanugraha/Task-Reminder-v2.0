/**
 * Repository — satu-satunya jalur resmi akses data.
 *
 * <p>ViewModel hanya boleh bicara ke Repository; Repository menyembunyikan
 * sumber data di baliknya (Room untuk Personal Mode, Firestore listener
 * untuk Team Mode). Berisi {@code TaskRepository}, {@code TaskLogRepository},
 * {@code TeamTaskRepository}, {@code AuthRepository}, {@code TeamRepository}.</p>
 *
 * <p>Ini menjawab masalah project lama: tidak ada lagi Singleton manager
 * yang saling memanggil secara tersembunyi.</p>
 */
package com.example.taskreminder2.data.repository;
