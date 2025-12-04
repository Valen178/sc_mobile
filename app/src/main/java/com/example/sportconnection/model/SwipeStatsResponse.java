package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class SwipeStatsResponse {
    @SerializedName("swipes_remaining")
    private Integer swipesRemaining; // null si es premium

    @SerializedName("is_premium")
    private boolean isPremium;

    @SerializedName("daily_limit")
    private int dailyLimit;

    public Integer getSwipesRemaining() {
        return swipesRemaining;
    }

    public void setSwipesRemaining(Integer swipesRemaining) {
        this.swipesRemaining = swipesRemaining;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public int getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(int dailyLimit) {
        this.dailyLimit = dailyLimit;
    }
}

