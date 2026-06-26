package com.example.taskreminder2.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taskreminder2.R;

/**
 * Entry point sementara (Day 1 — kerangka project).
 * Akan digantikan layar daftar tugas (TaskListActivity) pada Day 5.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
