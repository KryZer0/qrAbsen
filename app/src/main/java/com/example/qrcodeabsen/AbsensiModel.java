package com.example.qrcodeabsen;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AbsensiModel {
    private String nisn, kelas, keterangan, jam_keluar, jam_masuk, tanggal, nama;


    public AbsensiModel(String nisn, String kelas, String keterangan, String jam_masuk, String jam_keluar) {
        this.nisn = nisn;
        this.kelas = kelas;
        this.keterangan = keterangan;
        this.nama = nama;
        this.tanggal = tanggal;
        this.jam_masuk = jam_masuk;
        this.jam_keluar = jam_keluar;
    }

    public String getNisn() {
        return nisn;
    }
    public String getNama() {return nama;}

    public String getKelas() {
        return kelas;
    }
    public String getTanggal(){return formatTanggal(tanggal);}

    public String getketerangan() {
        return keterangan;
    }

    public String getJamMasuk() {
        return jam_masuk;
    }

    public String getJamPulang() {
        return jam_keluar;
    }

    private String formatTanggal(String tanggal) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            Date date = inputFormat.parse(tanggal);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return tanggal;
        }
    }
}
