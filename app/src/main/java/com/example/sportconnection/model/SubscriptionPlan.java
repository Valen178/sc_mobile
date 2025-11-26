package com.example.sportconnection.model;
import com.google.gson.annotations.SerializedName;
public class SubscriptionPlan {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("price")
    private double price;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    @Override
    public String toString() {
        return name + " - $" + price;
    }
}
