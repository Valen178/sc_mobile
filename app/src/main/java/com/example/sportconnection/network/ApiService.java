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
import com.example.sportconnection.model.Post;
import com.example.sportconnection.model.CreatePostRequest;
import com.example.sportconnection.model.CreatePostResponse;
import com.example.sportconnection.model.MyPostsResponse;
import com.example.sportconnection.model.DiscoverResponse;
import com.example.sportconnection.model.SwipeRequest;
import com.example.sportconnection.model.SwipeResponse;
import com.example.sportconnection.model.SwipeStatsResponse;
import com.example.sportconnection.model.ContactInfoResponse;
import com.example.sportconnection.model.MatchesResponse;
import com.example.sportconnection.model.SubscriptionPlan;
import com.example.sportconnection.model.CreateCheckoutRequest;
import com.example.sportconnection.model.CheckoutSessionResponse;
import com.example.sportconnection.model.SubscriptionStatus;
import com.example.sportconnection.model.Venue;
import com.example.sportconnection.model.VenuesResponse;

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

    @GET("/profile/{userId}")
    Call<GetProfileResponse> getUserProfile(
            @Header("Authorization") String token,
            @retrofit2.http.Path("userId") int userId
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

    // Endpoints de publicaciones
    @POST("/posts")
    Call<CreatePostResponse> createPost(
            @Header("Authorization") String token,
            @Body CreatePostRequest request
    );

    @GET("/posts")
    Call<List<Post>> getAllPosts();

    @GET("/posts/my-posts")
    Call<MyPostsResponse> getMyPosts(
            @Header("Authorization") String token
    );

    @DELETE("/posts/{id}")
    Call<Void> deletePost(
            @Header("Authorization") String token,
            @retrofit2.http.Path("id") int postId
    );

    // Endpoints de swipe y matches
    @GET("/swipe/discover")
    Call<DiscoverResponse> getDiscoverUsers(
            @Header("Authorization") String token,
            @retrofit2.http.Query("profile_type_filter") String profileTypeFilter,
            @retrofit2.http.Query("limit") Integer limit
    );

    @POST("/swipe")
    Call<SwipeResponse> swipe(
            @Header("Authorization") String token,
            @Body SwipeRequest request
    );

    @GET("/swipe/matches")
    Call<MatchesResponse> getMatches(
            @Header("Authorization") String token
    );

    @GET("/swipe/stats")
    Call<SwipeStatsResponse> getSwipeStats(
            @Header("Authorization") String token
    );

    @GET("/swipe/contact/{target_user_id}")
    Call<ContactInfoResponse> getDirectContact(
            @Header("Authorization") String token,
            @retrofit2.http.Path("target_user_id") int targetUserId
    );

    // Endpoints de suscripción
    @GET("/subscriptions/plans")
    Call<List<SubscriptionPlan>> getSubscriptionPlans(
            @Header("Authorization") String token
    );

    @POST("/subscriptions/create-checkout-session")
    Call<CheckoutSessionResponse> createCheckoutSession(
            @Header("Authorization") String token,
            @Body CreateCheckoutRequest request
    );

    @GET("/subscriptions/status")
    Call<SubscriptionStatus> getSubscriptionStatus(
            @Header("Authorization") String token
    );

    @POST("/subscriptions/cancel")
    Call<Void> cancelSubscription(
            @Header("Authorization") String token
    );

    // Endpoints de venues
    @GET("/venues")
    Call<VenuesResponse> getVenues(
            @Header("Authorization") String token
    );

    @GET("/venues/{id}")
    Call<VenuesResponse> getVenueById(
            @Header("Authorization") String token,
            @retrofit2.http.Path("id") int venueId
    );
}

