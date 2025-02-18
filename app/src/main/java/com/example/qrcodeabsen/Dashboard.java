package com.example.qrcodeabsen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Dashboard extends AppCompatActivity {
    private Button checkIn, checkOut, history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        checkIn = findViewById(R.id.check_in_btn);
        checkOut = findViewById(R.id.check_out_btn);
        history = findViewById(R.id.history_btn);
        setClickListener();
    }
    private void setClickListener()
    {
        checkIn.setOnClickListener(v -> checkIn());
        checkOut.setOnClickListener(v -> checkOut());
    }

    private void checkIn()
    {
        Intent intent = new Intent(Dashboard.this,MainActivity.class);
        intent.putExtra("absen","checkin");
        startActivity(intent);
    }

    private void checkOut()
    {
        Intent intent = new Intent(Dashboard.this,MainActivity.class);
        intent.putExtra("absen","checkout");
        startActivity(intent);
    }
}
