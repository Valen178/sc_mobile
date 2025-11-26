package com.example.sportconnection.repository;

import com.example.sportconnection.model.CheckoutSessionResponse;
import com.example.sportconnection.model.CreateCheckoutRequest;
import com.example.sportconnection.model.SubscriptionPlan;
import com.example.sportconnection.model.SubscriptionStatus;
import com.example.sportconnection.network.ApiClient;
import com.example.sportconnection.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class SubscriptionRepository {

    private final ApiService apiService;

    public SubscriptionRepository() {
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    // Obtener planes de suscripción disponibles
    public void getSubscriptionPlans(String token, Callback<List<SubscriptionPlan>> callback) {
        String authHeader = "Bearer " + token;
        Call<List<SubscriptionPlan>> call = apiService.getSubscriptionPlans(authHeader);
        call.enqueue(callback);
    }

    // Crear sesión de checkout de Stripe
    public void createCheckoutSession(String token, int planId, Callback<CheckoutSessionResponse> callback) {
        String authHeader = "Bearer " + token;
        CreateCheckoutRequest request = new CreateCheckoutRequest(planId);
        Call<CheckoutSessionResponse> call = apiService.createCheckoutSession(authHeader, request);
        call.enqueue(callback);
    }

    // Obtener estado de suscripción del usuario
    public void getSubscriptionStatus(String token, Callback<SubscriptionStatus> callback) {
        String authHeader = "Bearer " + token;
        Call<SubscriptionStatus> call = apiService.getSubscriptionStatus(authHeader);
        call.enqueue(callback);
    }
}

