package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class SwipeUser {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("name")
    private String name;

    @SerializedName("job")
    private String job;

    @SerializedName("description")
    private String description;

    @SerializedName("photo_url")
    private String photoUrl;

    @SerializedName("sport_id")
    private int sportId;

    @SerializedName("location_id")
    private int locationId;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("ig_user")
    private String igUser;

    @SerializedName("x_user")
    private String xUser;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("user")
    private User user;

    @SerializedName("sport")
    private Sport sport;

    @SerializedName("location")
    private Location location;

    @SerializedName("profile_type")
    private String profileType;

    // Inner class for User
    public static class User {
        @SerializedName("id")
        private int id;

        @SerializedName("created_at")
        private String createdAt;

        public int getId() {
            return id;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getJob() {
        return job;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public int getSportId() {
        return sportId;
    }

    public int getLocationId() {
        return locationId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getIgUser() {
        return igUser;
    }

    public String getXUser() {
        return xUser;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public Sport getSport() {
        return sport;
    }

    public Location getLocation() {
        return location;
    }

    public String getProfileType() {
        return profileType;
    }

    public String getProfileTypeDisplayName() {
        switch (profileType) {
            case "athlete":
                return "Atleta";
            case "team":
                return "Equipo";
            case "agent":
                return "Agente";
            default:
                return profileType;
        }
    }
}

