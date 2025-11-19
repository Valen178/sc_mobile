package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DiscoverResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("users")
    private List<SwipeUser> users;

    @SerializedName("user_profile_type")
    private String userProfileType;

    @SerializedName("user_sport_id")
    private int userSportId;

    @SerializedName("count")
    private int count;

    public boolean isSuccess() {
        return success;
    }

    public List<SwipeUser> getUsers() {
        return users;
    }

    public String getUserProfileType() {
        return userProfileType;
    }

    public int getUserSportId() {
        return userSportId;
    }

    public int getCount() {
        return count;
    }
}

