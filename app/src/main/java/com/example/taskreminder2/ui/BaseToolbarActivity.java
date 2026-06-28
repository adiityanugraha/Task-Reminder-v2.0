package com.example.taskreminder2.ui;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taskreminder2.R;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Kelas dasar untuk semua Activity berbasis {@link MaterialToolbar}.
 * Menghapus boilerplate yang sebelumnya diulang di tiap layar: memasang
 * toolbar sebagai action bar, judul, tombol up, dan navigasi up.
 */
public abstract class BaseToolbarActivity extends AppCompatActivity {

    /**
     * Memasang toolbar (id {@code R.id.toolbar}) sebagai action bar.
     * Panggil SETELAH {@code setContentView}.
     *
     * @param titleRes resource judul, atau 0 untuk membiarkan judul diatur
     *                 belakangan / memakai label Activity
     * @param showUp   true untuk menampilkan tombol kembali (up)
     */
    protected void setupToolbar(@StringRes int titleRes, boolean showUp) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            if (titleRes != 0) {
                getSupportActionBar().setTitle(titleRes);
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(showUp);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
