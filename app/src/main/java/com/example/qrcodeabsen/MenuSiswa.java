package com.example.qrcodeabsen;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuSiswa extends BaseActivity {
    private static final int WRITE_EXTERNAL_STORAGE = 100;
    private final ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
    private Button add, list, qr;
    private List<SiswaModel> siswaList = new ArrayList<>();
    private int imageLoadCounter = 0;
    private List<Bitmap> qrCodeBitmaps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siswa_menu);
        findViewById();
        setClickListener();
    }

    private void findViewById() {
        add = findViewById(R.id.btnTambahSiswa);
        list = findViewById(R.id.btnDaftarSiswa);
        qr = findViewById(R.id.btnGenerateQr);
    }

    private void setClickListener() {
        add.setOnClickListener(v -> tambahSiswa());
        list.setOnClickListener(v -> daftarSiswa());
        qr.setOnClickListener(v -> fetchAllSiswa());
    }

    private void tambahSiswa() {
        Intent intent = new Intent(MenuSiswa.this, FormSiswaActivity.class);
        intent.putExtra("status", "add");
        startActivity(intent);
    }

    private void daftarSiswa() {
        Intent intent = new Intent(MenuSiswa.this, DaftarSiswaActivity.class);
        startActivity(intent);
    }

    private void fetchAllSiswa() {
        Call<List<SiswaModel>> call = apiService.getAllDataQr();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<SiswaModel>> call, Response<List<SiswaModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    siswaList = response.body();
                    downloadAllQRCodesAndGeneratePDF();
                }
            }

            @Override
            public void onFailure(Call<List<SiswaModel>> call, Throwable t) {
                Toast.makeText(MenuSiswa.this, "Gagal mengambil data siswa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadAllQRCodesAndGeneratePDF() {
        Log.d("DEBUG", "Mulai downloadAllQRCodesAndGeneratePDF...");

        if (siswaList.isEmpty()) {
            Log.e("DEBUG", "siswaList kosong! Tidak bisa generate PDF.");
            return;
        }

        List<Bitmap> qrBitmaps = new ArrayList<>();
        int[] counter = {0};

        for (SiswaModel siswa : siswaList) {
            String qrCodeUrl = siswa.getQrCode();
            Log.d("DEBUG", "Memproses QR Code: " + qrCodeUrl);

            runOnUiThread(() -> Picasso.get().load(qrCodeUrl).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Log.d("DEBUG", "Berhasil download QR Code untuk: " + siswa.getNama());
                    qrBitmaps.add(bitmap);
                    counter[0]++;

                    if (counter[0] == siswaList.size()) {
                        Log.d("DEBUG", "Semua QR Code berhasil di-download, mulai generate PDF...");
                        generateBatchPDF(qrBitmaps);
                    }
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    Log.e("ERROR", "Gagal memuat QR Code untuk: " + siswa.getNama() + " - " + e.getMessage());
                    counter[0]++;

                    if (counter[0] == siswaList.size()) {
                        Log.d("DEBUG", "Beberapa QR Code gagal, tetap lanjut buat PDF...");
                        generateBatchPDF(qrBitmaps);
                    }
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    Log.d("DEBUG", "Persiapan load QR Code...");
                }
            }));
        }
    }

    private void generateBatchPDF(List<Bitmap> qrBitmaps) {
        AtomicInteger cellCount = new AtomicInteger();
        Log.d("DEBUG", "generateBatchPDF() mulai...");

        new Thread(() -> {
            String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/KartuNama.pdf";

            try {
                ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
                PdfWriter writer = new PdfWriter(pdfStream);
                PdfDocument pdfDocument = new PdfDocument(writer);
                Document document = new Document(pdfDocument);

                float[] columnWidths = {1, 1};
                Table table = new Table(columnWidths);

                for (int i = 0; i < siswaList.size(); i++) {

                    SiswaModel siswa = siswaList.get(i);
                    Bitmap qrBitmap = qrBitmaps.size() > i ? qrBitmaps.get(i) : null;

                    Cell cell = new Cell().setWidth(UnitValue.createPointValue(252))
                            .setHeight(UnitValue.createPointValue(144))
                            .setPadding(10)
                            .setMargin(10)
                            .setBorder(new SolidBorder(5));
                    cell.add(new Paragraph("Nama: " + siswa.getNama()));
                    cell.add(new Paragraph("NISN: " + siswa.getNisn()));

                    if (qrBitmap != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        Image qrImage = new Image(ImageDataFactory.create(stream.toByteArray()));
                        qrImage.setWidth(80).setHeight(80);
                        qrImage.setMarginTop(10);
                        cell.add(qrImage);
                    } else {
                        cell.add(new Paragraph("[QR Code Tidak Ditemukan]"));
                    }

                    table.addCell(cell);
                    cellCount.getAndIncrement();
                }
                document.add(table);
                document.close();

                savePdfToDownloads(pdfStream.toByteArray());

                runOnUiThread(() -> Toast.makeText(MenuSiswa.this, "PDF Berhasil Dibuat di "
                        + pdfPath, Toast.LENGTH_LONG).show());

            } catch (Exception e) {
                Log.e("ERROR", "Gagal membuat PDF: " + e.getMessage());
            }
        }).start();
    }

    private void savePdfToDownloads(byte[] pdfData) {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, "KartuNama.pdf");
        values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        Uri uri = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        }

        if (uri != null) {
            try (OutputStream out = resolver.openOutputStream(uri)) {
                out.write(pdfData);
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                resolver.update(uri, values, null, null);
                runOnUiThread(() -> Toast.makeText(this, "PDF tersimpan di Downloads", Toast.LENGTH_LONG).show());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
