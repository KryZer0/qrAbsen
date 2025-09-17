package com.example.qrcodeabsen;

import com.google.gson.annotations.SerializedName;

public class SiswaModel {
    private int nisn;
    private String jns_kelamin, kelas, jurusan;
    @SerializedName("nama")
    private String nama;

    @SerializedName("qr_code")
    private String qrcode;

    public String getKelas() {
        return kelas;
    }

    public void setKelas(String kelas) {
        this.kelas = kelas;
    }

    public String getJurusan() {
        return jurusan;
    }

    public void setJurusan(String jurusan) {
        this.jurusan = jurusan;
    }

    public SiswaModel(int nisn, String nama, String jns, String kelas, String jurusan, String qrcode) {
        this.nisn = nisn;
        this.nama = nama;
        this.jns_kelamin = jns;
        this.kelas = kelas;
        this.jurusan = jurusan;
        this.qrcode = qrcode;
    }

    public int getNisn() {
        return nisn;
    }

    public void setNisn(int nisn) {
        this.nisn = nisn;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getJns() {
        return jns_kelamin;
    }

    public void setJns(String jns) {
        this.jns_kelamin = jns;
    }

    public String getQrCode(){
        return qrcode;
    }
}
