package com.example.qrcodeabsen;

import java.io.IOException;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiUtils {
    public static String getErrorMessage(ResponseBody errorBody) {
        try {
            String responseString = errorBody.string();
            JSONObject jsonObject = new JSONObject(responseString);
            return jsonObject.optString("message", "Terjadi kesalahan");
        } catch (IOException | JSONException e) {
            return "Gagal membaca error response";
        }
    }

    public static String getSuccessMessage(ResponseBody responseBody) {
        try {
            String responseString = responseBody.string();
            JSONObject jsonObject = new JSONObject(responseString);
            return jsonObject.optString("message", "Berhasil");
        } catch (IOException | JSONException e) {
            return "Gagal membaca response";
        }
    }

}

