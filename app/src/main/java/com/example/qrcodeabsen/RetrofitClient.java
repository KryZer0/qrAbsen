package com.example.qrcodeabsen;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(urlConstant.BASE_URL+":"+urlConstant.BASE_PORT)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    private static String getErrorMessage(ResponseBody errorBody) {
        try {
            // Assuming the error response is in JSON format
            JsonObject errorJson = new JsonParser().parse(errorBody.string()).getAsJsonObject();
            return errorJson.get("message").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
            return "An error occurred";
        }
    }
}

