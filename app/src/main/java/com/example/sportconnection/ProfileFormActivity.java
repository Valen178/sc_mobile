package com.example.sportconnection;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.example.sportconnection.model.Location;
import com.example.sportconnection.model.ProfileRequest;
import com.example.sportconnection.model.ProfileResponse;
import com.example.sportconnection.model.GetProfileResponse;
import com.example.sportconnection.model.UpdateProfileRequest;
import com.example.sportconnection.model.Sport;
import com.example.sportconnection.repository.AuthRepository;
import com.example.sportconnection.repository.LookupRepository;
import com.example.sportconnection.utils.LoadingDialog;
import com.example.sportconnection.utils.SessionManager;
import com.example.sportconnection.utils.ThreadManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFormActivity extends AppCompatActivity {

    private static final String TAG = "ProfileFormActivity";

    private TextView titleProfileForm;
    private EditText editTextName, editTextLastName, editTextBirthdate;
    private EditText editTextHeight, editTextWeight;
    private Spinner spinnerLocation, spinnerSport;
    private EditText editTextPhone, editTextInstagram;
    private EditText editTextTwitter, editTextDescription, editTextAgency;
    private Button buttonComplete;

    private String email, password, profileType, token;
    private int userId;

    // Variables para modo edición
    private boolean isEditMode = false;
    private int profileId = -1;
    private GetProfileResponse.ProfileData currentProfile;

    private AuthRepository authRepository;
    private LookupRepository lookupRepository;
    private SessionManager sessionManager;
    private LoadingDialog loadingDialog;
    private ThreadManager threadManager;

    private List<Sport> sportList = new ArrayList<>();
    private List<Location> locationList = new ArrayList<>();
    private int selectedSportId = -1;
    private int selectedLocationId = -1;

    private String selectedBirthdate = ""; // Formato ISO: YYYY-MM-DD
    private Calendar birthdateCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_form);

        // Obtener datos del Intent
        isEditMode = getIntent().getBooleanExtra("isEditMode", false);

        if (isEditMode) {
            // Modo edición - obtener desde sesión o intent
            sessionManager = new SessionManager(this);
            token = sessionManager.getToken();
            userId = sessionManager.getUserId();
            email = sessionManager.getEmail();

            // Intentar obtener profileType del intent primero (más confiable), si no, de la sesión
            profileType = getIntent().getStringExtra("profileType");
            if (profileType == null) {
                profileType = sessionManager.getProfileType();
                Log.d(TAG, "onCreate: ProfileType obtenido de sesión: " + profileType);
            } else {
                Log.d(TAG, "onCreate: ProfileType obtenido de intent: " + profileType);
            }

            Log.d(TAG, "onCreate: Modo EDICIÓN - profileType=" + profileType);
        } else {
            // Modo creación - obtener desde intent
            email = getIntent().getStringExtra("email");
            password = getIntent().getStringExtra("password");
            profileType = getIntent().getStringExtra("profileType");
            token = getIntent().getStringExtra("token");
            userId = getIntent().getIntExtra("userId", -1);

            Log.d(TAG, "onCreate: Modo CREACIÓN - profileType=" + profileType + ", userId=" + userId);
        }

        // Inicializar vistas
        titleProfileForm = findViewById(R.id.titleProfileForm);
        editTextName = findViewById(R.id.editTextName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextBirthdate = findViewById(R.id.editTextBirthdate);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextWeight = findViewById(R.id.editTextWeight);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        spinnerSport = findViewById(R.id.spinnerSport);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextInstagram = findViewById(R.id.editTextInstagram);
        editTextTwitter = findViewById(R.id.editTextTwitter);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextAgency = findViewById(R.id.editTextAgency);
        buttonComplete = findViewById(R.id.buttonComplete);

        // Inicializar repositorios y utilidades
        authRepository = new AuthRepository();
        lookupRepository = new LookupRepository();
        if (sessionManager == null) {
            sessionManager = new SessionManager(this);
        }
        loadingDialog = new LoadingDialog(this);
        threadManager = ThreadManager.getInstance();

        // Configurar el formulario según el tipo de perfil
        setupFormByProfileType();

        // Cargar datos de sports y locations
        loadSportsAndLocations();

        // Configurar el selector de fecha de nacimiento
        setupBirthdatePicker();

        // Si es modo edición, cargar los datos actuales del perfil
        if (isEditMode) {
            buttonComplete.setText("Guardar Cambios");
            loadCurrentProfile();
        }

        // Configurar el botón de completar/guardar
        buttonComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    if (isEditMode) {
                        updateProfile();
                    } else {
                        completeRegistration();
                    }
                }
            }
        });
    }

    private void setupFormByProfileType() {
        Log.d(TAG, "setupFormByProfileType: " + profileType);

        switch (profileType.toLowerCase()) {
            case "athlete":
                titleProfileForm.setText("Perfil de Atleta");
                editTextName.setHint("Nombre");
                editTextLastName.setVisibility(View.VISIBLE);
                editTextLastName.setHint("Apellido");
                editTextBirthdate.setVisibility(View.VISIBLE);
                editTextHeight.setVisibility(View.VISIBLE);
                editTextWeight.setVisibility(View.VISIBLE);
                editTextAgency.setVisibility(View.GONE);
                break;

            case "agent":
                titleProfileForm.setText("Perfil de Agente");
                editTextName.setHint("Nombre");
                editTextLastName.setVisibility(View.VISIBLE);
                editTextLastName.setHint("Apellido");
                editTextBirthdate.setVisibility(View.GONE);
                editTextHeight.setVisibility(View.GONE);
                editTextWeight.setVisibility(View.GONE);
                editTextDescription.setHint("Descripción");
                editTextAgency.setVisibility(View.VISIBLE);
                break;

            case "team":
                titleProfileForm.setText("Perfil de Equipo");
                editTextName.setHint("Nombre del equipo");
                editTextLastName.setVisibility(View.VISIBLE);
                editTextLastName.setHint("Puesto/Rol");
                editTextBirthdate.setVisibility(View.GONE);
                editTextHeight.setVisibility(View.GONE);
                editTextWeight.setVisibility(View.GONE);
                editTextAgency.setVisibility(View.GONE);
                break;
        }
    }

    private void setupBirthdatePicker() {
        // Configurar fecha máxima (hoy)
        final Calendar maxDate = Calendar.getInstance();

        // Configurar fecha mínima (40 años atrás)
        final Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -40);

        // Configurar fecha inicial (18 años atrás, edad mínima típica)
        birthdateCalendar.add(Calendar.YEAR, -18);

        editTextBirthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = birthdateCalendar.get(Calendar.YEAR);
                int month = birthdateCalendar.get(Calendar.MONTH);
                int day = birthdateCalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ProfileFormActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            // Actualizar el calendario con la fecha seleccionada
                            birthdateCalendar.set(Calendar.YEAR, year);
                            birthdateCalendar.set(Calendar.MONTH, month);
                            birthdateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            // Formato para mostrar al usuario (DD/MM/YYYY)
                            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            String displayDate = displayFormat.format(birthdateCalendar.getTime());
                            editTextBirthdate.setText(displayDate);

                            // Formato ISO para enviar al backend (YYYY-MM-DD)
                            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            selectedBirthdate = isoFormat.format(birthdateCalendar.getTime());

                            Log.d(TAG, "Fecha seleccionada - Display: " + displayDate + ", ISO: " + selectedBirthdate);
                        }
                    },
                    year, month, day
                );

                // Configurar fechas límite
                datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
                datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

                datePickerDialog.show();
            }
        });
    }

    private void loadSportsAndLocations() {
        Log.d(TAG, "loadSportsAndLocations: Iniciando carga de datos");
        loadingDialog.show("Cargando datos...");

        // Cargar deportes
        lookupRepository.getSports(new Callback<List<Sport>>() {
            @Override
            public void onResponse(Call<List<Sport>> call, Response<List<Sport>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sportList = response.body();
                    Log.d(TAG, "Sports cargados: " + sportList.size());
                    setupSportSpinner();
                    checkIfDataLoaded();
                } else {
                    Log.e(TAG, "Error al cargar sports: " + response.code());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.dismiss();
                            Toast.makeText(ProfileFormActivity.this,
                                "Error al cargar deportes", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Sport>> call, Throwable t) {
                Log.e(TAG, "Failure al cargar sports: " + t.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();
                        Toast.makeText(ProfileFormActivity.this,
                            "Error de conexión al cargar deportes", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Cargar ubicaciones
        lookupRepository.getLocations(new Callback<List<Location>>() {
            @Override
            public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    locationList = response.body();
                    Log.d(TAG, "Locations cargadas: " + locationList.size());
                    setupLocationSpinner();
                    checkIfDataLoaded();
                } else {
                    Log.e(TAG, "Error al cargar locations: " + response.code());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.dismiss();
                            Toast.makeText(ProfileFormActivity.this,
                                "Error al cargar ubicaciones", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Location>> call, Throwable t) {
                Log.e(TAG, "Failure al cargar locations: " + t.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();
                        Toast.makeText(ProfileFormActivity.this,
                            "Error de conexión al cargar ubicaciones", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setupSportSpinner() {
        ArrayAdapter<Sport> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, sportList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinnerSport.setAdapter(adapter);
                spinnerSport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedSportId = sportList.get(position).getId();
                        Log.d(TAG, "Sport seleccionado: " + sportList.get(position).getName() +
                            " (ID: " + selectedSportId + ")");
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedSportId = -1;
                    }
                });
            }
        });
    }

    private void setupLocationSpinner() {
        ArrayAdapter<Location> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, locationList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinnerLocation.setAdapter(adapter);
                spinnerLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedLocationId = locationList.get(position).getId();
                        Log.d(TAG, "Location seleccionada: " + locationList.get(position).toString() +
                            " (ID: " + selectedLocationId + ")");
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedLocationId = -1;
                    }
                });
            }
        });
    }

    private void checkIfDataLoaded() {
        if (!sportList.isEmpty() && !locationList.isEmpty()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.dismiss();
                    Log.d(TAG, "Datos cargados completamente");

                    // Si estamos en modo edición y ya tenemos el perfil, llenar el formulario
                    if (isEditMode && currentProfile != null) {
                        populateFormWithProfileData();
                    }
                }
            });
        }
    }

    private boolean validateForm() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa el nombre", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (profileType.equalsIgnoreCase("athlete") || profileType.equalsIgnoreCase("agent")) {
            String lastName = editTextLastName.getText().toString().trim();
            if (lastName.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa el apellido", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (profileType.equalsIgnoreCase("athlete")) {
            String height = editTextHeight.getText().toString().trim();
            String weight = editTextWeight.getText().toString().trim();

            if (selectedBirthdate.isEmpty()) {
                Toast.makeText(this, "Por favor, selecciona tu fecha de nacimiento", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (height.isEmpty() || weight.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (profileType.equalsIgnoreCase("agent")) {
            String agency = editTextAgency.getText().toString().trim();
        }

        if (profileType.equalsIgnoreCase("team")) {
            String job = editTextLastName.getText().toString().trim();
            if (job.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa el puesto/rol", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (selectedLocationId == -1 || selectedSportId == -1) {
            Toast.makeText(this, "Por favor, selecciona ubicación y deporte", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (phone.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa el número de teléfono", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void completeRegistration() {
        Log.d(TAG, "completeRegistration: Iniciando");

        // Mostrar diálogo de carga
        loadingDialog.show("Creando perfil...");
        buttonComplete.setEnabled(false);

        // Crear el objeto ProfileRequest en segundo plano
        threadManager.executeInBackground(new Runnable() {
            @Override
            public void run() {
                final ProfileRequest profileRequest = new ProfileRequest();
                profileRequest.setProfileType(profileType.toLowerCase());
                profileRequest.setName(editTextName.getText().toString().trim());
                profileRequest.setDescription(editTextDescription.getText().toString().trim());
                profileRequest.setPhoneNumber(editTextPhone.getText().toString().trim());
                profileRequest.setIgUser(editTextInstagram.getText().toString().trim());
                profileRequest.setxUser(editTextTwitter.getText().toString().trim());
                profileRequest.setLocationId(selectedLocationId);
                profileRequest.setSportId(selectedSportId);

                Log.d(TAG, "ProfileRequest: profileType=" + profileType.toLowerCase() +
                    ", locationId=" + selectedLocationId + ", sportId=" + selectedSportId);

                // Campos específicos según el tipo de perfil
                if (profileType.equalsIgnoreCase("athlete")) {
                    profileRequest.setLastName(editTextLastName.getText().toString().trim());
                    // Usar la fecha en formato ISO (YYYY-MM-DD)
                    profileRequest.setBirthdate(selectedBirthdate);
                    profileRequest.setHeight(editTextHeight.getText().toString().trim());
                    profileRequest.setWeight(editTextWeight.getText().toString().trim());

                    Log.d(TAG, "Athlete profile - Birthdate ISO: " + selectedBirthdate);
                } else if (profileType.equalsIgnoreCase("agent")) {
                    profileRequest.setLastName(editTextLastName.getText().toString().trim());
                    profileRequest.setAgency(editTextAgency.getText().toString().trim());
                } else if (profileType.equalsIgnoreCase("team")) {
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
                                    // Si la respuesta HTTP es exitosa (200-299), el perfil se creó correctamente
                                    Log.d(TAG, "Perfil creado exitosamente - Status: " + response.code());

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
                                                            Log.d(TAG, "Navegando a HomeActivity...");
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
                                    // Manejar error del servidor
                                    Log.e(TAG, "Error al crear perfil - Status: " + response.code());
                                    String errorMsg = "Error al crear perfil";

                                    try {
                                        if (response.errorBody() != null) {
                                            String errorBody = response.errorBody().string();
                                            Log.e(TAG, "Error body: " + errorBody);
                                            // Intentar extraer el mensaje de error del JSON
                                            if (errorBody.contains("message")) {
                                                int start = errorBody.indexOf("\"message\":\"") + 11;
                                                int end = errorBody.indexOf("\"", start);
                                                if (start > 10 && end > start) {
                                                    errorMsg = errorBody.substring(start, end);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error al parsear error body: " + e.getMessage());
                                    }

                                    Toast.makeText(ProfileFormActivity.this,
                                        errorMsg, Toast.LENGTH_LONG).show();
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

    private void loadCurrentProfile() {
        Log.d(TAG, "loadCurrentProfile: Cargando perfil del usuario autenticado");
        loadingDialog.show("Cargando perfil...");

        authRepository.getProfile(token, new Callback<GetProfileResponse>() {
            @Override
            public void onResponse(Call<GetProfileResponse> call, Response<GetProfileResponse> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    GetProfileResponse profileResponse = response.body();
                    if (profileResponse.getProfile() != null) {
                        currentProfile = profileResponse.getProfile();
                        profileId = currentProfile.getId(); // Guardar el ID del perfil para la actualización
                        profileType = profileResponse.getProfileType(); // Actualizar el profileType del nivel superior
                        populateFormWithProfileData();
                    } else {
                        Toast.makeText(ProfileFormActivity.this,
                            "Error: Perfil no encontrado",
                            Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Log.e(TAG, "Error al cargar perfil: " + response.code());
                    Toast.makeText(ProfileFormActivity.this,
                        "Error al cargar el perfil",
                        Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<GetProfileResponse> call, Throwable t) {
                loadingDialog.dismiss();
                Log.e(TAG, "Error de conexión: " + t.getMessage());
                Toast.makeText(ProfileFormActivity.this,
                    "Error de conexión",
                    Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFormWithProfileData() {
        if (currentProfile == null) return;

        Log.d(TAG, "populateFormWithProfileData: Llenando formulario con datos");

        // Llenar campos comunes
        editTextName.setText(currentProfile.getName());
        editTextDescription.setText(currentProfile.getDescription());
        editTextPhone.setText(currentProfile.getPhoneNumber());
        editTextInstagram.setText(currentProfile.getIgUser());
        editTextTwitter.setText(currentProfile.getxUser());

        // Seleccionar ubicación en el spinner
        if (currentProfile.getLocationId() != null && !locationList.isEmpty()) {
            for (int i = 0; i < locationList.size(); i++) {
                if (locationList.get(i).getId() == currentProfile.getLocationId()) {
                    spinnerLocation.setSelection(i);
                    selectedLocationId = currentProfile.getLocationId();
                    break;
                }
            }
        }

        // Seleccionar deporte en el spinner
        if (currentProfile.getSportId() != null && !sportList.isEmpty()) {
            for (int i = 0; i < sportList.size(); i++) {
                if (sportList.get(i).getId() == currentProfile.getSportId()) {
                    spinnerSport.setSelection(i);
                    selectedSportId = currentProfile.getSportId();
                    break;
                }
            }
        }

        // Llenar campos específicos por tipo de perfil
        if ("athlete".equalsIgnoreCase(profileType)) {
            editTextLastName.setText(currentProfile.getLastName());
            editTextHeight.setText(currentProfile.getHeight());
            editTextWeight.setText(currentProfile.getWeight());

            // Convertir fecha de ISO (YYYY-MM-DD) a formato display (DD/MM/YYYY)
            if (currentProfile.getBirthdate() != null && !currentProfile.getBirthdate().isEmpty()) {
                selectedBirthdate = currentProfile.getBirthdate();
                String displayDate = formatDateForDisplay(currentProfile.getBirthdate());
                editTextBirthdate.setText(displayDate);

                // Actualizar el calendario
                try {
                    String[] parts = currentProfile.getBirthdate().split("-");
                    if (parts.length == 3) {
                        birthdateCalendar.set(Calendar.YEAR, Integer.parseInt(parts[0]));
                        birthdateCalendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
                        birthdateCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al parsear fecha: " + e.getMessage());
                }
            }
        } else if ("agent".equalsIgnoreCase(profileType)) {
            editTextLastName.setText(currentProfile.getLastName());
            editTextAgency.setText(currentProfile.getAgency());
        } else if ("team".equalsIgnoreCase(profileType)) {
            editTextLastName.setText(currentProfile.getJob());
        }
    }

    private String formatDateForDisplay(String isoDate) {
        // Convertir de YYYY-MM-DD a DD/MM/YYYY
        if (isoDate != null && isoDate.length() == 10) {
            String[] parts = isoDate.split("-");
            if (parts.length == 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
        }
        return isoDate;
    }

    private void updateProfile() {
        Log.d(TAG, "updateProfile: Actualizando perfil");

        loadingDialog.show("Actualizando perfil...");
        buttonComplete.setEnabled(false);

        threadManager.executeInBackground(new Runnable() {
            @Override
            public void run() {
                // Crear el objeto UpdateProfileRequest con la estructura correcta
                final UpdateProfileRequest updateRequest = new UpdateProfileRequest();

                // Crear el objeto profileUpdates
                UpdateProfileRequest.ProfileUpdates profileUpdates = new UpdateProfileRequest.ProfileUpdates();
                profileUpdates.setName(editTextName.getText().toString().trim());
                profileUpdates.setDescription(editTextDescription.getText().toString().trim());
                profileUpdates.setPhoneNumber(editTextPhone.getText().toString().trim());
                profileUpdates.setIgUser(editTextInstagram.getText().toString().trim());
                profileUpdates.setxUser(editTextTwitter.getText().toString().trim());
                profileUpdates.setLocationId(selectedLocationId);
                profileUpdates.setSportId(selectedSportId);

                // Campos específicos según el tipo de perfil
                if (profileType.equalsIgnoreCase("athlete")) {
                    profileUpdates.setLastName(editTextLastName.getText().toString().trim());
                    profileUpdates.setBirthdate(selectedBirthdate);
                    profileUpdates.setHeight(editTextHeight.getText().toString().trim());
                    profileUpdates.setWeight(editTextWeight.getText().toString().trim());
                } else if (profileType.equalsIgnoreCase("agent")) {
                    profileUpdates.setLastName(editTextLastName.getText().toString().trim());
                    profileUpdates.setAgency(editTextAgency.getText().toString().trim());
                } else if (profileType.equalsIgnoreCase("team")) {
                    profileUpdates.setJob(editTextLastName.getText().toString().trim());
                }

                // Establecer profileUpdates en el request
                updateRequest.setProfileUpdates(profileUpdates);

                Log.d(TAG, "updateProfile: Enviando actualización al backend");

                // Enviar al backend
                authRepository.updateProfile(token, updateRequest, new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                        threadManager.executeOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismiss();
                                buttonComplete.setEnabled(true);

                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Perfil actualizado exitosamente");
                                    Toast.makeText(ProfileFormActivity.this,
                                        "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();

                                    // Volver a la pantalla anterior
                                    finish();
                                } else {
                                    Log.e(TAG, "Error al actualizar perfil - Status: " + response.code());
                                    String errorMsg = "Error al actualizar perfil";

                                    try {
                                        if (response.errorBody() != null) {
                                            String errorBody = response.errorBody().string();
                                            Log.e(TAG, "Error body: " + errorBody);
                                            if (errorBody.contains("message")) {
                                                int start = errorBody.indexOf("\"message\":\"") + 11;
                                                int end = errorBody.indexOf("\"", start);
                                                if (start > 10 && end > start) {
                                                    errorMsg = errorBody.substring(start, end);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error al parsear error body: " + e.getMessage());
                                    }

                                    Toast.makeText(ProfileFormActivity.this,
                                        errorMsg, Toast.LENGTH_LONG).show();
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

