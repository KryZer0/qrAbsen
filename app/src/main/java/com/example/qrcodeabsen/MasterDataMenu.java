package com.example.qrcodeabsen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MasterDataMenu extends BaseActivity {
    private Button history, siswa;
    TextView dateText, welcomeText;
    String role;
    private int setUpCounter = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_data);
        history = findViewById(R.id.guru_btn);
        siswa = findViewById(R.id.siswa_btn);
        dateText = findViewById(R.id.currentDate);
        welcomeText = findViewById(R.id.welcomeText);

        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        dateText.setText(currentDate);
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        role = sharedPref.getString("role", "");

        if (role.equals("admin")) {
            welcomeText.setText("Selamat datang, Kesiswaan/Admin");
        } else {
            welcomeText.setText("Selamat datang, Bapak/Ibu Guru");
        }
        setClickListener();
    }
    private void setClickListener()
    {
        history.setOnClickListener(v -> guru());
        siswa.setOnClickListener(v -> siswa());
    }

    private void siswa() {
        Intent intent = new Intent(MasterDataMenu.this, DaftarSiswaActivity.class);
        startActivity(intent);
    }

    private void guru() {
        Intent intent = new Intent(MasterDataMenu.this, DaftarGuruActivity.class);
        startActivity(intent);
    }

}
