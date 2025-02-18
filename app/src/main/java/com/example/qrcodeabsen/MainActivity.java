package com.example.qrcodeabsen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private final ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
    private ExecutorService cameraExecutor;
    private PreviewView previewView;
    private Button btnAbsenMasuk, btnAbsenKeluar;
    private TextView textAbsen;
    private Toast toastMessage;
    private boolean statusAbsen;
    private String absenStatus;
    private long lastScanTime = 0;
    private static final long SCAN_DELAY_MS = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textAbsen = findViewById(R.id.absenText);
        btnAbsenMasuk = findViewById(R.id.btn_absen_masuk);
        btnAbsenMasuk.setOnClickListener(v -> setButtonState(true));
        btnAbsenKeluar = findViewById(R.id.btn_absen_keluar);
        btnAbsenKeluar.setOnClickListener(v -> setButtonState(false));
        previewView = findViewById(R.id.view_finder);
        cameraExecutor = Executors.newFixedThreadPool(2);
        setStatusAbsen();
        requestCameraPermission();
    }
    private void setStatusAbsen ()
    {
        Intent intent = getIntent();
        absenStatus = intent.getStringExtra("absen");
        if (("checkin").equals(absenStatus))
        {
            statusAbsen = true;
        }
        if (("checkout").equals(absenStatus))
        {
            statusAbsen = false;
        }
        setButtonState(statusAbsen);
    }
    private void setButtonState(boolean isAbsenMasukDiproses) {
        if (isAbsenMasukDiproses) {
            // Logika ketika tombol Absen Masuk ditekan
            btnAbsenMasuk.setEnabled(false); // Nonaktifkan tombol Absen Masuk
            btnAbsenMasuk.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
            btnAbsenKeluar.setEnabled(true); // Aktifkan tombol Absen Keluar
            btnAbsenKeluar.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green));
            textAbsen.setText("Absen Masuk");
            statusAbsen = true;
        } else {
            // Logika ketika tombol Absen Keluar ditekan
            btnAbsenKeluar.setEnabled(false); // Nonaktifkan tombol Absen Keluar
            btnAbsenKeluar.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray)); // Ubah warna tombol
            btnAbsenMasuk.setEnabled(true); // Aktifkan tombol Absen Masuk
            btnAbsenMasuk.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green)); // Ubah warna tombol
            textAbsen.setText("Absen Pulang");
            statusAbsen = false;
        }
    }

    private void requestCameraPermission() {
        // Lakukan pengecekan izin mengakses kamera granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Jika akses kamera belum ada, minta akses untuk akses kamera
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // jika telah mendapatkan akses kamera, jalankan kamera
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use this app", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        // Mendapatkan instance dari ProcessCameraProvider yang akan digunakan untuk mengelola lifecycle kamera
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider
                .getInstance(this);

        // Menambahkan listener untuk menangani hasil dari cameraProviderFuture
        cameraProviderFuture.addListener(() -> {
            try {
                // Mendapatkan instance ProcessCameraProvider dari future
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Membuat instance Preview yang akan digunakan untuk menampilkan pratinjau kamera
                Preview preview = new Preview.Builder().build();
                PreviewView previewView = findViewById(R.id.view_finder);
                // Mengatur SurfaceProvider dari PreviewView ke Preview agar pratinjau dapat ditampilkan
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Membuat CameraSelector untuk memilih kamera belakang (back camera)
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Membuat instance ImageAnalysis untuk menganalisis frame gambar dari kamera
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        // Mengatur strategi backpressure
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                // Mengatur analyzer untuk ImageAnalysis

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    Image mediaImage = imageProxy.getImage();
                    if (mediaImage != null) {
                        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy
                                .getImageInfo()
                                .getRotationDegrees());
                        // Memanggil metode scanQRCode untuk memindai QR code dari gambar
                        scanQRCode(image, imageProxy::close); //Pastikan dipanggil setelah pemindaian selesai
                    } else {
                        // Menutup imageProxy untuk menghindari memory leak
                        imageProxy.close();
                    }
                });
                // Melepaskan semua use case yang sebelumnya terikat ke cameraProvider
                cameraProvider.unbindAll();
                // Mengikat use case (preview dan imageAnalysis) ke lifecycle kamera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
                enableAutoFocus(cameraProvider, cameraSelector);

            } catch (Exception e) {
                // Menangani kesalahan yang terjadi selama proses binding use case
                Log.e("CameraX", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this)); // Menjalankan listener di main executor
    }
    private void scanQRCode(InputImage image, Runnable onComplete) {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        BarcodeScanner scanner = BarcodeScanning.getClient(options);

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    //  operasi pengiriman ke server ke background thread agar tidak terlalu
                    //  membebani UI Thread
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastScanTime < SCAN_DELAY_MS) return; // Mencegah pemrosesan ganda
                    lastScanTime = currentTime;

                    for (Barcode barcode : barcodes) {
                        String nisn = barcode.getRawValue();
                        new Thread(() -> sendToServer(nisn)).start();
                    }
                })
                .addOnFailureListener(e -> Log.e("QR_SCAN", "Gagal memindai QR code: " + e.getMessage()))
                .addOnCompleteListener(task -> onComplete.run()); // Tutup imageProxy setelah pemindaian selesai
    }
    //  Setelah melakukan pemindaian QRCode, aplikasi akan mendapatkan nisn yang terdapat pada QRCode
    //  kemudian retrofit akan menambahkan pada baseURL.

    private void sendToServer(String qrData) {
        Call<ResponseBody> call;
        if (statusAbsen) {
            call = apiService.check(qrData);
        } else {
            call = apiService.checkout(qrData);
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Handle success
                    Log.d("API_RESPONSE", "Check-in successful");
                    if (toastMessage != null) {
                        toastMessage.cancel();
                    }
                    toastMessage = Toast.makeText(MainActivity.this, "Absen Berhasil", Toast.LENGTH_SHORT);
                    toastMessage.show();
                } else {
                    // Handle error
                    String errorMessage = "Terjadi Error";
                    if (response.errorBody() != null) {
                        errorMessage = getErrorMessage(response.errorBody());
                    }
                    Log.e("API_RESPONSE", "Check-in failed: " + errorMessage);
                    if (toastMessage != null) {
                        toastMessage.cancel();
                    }
                    toastMessage = Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT);
                    toastMessage.show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                }
                Log.e("API_RESPONSE", "Check-in failed: " + t.getMessage());
            }
        });
    }

    private String getErrorMessage(ResponseBody errorBody) {
        try {
            // Assuming the error response is in JSON format
            JsonObject errorJson = new JsonParser().parse(errorBody.string()).getAsJsonObject();
            return errorJson.get("message").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
            return "An error occurred";
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void enableAutoFocus(ProcessCameraProvider cameraProvider, CameraSelector cameraSelector) {
        previewView.setOnTouchListener((v, event) -> {
            MeteringPoint point = previewView.getMeteringPointFactory().createPoint(event.getX(), event.getY());
            FocusMeteringAction action = new FocusMeteringAction.Builder(point).build();
            cameraProvider.bindToLifecycle(this, cameraSelector).getCameraControl().startFocusAndMetering(action);
            return true;
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}