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
    private Spinner jenis;
    private Button submit;
    private ImageButton qrCodeButton;
    private Toast toastMessage;
    private String status, oldnisn;
    private String[] options = {"Laki-laki", "Perempuan"};

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
        submit = findViewById(R.id.submitformsiswa);
        header = findViewById(R.id.headerTitle);
        qrCodeButton = findViewById(R.id.generateQr);
        status = getIntent().getStringExtra("status");
        if (status.equals("edit")){
            header.setText(R.string.edit_siswa);
            submit.setText(R.string.edit);
            qrCodeButton.setVisibility(View.VISIBLE);
            qrCodeButton.setEnabled(true);
        } else {
            header.setText(R.string.tambah_siswa);
            submit.setText(R.string.tambah);
            qrCodeButton.setVisibility(View.INVISIBLE);
            qrCodeButton.setEnabled(false);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        jenis.setAdapter(adapter);
    }

    private void setClickListener() {
        submit.setOnClickListener(v -> simpanData());
        qrCodeButton.setOnClickListener(v -> generateQr());
    }
    private void simpanData() {
        String nisnText = nisn.getText().toString();
        String nama_siswa = nama.getText().toString();
        String jenis_kelamin = jenis.getSelectedItem().toString();
        String jns;

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
        SiswaModel siswa = new SiswaModel(nisn_siswa, nama_siswa, jns, "");

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
        String number, name, gender, genderChar;

        if (status.equals("edit")){
            number = getIntent().getStringExtra("nisn");
            name = getIntent().getStringExtra("nama");
            gender = getIntent().getStringExtra("jenis_kelamin");
            if (gender.equals("L")){
                genderChar = "Laki-Laki";
            } else {
                genderChar = "Perempuan";
            }
            nisn.setText(number);
            oldnisn = number;
            nama.setText(name);
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) jenis.getAdapter();
            int position = adapter.getPosition(genderChar);
            jenis.setSelection(position);
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
}
