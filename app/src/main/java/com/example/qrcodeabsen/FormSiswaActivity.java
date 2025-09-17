package com.example.qrcodeabsen;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormSiswaActivity extends BaseActivity{
    private final ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
    private TextView header;
    private EditText nisn, nama;
    private Spinner jenis, kelas, jurusan;
    private Button submit, file;
    private Toast toastMessage;
    private String status, oldnisn;
    private String[] options = {"Laki-laki", "Perempuan"};
    private String[] kelasOptions = {"X", "XI", "XII"};
    private String[] jurusanOptions = {
            "Teknik Kendaraan Ringan",
            "Teknik Mesin Industri",
            "Administrasi Perkantoran"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_siswa);
        setViewById();
        setClickListener();
        statusForm();
    }
    private void setViewById() {
        nisn = findViewById(R.id.nisn);
        nama = findViewById(R.id.nama);
        jenis = findViewById(R.id.jns);
        kelas = findViewById(R.id.kelas);
        jurusan = findViewById(R.id.jurusan);
        submit = findViewById(R.id.submitformsiswa);
        header = findViewById(R.id.headerTitle);
        file = findViewById(R.id.filecsv);
        status = getIntent().getStringExtra("status");

        if (status.equals("edit")){
            header.setText(R.string.edit_siswa);
            submit.setText(R.string.edit);
            file.setVisibility(View.GONE);
            file.setEnabled(false);
        } else {
            header.setText(R.string.tambah_siswa);
            submit.setText(R.string.tambah);
            file.setVisibility(View.VISIBLE);
            file.setEnabled(true);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        jenis.setAdapter(adapter);

        ArrayAdapter<String> adapterKelas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kelasOptions);
        adapterKelas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kelas.setAdapter(adapterKelas);

        ArrayAdapter<String> adapterJurusan = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, jurusanOptions);
        adapterJurusan.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jurusan.setAdapter(adapterJurusan);

    }

    private void setClickListener() {
        submit.setOnClickListener(v -> simpanData());
        file.setOnClickListener(v -> CsvImport());
    }
    private void simpanData() {
        String nisnText = nisn.getText().toString();
        String nama_siswa = nama.getText().toString();
        String jenis_kelamin = jenis.getSelectedItem().toString();
        String jns;
        String kls = kelas.getSelectedItem().toString();
        String jrs = jurusan.getSelectedItem().toString();

        if (nisnText.isEmpty() || nama_siswa.isEmpty()) {
            if (toastMessage != null) {
                toastMessage.cancel();
            }
            toastMessage = Toast.makeText(this, "NISN dan Nama tidak boleh kosong!", Toast.LENGTH_SHORT);
            toastMessage.show();
            return;
        }

        if (jenis_kelamin.equals("Laki-laki")){
            jns = "L";
        } else {
            jns = "P";
        }

        int nisn_siswa = Integer.parseInt(nisnText);
        SiswaModel siswa = new SiswaModel(nisn_siswa, nama_siswa, jns, kls, jrs, "");

        Call<ResponseBody> call;
        if (status.contains("edit")) {
            call = apiService.updateSiswa(oldnisn,siswa);
        } else {
            call = apiService.tambahSiswa(siswa);
        }

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (toastMessage != null) {
                        toastMessage.cancel();
                    }
                    toastMessage = Toast.makeText(FormSiswaActivity.this, "Data berhasil disimpan", Toast.LENGTH_SHORT);
                    toastMessage.show();
                    finish();
                } else {
                    String errorMessage = ApiUtils.getErrorMessage(response.errorBody());
                    if (toastMessage != null) {
                        toastMessage.cancel();
                    }
                    toastMessage = Toast.makeText(FormSiswaActivity.this, errorMessage, Toast.LENGTH_SHORT);
                    toastMessage.show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (toastMessage != null) {
                    toastMessage.cancel();
                }
                if (t instanceof IOException) {
                    toastMessage = Toast.makeText(FormSiswaActivity.this, "No Internet Connection", Toast.LENGTH_SHORT);
                } else {
                    toastMessage = Toast.makeText(FormSiswaActivity.this, "Server Error", Toast.LENGTH_SHORT);
                }
                toastMessage.show();
            }
        });
    }

    private void statusForm() {
        String number, name, gender, genderChar, kelasIntent, jurusanIntent;

        if (status.equals("edit")) {
            number = getIntent().getStringExtra("nisn");
            name = getIntent().getStringExtra("nama");
            gender = getIntent().getStringExtra("jenis_kelamin");
            kelasIntent = getIntent().getStringExtra("kelas");
            jurusanIntent = getIntent().getStringExtra("jurusan");

            // Konversi kode gender ke string
            if (gender.equals("L")) {
                genderChar = "Laki-laki";
            } else {
                genderChar = "Perempuan";
            }

            nisn.setText(number);
            oldnisn = number;
            nama.setText(name);

            // Set spinner jenis kelamin
            ArrayAdapter<String> adapterJenis = (ArrayAdapter<String>) jenis.getAdapter();
            int positionJenis = adapterJenis.getPosition(genderChar);
            jenis.setSelection(positionJenis);

            // Set spinner kelas
            ArrayAdapter<String> adapterKelas = (ArrayAdapter<String>) kelas.getAdapter();
            int positionKelas = adapterKelas.getPosition(kelasIntent);
            kelas.setSelection(positionKelas);

            // Set spinner jurusan
            ArrayAdapter<String> adapterJurusan = (ArrayAdapter<String>) jurusan.getAdapter();
            int positionJurusan = adapterJurusan.getPosition(jurusanIntent);
            jurusan.setSelection(positionJurusan);
        }
    }

    private void generateQr() {
        Call<ResponseBody> call;
        call = apiService.generateQr(oldnisn);


        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (toastMessage != null) {
                        toastMessage.cancel();
                    }
                    String successMessage = ApiUtils.getSuccessMessage(response.body());
                    toastMessage = Toast.makeText(FormSiswaActivity.this, successMessage, Toast.LENGTH_SHORT);
                    toastMessage.show();
                    finish();
                } else {
                    String errorMessage = ApiUtils.getErrorMessage(response.errorBody());
                    if (toastMessage != null) {
                        toastMessage.cancel();
                    }
                    toastMessage = Toast.makeText(FormSiswaActivity.this, errorMessage, Toast.LENGTH_SHORT);
                    toastMessage.show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (toastMessage != null) {
                    toastMessage.cancel();
                }
                if (t instanceof IOException) {
                    toastMessage = Toast.makeText(FormSiswaActivity.this, "No Internet Connection", Toast.LENGTH_SHORT);
                } else {
                    toastMessage = Toast.makeText(FormSiswaActivity.this, "Server Error", Toast.LENGTH_SHORT);
                }
                toastMessage.show();
            }
        });
    }

    private void CsvImport() {
        Intent intent = new Intent(FormSiswaActivity.this, CsvImportActivity.class);
        startActivity(intent);
    }
}
