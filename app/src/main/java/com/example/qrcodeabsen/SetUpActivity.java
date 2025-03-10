package com.example.qrcodeabsen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/*  Kelas ini digunakan untuk SetUp Server. Pada masa pengembangan
*   Local server digunakan, dengan Ip address yang berada di kelas urlConstant
*   Mempermudah pengembang agar tidak perlu mengganti Ip address setiap kali
*   sistem mengubah local IP address.
*/

public class SetUpActivity extends BaseActivity
{
    private EditText url, port;
    private Button btnSetup;
    SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "UrlPrefs";
    private static final String KEY_BASE_URL = "BASE_URL";
    private static final String KEY_BASE_PORT = "BASE_PORT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        url = findViewById(R.id.urlip);
        port = findViewById(R.id.port);
        btnSetup = findViewById(R.id.setupbtn);
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        if (sharedPreferences.contains(KEY_BASE_URL) && sharedPreferences.contains(KEY_BASE_PORT)) {
            urlConstant.BASE_URL = sharedPreferences.getString(KEY_BASE_URL, urlConstant.BASE_URL);
            urlConstant.BASE_PORT = sharedPreferences.getString(KEY_BASE_PORT, urlConstant.BASE_PORT);
        }
        url.setText(urlConstant.BASE_URL);
        port.setText(urlConstant.BASE_PORT);
        setClickListener();
    }

    private void setClickListener()
    {
        btnSetup.setOnClickListener(v -> setUpUrl());
    }

    private void setUpUrl()
    {
        String newUrl = url.getText().toString().trim();
        String newPort = port.getText().toString().trim();
        if (!TextUtils.isEmpty(newUrl)) {
            urlConstant.BASE_URL = newUrl;
            // Simpan ke SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_BASE_URL, newUrl);
            editor.remove(KEY_BASE_PORT);

            if (!TextUtils.isEmpty(newPort)){
                urlConstant.BASE_PORT = newPort;
                editor.putString(KEY_BASE_PORT, newPort);
                Toast.makeText(this, "URL updated to: " + newUrl + ":" + newPort + "/", Toast.LENGTH_SHORT).show();
                port.setText(urlConstant.BASE_PORT);
            }
            editor.apply();

            Toast.makeText(this, "URL updated to: " + newUrl, Toast.LENGTH_SHORT).show();
            url.setText(urlConstant.BASE_URL);
        } else {
            Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}
