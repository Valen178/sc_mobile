package com.example.sportconnection.model;

public class GoogleSignInRequest {
    private String token;

    public GoogleSignInRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

