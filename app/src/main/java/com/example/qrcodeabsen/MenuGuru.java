package com.example.qrcodeabsen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MenuGuru extends BaseActivity {
    private Button add, list, delete;
    private String role;
    TextView dateText, welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guru_menu);
        findViewById();
        setClickListener();
    }
    private void findViewById() {
        add = findViewById(R.id.btnTambahGuru);
        list = findViewById(R.id.btnDaftarGuru);
        delete = findViewById(R.id.btnDeleteGuru);
        dateText = findViewById(R.id.currentDate);
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        dateText.setText(currentDate);

        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        role = sharedPref.getString("role", "");
        welcomeText = findViewById(R.id.welcomeText);
        if (role.equals("admin")) {
            welcomeText.setText("Selamat datang, Kesiswaan/Admin");
        } else {
            welcomeText.setText("Selamat datang, Bapak/Ibu Guru");
        }
    }

    private void setClickListener() {
        add.setOnClickListener(v -> tambahGuru());
        list.setOnClickListener(v -> daftarGuru());
        delete.setOnClickListener(v -> deleteGuru());
    }

    private void tambahGuru() {
        Intent intent = new Intent(MenuGuru.this, FormGuruActivity.class);
        intent.putExtra("status", "add");
        startActivity(intent);
    }

    private void daftarGuru() {
        Intent intent = new Intent(MenuGuru.this, DaftarGuruActivity.class);
        intent.putExtra("status", "list");
        startActivity(intent);
    }

    private void deleteGuru() {
        Intent intent = new Intent(MenuGuru.this, DaftarGuruActivity.class);
        intent.putExtra("status", "delete");
        startActivity(intent);
    }
}
