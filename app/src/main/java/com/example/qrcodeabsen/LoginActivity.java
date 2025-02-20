package com.example.qrcodeabsen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity
{
    private EditText Username, Password;
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

        setClickListener();
    }

    private void setClickListener()
    {
        btnLogin.setOnClickListener(v -> loginIn());
        logoButton.setOnClickListener(v -> setUpUrl());
    }

    private void loginIn()
    {
        Intent intent = new Intent(LoginActivity.this,Dashboard.class);
        startActivity(intent);
    }

    private void setUpUrl() {
        if (setUpCounter == 1) {
            Intent intent = new Intent(LoginActivity.this, SetUpActivity.class);
            startActivity(intent);
        } else {
            setUpCounter--;

            // Hapus Toast sebelumnya jika masih ada
            if (toastMessage != null) {
                toastMessage.cancel();
            }

            // Buat Toast baru dan simpan referensinya
            toastMessage = Toast.makeText(LoginActivity.this, setUpCounter + " kali lagi klik untuk set up", Toast.LENGTH_SHORT);
            toastMessage.show();
        }
    }
}

