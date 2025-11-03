package com.example.sportconnection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.example.sportconnection.model.SignupResponse;
import com.example.sportconnection.model.LoginResponse;
import com.example.sportconnection.repository.AuthRepository;
import com.example.sportconnection.utils.LoadingDialog;
import com.example.sportconnection.utils.SessionManager;
import com.example.sportconnection.utils.ThreadManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonContinue;
    private Button buttonGoogleSignUp;

    private AuthRepository authRepository;
    private SessionManager sessionManager;
    private LoadingDialog loadingDialog;
    private ThreadManager threadManager;

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.titleRegister), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        editTextEmail = findViewById(R.id.editTextEmailRegister);
        editTextPassword = findViewById(R.id.editTextPasswordRegister);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonContinue = findViewById(R.id.buttonContinue);
        buttonGoogleSignUp = findViewById(R.id.buttonGoogleSignUp);

        // Inicializar repositorio, session manager y utilidades
        authRepository = new AuthRepository();
        sessionManager = new SessionManager(this);
        loadingDialog = new LoadingDialog(this);
        threadManager = ThreadManager.getInstance();

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configurar el launcher para el resultado de Google Sign-In
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    android.util.Log.d("RegisterActivity", "========== GOOGLE SIGN-IN RESULT ==========");
                    android.util.Log.d("RegisterActivity", "Result code: " + result.getResultCode());
                    android.util.Log.d("RegisterActivity", "RESULT_OK = " + RESULT_OK + " (-1)");
                    android.util.Log.d("RegisterActivity", "RESULT_CANCELED = " + RESULT_CANCELED + " (0)");

                    Intent data = result.getData();
                    android.util.Log.d("RegisterActivity", "Intent data: " + (data != null ? "Present" : "NULL"));

                    // Intentar procesar el resultado incluso si no es RESULT_OK
                    // A veces Google devuelve datos útiles con otros códigos
                    if (data != null) {
                        android.util.Log.d("RegisterActivity", "Processing intent data...");
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleGoogleSignInResult(task);
                    } else {
                        android.util.Log.e("RegisterActivity", "No intent data received!");
                        if (result.getResultCode() == RESULT_CANCELED) {
                            android.util.Log.w("RegisterActivity", "User canceled the sign-in flow");
                            Toast.makeText(RegisterActivity.this, "Inicio de sesión cancelado por el usuario", Toast.LENGTH_SHORT).show();
                        } else if (result.getResultCode() == RESULT_OK) {
                            android.util.Log.e("RegisterActivity", "RESULT_OK but no data - This is unusual!");
                            Toast.makeText(RegisterActivity.this, "Error: No se recibieron datos de Google", Toast.LENGTH_LONG).show();
                        } else {
                            android.util.Log.e("RegisterActivity", "Unexpected result code with no data: " + result.getResultCode());
                            Toast.makeText(RegisterActivity.this, "Error inesperado al iniciar sesión", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Configurar el botón de continuar
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                } else {
                    // Registrar usuario en el backend
                    registerUser(email, password);
                }
            }
        });

        // Configurar el botón de Google Sign-Up
        buttonGoogleSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpWithGoogle();
            }
        });
    }

    private void signUpWithGoogle() {
        android.util.Log.d("RegisterActivity", "========== STARTING GOOGLE SIGN-IN ==========");
        android.util.Log.d("RegisterActivity", "Web Client ID: " + getString(R.string.default_web_client_id));

        // Verificar si el Web Client ID está configurado
        String webClientId = getString(R.string.default_web_client_id);
        if (webClientId.equals("YOUR_WEB_CLIENT_ID_HERE")) {
            android.util.Log.e("RegisterActivity", "ERROR: Web Client ID not configured!");
            Toast.makeText(this, "Error: Web Client ID no está configurado. Revisa strings.xml", Toast.LENGTH_LONG).show();
            return;
        }

        // Cerrar sesión previa para forzar selección de cuenta
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            android.util.Log.d("RegisterActivity", "Previous session cleared");
            Intent signInIntent = googleSignInClient.getSignInIntent();
            android.util.Log.d("RegisterActivity", "Launching Google Sign-In intent...");
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        android.util.Log.d("RegisterActivity", "handleGoogleSignInResult called");
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            android.util.Log.d("RegisterActivity", "Account obtained: " + account.getEmail());

            String idToken = account.getIdToken();
            android.util.Log.d("RegisterActivity", "ID Token: " + (idToken != null ? "Present" : "NULL"));

            if (idToken != null) {
                android.util.Log.d("RegisterActivity", "Calling registerWithGoogle...");
                registerWithGoogle(idToken);
            } else {
                android.util.Log.e("RegisterActivity", "ID Token is NULL!");
                Toast.makeText(this, "Error al obtener token de Google. Verifica la configuración del Web Client ID", Toast.LENGTH_LONG).show();
            }
        } catch (ApiException e) {
            android.util.Log.e("RegisterActivity", "ApiException in Google Sign-In");
            android.util.Log.e("RegisterActivity", "Status code: " + e.getStatusCode());
            android.util.Log.e("RegisterActivity", "Status message: " + e.getStatusMessage());

            String errorMessage = "Error al registrarse con Google (código: " + e.getStatusCode() + ")";
            if (e.getStatusCode() == 10) {
                errorMessage = "Error de configuración. Verifica el Web Client ID en strings.xml";
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void registerWithGoogle(final String idToken) {
        android.util.Log.d("RegisterActivity", "registerWithGoogle called");
        loadingDialog.show("Registrando con Google...");
        buttonGoogleSignUp.setEnabled(false);

        threadManager.executeInBackground(new Runnable() {
            @Override
            public void run() {
                android.util.Log.d("RegisterActivity", "Calling API googleSignIn...");
                authRepository.googleSignIn(idToken, new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        android.util.Log.d("RegisterActivity", "Response received from server");
                        android.util.Log.d("RegisterActivity", "Response code: " + response.code());
                        android.util.Log.d("RegisterActivity", "Response successful: " + response.isSuccessful());

                        threadManager.executeOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismiss();
                                buttonGoogleSignUp.setEnabled(true);

                                if (response.isSuccessful() && response.body() != null) {
                                    LoginResponse loginResponse = response.body();

                                    android.util.Log.d("RegisterActivity", "Google Sign-In successful");
                                    android.util.Log.d("RegisterActivity", "Token: " + loginResponse.getToken());
                                    android.util.Log.d("RegisterActivity", "UserId: " + loginResponse.getUserId());

                                    final String token = loginResponse.getToken();
                                    final int userId = loginResponse.getUserId();

                                    // Verificar si requiere completar el perfil
                                    if (loginResponse.isRequiresProfile()) {
                                        android.util.Log.d("RegisterActivity", "User requires profile. Navigating to SelectProfileActivity");

                                        threadManager.executeWithDelay(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(RegisterActivity.this, SelectProfileActivity.class);
                                                intent.putExtra("token", token);
                                                intent.putExtra("userId", userId);
                                                intent.putExtra("isGoogleSignUp", true);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }, 500);
                                    } else {
                                        // Usuario ya tiene perfil, ir directamente a Home
                                        android.util.Log.d("RegisterActivity", "User already has profile. Navigating to HomeActivity");

                                        LoginResponse.UserData user = loginResponse.getUser();
                                        String email = user != null ? user.getEmail() : "";
                                        String profileType = user != null ? user.getProfileType() : "user";

                                        sessionManager.saveSession(token, userId, email, profileType);

                                        threadManager.executeWithDelay(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }, 500);
                                    }
                                } else {
                                    android.util.Log.e("RegisterActivity", "Response not successful");
                                    android.util.Log.e("RegisterActivity", "Response code: " + response.code());
                                    android.util.Log.e("RegisterActivity", "Response message: " + response.message());

                                    String errorMsg = response.message();
                                    if (errorMsg == null || errorMsg.isEmpty()) {
                                        errorMsg = "Error desconocido en el servidor";
                                    }
                                    Toast.makeText(RegisterActivity.this, "Error en el registro: " + errorMsg, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        android.util.Log.e("RegisterActivity", "API call failed");
                        android.util.Log.e("RegisterActivity", "Error: " + t.getMessage());
                        t.printStackTrace();

                        threadManager.executeOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismiss();
                                buttonGoogleSignUp.setEnabled(true);
                                String errorMsg = t.getMessage();
                                if (errorMsg == null || errorMsg.isEmpty()) {
                                    errorMsg = "Error desconocido";
                                }
                                Toast.makeText(RegisterActivity.this, "Error de conexión: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        });
    }

    private void registerUser(final String email, final String password) {
        // Mostrar diálogo de carga
        loadingDialog.show("Registrando usuario...");
        buttonContinue.setEnabled(false);

        // Ejecutar registro en segundo plano
        threadManager.executeInBackground(new Runnable() {
            @Override
            public void run() {
                authRepository.signup(email, password, new Callback<SignupResponse>() {
                    @Override
                    public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                        // Ejecutar en el hilo principal para actualizar la UI
                        threadManager.executeOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismiss();
                                buttonContinue.setEnabled(true);

                                if (response.isSuccessful() && response.body() != null) {
                                    SignupResponse signupResponse = response.body();

                                    // Log para debug
                                    android.util.Log.d("RegisterActivity", "Success: " + signupResponse.isSuccess());
                                    android.util.Log.d("RegisterActivity", "Message: " + signupResponse.getMessage());
                                    android.util.Log.d("RegisterActivity", "Token: " + signupResponse.getToken());
                                    android.util.Log.d("RegisterActivity", "UserId: " + signupResponse.getUserId());

                                    // Mostrar mensaje del backend
                                    if (signupResponse.getMessage() != null && !signupResponse.getMessage().isEmpty()) {
                                        Toast.makeText(RegisterActivity.this, signupResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                    // Si la respuesta es exitosa, navegar a la siguiente pantalla
                                    final String token = signupResponse.getToken();
                                    final int userId = signupResponse.getUserId();

                                    // Pequeño delay para que el usuario vea el mensaje antes de cambiar de pantalla
                                    threadManager.executeWithDelay(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(RegisterActivity.this, SelectProfileActivity.class);
                                            intent.putExtra("email", email);
                                            intent.putExtra("password", password);
                                            intent.putExtra("token", token);
                                            intent.putExtra("userId", userId);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }, 500); // 500ms delay
                                } else {
                                    android.util.Log.e("RegisterActivity", "Response code: " + response.code());
                                    android.util.Log.e("RegisterActivity", "Response message: " + response.message());

                                    String errorMsg = response.message();
                                    if (errorMsg == null || errorMsg.isEmpty()) {
                                        errorMsg = "Error desconocido en el servidor";
                                    }
                                    Toast.makeText(RegisterActivity.this, "Error en el registro: " + errorMsg, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<SignupResponse> call, Throwable t) {
                        // Ejecutar en el hilo principal para actualizar la UI
                        threadManager.executeOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismiss();
                                buttonContinue.setEnabled(true);
                                android.util.Log.e("RegisterActivity", "Error de conexión", t);

                                String errorMsg = t.getMessage();
                                if (errorMsg == null || errorMsg.isEmpty()) {
                                    errorMsg = "Error desconocido";
                                }
                                Toast.makeText(RegisterActivity.this, "Error de conexión: " + errorMsg, Toast.LENGTH_LONG).show();
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

