package com.example.sportconnection.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sportconnection.MainActivity;
import com.example.sportconnection.ProfileFormActivity;
import com.example.sportconnection.R;
import com.example.sportconnection.model.Location;
import com.example.sportconnection.model.GetProfileResponse;
import com.example.sportconnection.model.Sport;
import com.example.sportconnection.repository.AuthRepository;
import com.example.sportconnection.repository.LookupRepository;
import com.example.sportconnection.utils.LoadingDialog;
import com.example.sportconnection.utils.SessionManager;
import com.example.sportconnection.utils.ThreadManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private TextView textUserEmail;
    private TextView textUserType;
    private TextView textUserName;
    private TextView textUserLastName;
    private TextView textUserBirthdate;
    private TextView textUserHeight;
    private TextView textUserWeight;
    private TextView textUserLocation;
    private TextView textUserSport;
    private TextView textUserPhone;
    private TextView textUserInstagram;
    private TextView textUserTwitter;
    private TextView textUserAgency;
    private TextView textUserJob;
    private TextView textUserDescription;
    private Button buttonEditProfile;
    private Button buttonLogout;

    private SessionManager sessionManager;
    private AuthRepository authRepository;
    private LookupRepository lookupRepository;
    private LoadingDialog loadingDialog;
    private ThreadManager threadManager;

    private GetProfileResponse.ProfileData currentProfile;
    private String currentProfileType; // Tipo de perfil del nivel superior de la respuesta
    private List<Sport> sportList;
    private List<Location> locationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inicializar vistas
        textUserEmail = view.findViewById(R.id.textUserEmail);
        textUserType = view.findViewById(R.id.textUserType);
        textUserName = view.findViewById(R.id.textUserName);
        textUserLastName = view.findViewById(R.id.textUserLastName);
        textUserBirthdate = view.findViewById(R.id.textUserBirthdate);
        textUserHeight = view.findViewById(R.id.textUserHeight);
        textUserWeight = view.findViewById(R.id.textUserWeight);
        textUserLocation = view.findViewById(R.id.textUserLocation);
        textUserSport = view.findViewById(R.id.textUserSport);
        textUserPhone = view.findViewById(R.id.textUserPhone);
        textUserInstagram = view.findViewById(R.id.textUserInstagram);
        textUserTwitter = view.findViewById(R.id.textUserTwitter);
        textUserAgency = view.findViewById(R.id.textUserAgency);
        textUserJob = view.findViewById(R.id.textUserJob);
        textUserDescription = view.findViewById(R.id.textUserDescription);
        buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
        buttonLogout = view.findViewById(R.id.buttonLogout);

        // Inicializar utilidades
        sessionManager = new SessionManager(requireContext());
        authRepository = new AuthRepository();
        lookupRepository = new LookupRepository();
        loadingDialog = new LoadingDialog(requireContext());
        threadManager = ThreadManager.getInstance();

        // Cargar información del usuario
        loadUserProfile();

        // Configurar botón de editar perfil
        buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentProfile != null) {
                    openEditProfile();
                } else {
                    Toast.makeText(requireContext(), "Cargando información del perfil...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Configurar botón de cerrar sesión
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        return view;
    }

    private void loadUserProfile() {
        loadingDialog.show("Cargando perfil...");

        final String token = sessionManager.getToken();
        final int userId = sessionManager.getUserId();

        if (token == null || userId == -1) {
            loadingDialog.dismiss();
            Toast.makeText(requireContext(), "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Cargando perfil para userId: " + userId);

        // Cargar sports y locations primero
        loadLookupsData(new Runnable() {
            @Override
            public void run() {
                // Una vez cargados los lookups, cargar el perfil
                authRepository.getProfile(token, new Callback<GetProfileResponse>() {
                    @Override
                    public void onResponse(Call<GetProfileResponse> call, Response<GetProfileResponse> response) {
                        loadingDialog.dismiss();

                        if (response.isSuccessful() && response.body() != null) {
                            GetProfileResponse profileResponse = response.body();
                            if (profileResponse.getProfile() != null) {
                                currentProfile = profileResponse.getProfile();
                                currentProfileType = profileResponse.getProfileType(); // Guardar el tipo de perfil
                                displayProfileData();
                            } else {
                                Toast.makeText(requireContext(), "Error: Perfil no encontrado", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Error al cargar perfil: " + response.code());
                            Toast.makeText(requireContext(), "Error al cargar el perfil", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<GetProfileResponse> call, Throwable t) {
                        loadingDialog.dismiss();
                        Log.e(TAG, "Error de conexión: " + t.getMessage());
                        Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadLookupsData(final Runnable onComplete) {
        // Cargar deportes
        lookupRepository.getSports(new Callback<List<Sport>>() {
            @Override
            public void onResponse(Call<List<Sport>> call, Response<List<Sport>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sportList = response.body();
                    checkLookupsLoaded(onComplete);
                }
            }

            @Override
            public void onFailure(Call<List<Sport>> call, Throwable t) {
                Log.e(TAG, "Error al cargar sports: " + t.getMessage());
            }
        });

        // Cargar ubicaciones
        lookupRepository.getLocations(new Callback<List<Location>>() {
            @Override
            public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    locationList = response.body();
                    checkLookupsLoaded(onComplete);
                }
            }

            @Override
            public void onFailure(Call<List<Location>> call, Throwable t) {
                Log.e(TAG, "Error al cargar locations: " + t.getMessage());
            }
        });
    }

    private void checkLookupsLoaded(Runnable onComplete) {
        if (sportList != null && locationList != null && onComplete != null) {
            onComplete.run();
        }
    }

    private void displayProfileData() {
        if (currentProfile == null) return;

        String email = sessionManager.getEmail();
        String profileType = currentProfileType; // Usar el profileType guardado del nivel superior

        textUserEmail.setText("Email: " + (email != null ? email : "No disponible"));

        // Traducir el tipo de perfil
        String profileTypeText = "No disponible";
        if (profileType != null) {
            switch (profileType.toLowerCase()) {
                case "athlete":
                    profileTypeText = "Atleta";
                    break;
                case "agent":
                    profileTypeText = "Agente";
                    break;
                case "team":
                    profileTypeText = "Equipo";
                    break;
                default:
                    profileTypeText = profileType;
            }
        }
        textUserType.setText("Tipo: " + profileTypeText);

        // Información común
        textUserName.setText("Nombre: " + (currentProfile.getName() != null ? currentProfile.getName() : "No disponible"));

        if (currentProfile.getLastName() != null && !currentProfile.getLastName().isEmpty()) {
            textUserLastName.setVisibility(View.VISIBLE);
            textUserLastName.setText("Apellido: " + currentProfile.getLastName());
        }

        // Ubicación
        String locationText = "No disponible";
        if (currentProfile.getLocationId() != null && locationList != null) {
            for (Location location : locationList) {
                if (location.getId() == currentProfile.getLocationId()) {
                    locationText = location.toString();
                    break;
                }
            }
        }
        textUserLocation.setText("Ubicación: " + locationText);

        // Deporte
        String sportText = "No disponible";
        if (currentProfile.getSportId() != null && sportList != null) {
            for (Sport sport : sportList) {
                if (sport.getId() == currentProfile.getSportId()) {
                    sportText = sport.getName();
                    break;
                }
            }
        }
        textUserSport.setText("Deporte: " + sportText);

        textUserPhone.setText("Teléfono: " + (currentProfile.getPhoneNumber() != null ? currentProfile.getPhoneNumber() : "No disponible"));

        if (currentProfile.getIgUser() != null && !currentProfile.getIgUser().isEmpty()) {
            textUserInstagram.setVisibility(View.VISIBLE);
            textUserInstagram.setText("Instagram: " + currentProfile.getIgUser());
        }

        if (currentProfile.getxUser() != null && !currentProfile.getxUser().isEmpty()) {
            textUserTwitter.setVisibility(View.VISIBLE);
            textUserTwitter.setText("X (Twitter): " + currentProfile.getxUser());
        }

        if (currentProfile.getDescription() != null && !currentProfile.getDescription().isEmpty()) {
            textUserDescription.setVisibility(View.VISIBLE);
            textUserDescription.setText("Descripción: " + currentProfile.getDescription());
        }

        // Campos específicos por tipo de perfil
        if ("athlete".equalsIgnoreCase(profileType)) {
            if (currentProfile.getBirthdate() != null && !currentProfile.getBirthdate().isEmpty()) {
                textUserBirthdate.setVisibility(View.VISIBLE);
                textUserBirthdate.setText("Fecha de nacimiento: " + formatDate(currentProfile.getBirthdate()));
            }

            if (currentProfile.getHeight() != null && !currentProfile.getHeight().isEmpty()) {
                textUserHeight.setVisibility(View.VISIBLE);
                textUserHeight.setText("Altura: " + currentProfile.getHeight() + " cm");
            }

            if (currentProfile.getWeight() != null && !currentProfile.getWeight().isEmpty()) {
                textUserWeight.setVisibility(View.VISIBLE);
                textUserWeight.setText("Peso: " + currentProfile.getWeight() + " kg");
            }
        } else if ("agent".equalsIgnoreCase(profileType)) {
            if (currentProfile.getAgency() != null && !currentProfile.getAgency().isEmpty()) {
                textUserAgency.setVisibility(View.VISIBLE);
                textUserAgency.setText("Agencia: " + currentProfile.getAgency());
            }
        } else if ("team".equalsIgnoreCase(profileType)) {
            if (currentProfile.getJob() != null && !currentProfile.getJob().isEmpty()) {
                textUserJob.setVisibility(View.VISIBLE);
                textUserJob.setText("Puesto: " + currentProfile.getJob());
            }
        }
    }

    private String formatDate(String isoDate) {
        // Convertir de YYYY-MM-DD a DD/MM/YYYY
        if (isoDate != null && isoDate.length() == 10) {
            String[] parts = isoDate.split("-");
            if (parts.length == 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
        }
        return isoDate;
    }

    private void openEditProfile() {
        Intent intent = new Intent(requireActivity(), ProfileFormActivity.class);
        intent.putExtra("isEditMode", true);
        intent.putExtra("profileType", currentProfileType);
        Log.d(TAG, "Abriendo formulario de edición con profileType: " + currentProfileType);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar el perfil cuando volvemos a este fragment
        if (currentProfile != null) {
            loadUserProfile();
        }
    }

    private void logoutUser() {
        loadingDialog.show("Cerrando sesión...");
        buttonLogout.setEnabled(false);

        threadManager.executeInBackground(new Runnable() {
            @Override
            public void run() {
                // Cerrar sesión
                sessionManager.logout();

                // Pequeño delay para dar feedback visual
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Redirigir a MainActivity en el hilo principal
                threadManager.executeOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();

                        Intent intent = new Intent(requireActivity(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cerrar el diálogo si está abierto
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}

