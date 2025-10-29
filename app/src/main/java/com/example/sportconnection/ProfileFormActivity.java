package com.example.sportconnection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sportconnection.model.ProfileRequest;
import com.example.sportconnection.model.ProfileResponse;
import com.example.sportconnection.repository.AuthRepository;
import com.example.sportconnection.utils.LoadingDialog;
import com.example.sportconnection.utils.SessionManager;
import com.example.sportconnection.utils.ThreadManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFormActivity extends AppCompatActivity {

    private TextView titleProfileForm;
    private EditText editTextName, editTextLastName, editTextBirthdate;
    private EditText editTextHeight, editTextWeight, editTextLocation;
    private EditText editTextSport, editTextPhone, editTextInstagram;
    private EditText editTextTwitter, editTextDescription;
    private Button buttonComplete;

    private String email, password, profileType, token;
    private int userId;

    private AuthRepository authRepository;
    private SessionManager sessionManager;
    private LoadingDialog loadingDialog;
    private ThreadManager threadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_form);

        // Obtener datos del Intent
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        profileType = getIntent().getStringExtra("profileType");
        token = getIntent().getStringExtra("token");
        userId = getIntent().getIntExtra("userId", -1);

        // Inicializar vistas
        titleProfileForm = findViewById(R.id.titleProfileForm);
        editTextName = findViewById(R.id.editTextName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextBirthdate = findViewById(R.id.editTextBirthdate);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextWeight = findViewById(R.id.editTextWeight);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextSport = findViewById(R.id.editTextSport);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextInstagram = findViewById(R.id.editTextInstagram);
        editTextTwitter = findViewById(R.id.editTextTwitter);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonComplete = findViewById(R.id.buttonComplete);

        // Inicializar repositorio, session manager y utilidades
        authRepository = new AuthRepository();
        sessionManager = new SessionManager(this);
        loadingDialog = new LoadingDialog(this);
        threadManager = ThreadManager.getInstance();

        // Configurar el formulario según el tipo de perfil
        setupFormByProfileType();

        // Configurar el botón de completar
        buttonComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    completeRegistration();
                }
            }
        });
    }

    private void setupFormByProfileType() {
        switch (profileType) {
            case "athlete":
                titleProfileForm.setText("Perfil de Atleta");
                editTextName.setHint("Nombre");
                editTextLastName.setVisibility(View.VISIBLE);
                editTextBirthdate.setVisibility(View.VISIBLE);
                editTextHeight.setVisibility(View.VISIBLE);
                editTextWeight.setVisibility(View.VISIBLE);
                break;

            case "agent":
                titleProfileForm.setText("Perfil de Agente");
                editTextName.setHint("Nombre");
                editTextLastName.setVisibility(View.VISIBLE);
                editTextBirthdate.setVisibility(View.GONE);
                editTextHeight.setVisibility(View.GONE);
                editTextWeight.setVisibility(View.GONE);
                editTextDescription.setHint("Descripción");
                break;

            case "team":
                titleProfileForm.setText("Perfil de Equipo");
                editTextName.setHint("Nombre del equipo");
                editTextLastName.setVisibility(View.VISIBLE);
                editTextLastName.setHint("Puesto/Rol");
                editTextBirthdate.setVisibility(View.GONE);
                editTextHeight.setVisibility(View.GONE);
                editTextWeight.setVisibility(View.GONE);
                break;
        }
    }

    private boolean validateForm() {
        String name = editTextName.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String sport = editTextSport.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa el nombre", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (profileType.equals("athlete") || profileType.equals("agent")) {
            String lastName = editTextLastName.getText().toString().trim();
            if (lastName.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa el apellido", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (profileType.equals("athlete")) {
            String birthdate = editTextBirthdate.getText().toString().trim();
            String height = editTextHeight.getText().toString().trim();
            String weight = editTextWeight.getText().toString().trim();

            if (birthdate.isEmpty() || height.isEmpty() || weight.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (location.isEmpty() || sport.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Por favor, completa ubicación, deporte y teléfono", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void completeRegistration() {
        // Mostrar diálogo de carga
        loadingDialog.show("Creando perfil...");
        buttonComplete.setEnabled(false);

        // Crear el objeto ProfileRequest en segundo plano
        threadManager.executeInBackground(new Runnable() {
            @Override
            public void run() {
                final ProfileRequest profileRequest = new ProfileRequest();
                profileRequest.setUserId(userId);
                profileRequest.setProfileType(profileType);
                profileRequest.setName(editTextName.getText().toString().trim());
                profileRequest.setDescription(editTextDescription.getText().toString().trim());
                profileRequest.setPhoneNumber(editTextPhone.getText().toString().trim());
                profileRequest.setIgUser(editTextInstagram.getText().toString().trim());
                profileRequest.setxUser(editTextTwitter.getText().toString().trim());

                // Convertir location_id y sport_id a Integer
                try {
                    String locationStr = editTextLocation.getText().toString().trim();
                    if (!locationStr.isEmpty()) {
                        profileRequest.setLocationId(Integer.parseInt(locationStr));
                    }
                } catch (NumberFormatException e) {
                    threadManager.executeOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.dismiss();
                            Toast.makeText(ProfileFormActivity.this, "ID de ubicación inválido", Toast.LENGTH_SHORT).show();
                            buttonComplete.setEnabled(true);
                        }
                    });
                    return;
                }

                try {
                    String sportStr = editTextSport.getText().toString().trim();
                    if (!sportStr.isEmpty()) {
                        profileRequest.setSportId(Integer.parseInt(sportStr));
                    }
                } catch (NumberFormatException e) {
                    threadManager.executeOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.dismiss();
                            Toast.makeText(ProfileFormActivity.this, "ID de deporte inválido", Toast.LENGTH_SHORT).show();
                            buttonComplete.setEnabled(true);
                        }
                    });
                    return;
                }

                // Campos específicos según el tipo de perfil
                if (profileType.equals("athlete")) {
                    profileRequest.setLastName(editTextLastName.getText().toString().trim());
                    profileRequest.setBirthdate(editTextBirthdate.getText().toString().trim());
                    profileRequest.setHeight(editTextHeight.getText().toString().trim());
                    profileRequest.setWeight(editTextWeight.getText().toString().trim());
                } else if (profileType.equals("agent")) {
                    profileRequest.setLastName(editTextLastName.getText().toString().trim());
                } else if (profileType.equals("team")) {
                    profileRequest.setJob(editTextLastName.getText().toString().trim());
                }

                // Enviar al backend
                authRepository.createProfile(token, profileRequest, new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                        threadManager.executeOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismiss();
                                buttonComplete.setEnabled(true);

                                if (response.isSuccessful() && response.body() != null) {
                                    ProfileResponse profileResponse = response.body();

                                    if (profileResponse.isSuccess()) {
                                        // Guardar la sesión en segundo plano
                                        threadManager.executeInBackground(new Runnable() {
                                            @Override
                                            public void run() {
                                                sessionManager.saveSession(token, userId, email, profileType);

                                                // Navegar en el hilo principal
                                                threadManager.executeOnMainThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(ProfileFormActivity.this,
                                                            "¡Perfil creado exitosamente!", Toast.LENGTH_SHORT).show();

                                                        // Pequeño delay para mostrar el mensaje
                                                        threadManager.executeWithDelay(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Intent intent = new Intent(ProfileFormActivity.this, HomeActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        }, 500);
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        String errorMsg = profileResponse.getMessage();
                                        if (errorMsg == null || errorMsg.isEmpty()) {
                                            errorMsg = "Error al crear el perfil";
                                        }
                                        Toast.makeText(ProfileFormActivity.this,
                                            errorMsg, Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    String errorMsg = response.message();
                                    if (errorMsg == null || errorMsg.isEmpty()) {
                                        errorMsg = "Error desconocido en el servidor";
                                    }
                                    Toast.makeText(ProfileFormActivity.this,
                                        "Error al crear perfil: " + errorMsg, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {
                        threadManager.executeOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismiss();
                                buttonComplete.setEnabled(true);

                                String errorMsg = t.getMessage();
                                if (errorMsg == null || errorMsg.isEmpty()) {
                                    errorMsg = "Error desconocido";
                                }
                                Toast.makeText(ProfileFormActivity.this,
                                    "Error de conexión: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cerrar el diálogo si está abierto
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}

