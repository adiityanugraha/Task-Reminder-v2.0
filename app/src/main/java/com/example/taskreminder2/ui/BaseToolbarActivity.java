package com.example.taskreminder2.ui;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taskreminder2.R;

/**
 * Kelas dasar layar dengan header kustom "Emerald Sand" ({@code include_header}).
 * Menghapus boilerplate back/judul yang sebelumnya diulang di tiap layar.
 */
public abstract class BaseToolbarActivity extends AppCompatActivity {

    /**
     * Wiring header kustom {@code include_header}: tombol back → {@code finish()},
     * dan judul tengah. Panggil SETELAH {@code setContentView}. Aman bila layar
     * belum memakai header baru (findViewById null diabaikan).
     *
     * @param titleRes resource judul, atau 0 untuk membiarkan judul kosong /
     *                 diatur belakangan (mis. nama team yang dinamis)
     */
    protected void setupHeader(@StringRes int titleRes) {
        View back = findViewById(R.id.btnBack);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }
        if (titleRes != 0) {
            TextView title = findViewById(R.id.textHeaderTitle);
            if (title != null) {
                title.setText(titleRes);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
