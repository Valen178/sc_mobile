package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class CreatePostRequest {
    @SerializedName("text")
    private String text;

    @SerializedName("url")
    private String url;

    public CreatePostRequest(String text, String url) {
        this.text = text;
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

