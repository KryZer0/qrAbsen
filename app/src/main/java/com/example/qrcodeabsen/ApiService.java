package com.example.qrcodeabsen;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/v1/check/{nisn}")
    Call<ResponseBody> check(@Path("nisn") String nisn);

    @PATCH("api/v1/checkout/{nisn}")
    Call<ResponseBody> checkout(@Path("nisn") String nisn);

    @GET("api/v1/history")
    Call<List<AbsensiModel>> getHistory();

    @POST("api/v1/siswa/store")
    Call<ResponseBody> tambahSiswa(@Body SiswaModel siswa);

    @GET("api/v1/siswa/fetch")
    Call<List<SiswaModel>> fetchSiswa();

    @PUT("api/v1/siswa/update/{nisn}")
    Call<ResponseBody> updateSiswa(@Path("nisn")String oldnisn, @Body SiswaModel siswa);

    @GET("api/v1/siswa/generatecardbatch")
    Call<List<SiswaModel>> getAllDataQr();

    @GET("api/v1/generate-qr/{nisn}")
    Call<ResponseBody> generateQr(@Path("nisn") String nisn);
}
