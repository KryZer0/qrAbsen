package com.example.qrcodeabsen;

import com.google.gson.annotations.SerializedName;

public class SiswaModel {
    private int nisn;
    private String jns_kelamin;
    @SerializedName("nama")
    private String nama;

    @SerializedName("qr_code")
    private String qrcode;

    public SiswaModel(int nisn, String nama, String jns, String qrcode) {
        this.nisn = nisn;
        this.nama = nama;
        this.jns_kelamin = jns;
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
