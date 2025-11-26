package com.example.sportconnection.model;
import com.google.gson.annotations.SerializedName;
public class CreateCheckoutRequest {
    @SerializedName("plan_id")
    private int planId;
    public CreateCheckoutRequest(int planId) {
        this.planId = planId;
    }
    public int getPlanId() {
        return planId;
    }
    public void setPlanId(int planId) {
        this.planId = planId;
    }
}
