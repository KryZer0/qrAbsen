package com.example.qrcodeabsen;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class izinSiswaActivity extends BaseActivity {
    private final ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
    private Spinner nama, kelas, jurusan, jenisIzin;
    private Button submit;

    private String[] kelasOptions = {"-","X", "XI", "XII"};
    private String[] jurusanOptions = {
            "-",
            "Teknik Kendaraan Ringan",
            "Teknik Mesin Industri",
            "Administrasi Perkantoran"
    };

    private String[] jenisIzinOptions = {
            "Masuk","Telat","Sakit","Izin",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_izin);
        setViewById();
        setClickListener();
    }

    private void setViewById() {
        nama = findViewById(R.id.nama);
        kelas = findViewById(R.id.kelas);
        jurusan = findViewById(R.id.jurusan);
        jenisIzin = findViewById(R.id.jns);
        submit = findViewById(R.id.submitformizin);

        ArrayAdapter<String> adapterKelas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kelasOptions);
        adapterKelas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kelas.setAdapter(adapterKelas);

        ArrayAdapter<String> adapterJurusan = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, jurusanOptions);
        adapterJurusan.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jurusan.setAdapter(adapterJurusan);

        ArrayAdapter<String> adapterJenis = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, jenisIzinOptions);
        adapterJenis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jenisIzin.setAdapter(adapterJenis);
    }

    private void setClickListener() {
        kelas.setOnItemSelectedListener(new AdapterListener());
        jurusan.setOnItemSelectedListener(new AdapterListener());

        submit.setOnClickListener(v -> {
            String selectedNama = (String) nama.getSelectedItem();
            String selectedKelas = (String) kelas.getSelectedItem();
            String selectedJurusan = (String) jurusan.getSelectedItem();
            String selectedJenisIzin = (String) jenisIzin.getSelectedItem();

            if (selectedNama == null || selectedNama.isEmpty() || selectedJenisIzin == null || selectedJenisIzin.isEmpty()) {
                Toast.makeText(izinSiswaActivity.this, "Nama dan Jenis Izin tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                return;
            }

            String nisn = selectedNama.split(" - ")[0];

            AbsensiModel absensi = new AbsensiModel(
                    nisn,
                    selectedKelas,
                    selectedJenisIzin,
                    null,
                    null
            );

            Call<Void> izinCall = apiService.saveIzinSiswa(absensi);
            izinCall.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(izinSiswaActivity.this, "Izin berhasil dikirim", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(izinSiswaActivity.this, "Gagal mengirim izin : " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(izinSiswaActivity.this, "Kesalahan jaringan", Toast.LENGTH_SHORT).show();
                }
            });
        });


    }
    private void fetchSiswaByKelasJurusan(String selectedKelas, String selectedJurusan) {
        Call<List<SiswaModel>> call = apiService.getSiswaByKelasJurusan(selectedKelas, selectedJurusan);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<SiswaModel>> call, Response<List<SiswaModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SiswaModel> siswaList = response.body();
                    List<String> namaSiswa = new ArrayList<>();
                    for (SiswaModel siswa : siswaList) {
                        namaSiswa.add(siswa.getNisn() + " - " + siswa.getNama());
                    }

                    ArrayAdapter<String> adapterNama = new ArrayAdapter<>(izinSiswaActivity.this, android.R.layout.simple_spinner_item, namaSiswa);
                    adapterNama.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    nama.setAdapter(adapterNama);
                } else {
                    Toast.makeText(izinSiswaActivity.this, "Gagal memuat siswa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SiswaModel>> call, Throwable t) {
                Toast.makeText(izinSiswaActivity.this, "Kesalahan jaringan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class AdapterListener implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            String selectedKelas = kelas.getSelectedItem().toString();
            String selectedJurusan = jurusan.getSelectedItem().toString();
            if (!selectedKelas.equals("-") && !selectedJurusan.equals("-")) {
                fetchSiswaByKelasJurusan(selectedKelas, selectedJurusan);
            }
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
    }

}

