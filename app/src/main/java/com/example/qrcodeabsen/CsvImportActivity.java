package com.example.qrcodeabsen;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import java.io.File;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CsvImportActivity extends BaseActivity {
    private static final int PICK_CSV_REQUEST = 1;
    private final ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
    private Uri csvUri;
    private TextView txtFileName;
    private ProgressBar progressBar;
    private Button btnUpload;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_csv);

        txtFileName = findViewById(R.id.txtFileName);
        progressBar = findViewById(R.id.progressBar);
        btnUpload = findViewById(R.id.btnUpload);

        findViewById(R.id.btnSelectFile).setOnClickListener(view -> openFilePicker());

        btnUpload.setOnClickListener(view -> {
            if (csvUri != null) {
                uploadCsv();
            } else {
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(this, "Pilih file CSV terlebih dahulu!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"text/csv", "application/csv", "application/vnd.ms-excel", "text/plain",
                "application/vnd.ms-excel", "text/comma-separated-values"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, PICK_CSV_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CSV_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            csvUri = data.getData();
            txtFileName.setText(csvUri.getLastPathSegment());
        }
    }

    private void uploadCsv() {
        progressBar.setVisibility(View.VISIBLE);
        btnUpload.setEnabled(false);

        File file = new File(Objects.requireNonNull(ApiUtils.getPath(this, csvUri)));
        RequestBody requestFile = RequestBody.create(MediaType.parse("text/csv"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("csv_file", file.getName(), requestFile);

        Call<ResponseBody> call = apiService.uploadCsv(body);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                btnUpload.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(CsvImportActivity.this, ApiUtils.getSuccessMessage(response.body()), Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Log.d(TAG, "onResponse: " + ApiUtils.getErrorMessage(response.errorBody()));
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(CsvImportActivity.this, "Gagal " + ApiUtils.getErrorMessage(response.errorBody()), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnUpload.setEnabled(true);
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(CsvImportActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
