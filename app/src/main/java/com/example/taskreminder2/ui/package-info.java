/**
 * Lapisan presentasi — Activity, ViewModel, dan Adapter, dikelompokkan
 * per layar/fitur (mis. {@code tasklist}, {@code taskdetail}, {@code team}).
 *
 * <p>Aturan arsitektur yang dipaksa: kelas di sini hanya boleh bicara ke
 * ViewModel, tidak pernah memanggil Repository atau DAO langsung. Pelanggaran
 * aturan ini adalah sinyal "kebocoran arsitektur" yang harus segera
 * diperbaiki (lihat roadmap Day 31).</p>
 */
package com.example.taskreminder2.ui;
