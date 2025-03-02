package com.example.qrcodeabsen;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String language = prefs.getString("Language", "en");
        super.attachBaseContext(BahasaHelper.setLocale(newBase, language));
    }
}
