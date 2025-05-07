package com.example.qrcodeabsen;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/v1/check/{nisn}")
    Call<ResponseBody> check(@Path("nisn") String nisn);

    @PATCH("api/v1/checkout/{nisn}")
    Call<ResponseBody> checkout(@Path("nisn") String nisn);

    @GET("api/v1/history")
    Call<PaginatedResponse<AbsensiModel>> getHistory(@Query("page") int page, @Query("per_page") int perPage);

    @POST("api/v1/siswa/store")
    Call<ResponseBody> tambahSiswa(@Body SiswaModel siswa);

    @GET("api/v1/siswa/fetch")
    Call<PaginatedResponse<SiswaModel>> fetchSiswa(@Query("page") int page, @Query("per_page") int perPage);

    @PUT("api/v1/siswa/update/{nisn}")
    Call<ResponseBody> updateSiswa(@Path("nisn")String oldnisn, @Body SiswaModel siswa);

    @GET("api/v1/siswa/generatecardbatch")
    Call<List<SiswaModel>> getAllDataQr();

    @GET("api/v1/generate-qr/{nisn}")
    Call<ResponseBody> generateQr(@Path("nisn") String nisn);

    @Multipart
    @POST("api/v1/siswa/store-batch")
    Call<ResponseBody> uploadCsv(@Part MultipartBody.Part file);

    @GET("api/v1/generate-qr-batch")
    Call<ResponseBody> generateAllQr();
}
