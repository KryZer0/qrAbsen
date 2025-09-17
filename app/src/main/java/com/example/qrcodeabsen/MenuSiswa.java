package com.example.qrcodeabsen;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.AreaBreakType;
import com.itextpdf.layout.property.UnitValue;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuSiswa extends BaseActivity {
    private static final int WRITE_EXTERNAL_STORAGE = 100;
    private final ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
    private Button add, list, qr, export, izin, delete;
    private List<SiswaModel> siswaList = new ArrayList<>();
    private int imageLoadCounter = 0;
    private List<Bitmap> qrCodeBitmaps = new ArrayList<>();
    private TextView dateText, welcomeText;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siswa_menu);
        findViewById();
        setClickListener();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void findViewById() {
        add = findViewById(R.id.btnTambahSiswa);
        list = findViewById(R.id.btnDaftarSiswa);
        qr = findViewById(R.id.btnGenerateQr);
        export = findViewById(R.id.btnExportAbsen);
        dateText = findViewById(R.id.currentDate);
        delete = findViewById(R.id.btnDeleteSiswa);
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        dateText.setText(currentDate);

        welcomeText = findViewById(R.id.welcomeText);
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        role = sharedPref.getString("role", "");
        if (role.equals("admin")) {
            welcomeText.setText("Selamat datang, Kesiswaan/Admin");
        } else {
            welcomeText.setText("Selamat datang, Bapak/Ibu Guru");
        }
    }

    private void setClickListener() {
        add.setOnClickListener(v -> tambahSiswa());
        list.setOnClickListener(v -> daftarSiswa());
        qr.setOnClickListener(v -> fetchAllSiswa());
        export.setOnClickListener(v -> exportAbsen());
        delete.setOnClickListener(v -> deleteSiswa());
    }

    private void tambahSiswa() {
        Intent intent = new Intent(MenuSiswa.this, FormSiswaActivity.class);
        intent.putExtra("status", "add");
        startActivity(intent);
    }

    private void daftarSiswa() {
        Intent intent = new Intent(MenuSiswa.this, DaftarSiswaActivity.class);
        intent.putExtra("status", "list");
        startActivity(intent);
    }

    private void deleteSiswa() {
        Intent intent = new Intent(MenuSiswa.this, DaftarSiswaActivity.class);
        intent.putExtra("status", "delete");
        startActivity(intent);
    }

    // fungsi ini untuk export absen, memilih absen berdasarkan bulan yang dipilih
    private void exportAbsen() {
        String[] kelasOptions = {"-", "X", "XI", "XII"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Kelas")
                .setItems(kelasOptions, (dialog, which) -> {
                    String selectedKelas = kelasOptions[which];
                    showDatePicker(selectedKelas);
                })
                .setCancelable(true)
                .show();
    }

    private void showDatePicker(String kelas) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, dayOfMonth) -> {
                    selectedMonth += 1;
                    String monthYear = String.format(Locale.getDefault(), "%02d-%d", selectedMonth, selectedYear);

                    if (kelas.equals("-")) {
                        downloadExcel(monthYear);
                    } else {
                        downloadExcelByKelas(kelas, monthYear);
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

    private void downloadExcel(String monthYear) {
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
                    boolean saved = writeResponseBodyToDisk(response.body(), monthYear);
                    Toast.makeText(MenuSiswa.this, saved ? "File berhasil diunduh" : "Gagal menyimpan file", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MenuSiswa.this, "Gagal mengunduh. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MenuSiswa.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadExcelByKelas(String kelas, String monthYear) {
        Call<ResponseBody> call = apiService.exportAbsensiByKelas(kelas, monthYear);
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengunduh data presensi...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    boolean saved = writeResponseBodyToDisk(response.body(), monthYear);
                    Toast.makeText(MenuSiswa.this, saved ? "File berhasil diunduh" : "Gagal menyimpan file", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MenuSiswa.this, "Gagal mengunduh. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MenuSiswa.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String monthYear) {
        try {
            byte[] fileBytes = body.bytes();
            String fileName = "presensi-" + monthYear + "-" + System.currentTimeMillis() + ".xlsx";

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
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
    private void fetchAllSiswa() {
        Call<List<SiswaModel>> call = apiService.getAllDataQr();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang Mengambil data ...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        call.enqueue(new Callback<List<SiswaModel>>() {
            @Override
            public void onResponse(Call<List<SiswaModel>> call, Response<List<SiswaModel>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    siswaList = response.body();
                    downloadAllQRCodesAndGeneratePDF();
                } else {
                    Toast.makeText(MenuSiswa.this, "Gagal mengambil data siswa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SiswaModel>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MenuSiswa.this, "Gagal mengambil data siswa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadAllQRCodesAndGeneratePDF() {
        if (siswaList.isEmpty()) return;

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengunduh QR Code...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(siswaList.size());
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.show();

        List<Bitmap> qrBitmaps = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);

        for (SiswaModel siswa : siswaList) {
            String qrCodeUrl = siswa.getQrCode();

            runOnUiThread(() -> Picasso.get().load(qrCodeUrl).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    qrBitmaps.add(bitmap);
                    int progress = counter.incrementAndGet();
                    progressDialog.setProgress(progress);

                    if (progress == siswaList.size()) {
                        progressDialog.dismiss();
                        generateBatchPDF(qrBitmaps);
                    }
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    int progress = counter.incrementAndGet();
                    progressDialog.setProgress(progress);

                    if (progress == siswaList.size()) {
                        progressDialog.dismiss();
                        generateBatchPDF(qrBitmaps);
                    }
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {}
            }));
        }
    }

    private void generateBatchPDF(List<Bitmap> qrBitmaps) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang membuat PDF...");
        progressDialog.setCancelable(true);
        progressDialog.show();

        new Thread(() -> {
            try {
                ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
                PdfWriter writer = new PdfWriter(pdfStream);
                PdfDocument pdfDocument = new PdfDocument(writer);
                Document document = new Document(pdfDocument);

                float[] columnWidths = {5, 252, 5, 252, 5};
                Table table = new Table(columnWidths);

                int maxPerPage = 8;

                for (int i = 0; i < siswaList.size(); i += 2) {
                    if (!progressDialog.isShowing()) return;

                    table.addCell(new Cell().setBorder(Border.NO_BORDER));
                    table.addCell(generateCardCell(siswaList.get(i), qrBitmaps.size() > i ? qrBitmaps.get(i) : null));
                    table.addCell(new Cell().setBorder(Border.NO_BORDER));

                    if (i + 1 < siswaList.size()) {
                        table.addCell(generateCardCell(siswaList.get(i + 1), qrBitmaps.size() > (i + 1) ? qrBitmaps.get(i + 1) : null));
                    } else {
                        table.addCell(new Cell().setBorder(Border.NO_BORDER));
                    }

                    table.addCell(new Cell().setBorder(Border.NO_BORDER));

                    for (int j = 0; j < 5; j++) {
                        table.addCell(new Cell().setHeight(5).setBorder(Border.NO_BORDER));
                    }

                    if ((i / 2) + 1 == maxPerPage / 2) {
                        document.add(table);
                        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                        table = new Table(columnWidths);
                    }
                }

                document.add(table);
                document.close();

                savePdfToDownloads(pdfStream.toByteArray());

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(MenuSiswa.this, "PDF Berhasil disimpan di Downloads", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(MenuSiswa.this, "Gagal membuat PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void savePdfToDownloads(byte[] pdfData) {
        try {
            String fileName = "KartuNama-" + System.currentTimeMillis() + ".pdf";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) return;

                try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                    out.write(pdfData);
                    out.flush();
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
                    return;
                }

                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!path.exists()) path.mkdirs();

                File file = new File(path, fileName);
                try (OutputStream out = new FileOutputStream(file)) {
                    out.write(pdfData);
                    out.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Cell generateCardCell(SiswaModel siswa, Bitmap qrBitmap) {
        Cell cell = new Cell()
                .setPadding(10)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 5))
                .setBackgroundColor(ColorConstants.WHITE);

        cell.add(new Paragraph("Nama: " + siswa.getNama()));
        cell.add(new Paragraph("NISN: " + siswa.getNisn()));
        cell.add(new Paragraph("Kelas: " + siswa.getKelas() + " " + siswa.getJurusan()));

        if (qrBitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Image qrImage = new Image(ImageDataFactory.create(stream.toByteArray()));
            qrImage.setWidth(80).setHeight(80).setMarginTop(10);
            cell.add(qrImage);
        } else {
            cell.add(new Paragraph("[QR Code Tidak Ditemukan]"));
        }
        return cell;
    }
}
