package com.example.qrcodeabsen;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {
    private static final String PREF_NAME = "UrlPrefs";
    private static final String KEY_BASE_URL = "BASE_URL";
    private static final String KEY_BASE_PORT = "BASE_PORT";

    private static SharedPreferences getPrefs() {
        return MyApp.getAppContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveServerConfig(String url, String port) {
        getPrefs().edit()
                .putString(KEY_BASE_URL, url)
                .putString(KEY_BASE_PORT, port)
                .apply();
    }

    public static String getBaseUrl() {
        return getPrefs().getString(KEY_BASE_URL, "http://192.168.100.52");
    }

    public static String getBasePort() {
        return getPrefs().getString(KEY_BASE_PORT, "8000");
    }

    public static String getFullBaseUrl() {
        return getBaseUrl() + ":" + getBasePort() + "/";
    }
}
