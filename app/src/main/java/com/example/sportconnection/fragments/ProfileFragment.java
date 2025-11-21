package com.example.sportconnection.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.sportconnection.HomeActivity;
import com.example.sportconnection.MainActivity;
import com.example.sportconnection.ProfileFormActivity;
import com.example.sportconnection.R;
import com.example.sportconnection.model.DeletePhotoResponse;
import com.example.sportconnection.model.Location;
import com.example.sportconnection.model.GetProfileResponse;
import com.example.sportconnection.model.Sport;
import com.example.sportconnection.model.UploadPhotoResponse;
import com.example.sportconnection.repository.AuthRepository;
import com.example.sportconnection.repository.LookupRepository;
import com.example.sportconnection.utils.LoadingDialog;
import com.example.sportconnection.utils.SessionManager;
import com.example.sportconnection.utils.ThreadManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final int REQUEST_PERMISSION_READ_STORAGE = 100;

    private ImageView imageProfilePhoto;
    private Button buttonChangePhoto;
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

    // API Calls tracking
    private Call<GetProfileResponse> currentProfileCall;
    private Call<UploadPhotoResponse> currentUploadCall;
    private Call<DeletePhotoResponse> currentDeleteCall;
    private Call<List<Sport>> currentSportsCall;
    private Call<List<Location>> currentLocationsCall;

    // Launcher para seleccionar imagen
    private ActivityResultLauncher<Intent> pickImageLauncher;
    // Launcher para permisos
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inicializar launchers
        initializeLaunchers();

        // Inicializar vistas
        imageProfilePhoto = view.findViewById(R.id.imageProfilePhoto);
        buttonChangePhoto = view.findViewById(R.id.buttonChangePhoto);
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

        // Configurar botón de cambiar foto
        buttonChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotoOptions();
            }
        });

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


    private void initializeLaunchers() {
        // Launcher para seleccionar imagen
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadPhotoFromUri(imageUri);
                        }
                    }
                }
        );

        // Launcher para permisos
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(requireContext(), "Permiso denegado", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void showPhotoOptions() {
        // Mostrar opciones para cambiar o eliminar foto
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Foto de perfil");

        String[] options;
        if (currentProfile != null && currentProfile.getPhotoUrl() != null && !currentProfile.getPhotoUrl().isEmpty()) {
            options = new String[]{"Cambiar foto", "Eliminar foto", "Cancelar"};
        } else {
            options = new String[]{"Subir foto", "Cancelar"};
        }

        builder.setItems(options, (dialog, which) -> {
            if (options[which].equals("Cambiar foto") || options[which].equals("Subir foto")) {
                checkPermissionAndOpenPicker();
            } else if (options[which].equals("Eliminar foto")) {
                deleteProfilePhoto();
            }
        });

        builder.show();
    }

    private void checkPermissionAndOpenPicker() {
        // En Android 13+ (API 33+) no se necesita permiso para leer imágenes seleccionadas por el usuario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openImagePicker();
        } else {
            // Para versiones anteriores, verificar permiso de lectura
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void uploadPhotoFromUri(Uri imageUri) {
        loadingDialog.show("Subiendo foto...");

        threadManager.executeInBackground(() -> {
            try {
                // Copiar el archivo a la caché de la app
                File cacheDir = requireContext().getCacheDir();
                File photoFile = new File(cacheDir, "profile_photo_" + System.currentTimeMillis() + ".jpg");

                InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                FileOutputStream outputStream = new FileOutputStream(photoFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                inputStream.close();
                outputStream.close();

                // Subir el archivo en el hilo principal
                threadManager.executeOnMainThread(() -> {
                    uploadPhoto(photoFile);
                });

            } catch (Exception e) {
                Log.e(TAG, "Error al copiar archivo: " + e.getMessage());
                threadManager.executeOnMainThread(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(requireContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void uploadPhoto(File photoFile) {
        String token = sessionManager.getToken();
        if (token == null) {
            loadingDialog.dismiss();
            Toast.makeText(requireContext(), "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        authRepository.uploadProfilePhoto(token, photoFile, new Callback<UploadPhotoResponse>() {
            @Override
            public void onResponse(Call<UploadPhotoResponse> call, Response<UploadPhotoResponse> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    UploadPhotoResponse uploadResponse = response.body();
                    Toast.makeText(requireContext(), "Foto subida exitosamente", Toast.LENGTH_SHORT).show();

                    // Actualizar la imagen inmediatamente
                    if (uploadResponse.getPhotoUrl() != null) {
                        loadProfilePhoto(uploadResponse.getPhotoUrl());
                    }

                    // Recargar el perfil completo
                    loadUserProfile();
                } else {
                    Toast.makeText(requireContext(), "Error al subir la foto", Toast.LENGTH_SHORT).show();
                }

                // Eliminar el archivo temporal
                if (photoFile.exists()) {
                    photoFile.delete();
                }
            }

            @Override
            public void onFailure(Call<UploadPhotoResponse> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();

                // Eliminar el archivo temporal
                if (photoFile.exists()) {
                    photoFile.delete();
                }
            }
        });
    }

    private void deleteProfilePhoto() {
        loadingDialog.show("Eliminando foto...");

        String token = sessionManager.getToken();
        if (token == null) {
            loadingDialog.dismiss();
            Toast.makeText(requireContext(), "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        authRepository.deleteProfilePhoto(token, new Callback<DeletePhotoResponse>() {
            @Override
            public void onResponse(Call<DeletePhotoResponse> call, Response<DeletePhotoResponse> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Foto eliminada exitosamente", Toast.LENGTH_SHORT).show();

                    // Mostrar imagen por defecto
                    imageProfilePhoto.setImageResource(android.R.drawable.ic_menu_camera);

                    // Recargar el perfil
                    loadUserProfile();
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar la foto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DeletePhotoResponse> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfilePhoto(String photoUrl) {
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .error(android.R.drawable.ic_menu_camera)
                    .into(imageProfilePhoto);
        } else {
            imageProfilePhoto.setImageResource(android.R.drawable.ic_menu_camera);
        }
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
                // Error al cargar sports
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

        // Cargar foto de perfil
        loadProfilePhoto(currentProfile.getPhotoUrl());

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
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar el perfil cuando volvemos a este fragment
        if (currentProfile != null) {
            loadUserProfile();
        }
        // Notificar que el fragmento está listo
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).setFragmentLoading(false);
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

        // Cancelar llamadas API pendientes
        if (currentProfileCall != null && !currentProfileCall.isCanceled()) {
            currentProfileCall.cancel();
        }
        if (currentUploadCall != null && !currentUploadCall.isCanceled()) {
            currentUploadCall.cancel();
        }
        if (currentDeleteCall != null && !currentDeleteCall.isCanceled()) {
            currentDeleteCall.cancel();
        }
        if (currentSportsCall != null && !currentSportsCall.isCanceled()) {
            currentSportsCall.cancel();
        }
        if (currentLocationsCall != null && !currentLocationsCall.isCanceled()) {
            currentLocationsCall.cancel();
        }

        // Cerrar el diálogo si está abierto
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}

