package com.example.qrcodeabsen;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    public static String getPath(Context context, Uri uri) {
        String filePath = null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("temp_csv", ".csv", context.getCacheDir());
            OutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            filePath = tempFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }
}

