package com.example.qrcodeabsen;

import static android.content.ContentValues.TAG;

import static com.example.qrcodeabsen.ApiUtils.formatTextWithNewLine;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DaftarSiswaActivity extends BaseActivity {
    private final ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
    private static final int WRITE_EXTERNAL_STORAGE = 100;
    private TableLayout stk;
    private List<SiswaModel> data;
    private List<SiswaModel> originalData;
    private boolean isAscending = true;
    private TextView nisn, nama, jenis_kelamin, kelas, jurusan, edit, halaman , header;
    private List<SiswaModel> siswaList = new ArrayList<>();
    private EditText searchText;
    private Button prevButton, nextButton;
    private ImageButton GenerateAllQrBtn, tambahSiswa, cetakKartu;
    private Toast toastMessage;
    private Spinner kelasSpinner;
    private int currentPage = 1;
    private int perPage = 10;
    private String status;

    private String[] kelasOtions = {
            "-","X","XI","XII",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_siswa);
        stk = findViewById(R.id.tableSiswa);
        status = getIntent().getStringExtra("status");
        fetchStudents(currentPage);
        setViewById();
        setClickListener();
    }

    private void setViewById() {
        nisn = findViewById(R.id.headerNisn);
        nama = findViewById(R.id.headerNama);
        jenis_kelamin = findViewById(R.id.headerJenis);
        kelas = findViewById(R.id.headerKelas);
        jurusan = findViewById(R.id.headerJurusan);
        edit = findViewById(R.id.headerEdit);
        searchText = findViewById(R.id.searchEdit);
        halaman = findViewById(R.id.pageinfo);
        prevButton = findViewById(R.id.prevpage);
        nextButton = findViewById(R.id.nextpage);
        GenerateAllQrBtn = findViewById(R.id.generateAllQr);
        kelasSpinner = findViewById(R.id.kelasSpin);
        header = findViewById(R.id.headerTitle);
        tambahSiswa = findViewById(R.id.btnTambahSiswa);
        cetakKartu = findViewById(R.id.printCard);

        ArrayAdapter<String> adapterJenis = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kelasOtions);
        adapterJenis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kelasSpinner.setAdapter(adapterJenis);
    }

    private void setClickListener() {
        nisn.setOnClickListener(v -> sortData("nisn"));
        nama.setOnClickListener(v -> sortData("nama"));
        jenis_kelamin.setOnClickListener(v -> sortData("jenis_kelamin"));
        kelas.setOnClickListener(v -> sortData("kelas"));
        jurusan.setOnClickListener(v -> sortData("jurusan"));
        tambahSiswa.setOnClickListener(v -> tambahSiswa());
        cetakKartu.setOnClickListener(v ->fetchAllSiswa());
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchStudents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        kelasSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedKelas = kelasSpinner.getSelectedItem().toString();
                if (!selectedKelas.equals("-")) {
                    fetchStudentsByKelas(selectedKelas, currentPage);
                } else {
                    fetchStudents(currentPage); // default
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        nextButton.setOnClickListener(v -> {
            currentPage++;
            fetchStudents(currentPage);
        });

        prevButton.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                fetchStudents(currentPage);
            }
        });

        GenerateAllQrBtn.setOnClickListener(v -> generateAllQr());
    }

    private void tambahSiswa() {
        Intent intent = new Intent(DaftarSiswaActivity.this, FormSiswaActivity.class);
        intent.putExtra("status", "add");
        startActivity(intent);
    }

    private void fetchStudents(int page) {
        Call<PaginatedResponse<SiswaModel>> call = apiService.fetchSiswa(page, perPage);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PaginatedResponse<SiswaModel>> call, Response<PaginatedResponse<SiswaModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data = response.body().getData();
                    originalData = data;
                    initTable();
                    updatePaginationInfo(response.body().getCurrentPage(), response.body().getLastPage());
                } else {
                    Toast.makeText(DaftarSiswaActivity.this, "Gagal memuat data!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<SiswaModel>> call, Throwable t) {
                Toast.makeText(DaftarSiswaActivity.this, "Terjadi kesalahan jaringan!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchStudentsByKelas(String selectedKelas, int page) {
        Call<PaginatedResponse<SiswaModel>> call = apiService.fetchSiswaByKelas(selectedKelas ,page, perPage);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PaginatedResponse<SiswaModel>> call, Response<PaginatedResponse<SiswaModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data = response.body().getData();
                    originalData = data;
                    initTable();
                    updatePaginationInfo(response.body().getCurrentPage(), response.body().getLastPage());
                } else {
                    Toast.makeText(DaftarSiswaActivity.this, "Gagal memuat data!", Toast.LENGTH_SHORT).show();
                    try {
                        Log.e("API_ERROR", "Code: " + response.code() + ", Body: " + response.errorBody().string());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<SiswaModel>> call, Throwable t) {
                Toast.makeText(DaftarSiswaActivity.this, "Terjadi kesalahan jaringan!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateAllQr() {
        Call<ResponseBody> call = apiService.generateAllQr();

        call.enqueue(new Callback<>() {
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (toastMessage != null) {
                        toastMessage.cancel();
                    }
                    String successMessage = ApiUtils.getSuccessMessage(response.body());
                    toastMessage = Toast.makeText(DaftarSiswaActivity.this, successMessage, Toast.LENGTH_SHORT);
                    toastMessage.show();
                    finish();
                } else {
                    String errorMessage = ApiUtils.getErrorMessage(response.errorBody());
                    if (toastMessage != null) {
                        toastMessage.cancel();
                    }
                    toastMessage = Toast.makeText(DaftarSiswaActivity.this, errorMessage, Toast.LENGTH_SHORT);
                    toastMessage.show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (toastMessage != null) {
                    toastMessage.cancel();
                }
                if (t instanceof IOException) {
                    toastMessage = Toast.makeText(DaftarSiswaActivity.this, "No Internet Connection", Toast.LENGTH_SHORT);
                } else {
                    toastMessage = Toast.makeText(DaftarSiswaActivity.this, "Server Error", Toast.LENGTH_SHORT);
                }
                toastMessage.show();
            }
        });
    }

    private void initTable() {
        clearTableExceptHeader();
        for (int i = 0; i < data.size(); i++) {
            SiswaModel siswa = data.get(i);
            TableRow tbrow = new TableRow(this);
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            tbrow.setLayoutParams(params);

            // Cek null untuk kelas dan jurusan
            String kelas = (siswa.getKelas() != null) ? siswa.getKelas() : "-";
            String jurusan = (siswa.getJurusan() != null) ? siswa.getJurusan() : "-";

            tbrow.addView(createTextView(String.valueOf(i + 1)));
            tbrow.addView(createTextView(String.valueOf(siswa.getNisn())));
            tbrow.addView(createTextView(siswa.getNama()));
            tbrow.addView(createTextView(kelas));
            tbrow.addView(createTextView(jurusan));
            tbrow.addView(createTextView(siswa.getJns()));
            tbrow.addView(createEditButton(siswa));
            tbrow.addView(createDeleteButton(siswa));

            stk.addView(tbrow);
        }
    }

    private void sortData(String column) {
        if (data == null || data.isEmpty()) return;

        Comparator<SiswaModel> comparator;
        switch (column) {
            case "nisn":
                comparator = Comparator.comparing(SiswaModel::getNisn);
                break;
            case "nama":
                comparator = Comparator.comparing(SiswaModel::getNama);
                break;
            case "jenis_kelamin":
                comparator = Comparator.comparing(SiswaModel::getJns);
                break;
            case "kelas":
                comparator = Comparator.comparing(SiswaModel::getKelas);
                break;
            case "jurusan":
                comparator = Comparator.comparing(SiswaModel::getJurusan);
                break;
            default:
                return;
        }
        data.sort(isAscending ? comparator : comparator.reversed());
        isAscending = !isAscending;
        initTable();
    }

    private void searchStudents(String query) {
        if (originalData == null) return;
        List<SiswaModel> filteredData = new ArrayList<>();

        for (SiswaModel siswa : originalData) {
            String nisn = String.valueOf(siswa.getNisn());
            String nama = siswa.getNama() != null ? siswa.getNama().toLowerCase() : "";
            String jns = siswa.getJns() != null ? siswa.getJns().toLowerCase() : "";
            String kelas = siswa.getKelas() != null ? siswa.getKelas().toLowerCase() : "";
            String jurusan = siswa.getJurusan() != null ? siswa.getJurusan().toLowerCase() : "";

            if (nisn.contains(query) ||
                    nama.contains(query.toLowerCase()) ||
                    jns.contains(query.toLowerCase()) ||
                    kelas.contains(query.toLowerCase()) ||
                    jurusan.contains(query.toLowerCase())) {
                filteredData.add(siswa);
            }
        }

        data = filteredData;
        initTable();
    }


    private TextView createTextView(String text) {
        TextView tv = new TextView(this);
        text = formatTextWithNewLine(text, 20);

        tv.setText(text);
        tv.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        tv.setPadding(16, 8, 16, 8);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(16);
        tv.setBackgroundResource(R.drawable.squared_box);
        tv.setMinimumHeight(120);
        tv.setMaxHeight(120);

        return tv;
    }

    private ImageButton createEditButton(SiswaModel siswa) {
        ImageButton btn = new ImageButton(this);
        btn.setImageResource(R.drawable.edit);
        btn.setBackgroundResource(R.drawable.squared_box);
        btn.setOnClickListener(v -> editDataSiswa(siswa));
        btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        btn.setAdjustViewBounds(true);
        btn.setMinimumHeight(120);
        btn.setMaxHeight(120);

        return btn;
    }

    private ImageButton createDeleteButton(SiswaModel siswa){
        ImageButton btn = new ImageButton(this);
        btn.setImageResource(R.drawable.delete);
        btn.setBackgroundResource(R.drawable.squared_box);
        btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        btn.setAdjustViewBounds(true);
        btn.setMinimumHeight(120);
        btn.setMaxHeight(120);

        btn.setOnClickListener(v -> deleteDataSiswa(siswa));
        return btn;
    }

    private void clearTableExceptHeader() {
        while (stk.getChildCount() > 1) {
            stk.removeViewAt(1);
        }
    }

//    Gunakan fungsi ini jika fitur ubah data harus fetch ulang dari database.
//    private void editDataSiswa(int nisn) {
//        Intent intent = new Intent(DaftarSiswaActivity.this, FormSiswaActivity.class);
//        intent.putExtra("status","edit");
//        intent.putExtra("nisn",nisn);
//        startActivity(intent);
//    }

    private void editDataSiswa(SiswaModel siswa) {
        Intent intent = new Intent(DaftarSiswaActivity.this, FormSiswaActivity.class);
        intent.putExtra("status","edit");
        intent.putExtra("nisn",String.valueOf(siswa.getNisn()));
        intent.putExtra("nama", siswa.getNama());
        intent.putExtra("jenis_kelamin", siswa.getJns());
        intent.putExtra("kelas", siswa.getKelas());
        intent.putExtra("jurusan", siswa.getJurusan());
        startActivity(intent);
    }

    private void deleteDataSiswa(SiswaModel siswa) {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Penghapusan")
                .setMessage("Apakah Anda yakin ingin menghapus data Siswa ini?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    // Konfirmasi kedua
                    new AlertDialog.Builder(this)
                            .setTitle("Konfirmasi Ulang")
                            .setMessage("Penghapusan bersifat permanen. Yakin ingin lanjut?")
                            .setPositiveButton("Ya", (dialog2, which2) -> {
                                Call<ResponseBody> call = apiService.deleteSiswa(siswa.getNisn());

                                call.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if (response.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Data Siswa berhasil dihapus", Toast.LENGTH_SHORT).show();
                                            fetchStudents(currentPage);
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Gagal menghapus data siswa" , Toast.LENGTH_SHORT).show();
                                            try {
                                                Log.e("API_ERROR", "Code: " + response.code() + ", Body: " + response.errorBody().string());
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        Toast.makeText(getApplicationContext(), "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            })
                            .setNegativeButton("Batal", null)
                            .show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchStudents(currentPage);
    }

    private void updatePaginationInfo(int current, int last) {
        String text = getString(R.string.halaman) + " " + current + " " + getString(R.string.dari) + " " + last;
        halaman.setText(text);

        prevButton.setEnabled(current > 1);
        nextButton.setEnabled(current < last);
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
                    Toast.makeText(DaftarSiswaActivity.this, "Gagal mengambil data siswa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SiswaModel>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(DaftarSiswaActivity.this, "Gagal mengambil data siswa", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(DaftarSiswaActivity.this, "PDF Berhasil disimpan di Downloads", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(DaftarSiswaActivity.this, "Gagal membuat PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
