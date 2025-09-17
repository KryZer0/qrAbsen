package com.example.qrcodeabsen;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MonitoringAbsen extends BaseActivity {
    private final ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
    private static final int WRITE_EXTERNAL_STORAGE = 100;
    private Button laporan, rekap;
    TextView dateText, welcomeText;
    String role;
    private int setUpCounter = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_absensi);
        laporan = findViewById(R.id.guru_btn);
        rekap = findViewById(R.id.siswa_btn);
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
        laporan.setOnClickListener(v -> exportAbsen("laporan"));
        rekap.setOnClickListener(v -> exportAbsen("rekap"));
    }
    private void exportAbsen(String fungsi) {
        String[] kelasOptions = {"-", "X", "XI", "XII"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Kelas (data tanggal yang diambil hanya bulan)")
                .setItems(kelasOptions, (dialog, which) -> {
                    String selectedKelas = kelasOptions[which];
                    showDatePicker(selectedKelas, fungsi);
                })
                .setCancelable(true)
                .show();
    }
    private void showDatePicker(String kelas, String fungsi) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, dayOfMonth) -> {
                    selectedMonth += 1;
                    String monthYear = String.format(Locale.getDefault(), "%02d-%d", selectedMonth, selectedYear);

                    if (fungsi.equals("laporan")) {
                        if (kelas.equals("-")) {
                            downloadExcel(monthYear, kelas);
                        } else {
                            downloadExcelByKelas(kelas, monthYear, fungsi);
                        }
                    } else if (fungsi.equals("rekap")) {
                        if (kelas == null || kelas.equals("-")) {
                            Toast.makeText(this, "Kelas harus diisi untuk rekap!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        downloadExcelByKelas(kelas, monthYear, fungsi);
                    }
                },
                year, month, 1);

        try {
            Field dayField = datePickerDialog.getDatePicker().getClass().getDeclaredField("mDaySpinner");
            dayField.setAccessible(true);
            Object dayPicker = dayField.get(datePickerDialog.getDatePicker());
            ((View) dayPicker).setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        datePickerDialog.show();
    }

    private void downloadExcel(String monthYear, String kelas) {
        Call<ResponseBody> call = apiService.exportAbsensi(monthYear);
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengunduh data presensi...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    boolean saved = writeResponseBodyToDisk(response.body(), monthYear, kelas);
                    Toast.makeText(MonitoringAbsen.this, saved ? "File berhasil diunduh" : "Gagal menyimpan file", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MonitoringAbsen.this, "Gagal mengunduh. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MonitoringAbsen.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadExcelByKelas(String kelas, String monthYear, String fungsi) {
        Call<ResponseBody> call;
        if (fungsi.equals("laporan")){
            call = apiService.exportAbsensiByKelas(kelas, monthYear);
        } else {
            call = apiService.laporanAbsensiByKelas(kelas, monthYear);
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengunduh data presensi...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    boolean saved = writeResponseBodyToDisk(response.body(), monthYear, kelas);
                    Toast.makeText(MonitoringAbsen.this, saved ? "File berhasil diunduh" : "Gagal menyimpan file", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MonitoringAbsen.this, "Gagal mengunduh. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MonitoringAbsen.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String monthYear, String kelas) {
        try {
            byte[] fileBytes = body.bytes();
            String fileName;
            if (kelas != null && !kelas.equals("-")) {
                fileName = "presensi-" + monthYear + "-kelas-" + kelas + "-" + System.currentTimeMillis() + ".xlsx";
            } else {
                fileName = "presensi-" + monthYear + "-" + System.currentTimeMillis() + ".xlsx";
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) return false;

                try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                    out.write(fileBytes);
                    out.flush();
                    return true;
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
                    return false;
                }

                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!path.exists() && !path.mkdirs()) return false;

                File file = new File(path, fileName);
                try (OutputStream out = new FileOutputStream(file)) {
                    out.write(fileBytes);
                    out.flush();
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
