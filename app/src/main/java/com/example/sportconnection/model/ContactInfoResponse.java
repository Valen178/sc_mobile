package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class ContactInfoResponse {
    @SerializedName("contact_info")
    private ContactInfo contactInfo;

    @SerializedName("profile_type")
    private String profileType;

    @SerializedName("name")
    private String name;

    @SerializedName("last_name")
    private String lastName;

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
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

    public static class ContactInfo {
        @SerializedName("email")
        private String email;

        @SerializedName("phone")
        private String phone;

        @SerializedName("instagram")
        private String instagram;

        @SerializedName("twitter")
        private String twitter;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getInstagram() {
            return instagram;
        }

        public void setInstagram(String instagram) {
            this.instagram = instagram;
        }

        public String getTwitter() {
            return twitter;
        }

        public void setTwitter(String twitter) {
            this.twitter = twitter;
        }
    }
}

