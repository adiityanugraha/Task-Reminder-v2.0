package com.example.taskreminder2.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.example.taskreminder2.R;
import com.google.android.material.chip.ChipGroup;

/**
 * Menyambungkan search box + ChipGroup ke satu callback, dipakai bersama
 * daftar tugas Personal & Team Mode (Fitur-03).
 *
 * <p>Memetakan chip terpilih ke {@link TaskQuery.Filter} dan menyusun
 * {@link TaskQuery} setiap kali teks atau chip berubah — sebelumnya boilerplate
 * {@code TextWatcher} + pemetaan chip ini diduplikasi di kedua Activity.
 * Bergantung pada konvensi ID chip yang sama di kedua layout
 * ({@code chipAll}/{@code chipPriority}/{@code chipOverdue}).</p>
 */
public final class SearchFilterBinder {

    public interface OnQueryChanged {
        void onQuery(TaskQuery query);
    }

    private final EditText editSearch;
    private final ChipGroup chipGroup;

    private SearchFilterBinder(EditText editSearch, ChipGroup chipGroup, OnQueryChanged listener) {
        this.editSearch = editSearch;
        this.chipGroup = chipGroup;
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                listener.onQuery(currentQuery());
            }
        });
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> listener.onQuery(currentQuery()));
    }

    /** Pasang listener pada search box + chip; callback dipanggil tiap perubahan. */
    public static SearchFilterBinder bind(EditText editSearch, ChipGroup chipGroup, OnQueryChanged listener) {
        return new SearchFilterBinder(editSearch, chipGroup, listener);
    }

    /** Susun kriteria dari input saat ini (search box + chip terpilih). */
    public TaskQuery currentQuery() {
        String keyword = editSearch.getText() == null ? "" : editSearch.getText().toString().trim();
        TaskQuery.Filter filter;
        int checked = chipGroup.getCheckedChipId();
        if (checked == R.id.chipPriority) {
            filter = TaskQuery.Filter.PRIORITY_HIGH;
        } else if (checked == R.id.chipOverdue) {
            filter = TaskQuery.Filter.OVERDUE;
        } else {
            filter = TaskQuery.Filter.ALL;
        }
        return new TaskQuery(filter, keyword);
    }
}
