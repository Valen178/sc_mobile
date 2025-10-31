package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class Location {
    @SerializedName("id")
    private int id;

    @SerializedName("country")
    private String country;

    @SerializedName("province")
    private String province;

    @SerializedName("city")
    private String city;

    @SerializedName("created_at")
    private String createdAt;

    public Location() {
    }

    public Location(int id, String country, String province, String city) {
        this.id = id;
        this.country = country;
        this.province = province;
        this.city = city;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return city + ", " + province + ", " + country;
    }
}

