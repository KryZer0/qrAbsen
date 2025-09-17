package com.example.qrcodeabsen;

import static com.example.qrcodeabsen.ApiUtils.formatTextWithNewLine;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DaftarGuruActivity extends BaseActivity {
    private final ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
    private TableLayout stk;
    private List<Map<String, Object>> guruData;
    private List<Map<String, Object>> originalGuruData;
    private boolean isAscending = true;

    private TextView nama, email, edit, halaman, header;
    private EditText searchText;
    private Button searchButton, prevButton, nextButton, tambah;

    private int currentPage = 1;
    private int perPage = 10;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_guru);
        stk = findViewById(R.id.tableSiswa);
        status = getIntent().getStringExtra("status");
        setViewById();
        setClickListener();
        fetchGuru(currentPage);
    }

    private void setViewById() {
        nama = findViewById(R.id.headerNama);
        email = findViewById(R.id.headerEmail);
        edit = findViewById(R.id.headerEdit);
        searchButton = findViewById(R.id.searchButton);
        searchText = findViewById(R.id.searchEdit);
        halaman = findViewById(R.id.pageinfo);
        prevButton = findViewById(R.id.prevpage);
        nextButton = findViewById(R.id.nextpage);
        header = findViewById(R.id.headerTitle);
        tambah = findViewById(R.id.addButton);
        header.setText("Data Guru");
    }

    private void setClickListener() {
        nama.setOnClickListener(v -> sortData("name"));
        email.setOnClickListener(v -> sortData("email"));
        searchButton.setOnClickListener(v -> searchGuru(searchText.getText().toString()));
        searchText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchGuru(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        nextButton.setOnClickListener(v -> {
            currentPage++;
            fetchGuru(currentPage);
        });

        prevButton.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                fetchGuru(currentPage);
            }
        });
        tambah.setOnClickListener(v -> tambahGuru());
    }

    private void fetchGuru(int page) {
        Call<PaginatedResponse<Map<String, Object>>> call = apiService.fetchGuru(page, perPage);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PaginatedResponse<Map<String, Object>>> call, Response<PaginatedResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    guruData = response.body().getData();
                    originalGuruData = new ArrayList<>(guruData);
                    initTable(guruData);
                    updatePaginationInfo(response.body().getCurrentPage(), response.body().getLastPage());
                } else {
                    Toast.makeText(DaftarGuruActivity.this, "Gagal memuat data guru!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(DaftarGuruActivity.this, "Terjadi kesalahan jaringan!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initTable(List<Map<String, Object>> dataList) {
        clearTableExceptHeader();

        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> guru = dataList.get(i);
            TableRow tbrow = new TableRow(this);
            tbrow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            String name = String.valueOf(guru.get("name"));
            String email = String.valueOf(guru.get("email"));
            String role = String.valueOf(guru.get("id_role"));

            tbrow.addView(createTextView(String.valueOf(i + 1)));
            tbrow.addView(createTextView(name));
            tbrow.addView(createTextView(email));
            tbrow.addView(createEditButton(guru));
            tbrow.addView(createDeleteButton(guru));
            stk.addView(tbrow);
        }
    }

    private void sortData(String key) {
        if (guruData == null || guruData.isEmpty()) return;

        guruData.sort((a, b) -> {
            String valA = String.valueOf(a.get(key)).toLowerCase();
            String valB = String.valueOf(b.get(key)).toLowerCase();
            return isAscending ? valA.compareTo(valB) : valB.compareTo(valA);
        });

        isAscending = !isAscending;
        initTable(guruData);
    }

    private void searchGuru(String query) {
        if (originalGuruData == null) return;
        List<Map<String, Object>> filtered = new ArrayList<>();

        for (Map<String, Object> guru : originalGuruData) {
            String name = String.valueOf(guru.get("name"));
            String email = String.valueOf(guru.get("email"));
            if (name.toLowerCase().contains(query.toLowerCase()) ||
                    email.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(guru);
            }
        }

        guruData = filtered;
        initTable(guruData);
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

    private void clearTableExceptHeader() {
        while (stk.getChildCount() > 1) {
            stk.removeViewAt(1);
        }
    }

    private void updatePaginationInfo(int current, int last) {
        String text = getString(R.string.halaman) + " " + current + " " + getString(R.string.dari) + " " + last;
        halaman.setText(text);
        prevButton.setEnabled(current > 1);
        nextButton.setEnabled(current < last);
    }
    private ImageButton createEditButton(Map<String, Object> guru) {
        ImageButton btn = new ImageButton(this);
        btn.setImageResource(R.drawable.edit); // Icon edit
        btn.setBackgroundResource(R.drawable.squared_box);
        btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        btn.setAdjustViewBounds(true);
        btn.setMinimumHeight(120);
        btn.setMaxHeight(120);

        btn.setOnClickListener(v -> editDataGuru(guru));
        return btn;
    }

    private ImageButton createDeleteButton(Map <String, Object> guru){
        ImageButton btn = new ImageButton(this);
        btn.setImageResource(R.drawable.delete); // Icon edit
        btn.setBackgroundResource(R.drawable.squared_box);
        btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        btn.setAdjustViewBounds(true);
        btn.setMinimumHeight(120);
        btn.setMaxHeight(120);

        btn.setOnClickListener(v -> deleteDataGuru(guru));
        return btn;
    }

    private void tambahGuru(){
        Intent intent = new Intent(DaftarGuruActivity.this, FormGuruActivity.class);
        intent.putExtra("status", "tambah");
        startActivity(intent);
    }

    private void editDataGuru(Map<String, Object> guru) {
        Intent intent = new Intent(DaftarGuruActivity.this, FormGuruActivity.class);
        intent.putExtra("status", "edit");
        intent.putExtra("id", String.valueOf(((Double) Objects.requireNonNull(guru.get("id"))).intValue()));
        intent.putExtra("name", String.valueOf(guru.get("name")));
        intent.putExtra("email", String.valueOf(guru.get("email")));
        intent.putExtra("id_role", String.valueOf(guru.get("id_role")));
        startActivity(intent);
    }

    private void deleteDataGuru(Map<String, Object> guru) {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Penghapusan")
                .setMessage("Apakah Anda yakin ingin menghapus data guru ini?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    // Konfirmasi kedua
                    new AlertDialog.Builder(this)
                            .setTitle("Konfirmasi Ulang")
                            .setMessage("Penghapusan bersifat permanen. Yakin ingin lanjut?")
                            .setPositiveButton("Ya", (dialog2, which2) -> {
                                String id = String.valueOf(guru.get("id"));
                                Call<ResponseBody> call = apiService.deleteGuru(id);

                                call.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if (response.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Data guru berhasil dihapus", Toast.LENGTH_SHORT).show();
                                            fetchGuru(currentPage);
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Gagal menghapus data guru", Toast.LENGTH_SHORT).show();
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
        fetchGuru(currentPage);
    }
}

