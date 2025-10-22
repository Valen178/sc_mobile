package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {
    private boolean success;
    private String message;
    private ProfileData profile;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ProfileData getProfile() {
        return profile;
    }

    public void setProfile(ProfileData profile) {
        this.profile = profile;
    }

    public static class ProfileData {
        private int id;

        @SerializedName("user_id")
        private int userId;

        @SerializedName("profile_type")
        private String profileType;

        private String name;

        @SerializedName("last_name")
        private String lastName;

        private String description;

        @SerializedName("location_id")
        private Integer locationId;

        @SerializedName("sport_id")
        private Integer sportId;

        @SerializedName("phone_number")
        private String phoneNumber;

        @SerializedName("ig_user")
        private String igUser;

        @SerializedName("x_user")
        private String xUser;

        private String birthdate;
        private String height;
        private String weight;
        private String agency;
        private String job;

        // Getters y Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getProfileType() {
            return profileType;
        }

        public void setProfileType(String profileType) {
            this.profileType = profileType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getLocationId() {
            return locationId;
        }

        public void setLocationId(Integer locationId) {
            this.locationId = locationId;
        }

        public Integer getSportId() {
            return sportId;
        }

        public void setSportId(Integer sportId) {
            this.sportId = sportId;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getIgUser() {
            return igUser;
        }

        public void setIgUser(String igUser) {
            this.igUser = igUser;
        }

        public String getxUser() {
            return xUser;
        }

        public void setxUser(String xUser) {
            this.xUser = xUser;
        }

        public String getBirthdate() {
            return birthdate;
        }

        public void setBirthdate(String birthdate) {
            this.birthdate = birthdate;
        }

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public String getAgency() {
            return agency;
        }

        public void setAgency(String agency) {
            this.agency = agency;
        }

        public String getJob() {
            return job;
        }

        public void setJob(String job) {
            this.job = job;
        }
    }
}

