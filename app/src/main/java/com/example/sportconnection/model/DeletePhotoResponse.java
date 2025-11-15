package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class DeletePhotoResponse {
    @SerializedName("message")
    private String message;

    public DeletePhotoResponse() {
    }

    public DeletePhotoResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

