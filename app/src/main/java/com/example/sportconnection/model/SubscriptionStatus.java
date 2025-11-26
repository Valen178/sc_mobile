package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class SubscriptionStatus {
    @SerializedName("active")
    private boolean active;

    @SerializedName("message")
    private String message;

    @SerializedName("subscription_details")
    private SubscriptionDetails subscriptionDetails;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SubscriptionDetails getSubscriptionDetails() {
        return subscriptionDetails;
    }

    public void setSubscriptionDetails(SubscriptionDetails subscriptionDetails) {
        this.subscriptionDetails = subscriptionDetails;
    }

    public static class SubscriptionDetails {
        @SerializedName("plan_name")
        private String planName;

        @SerializedName("start_date")
        private String startDate;

        @SerializedName("end_date")
        private String endDate;

        @SerializedName("status")
        private String status;

        public String getPlanName() {
            return planName;
        }

        public void setPlanName(String planName) {
            this.planName = planName;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

