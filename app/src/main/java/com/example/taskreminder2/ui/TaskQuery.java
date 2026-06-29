package com.example.taskreminder2.ui;

/**
 * Kriteria pencarian & filter daftar tugas (Fitur-03), dipakai bersama
 * Personal Mode ({@code TaskListViewModel}) dan Team Mode
 * ({@code TeamTaskViewModel}).
 *
 * <p>Sebelumnya enum {@code Filter} dan kelas kriteria ini diduplikasi di
 * kedua ViewModel; disatukan di sini agar perubahan kriteria cukup di satu
 * tempat. Immutable — sekali dibuat tidak berubah.</p>
 */
public final class TaskQuery {

    /** Mode filter. Pencarian judul ditangani lewat {@code keyword} terpisah. */
    public enum Filter { ALL, PRIORITY_HIGH, OVERDUE }

    public final Filter filter;
    public final String keyword;

    public TaskQuery(Filter filter, String keyword) {
        this.filter = filter != null ? filter : Filter.ALL;
        this.keyword = keyword;
    }

    /** True jika ada kata kunci pencarian (mengabaikan spasi). */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    /** Kata kunci tanpa spasi pinggir; string kosong jika tidak ada. */
    public String trimmedKeyword() {
        return keyword == null ? "" : keyword.trim();
    }

    /** True bila tampilan sedang dipersempit (untuk memilih pesan kosong). */
    public boolean isFiltering() {
        return hasKeyword() || filter != Filter.ALL;
    }
}
