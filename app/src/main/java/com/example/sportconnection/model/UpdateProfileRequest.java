package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {
    @SerializedName("userUpdates")
    private UserUpdates userUpdates;

    @SerializedName("profileUpdates")
    private ProfileUpdates profileUpdates;

    public UpdateProfileRequest() {
    }

    public UserUpdates getUserUpdates() {
        return userUpdates;
    }

    public void setUserUpdates(UserUpdates userUpdates) {
        this.userUpdates = userUpdates;
    }

    public ProfileUpdates getProfileUpdates() {
        return profileUpdates;
    }

    public void setProfileUpdates(ProfileUpdates profileUpdates) {
        this.profileUpdates = profileUpdates;
    }

    public static class UserUpdates {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class ProfileUpdates {
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

        // Campos específicos de ATHLETE
        private String birthdate;
        private String height;
        private String weight;

        // Campo específico de AGENT
        private String agency;

        // Campo específico de TEAM
        private String job;

        // Getters y Setters
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

