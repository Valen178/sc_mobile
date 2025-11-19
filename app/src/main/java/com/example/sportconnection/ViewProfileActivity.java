package com.example.sportconnection;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.sportconnection.model.GetProfileResponse;
import com.example.sportconnection.network.ApiClient;
import com.example.sportconnection.network.ApiService;
import com.example.sportconnection.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewProfileActivity extends AppCompatActivity {

    private ImageView profilePhoto;
    private TextView profileName;
    private TextView profileType;
    private TextView profileJob;
    private TextView profileLocation;
    private TextView profileSport;
    private TextView profileDescription;
    private TextView profilePhone;
    private TextView profileInstagram;
    private TextView profileTwitter;
    private ImageButton btnWhatsApp;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private int userId;
    private String token;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        initializeViews();

        // Obtener datos del intent
        userId = getIntent().getIntExtra("user_id", -1);
        sessionManager = new SessionManager(this);
        token = sessionManager.getToken();

        if (token == null) {
            token = "";
        }

        if (userId == -1) {
            Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = ApiClient.getApiService();
        loadProfile();
    }

    private void initializeViews() {
        profilePhoto = findViewById(R.id.profilePhoto);
        profileName = findViewById(R.id.profileName);
        profileType = findViewById(R.id.profileType);
        profileJob = findViewById(R.id.profileJob);
        profileLocation = findViewById(R.id.profileLocation);
        profileSport = findViewById(R.id.profileSport);
        profileDescription = findViewById(R.id.profileDescription);
        profilePhone = findViewById(R.id.profilePhone);
        profileInstagram = findViewById(R.id.profileInstagram);
        profileTwitter = findViewById(R.id.profileTwitter);
        btnWhatsApp = findViewById(R.id.btnWhatsAppProfile);
        btnBack = findViewById(R.id.btnBackProfile);
        progressBar = findViewById(R.id.progressBarProfile);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadProfile() {
        showLoading(true);

        Call<GetProfileResponse> call = apiService.getUserProfile("Bearer " + token, userId);
        call.enqueue(new Callback<GetProfileResponse>() {
            @Override
            public void onResponse(Call<GetProfileResponse> call, Response<GetProfileResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    displayProfile(response.body());
                } else {
                    Toast.makeText(ViewProfileActivity.this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<GetProfileResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ViewProfileActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayProfile(GetProfileResponse profile) {
        // Nombre
        profileName.setText(profile.getName());

        // Tipo de perfil
        String typeText = getProfileTypeDisplayName(profile.getProfileType());
        profileType.setText(typeText);

        // Trabajo/Posición
        if (profile.getJob() != null && !profile.getJob().isEmpty()) {
            profileJob.setText(profile.getJob());
            profileJob.setVisibility(View.VISIBLE);
        } else {
            profileJob.setVisibility(View.GONE);
        }

        // Ubicación
        if (profile.getLocation() != null) {
            String location = profile.getLocation().getCity() + ", " +
                            profile.getLocation().getProvince() + ", " +
                            profile.getLocation().getCountry();
            profileLocation.setText(location);
        }

        // Deporte
        if (profile.getSport() != null) {
            profileSport.setText("Deporte: " + profile.getSport().getName());
        }

        // Descripción
        if (profile.getDescription() != null && !profile.getDescription().isEmpty()) {
            profileDescription.setText(profile.getDescription());
            profileDescription.setVisibility(View.VISIBLE);
        } else {
            profileDescription.setVisibility(View.GONE);
        }

        // Foto de perfil
        if (profile.getPhotoUrl() != null && !profile.getPhotoUrl().isEmpty()) {
            Glide.with(this)
                    .load(profile.getPhotoUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(profilePhoto);
        }

        // Información de contacto (solo si hay match)
        if (profile.isHasMatch()) {
            // Teléfono
            if (profile.getPhoneNumber() != null && !profile.getPhoneNumber().isEmpty()) {
                profilePhone.setText("📱 " + profile.getPhoneNumber());
                profilePhone.setVisibility(View.VISIBLE);

                // Botón de WhatsApp
                btnWhatsApp.setVisibility(View.VISIBLE);
                btnWhatsApp.setOnClickListener(v -> openWhatsApp(profile.getPhoneNumber()));
            } else {
                profilePhone.setVisibility(View.GONE);
                btnWhatsApp.setVisibility(View.GONE);
            }

            // Instagram
            if (profile.getIgUser() != null && !profile.getIgUser().isEmpty()) {
                profileInstagram.setText("📷 @" + profile.getIgUser());
                profileInstagram.setVisibility(View.VISIBLE);
                profileInstagram.setOnClickListener(v -> openInstagram(profile.getIgUser()));
            } else {
                profileInstagram.setVisibility(View.GONE);
            }

            // Twitter/X
            if (profile.getXUser() != null && !profile.getXUser().isEmpty()) {
                profileTwitter.setText("𝕏 @" + profile.getXUser());
                profileTwitter.setVisibility(View.VISIBLE);
                profileTwitter.setOnClickListener(v -> openTwitter(profile.getXUser()));
            } else {
                profileTwitter.setVisibility(View.GONE);
            }
        } else {
            profilePhone.setVisibility(View.GONE);
            profileInstagram.setVisibility(View.GONE);
            profileTwitter.setVisibility(View.GONE);
            btnWhatsApp.setVisibility(View.GONE);
        }
    }

    private String getProfileTypeDisplayName(String profileType) {
        switch (profileType) {
            case "athlete":
                return "Atleta";
            case "team":
                return "Equipo";
            case "agent":
                return "Agente";
            default:
                return profileType;
        }
    }

    private void openWhatsApp(String phoneNumber) {
        try {
            String cleanNumber = phoneNumber.replaceAll("[^0-9+]", "");
            String url = "https://wa.me/" + cleanNumber;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }

    private void openInstagram(String username) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.instagram.com/" + username));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir Instagram", Toast.LENGTH_SHORT).show();
        }
    }

    private void openTwitter(String username) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://twitter.com/" + username));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir Twitter", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}

