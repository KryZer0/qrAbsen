package com.example.qrcodeabsen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Dashboard extends BaseActivity {
    private Button checkIn, checkOut, history, siswa, izin, laporan, setting;
    private ImageView logo;
    TextView dateText, welcomeText;
    String role;
    private int setUpCounter = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        checkIn = findViewById(R.id.check_in_btn);
        checkOut = findViewById(R.id.check_out_btn);
        history = findViewById(R.id.history_btn);
        siswa = findViewById(R.id.siswa_btn);
        dateText = findViewById(R.id.currentDate);
        welcomeText = findViewById(R.id.welcomeText);
        izin = findViewById(R.id.btnIzin);
        logo = findViewById(R.id.logo);
        laporan = findViewById(R.id.laporan_rekap);
        setting = findViewById(R.id.setting_btn);

        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        dateText.setText(currentDate);
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        history.setText("Monitoring \nPresensi");

        role = sharedPref.getString("role", "");
        if (role.equals("admin")) {
            welcomeText.setText("Selamat datang, Kesiswaan/Admin");
            checkIn.setVisibility(View.GONE);
            checkOut.setVisibility(View.GONE);
            izin.setVisibility(View.GONE);
            checkOut.setVisibility(View.GONE);
        } else {
            welcomeText.setText("Selamat datang, Bapak/Ibu Guru");
            siswa.setVisibility(View.GONE);
            checkOut.setVisibility(View.GONE);
            laporan.setVisibility(View.GONE);
        }

        setClickListener();
    }
    private void setClickListener()
    {
        checkIn.setOnClickListener(v -> checkIn());
        checkOut.setOnClickListener(v -> checkOut());
        history.setOnClickListener(v -> history());
        siswa.setOnClickListener(v -> siswa());
        izin.setOnClickListener(v -> izinSiswa());
        logo.setOnClickListener(v -> gantiPassword());
        laporan.setOnClickListener(v -> menuHistory());
        setting.setOnClickListener(v -> gantiPassword());
    }

    private void checkIn()
    {
        Intent intent = new Intent(Dashboard.this,MainActivity.class);
        intent.putExtra("absen","checkin");
        startActivity(intent);
    }

    private void checkOut()
    {
        Intent intent = new Intent(Dashboard.this,MainActivity.class);
        intent.putExtra("absen","checkout");
        startActivity(intent);
    }

    private void history()
    {
        Intent intent = new Intent(Dashboard.this,HistoryActivity.class);
        startActivity(intent);
    }

    private void menuHistory()
    {
        Intent intent = new Intent(Dashboard.this,MonitoringAbsen.class);
        startActivity(intent);
    }

    private void siswa() {
       Intent intent = new Intent(Dashboard.this, MasterDataMenu.class);
       startActivity(intent);
    }

    private void izinSiswa() {
        Intent intent = new Intent(Dashboard.this, izinSiswaActivity.class);
        startActivity(intent);
    }

    private void guru() {
        Intent intent = new Intent(Dashboard.this, MenuGuru.class);
        startActivity(intent);
    }

    private void gantiPassword() {
        Intent intent = new Intent(Dashboard.this, ChangePasswordActivity.class);
        startActivity(intent);
    }
}
