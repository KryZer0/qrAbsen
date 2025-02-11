package com.example.qrcodeabsen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Dashboard extends AppCompatActivity {
    private Button Left, Right;
    private FloatingActionButton QrCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Left = findViewById(R.id.btnLeft);
        Right = findViewById(R.id.btnRight);
        QrCode = findViewById(R.id.btnCenter);
        setClickListener();
    }
    private void setClickListener()
    {
        QrCode.setOnClickListener(v -> loginIn());
    }

    private void loginIn()
    {
        Intent intent = new Intent(Dashboard.this,MainActivity.class);
        startActivity(intent);
    }
}
