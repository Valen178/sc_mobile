package com.example.sportconnection.repository;

import android.util.Log;

import com.example.sportconnection.model.LoginRequest;
import com.example.sportconnection.model.LoginResponse;
import com.example.sportconnection.model.SignupRequest;
import com.example.sportconnection.model.ProfileRequest;
import com.example.sportconnection.model.ProfileResponse;
import com.example.sportconnection.model.GetProfileResponse;
import com.example.sportconnection.model.UpdateProfileRequest;
import com.example.sportconnection.model.SignupResponse;
import com.example.sportconnection.model.GoogleSignInRequest;
import com.example.sportconnection.network.ApiClient;
import com.example.sportconnection.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private final ApiService apiService;

    public AuthRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    // Registro de usuario
    public void signup(String email, String password, Callback<SignupResponse> callback) {
        SignupRequest request = new SignupRequest(email, password);
        Call<SignupResponse> call = apiService.signup(request);

        Log.d(TAG, "Registrando usuario: " + email);
        call.enqueue(callback);
    }

    // Login de usuario
    public void login(String email, String password, Callback<LoginResponse> callback) {
        LoginRequest request = new LoginRequest(email, password);
        Call<LoginResponse> call = apiService.login(request);

        Log.d(TAG, "Iniciando sesión: " + email);
        call.enqueue(callback);
    }

    // Google Sign-In (unificado para login y signup)
    public void googleSignIn(String idToken, Callback<LoginResponse> callback) {
        GoogleSignInRequest request = new GoogleSignInRequest(idToken);
        Call<LoginResponse> call = apiService.googleSignIn(request);

        Log.d(TAG, "Iniciando sesión con Google (unificado)");
        call.enqueue(callback);
    }

    // Crear perfil
    public void createProfile(String token, ProfileRequest request, Callback<ProfileResponse> callback) {
        String authHeader = "Bearer " + token;
        Call<ProfileResponse> call = apiService.createProfile(authHeader, request);

        Log.d(TAG, "Creando perfil: " + request.getProfileType());
        call.enqueue(callback);
    }

    // Obtener perfil
    public void getProfile(String token, Callback<GetProfileResponse> callback) {
        String authHeader = "Bearer " + token;
        Call<GetProfileResponse> call = apiService.getProfile(authHeader);

        Log.d(TAG, "Obteniendo perfil del usuario autenticado");
        call.enqueue(callback);
    }

    // Actualizar perfil
    public void updateProfile(String token, UpdateProfileRequest request, Callback<ProfileResponse> callback) {
        String authHeader = "Bearer " + token;
        Call<ProfileResponse> call = apiService.updateProfile(authHeader, request);

        Log.d(TAG, "Actualizando perfil del usuario autenticado");
        call.enqueue(callback);
    }
}

