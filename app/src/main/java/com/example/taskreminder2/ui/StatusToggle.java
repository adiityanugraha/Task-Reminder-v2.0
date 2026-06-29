package com.example.taskreminder2.ui;

import com.example.taskreminder2.R;
import com.google.android.material.button.MaterialButtonToggleGroup;

/**
 * Pemetaan dua arah antara segmented control status (toggle group di
 * {@code activity_task_form}) dan index {@code TaskStatus.VALUES}. Dipakai
 * bersama form Personal & Team agar logikanya tidak diduplikasi.
 */
public final class StatusToggle {

    /** Index status terpilih (0=Belum, 1=Dikerjakan, 2=Selesai). */
    public static int indexOf(MaterialButtonToggleGroup group) {
        int id = group.getCheckedButtonId();
        if (id == R.id.btnStatusInProgress) {
            return 1;
        }
        if (id == R.id.btnStatusDone) {
            return 2;
        }
        return 0;
    }

    /** Id tombol toggle untuk sebuah index status. */
    public static int buttonId(int index) {
        switch (index) {
            case 1:
                return R.id.btnStatusInProgress;
            case 2:
                return R.id.btnStatusDone;
            default:
                return R.id.btnStatusNotStarted;
        }
    }

    private StatusToggle() {
    }
}
