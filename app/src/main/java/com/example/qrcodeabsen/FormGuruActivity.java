package com.example.qrcodeabsen;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormGuruActivity extends BaseActivity {

    private final ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
    private TextView header;
    private EditText nama, email, id;
    private String status, idGuru;
    private Button submit, resetPass;
    private Toast toastMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_guru);
        status = getIntent().getStringExtra("status");
        setViewById();
        setClickListener();
        statusForm();
    }

    private void setViewById() {
        id = findViewById(R.id.id_guru);
        nama = findViewById(R.id.nama_guru);
        email = findViewById(R.id.email_guru);
        header = findViewById(R.id.headerTitle);
        submit = findViewById(R.id.submitformguru);
        resetPass = findViewById(R.id.resetPassword);

        if (status.equals("edit")) {
            header.setText("Edit Guru");
            submit.setText("Edit");
            resetPass.setVisibility(View.VISIBLE);
        } else {
            header.setText("Tambah Guru");
            submit.setText("Tambah");
            resetPass.setVisibility(View.GONE);
        }
    }

    private void setClickListener() {
        submit.setOnClickListener(v -> simpanData());
        resetPass.setOnClickListener(v -> resetPassword());
    }
    private void statusForm() {
        if (status.equals("edit")) {
            String nama_guru = getIntent().getStringExtra("name");
            String email_guru = getIntent().getStringExtra("email");
            String id_guru = getIntent().getStringExtra("id");

            idGuru = id_guru;

            nama.setText(nama_guru);
            email.setText(email_guru);
            id.setText(id_guru);
        }
    }

    private void simpanData() {
        String id_Guru = id.getText().toString().trim();
        String namaGuru = nama.getText().toString().trim();
        String emailGuru = email.getText().toString().trim();

        if (namaGuru.isEmpty() || emailGuru.isEmpty()) {
            if (toastMessage != null) {
                toastMessage.cancel();
            }
            toastMessage = Toast.makeText(this, "Nama dan Email tidak boleh kosong!", Toast.LENGTH_SHORT);
            toastMessage.show();
            return;
        }

        if (namaGuru.matches(".*\\d+.*")) {
            if (toastMessage != null) {
                toastMessage.cancel();
            }
            toastMessage = Toast.makeText(this, "Nama guru tidak boleh mengandung angka", Toast.LENGTH_SHORT);
            toastMessage.show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailGuru).matches()) {
            if (toastMessage != null) {
                toastMessage.cancel();
            }
            toastMessage = Toast.makeText(this, "Email guru tidak valid", Toast.LENGTH_SHORT);
            toastMessage.show();
            return;
        }

        Call<ResponseBody> call;
        if (status.contains("edit")) {
            call = apiService.updateGuru(idGuru, id_Guru, namaGuru, emailGuru);
        } else {
            call = apiService.tambahGuru(id_Guru, namaGuru, emailGuru);
        }

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (toastMessage != null) {
                        toastMessage.cancel();
                    }
                    toastMessage = Toast.makeText(FormGuruActivity.this, "Data guru berhasil disimpan", Toast.LENGTH_SHORT);
                    toastMessage.show();
                    finish();
                } else {
                    String errorMessage = ApiUtils.getErrorMessage(response.errorBody());
                    if (toastMessage != null) {
                        toastMessage.cancel();
                    }
                    toastMessage = Toast.makeText(FormGuruActivity.this, errorMessage, Toast.LENGTH_SHORT);
                    toastMessage.show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (toastMessage != null) {
                    toastMessage.cancel();
                }
                if (t instanceof IOException) {
                    toastMessage = Toast.makeText(FormGuruActivity.this, "Tidak ada koneksi internet", Toast.LENGTH_SHORT);
                } else {
                    toastMessage = Toast.makeText(FormGuruActivity.this, "Terjadi kesalahan server", Toast.LENGTH_SHORT);
                }
                toastMessage.show();
            }
        });
    }

    private void resetPassword() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Reset Password")
                .setMessage("Pastikan Guru mengetahui reset password. Yakin ingin lanjut? \n Default password = password")
                .setPositiveButton("Ya", (dialog2, which2) -> {
                    String id = String.valueOf(idGuru);
                    Call<ResponseBody> call = apiService.resetPassword(id);

                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "password berhasil direset", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "password Gagal direset", Toast.LENGTH_SHORT).show();
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
    }
}
