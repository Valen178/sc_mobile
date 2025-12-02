package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VenuesResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Venue> data;

    public VenuesResponse() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Venue> getData() {
        return data;
    }

    public void setData(List<Venue> data) {
        this.data = data;
    }
}

