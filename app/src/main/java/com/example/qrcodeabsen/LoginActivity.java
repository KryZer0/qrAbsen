package com.example.qrcodeabsen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity
{
    private EditText Username, Password;
    ImageView showHidePassword;
    private TextView lupaButton;
    private Button btnLogin;
    private ImageView logoButton;
    private Toast toastMessage;
    private int setUpCounter = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Username = findViewById(R.id.username);
        Password = findViewById(R.id.password);
        btnLogin = findViewById(R.id.loginButton);
        logoButton = findViewById(R.id.logo);
        lupaButton = findViewById(R.id.lupa);
        showHidePassword = findViewById(R.id.showHidePassword);

        setClickListener();
    }

    private void setClickListener()
    {
        btnLogin.setOnClickListener(v -> loginIn());
        logoButton.setOnClickListener(v -> setUpUrl());
        lupaButton.setOnClickListener(v ->lupaPassword());

        showHidePassword.setOnClickListener(v -> {
            if (Password.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                // show password
                Password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                showHidePassword.setImageResource(R.drawable.visible);
            } else {
                // hide password
                Password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showHidePassword.setImageResource(R.drawable.invisible);
            }
            Password.setSelection(Password.getText().length());
        });
    }

    private void loginIn() {
        String email = Username.getText().toString().trim();
        String pass = Password.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Email dan Password wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(email, pass);
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);

        apiService.loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String nama = response.body().getNama();
                    int roleId = Integer.parseInt(response.body().getIdRole());
                    int idUser = Integer.parseInt(response.body().getIdUser());
                    String role;

                    if (roleId == 1){
                        role = "admin";
                    } else {
                        role = "guru";
                    }

                    SharedPreferences sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    sharedPref.edit()
                            .putInt("id", idUser)
                            .putString("nama", nama)
                            .putString("role", role)
                            .apply();

                    Toast.makeText(LoginActivity.this, "Login sukses sebagai " + nama, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, Dashboard.class);
                    startActivity(intent);
                    finishAffinity();
                } else {
                    Toast.makeText(LoginActivity.this, "Email atau password salah : " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Gagal koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void setUpUrl() {
        if (setUpCounter == 1) {
            Intent intent = new Intent(LoginActivity.this, SetUpActivity.class);
            startActivity(intent);
        } else {
            setUpCounter--;

            if (toastMessage != null) {
                toastMessage.cancel();
            }

            toastMessage = Toast.makeText(LoginActivity.this, setUpCounter + " kali lagi klik untuk set up", Toast.LENGTH_SHORT);
            toastMessage.show();
        }
    }

    private void lupaPassword () {
        toastMessage = Toast.makeText(LoginActivity.this, "Minta Admin/Kesiswaan \n Untuk direset password", Toast.LENGTH_SHORT);
        toastMessage.show();
    }
}

