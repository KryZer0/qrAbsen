package com.example.qrcodeabsen;

public class LoginResponse {
    private boolean success;
    private String message;
    private String token, id_role, nama, id_user;

    public boolean isSuccess() {
        return success;
    }

    public String getNama() {
        return nama;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public String getIdRole(){
        return id_role;
    }
    public String getIdUser() {return id_user;}
}

