package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class SwipeRequest {
    @SerializedName("swiped_user_id")
    private int swipedUserId;

    @SerializedName("action")
    private String action; // "like" or "dislike"

    public SwipeRequest(int swipedUserId, String action) {
        this.swipedUserId = swipedUserId;
        this.action = action;
    }

    public int getSwipedUserId() {
        return swipedUserId;
    }

    public void setSwipedUserId(int swipedUserId) {
        this.swipedUserId = swipedUserId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}

