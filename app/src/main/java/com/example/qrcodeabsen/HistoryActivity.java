package com.example.qrcodeabsen;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends BaseActivity {
    private final ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
    private TableLayout stk;
    private List<AbsensiModel> data, originalData;

    private boolean isAscending = true;

    private TextView Nisn, Ket, Tanggal, JamMasuk, JamPulang;
    private EditText searchText;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Tidak digunakan
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHistory(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Tidak digunakan
            }
        });

    }

    private void fetchHistory() {
        Call<List<AbsensiModel>> call = apiService.getHistory();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<AbsensiModel>> call, @NonNull Response<List<AbsensiModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data = response.body();
                    originalData = data;
                    initTable();
                } else {
                    Toast.makeText(HistoryActivity.this, "Gagal memuat data!", Toast.
                            LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AbsensiModel>> call, @NonNull Throwable t) {
                Log.e("HistoryActivity", "Error: " + t.getMessage());
                Toast.makeText(HistoryActivity.this, "Terjadi kesalahan jaringan!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initTable() {
        clearTableExceptHeader();
        for (int i = 0; i < data.size(); i++) {
            AbsensiModel absensi = data.get(i);
            TableRow tbrow = new TableRow(this);

            tbrow.addView(createTextView(String.valueOf(i + 1)));
            tbrow.addView(createTextView(absensi.getNama()));
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
                comparator = Comparator.comparing(AbsensiModel::getNama);
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
            data.sort(comparator);
        } else {
            data.sort(comparator.reversed());

        }
        isAscending = !isAscending;
        initTable();
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
        if (originalData == null) return;

        List<AbsensiModel> filteredData = new ArrayList<>();

        // Parsing query
        String[] parts = query.split("=");
        if (parts[2].contains("\"")) {
            parts[2].replace("\"", "");
        }
        if (parts.length != 2) {
            for (AbsensiModel absensi : originalData) {
                if (absensi.getNisn().contains(query) ||
                        absensi.getketerangan().toLowerCase().contains(query.toLowerCase()) ||
                        absensi.getJamMasuk().contains(query) ||
                        absensi.getJamPulang().contains(query) ||
                        absensi.getNama().contains(query)){

                    filteredData.add(absensi);
                }
            }
            updateTable(filteredData);
            return;
        }

        String key = parts[0].trim().toLowerCase();
        String value = parts[1].trim();

        for (AbsensiModel absensi : originalData) {
            switch (key) {
                case "nisn":
                    if (absensi.getNisn().contains(value)) {
                        filteredData.add(absensi);
                    }
                    break;
                case "keterangan":
                    if (absensi.getketerangan().toLowerCase().contains(value.toLowerCase())) {
                        filteredData.add(absensi);
                    }
                    break;
                case "jammasuk":
                    if (absensi.getJamMasuk().contains(value)) {
                        filteredData.add(absensi);
                    }
                    break;
                case "jampulang":
                    if (absensi.getJamPulang().contains(value)) {
                        filteredData.add(absensi);
                    }
                    break;
                case "nama":
                    if (absensi.getNama().contains(value)){
                        filteredData.add(absensi);
                    }
                default:
                    // Jika key tidak dikenali, kembalikan semua data atau tampilkan pesan error
                    updateTable(originalData);
                    return;
            }
        }
        updateTable(filteredData);
    }

    private void updateTable(List<AbsensiModel> data) {
        clearTableExceptHeader();

        for (int i = 0; i < data.size(); i++) {
            AbsensiModel absensi = data.get(i);
            TableRow tbrow = new TableRow(this);

            tbrow.addView(createTextView(String.valueOf(i + 1)));
            tbrow.addView(createTextView(absensi.getNama()));
            tbrow.addView(createTextView(absensi.getketerangan()));
            tbrow.addView(createTextView(absensi.getTanggal()));
            tbrow.addView(createTextView(absensi.getJamMasuk()));
            tbrow.addView(createTextView(absensi.getJamPulang()));

            stk.addView(tbrow);
        }
    }

}
