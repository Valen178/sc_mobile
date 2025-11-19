package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class SwipeResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("match")
    private boolean match;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public boolean isMatch() {
        return match;
    }

    public String getMessage() {
        return message;
    }
}

