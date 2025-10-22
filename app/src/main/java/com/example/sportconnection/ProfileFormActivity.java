package com.example.sportconnection;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sportconnection.model.ProfileRequest;
import com.example.sportconnection.model.ProfileResponse;
import com.example.sportconnection.repository.AuthRepository;
import com.example.sportconnection.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFormActivity extends AppCompatActivity {

    private TextView titleProfileForm;
    private EditText editTextName, editTextLastName, editTextBirthdate;
    private EditText editTextHeight, editTextWeight, editTextLocation;
    private EditText editTextSport, editTextPhone, editTextInstagram;
    private EditText editTextTwitter, editTextDescription, editTextAgency;
    private Button buttonComplete;

    private String email, password, profileType, token;
    private int userId;

    private AuthRepository authRepository;
    private SessionManager sessionManager;

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

        // Inicializar repositorio y session manager
        authRepository = new AuthRepository();
        sessionManager = new SessionManager(this);

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
            case "ATHLETE":
                titleProfileForm.setText("Perfil de Atleta");
                // Todos los campos son visibles para atleta
                editTextName.setHint("Nombre");
                editTextLastName.setVisibility(View.VISIBLE);
                editTextBirthdate.setVisibility(View.VISIBLE);
                editTextHeight.setVisibility(View.VISIBLE);
                editTextWeight.setVisibility(View.VISIBLE);
                break;

            case "AGENT":
                titleProfileForm.setText("Perfil de Agente");
                editTextName.setHint("Nombre");
                editTextLastName.setVisibility(View.VISIBLE);
                editTextBirthdate.setVisibility(View.GONE);
                editTextHeight.setVisibility(View.GONE);
                editTextWeight.setVisibility(View.GONE);
                editTextDescription.setHint("Descripción");
                break;

            case "TEAM":
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

        if (profileType.equals("ATHLETE") || profileType.equals("AGENT")) {
            String lastName = editTextLastName.getText().toString().trim();
            if (lastName.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa el apellido", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (profileType.equals("ATHLETE")) {
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
        buttonComplete.setEnabled(false);

        // Crear el objeto ProfileRequest
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setEmail(email);
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
            Toast.makeText(this, "ID de ubicación inválido", Toast.LENGTH_SHORT).show();
            buttonComplete.setEnabled(true);
            return;
        }

        try {
            String sportStr = editTextSport.getText().toString().trim();
            if (!sportStr.isEmpty()) {
                profileRequest.setSportId(Integer.parseInt(sportStr));
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "ID de deporte inválido", Toast.LENGTH_SHORT).show();
            buttonComplete.setEnabled(true);
            return;
        }

        // Campos específicos según el tipo de perfil
        if (profileType.equals("ATHLETE")) {
            profileRequest.setLastName(editTextLastName.getText().toString().trim());
            profileRequest.setBirthdate(editTextBirthdate.getText().toString().trim());
            profileRequest.setHeight(editTextHeight.getText().toString().trim());
            profileRequest.setWeight(editTextWeight.getText().toString().trim());
        } else if (profileType.equals("AGENT")) {
            profileRequest.setLastName(editTextLastName.getText().toString().trim());
            // Para agente, la descripción puede incluir el nombre de la agencia
            // o puedes agregar un campo adicional
        } else if (profileType.equals("TEAM")) {
            // Para TEAM, el campo lastName se usa como "job" (puesto/rol)
            profileRequest.setJob(editTextLastName.getText().toString().trim());
        }

        // Enviar al backend
        authRepository.createProfile(token, profileRequest, new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                buttonComplete.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profileResponse = response.body();

                    if (profileResponse.isSuccess()) {
                        // Guardar la sesión completa con el perfil
                        sessionManager.saveSession(token, userId, email, profileType);

                        Toast.makeText(ProfileFormActivity.this,
                            "¡Perfil creado exitosamente!", Toast.LENGTH_LONG).show();

                        // Navegar a la pantalla principal
                        // TODO: Crear pantalla principal de la app
                        finish();
                    } else {
                        Toast.makeText(ProfileFormActivity.this,
                            profileResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ProfileFormActivity.this,
                        "Error al crear perfil: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                buttonComplete.setEnabled(true);
                Toast.makeText(ProfileFormActivity.this,
                    "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

