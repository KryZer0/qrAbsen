package com.example.qrcodeabsen;

import static com.example.qrcodeabsen.ApiUtils.formatTextWithNewLine;
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

import java.util.ArrayList;
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

    private TextView Nisn, Ket, Tanggal, JamMasuk, JamPulang, pageInfo;
    private EditText searchText;
    private Button searchButton, prevPage, nextPage;
    private int currentPage = 1;
    private int perPage = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        stk = findViewById(R.id.tableAbsensi);
        fetchHistory(currentPage);

        Nisn = findViewById(R.id.headerNisn);
        Ket = findViewById(R.id.headerKet);
        Tanggal = findViewById(R.id.headerTanggal);
        JamMasuk = findViewById(R.id.headerJamasuk);
        JamPulang = findViewById(R.id.headerJampulang);
        searchButton = findViewById(R.id.searchButton);
        searchText = findViewById(R.id.searchEdit);
        prevPage = findViewById(R.id.prevpage);
        nextPage = findViewById(R.id.nextpage);
        pageInfo = findViewById(R.id.pageinfo);


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

        nextPage.setOnClickListener(v -> {
            currentPage++;
            fetchHistory(currentPage);
        });

        prevPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                fetchHistory(currentPage);
            }
        });
    }

    private void fetchHistory(int page) {
        Call<PaginatedResponse<AbsensiModel>> call = apiService.getHistory(page, perPage);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse<AbsensiModel>> call, @NonNull Response<PaginatedResponse<AbsensiModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data = response.body().getData();
                    originalData = data;
                    initTable();
                    updatePaginationInfo(response.body().getCurrentPage(), response.body().getLastPage());
                } else {
                    Toast.makeText(HistoryActivity.this, "Gagal memuat data!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedResponse<AbsensiModel>> call, @NonNull Throwable t) {
                Log.e("HistoryActivity", "Error: " + t.getMessage());
                Toast.makeText(HistoryActivity.this, "Terjadi kesalahan jaringan!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePaginationInfo(int current, int last) {
        String text = getString(R.string.halaman) + " " + current + " " + getString(R.string.dari) + " " + last;
        pageInfo.setText(text);

        prevPage.setEnabled(current > 1);
        nextPage.setEnabled(current < last);
    }


    private void initTable() {
        clearTableExceptHeader();
        for (int i = 0; i < data.size(); i++) {
            AbsensiModel absensi = data.get(i);
            TableRow tbrow = new TableRow(this);
            String masuk = null;
            String pulang = null;
            if (!(absensi.getJamMasuk() == null)) {
                masuk = absensi.getJamMasuk();
            } else {
                masuk = "-";
            }

            if (!(absensi.getJamPulang() == null)){
                pulang = absensi.getJamPulang();
            } else {
                pulang = "-";
            }

            tbrow.addView(createTextView(String.valueOf(i + 1)));
            tbrow.addView(createTextView(absensi.getNama()));
            tbrow.addView(createTextView(absensi.getketerangan()));
            tbrow.addView(createTextView(absensi.getTanggal()));
            tbrow.addView(createTextView(masuk));
            tbrow.addView(createTextView(pulang));

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
        text = formatTextWithNewLine(text, 20);

        tv.setText(text);
        tv.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        tv.setPadding(16, 8, 16, 8);
        tv.setTextColor(Color.WHITE);
        tv.setBackgroundResource(R.drawable.squared_box);
        tv.setMinimumHeight(120);
        tv.setMaxHeight(120);
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

        String[] conditions = query.toLowerCase().split("&");

        for (AbsensiModel absensi : originalData) {
            boolean allMatch = true;

            for (String condition : conditions) {
                condition = condition.trim();

                boolean match = absensi.getketerangan().toLowerCase().contains(condition) ||
                        absensi.getNisn().contains(condition) ||
                        absensi.getJamMasuk().contains(condition) ||
                        absensi.getJamPulang().contains(condition) ||
                        absensi.getNama().toLowerCase().contains(condition) ||
                        absensi.getTanggal().contains(condition);
                if (!match) {
                    allMatch = false;
                    break;
                }
            }

            if (allMatch) {
                filteredData.add(absensi);
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
