package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class GetProfileResponse {
    @SerializedName("user_id")
    private int userId;

    @SerializedName(value = "profileType", alternate = {"profile_type"})
    private String profileType;

    private ProfileData profile;

    private Relationship relationship;

    @SerializedName("limited_view")
    private boolean limitedView;

    public int getUserId() {
        return userId;
    }

    public String getProfileType() {
        return profileType;
    }

    public ProfileData getProfile() {
        return profile;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public boolean isLimitedView() {
        return limitedView;
    }

    public static class Relationship {
        @SerializedName("has_interaction")
        private boolean hasInteraction;

        @SerializedName("has_match")
        private boolean hasMatch;

        @SerializedName("can_view_full_profile")
        private boolean canViewFullProfile;

        public boolean isHasInteraction() {
            return hasInteraction;
        }

        public boolean isHasMatch() {
            return hasMatch;
        }

        public boolean isCanViewFullProfile() {
            return canViewFullProfile;
        }
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

        @SerializedName("photo_url")
        private String photoUrl;

        @SerializedName("created_at")
        private String createdAt;

        // Objetos relacionados
        private Sport sport;
        private Location location;

        // Getters
        public int getId() {
            return id;
        }

        public int getUserId() {
            return userId;
        }

        public String getProfileType() {
            return profileType;
        }

        public String getName() {
            return name;
        }

        public String getLastName() {
            return lastName;
        }

        public String getDescription() {
            return description;
        }

        public Integer getLocationId() {
            return locationId;
        }

        public Integer getSportId() {
            return sportId;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getIgUser() {
            return igUser;
        }

        public String getxUser() {
            return xUser;
        }

        public String getBirthdate() {
            return birthdate;
        }

        public String getHeight() {
            return height;
        }

        public String getWeight() {
            return weight;
        }

        public String getAgency() {
            return agency;
        }

        public String getJob() {
            return job;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public Sport getSport() {
            return sport;
        }

        public Location getLocation() {
            return location;
        }
    }

    // Métodos auxiliares para acceder a datos del perfil más fácilmente
    public String getName() {
        return profile != null ? profile.getName() : null;
    }

    public String getJob() {
        return profile != null ? profile.getJob() : null;
    }

    public String getDescription() {
        return profile != null ? profile.getDescription() : null;
    }

    public String getPhotoUrl() {
        return profile != null ? profile.getPhotoUrl() : null;
    }

    public String getPhoneNumber() {
        return profile != null ? profile.getPhoneNumber() : null;
    }

    public String getIgUser() {
        return profile != null ? profile.getIgUser() : null;
    }

    public String getXUser() {
        return profile != null ? profile.getxUser() : null;
    }

    public Location getLocation() {
        return profile != null ? profile.getLocation() : null;
    }

    public Sport getSport() {
        return profile != null ? profile.getSport() : null;
    }

    public boolean isHasMatch() {
        return relationship != null && relationship.isHasMatch();
    }
}

