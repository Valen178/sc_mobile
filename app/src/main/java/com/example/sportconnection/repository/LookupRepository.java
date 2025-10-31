package com.example.sportconnection.repository;

import com.example.sportconnection.model.Location;
import com.example.sportconnection.model.Sport;
import com.example.sportconnection.network.ApiClient;
import com.example.sportconnection.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class LookupRepository {
    private final ApiService apiService;

    public LookupRepository() {
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    // Obtener todos los deportes
    public void getSports(Callback<List<Sport>> callback) {
        Call<List<Sport>> call = apiService.getSports();
        call.enqueue(callback);
    }

    // Obtener todas las ubicaciones
    public void getLocations(Callback<List<Location>> callback) {
        Call<List<Location>> call = apiService.getLocations();
        call.enqueue(callback);
    }
}

