package com.example.sportconnection.network;

import com.example.sportconnection.model.LoginRequest;
import com.example.sportconnection.model.LoginResponse;
import com.example.sportconnection.model.SignupRequest;
import com.example.sportconnection.model.SignupResponse;
import com.example.sportconnection.model.ProfileRequest;
import com.example.sportconnection.model.ProfileResponse;
import com.example.sportconnection.model.GetProfileResponse;
import com.example.sportconnection.model.UpdateProfileRequest;
import com.example.sportconnection.model.Sport;
import com.example.sportconnection.model.Location;
import com.example.sportconnection.model.GoogleSignInRequest;
import com.example.sportconnection.model.UploadPhotoResponse;
import com.example.sportconnection.model.DeletePhotoResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ApiService {

    // Endpoints de autenticación
    @POST("/auth/signup")
    Call<SignupResponse> signup(@Body SignupRequest request);

    @POST("/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Endpoint de Google Sign-In (único para login y signup)
    @POST("/auth/google")
    Call<LoginResponse> googleSignIn(@Body GoogleSignInRequest request);

    // Endpoints de perfil
    @POST("/auth/complete-profile")
    Call<ProfileResponse> createProfile(
            @Header("Authorization") String token,
            @Body ProfileRequest request
    );

    @GET("/profile/me")
    Call<GetProfileResponse> getProfile(
            @Header("Authorization") String token
    );

    @PUT("/profile/me")
    Call<ProfileResponse> updateProfile(
            @Header("Authorization") String token,
            @Body UpdateProfileRequest request
    );

    // Endpoints de lookup (deportes y ubicaciones)
    @GET("/lookup/sports")
    Call<List<Sport>> getSports();

    @GET("/lookup/locations")
    Call<List<Location>> getLocations();

    // Endpoints de foto de perfil
    @Multipart
    @POST("/profile-photo/upload")
    Call<UploadPhotoResponse> uploadProfilePhoto(
            @Header("Authorization") String token,
            @Part MultipartBody.Part photo
    );

    @DELETE("/profile-photo/delete")
    Call<DeletePhotoResponse> deleteProfilePhoto(
            @Header("Authorization") String token
    );
}

