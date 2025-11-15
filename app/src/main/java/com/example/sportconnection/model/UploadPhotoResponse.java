package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class UploadPhotoResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("photo_url")
    private String photoUrl;

    @SerializedName("profileType")
    private String profileType;

    public UploadPhotoResponse() {
    }

    public UploadPhotoResponse(String message, String photoUrl, String profileType) {
        this.message = message;
        this.photoUrl = photoUrl;
        this.profileType = profileType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getProfileType() {
        return profileType;
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }
}

