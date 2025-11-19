package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class Match {
    @SerializedName("match_id")
    private int matchId;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("other_user")
    private OtherUser otherUser;

    // Inner class for OtherUser
    public static class OtherUser {
        @SerializedName("id")
        private int id;

        @SerializedName("profile_type")
        private String profileType;

        @SerializedName("profile")
        private MatchProfile profile;

        public int getId() {
            return id;
        }

        public String getProfileType() {
            return profileType;
        }

        public MatchProfile getProfile() {
            return profile;
        }
    }

    // Inner class for MatchProfile
    public static class MatchProfile {
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
    }

    // Getters
    public int getMatchId() {
        return matchId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public OtherUser getOtherUser() {
        return otherUser;
    }
}

