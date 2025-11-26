package com.example.sportconnection.model;
import com.google.gson.annotations.SerializedName;
public class CheckoutSessionResponse {
    @SerializedName("subscription_id")
    private int subscriptionId;
    @SerializedName("checkout_url")
    private String checkoutUrl;
    public int getSubscriptionId() {
        return subscriptionId;
    }
    public void setSubscriptionId(int subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
    public String getCheckoutUrl() {
        return checkoutUrl;
    }
    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }
}
