package com.example.qrcodeabsen;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("/check/{nisn}")
    Call<ResponseBody> check(@Path("nisn") String nisn);

    @PATCH("/checkout/{nisn}")
    Call<ResponseBody> checkout(@Path("nisn") String nisn);
}
