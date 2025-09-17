package com.example.qrcodeabsen;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetUpActivity extends BaseActivity {
    private EditText url, port;
    private Button btnSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        url = findViewById(R.id.urlip);
        port = findViewById(R.id.port);
        btnSetup = findViewById(R.id.setupbtn);

        url.setText(PreferenceHelper.getBaseUrl());
        port.setText(PreferenceHelper.getBasePort());

        btnSetup.setOnClickListener(v -> setUpUrl());
    }

    private void setUpUrl() {
        String newUrl = url.getText().toString().trim();
        String newPort = port.getText().toString().trim();

        if (TextUtils.isEmpty(newUrl)) {
            Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newPort)) {
            newPort = PreferenceHelper.getBasePort();
        }

        PreferenceHelper.saveServerConfig(newUrl, newPort);
        RetrofitClient.resetInstance(); // supaya Retrofit pakai URL baru di call berikutnya

        Toast.makeText(this, "URL updated to: " + newUrl + ":" + newPort, Toast.LENGTH_SHORT).show();
    }
}
