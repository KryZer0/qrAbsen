package com.example.qrcodeabsen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


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
        history.setOnClickListener(v -> history());
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

    private void history()
    {
        Intent intent = new Intent(Dashboard.this,HistoryActivity.class);
        startActivity(intent);
    }
}
