/**
 * Room DAO — Data Access Object, kumpulan query untuk tiap Entity.
 *
 * <p>Berisi {@code TaskDao}, {@code CourseDao}, {@code TaskLogDao}.
 * Query SQL di sini divalidasi Room saat <em>compile</em>, bukan saat
 * runtime — typo nama kolom/tabel menggagalkan build, bukan meng-crash
 * app di tangan user.</p>
 */
package com.example.taskreminder2.data.local.dao;
