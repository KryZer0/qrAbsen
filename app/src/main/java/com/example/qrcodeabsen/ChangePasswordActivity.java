package com.example.qrcodeabsen;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText currentPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ganti_password);

        currentPasswordEditText = findViewById(R.id.nisn);
        newPasswordEditText = findViewById(R.id.pass1);
        confirmPasswordEditText = findViewById(R.id.pass2);
        submitButton = findViewById(R.id.submitformsiswa);

        submitButton.setOnClickListener(view -> changePassword());
    }

    private void changePassword() {
        String currentPass = currentPasswordEditText.getText().toString().trim();
        String newPass = newPasswordEditText.getText().toString().trim();
        String confirmPass = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(currentPass) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(ChangePasswordActivity.this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 8) {
            Toast.makeText(ChangePasswordActivity.this, "Password baru minimal 8 karakter", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(ChangePasswordActivity.this, "Password baru dan konfirmasi tidak cocok", Toast.LENGTH_SHORT).show();
            return;
        }

        sendChangePasswordRequest(currentPass, newPass);
    }

    private void sendChangePasswordRequest(String currentPassword, String newPassword) {
        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Semua kolom password harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = sharedPref.getInt("id", -1);

        if (userId == -1) {
            Toast.makeText(this, "ID pengguna tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("id", String.valueOf(userId));
        requestBody.put("current_password", currentPassword);
        requestBody.put("new_password", newPassword);
        requestBody.put("new_password_confirmation", newPassword);

        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);

        apiService.changePassword(requestBody).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePasswordActivity.this, "Password berhasil diubah", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Gagal ubah password: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
