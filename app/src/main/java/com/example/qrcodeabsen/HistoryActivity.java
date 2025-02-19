package com.example.qrcodeabsen;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {
    private final ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
    private TableLayout stk;
    private List<AbsensiModel> data, originalData;

    private boolean isAscending = true;

    private TextView Nisn, Ket, Tanggal, JamMasuk, JamPulang;
    private EditText searchText;
    private Button searchButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        stk = findViewById(R.id.tableAbsensi);
        fetchHistory();

        Nisn = findViewById(R.id.headerNisn);
        Ket = findViewById(R.id.headerKet);
        Tanggal = findViewById(R.id.headerTanggal);
        JamMasuk = findViewById(R.id.headerJamasuk);
        JamPulang = findViewById(R.id.headerJampulang);
        searchButton = findViewById(R.id.searchButton);
        searchText = findViewById(R.id.searchEdit);

        setClickListener();
    }

    private void setClickListener() {
        Nisn.setOnClickListener(v -> history("nisn"));
        Ket.setOnClickListener(v -> history("keterangan"));
        Tanggal.setOnClickListener(v -> history("tanggal"));
        JamMasuk.setOnClickListener(v -> history("jam_masuk"));
        JamPulang.setOnClickListener(v -> history("jam_pulang"));
        searchButton.setOnClickListener(v -> searchHistory(searchText.getText().toString()));
    }

    private void fetchHistory() {
        Call<List<AbsensiModel>> call = apiService.getHistory();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<AbsensiModel>> call, Response<List<AbsensiModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data = response.body();
                    originalData = data;
                    initTable();
                } else {
                    Toast.makeText(HistoryActivity.this, "Gagal memuat data!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AbsensiModel>> call, Throwable t) {
                Log.e("HistoryActivity", "Error: " + t.getMessage());
                Toast.makeText(HistoryActivity.this, "Terjadi kesalahan jaringan!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initTable() {
        clearTableExceptHeader();
        for (int i = 0; i < data.size(); i++) {
            AbsensiModel absensi = data.get(i);
            TableRow tbrow = new TableRow(this);

            tbrow.addView(createTextView(String.valueOf(i + 1)));
            tbrow.addView(createTextView(absensi.getNisn()));
            tbrow.addView(createTextView(absensi.getketerangan()));
            tbrow.addView(createTextView(absensi.getTanggal()));
            tbrow.addView(createTextView(absensi.getJamMasuk()));
            tbrow.addView(createTextView(absensi.getJamPulang()));

            stk.addView(tbrow);
        }
    }

    private void history(String column) {
        if (data == null || data.isEmpty()) return;

        Comparator<AbsensiModel> comparator;
        switch (column) {
            case "nisn":
                comparator = Comparator.comparing(AbsensiModel::getNisn);
                break;
            case "keterangan":
                comparator = Comparator.comparing(AbsensiModel::getketerangan);
                break;
            case "jam_masuk":
                comparator = Comparator.comparing(AbsensiModel::getJamMasuk);
                break;
            case "jam_pulang":
                comparator = Comparator.comparing(AbsensiModel::getJamPulang);
                break;
            case "tanggal":
                comparator = Comparator.comparing(AbsensiModel::getTanggal);
                break;
            default:
                return;
        }

        // Toggle sorting order
        if (isAscending) {
            Collections.sort(data, comparator);
        } else {
            Collections.sort(data, comparator.reversed());

        }
        isAscending = !isAscending; // Ubah status sorting
        initTable(); // Refresh tampilan tabel
    }

    private TextView createTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(16, 8, 16, 8);
        tv.setTextColor(Color.WHITE);
        tv.setBackgroundResource(R.drawable.squared_box);

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        tv.setLayoutParams(params);

        return tv;
    }

    private void clearTableExceptHeader() {
        TableLayout stk = findViewById(R.id.tableAbsensi);

        while (stk.getChildCount() > 1) {
            stk.removeViewAt(1);
        }
    }

    private void searchHistory(String query) {
        if (originalData == null) return; // Jika data belum dimuat, hentikan

        List<AbsensiModel> filteredData = new ArrayList<>();

        for (AbsensiModel absensi : originalData) {
            if (absensi.getNisn().contains(query) ||
                    absensi.getketerangan().toLowerCase().contains(query.toLowerCase()) ||
                    absensi.getJamMasuk().contains(query) ||
                    absensi.getJamPulang().contains(query)) {

                filteredData.add(absensi);
            }
        }
        // Perbarui tampilan tabel dengan hasil filter
        updateTable(filteredData);
    }

    private void updateTable(List<AbsensiModel> data) {
        clearTableExceptHeader();

        for (int i = 0; i < data.size(); i++) {
            AbsensiModel absensi = data.get(i);
            TableRow tbrow = new TableRow(this);

            tbrow.addView(createTextView(String.valueOf(i + 1)));
            tbrow.addView(createTextView(absensi.getNisn()));
            tbrow.addView(createTextView(absensi.getketerangan()));
            tbrow.addView(createTextView(absensi.getTanggal()));
            tbrow.addView(createTextView(absensi.getJamMasuk()));
            tbrow.addView(createTextView(absensi.getJamPulang()));

            stk.addView(tbrow);
        }
    }

}
