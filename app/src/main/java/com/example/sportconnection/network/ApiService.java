package com.example.sportconnection.network;

import com.example.sportconnection.model.LoginRequest;
import com.example.sportconnection.model.LoginResponse;
import com.example.sportconnection.model.SignupRequest;
import com.example.sportconnection.model.SignupResponse;
import com.example.sportconnection.model.ProfileRequest;
import com.example.sportconnection.model.ProfileResponse;
import com.example.sportconnection.model.Sport;
import com.example.sportconnection.model.Location;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // Endpoints de autenticación
    @POST("auth/signup")
    Call<SignupResponse> signup(@Body SignupRequest request);

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Endpoints de perfil
    @POST("auth/complete-profile")
    Call<ProfileResponse> createProfile(
            @Header("Authorization") String token,
            @Body ProfileRequest request
    );

    @GET("profile/me/{id}")
    Call<ProfileResponse> getProfile(
            @Header("Authorization") String token,
            @Path("id") int profileId
    );

    @PUT("profile/me/{id}")
    Call<ProfileResponse> updateProfile(
            @Header("Authorization") String token,
            @Path("id") int profileId,
            @Body ProfileRequest request
    );

    // Endpoints de lookup (deportes y ubicaciones)
    @GET("lookup/sports")
    Call<List<Sport>> getSports();

    @GET("lookup/locations")
    Call<List<Location>> getLocations();
}

