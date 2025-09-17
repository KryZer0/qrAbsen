package com.example.qrcodeabsen;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
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

    @GET("api/v1/export-absensi")
    @Headers("Cache-Control: no-cache")
    Call<ResponseBody> exportAbsensi(@Query("month") String monthYear);

    @GET("api/v1/siswa/fetch-kelas-jurusan")
    Call<List<SiswaModel>> getSiswaByKelasJurusan(
            @Query("kelas") String kelas,
            @Query("jurusan") String jurusan
    );

    @POST("api/v1/siswa/save-izin")
    Call<Void> saveIzinSiswa(@Body AbsensiModel izin);

    @POST("api/v1/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    @POST("api/v1/guru/tambah")
    @FormUrlEncoded
    Call<ResponseBody> tambahGuru(
            @Field("id") String id,
            @Field("nama") String nama,
            @Field("email") String email
    );

    @GET("api/v1/guru/fetch")
    Call<PaginatedResponse<Map<String, Object>>> fetchGuru(
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    @FormUrlEncoded
    @PUT("api/v1/guru/{id}")
    Call<ResponseBody> updateGuru(
            @Path("id") String old_id,
            @Field("id") String new_id,
            @Field("name") String nama,
            @Field("email") String email
    );

    @DELETE("api/v1/guru/{id}")
    Call<ResponseBody> deleteGuru(@Path("id") String id);

    @GET("api/v1/export-absensi/{kelas}")
    @Headers("Cache-Control: no-cache")
    Call<ResponseBody> exportAbsensiByKelas(
            @Path("kelas") String kelas,
            @Query("bulan") String monthYear
    );

    @GET("api/v1/history/kelas")
    Call<PaginatedResponse<AbsensiModel>> getHistoryByKelas(
            @Query("kelas") String kelas,
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    @GET("api/v1/siswa/fetch/kelas")
    Call<PaginatedResponse<SiswaModel>> fetchSiswaByKelas(
            @Query("kelas") String kelas,
            @Query("page") int page,
            @Query("per_page") int perPage);

    @DELETE("api/v1/siswa/{nisn}")
    Call<ResponseBody> deleteSiswa(@Path("nisn") int nisn);

    @POST("api/v1/guru/reset/{id}")
    Call<ResponseBody> resetPassword(@Path("id") String id);

    @POST("api/v1/change-password")
    Call<ResponseBody> changePassword(@Body Map<String, String> request);

    @GET("api/v1/rekap-absensi/{kelas}")
    @Headers("Cache-Control: no-cache")
    Call<ResponseBody> laporanAbsensiByKelas(
            @Path("kelas") String kelas,
            @Query("bulan") String monthYear
    );
}
