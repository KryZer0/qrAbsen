package com.example.qrcodeabsen;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import java.util.Comparator;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DaftarSiswaActivity extends BaseActivity {
    private final ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
    private TableLayout stk;
    private List<SiswaModel> data;
    private List<SiswaModel> originalData;
    private boolean isAscending = true;
    private TextView nisn, nama, jenis_kelamin, edit;
    private EditText searchText;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_siswa);
        stk = findViewById(R.id.tableSiswa);
        fetchStudents();
        setViewById();
        setClickListener();
    }

    private void setViewById() {
        nisn = findViewById(R.id.headerNisn);
        nama = findViewById(R.id.headerNama);
        jenis_kelamin = findViewById(R.id.headerJenis);
        edit = findViewById(R.id.headerEdit);
        searchButton = findViewById(R.id.searchButton);
        searchText = findViewById(R.id.searchEdit);
    }

    private void setClickListener() {
        nisn.setOnClickListener(v -> sortData("nisn"));
        nama.setOnClickListener(v -> sortData("nama"));
        jenis_kelamin.setOnClickListener(v -> sortData("jenis_kelamin"));
        searchButton.setOnClickListener(v -> searchStudents(searchText.getText().toString()));
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
    }

    private void fetchStudents() {
        Call<List<SiswaModel>> call = apiService.fetchSiswa();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<SiswaModel>> call, Response<List<SiswaModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data = response.body();
                    originalData = data;
                    initTable();
                } else {
                    Toast.makeText(DaftarSiswaActivity.this, "Gagal memuat data!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SiswaModel>> call, Throwable t) {
                Toast.makeText(DaftarSiswaActivity.this, "Terjadi kesalahan jaringan!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initTable() {
        clearTableExceptHeader();
        for (int i = 0; i < data.size(); i++) {
            SiswaModel siswa = data.get(i);
            TableRow tbrow = new TableRow(this);
            tbrow.addView(createTextView(String.valueOf(i + 1)));
            tbrow.addView(createTextView(String.valueOf(siswa.getNisn())));
            tbrow.addView(createTextView(siswa.getNama()));
            tbrow.addView(createTextView(siswa.getJns()));
            tbrow.addView(createEditButton(siswa));
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
            if (String.valueOf(siswa.getNisn()).contains(query) ||
                    siswa.getNama().toLowerCase().contains(query.toLowerCase()) ||
                    siswa.getJns().toLowerCase().contains(query.toLowerCase())) {
                filteredData.add(siswa);
            }
        }
        data = filteredData;
        initTable();
    }

    private TextView createTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(16, 8, 16, 8);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(22);
        tv.setBackgroundResource(R.drawable.squared_box);
        return tv;
    }

    private ImageButton createEditButton(SiswaModel siswa) {
        ImageButton btn = new ImageButton(this);
        btn.setImageResource(R.drawable.edit);
        btn.setBackgroundResource(R.drawable.squared_box);
        btn.setPadding(16, 8, 16, 8);
        btn.setOnClickListener(v -> editDataSiswa(siswa));
        btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        btn.setAdjustViewBounds(true);

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
        startActivity(intent);
    }
    @Override
    protected void onResume() {
        super.onResume();
        fetchStudents();
    }

}
