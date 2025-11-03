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

import com.example.sportconnection.model.LoginResponse;
import com.example.sportconnection.repository.AuthRepository;
import com.example.sportconnection.utils.LoadingDialog;
import com.example.sportconnection.utils.Logger;
import com.example.sportconnection.utils.SessionManager;
import com.example.sportconnection.utils.ThreadManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonGoogleSignIn;

    private AuthRepository authRepository;
    private SessionManager sessionManager;
    private LoadingDialog loadingDialog;
    private ThreadManager threadManager;

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.forceLog(TAG, "========== INICIANDO LOGINACTIVITY ==========");
        Logger.d(TAG, "onCreate() llamado");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.titleLogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn);

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
                    Logger.forceLog(TAG, "========== GOOGLE SIGN-IN RESULT ==========");
                    Logger.forceLog(TAG, "Result code: " + result.getResultCode());
                    Logger.forceLog(TAG, "RESULT_OK = " + RESULT_OK + " (-1)");
                    Logger.forceLog(TAG, "RESULT_CANCELED = " + RESULT_CANCELED + " (0)");

                    Intent data = result.getData();
                    Logger.forceLog(TAG, "Intent data: " + (data != null ? "Present" : "NULL"));

                    if (data != null) {
                        Logger.d(TAG, "Processing intent data...");
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleGoogleSignInResult(task);
                    } else {
                        Logger.forceLog(TAG, "No intent data received!");
                        if (result.getResultCode() == RESULT_CANCELED) {
                            Logger.d(TAG, "User canceled the sign-in flow");
                            Toast.makeText(LoginActivity.this, "Inicio de sesión cancelado por el usuario", Toast.LENGTH_SHORT).show();
                        } else if (result.getResultCode() == RESULT_OK) {
                            Logger.forceLog(TAG, "RESULT_OK but no data - This is unusual!");
                            Toast.makeText(LoginActivity.this, "Error: No se recibieron datos de Google", Toast.LENGTH_LONG).show();
                        } else {
                            Logger.forceLog(TAG, "Unexpected result code with no data: " + result.getResultCode());
                            Toast.makeText(LoginActivity.this, "Error inesperado al iniciar sesión", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Configurar el botón de login
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(email, password);
                }
            }
        });

        // Configurar el botón de Google Sign-In
        buttonGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    private void signInWithGoogle() {
        Logger.forceLog(TAG, "========== STARTING GOOGLE SIGN-IN ==========");
        Logger.forceLog(TAG, "Web Client ID: " + getString(R.string.default_web_client_id));

        // Verificar si el Web Client ID está configurado
        String webClientId = getString(R.string.default_web_client_id);
        if (webClientId.equals("YOUR_WEB_CLIENT_ID_HERE")) {
            Logger.forceLog(TAG, "ERROR: Web Client ID not configured!");
            Toast.makeText(this, "Error: Web Client ID no está configurado. Revisa strings.xml", Toast.LENGTH_LONG).show();
            return;
        }

        // Cerrar sesión previa para forzar selección de cuenta
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Logger.d(TAG, "Previous session cleared");
            Intent signInIntent = googleSignInClient.getSignInIntent();
            Logger.d(TAG, "Launching Google Sign-In intent...");
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Logger.d(TAG, "handleGoogleSignInResult called");
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Logger.d(TAG, "Account obtained: " + account.getEmail());

            String idToken = account.getIdToken();
            Logger.d(TAG, "ID Token: " + (idToken != null ? "Present" : "NULL"));

            if (idToken != null) {
                Logger.d(TAG, "Calling loginWithGoogle...");
                loginWithGoogle(idToken);
            } else {
                Logger.forceLog(TAG, "ID Token is NULL!");
                Toast.makeText(this, "Error al obtener token de Google. Verifica la configuración del Web Client ID", Toast.LENGTH_LONG).show();
            }
        } catch (ApiException e) {
            Logger.forceLog(TAG, "ApiException in Google Sign-In");
            Logger.forceLog(TAG, "Status code: " + e.getStatusCode());
            Logger.forceLog(TAG, "Status message: " + e.getStatusMessage());

            String errorMessage = "Error al iniciar sesión con Google (código: " + e.getStatusCode() + ")";
            if (e.getStatusCode() == 10) {
                errorMessage = "Error de configuración. Verifica el Web Client ID en strings.xml";
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void loginWithGoogle(final String idToken) {
        Logger.d(TAG, "loginWithGoogle called");
        loadingDialog.show("Iniciando sesión con Google...");
        buttonGoogleSignIn.setEnabled(false);

        threadManager.executeInBackground(new Runnable() {
            @Override
            public void run() {
                Logger.d(TAG, "Calling API googleSignIn...");
                authRepository.googleSignIn(idToken, new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        Logger.d(TAG, "Response received from server");
                        Logger.d(TAG, "Response code: " + response.code());
                        Logger.d(TAG, "Response successful: " + response.isSuccessful());

                        threadManager.executeOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismiss();
                                buttonGoogleSignIn.setEnabled(true);

                                if (response.isSuccessful() && response.body() != null) {
                                    LoginResponse loginResponse = response.body();

                                    Logger.d(TAG, "Login response success: " + loginResponse.isSuccess());

                                    if (loginResponse.isSuccess()) {
                                        LoginResponse.UserData user = loginResponse.getUser();
                                        String token = loginResponse.getToken();

                                        Logger.d(TAG, "User: " + (user != null ? user.getEmail() : "null"));
                                        Logger.d(TAG, "Token: " + (token != null ? "present" : "null"));

                                        if (user != null && token != null) {
                                            sessionManager.saveSession(
                                                token,
                                                user.getId(),
                                                user.getEmail(),
                                                user.getProfileType() != null ? user.getProfileType() : "user"
                                            );

                                            Toast.makeText(LoginActivity.this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();

                                            threadManager.executeWithDelay(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Logger.d(TAG, "Navigating to HomeActivity");
                                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }, 300);
                                        }
                                    } else {
                                        Logger.d(TAG, "Login not successful: " + loginResponse.getMessage());
                                        Toast.makeText(LoginActivity.this, loginResponse.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Logger.forceLog(TAG, "Response not successful or body is null");
                                    Logger.forceLog(TAG, "Response code: " + response.code());
                                    Toast.makeText(LoginActivity.this, "Error en el login con Google", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Logger.forceLog(TAG, "API call failed");
                        Logger.forceLog(TAG, "Error: " + t.getMessage());
                        t.printStackTrace();

                        threadManager.executeOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismiss();
                                buttonGoogleSignIn.setEnabled(true);
                                Toast.makeText(LoginActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        });
    }

    private void loginUser(final String email, final String password) {
        // Mostrar diálogo de carga
        loadingDialog.show("Iniciando sesión...");
        buttonLogin.setEnabled(false);

        // Ejecutar login en segundo plano
        threadManager.executeInBackground(new Runnable() {
            @Override
            public void run() {
                authRepository.login(email, password, new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        // Ejecutar en el hilo principal para actualizar la UI
                        threadManager.executeOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismiss();
                                buttonLogin.setEnabled(true);

                                if (response.isSuccessful() && response.body() != null) {
                                    LoginResponse loginResponse = response.body();

                                    Logger.forceLog(TAG, "Response recibida del servidor");
                                    Logger.forceLog(TAG, "Token presente: " + (loginResponse.getToken() != null));
                                    Logger.forceLog(TAG, "User presente: " + (loginResponse.getUser() != null));

                                    if (loginResponse.isSuccess()) {
                                        // Log para debug
                                        Logger.forceLog(TAG, "Login exitoso!");

                                        LoginResponse.UserData user = loginResponse.getUser();

                                        if (user == null) {
                                            Logger.forceLog(TAG, "ERROR: User data es NULL");
                                            Toast.makeText(LoginActivity.this, "Error: Datos de usuario no recibidos", Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        String token = loginResponse.getToken();
                                        if (token == null || token.isEmpty()) {
                                            Logger.forceLog(TAG, "ERROR: Token es NULL o vacío");
                                            Toast.makeText(LoginActivity.this, "Error: Token no recibido", Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        // Log de datos del usuario
                                        Logger.forceLog(TAG, "Token: " + token);
                                        Logger.forceLog(TAG, "UserId: " + user.getId());
                                        Logger.forceLog(TAG, "Email: " + user.getEmail());
                                        Logger.forceLog(TAG, "ProfileType/Role: " + user.getProfileType());

                                        // Guardar sesión SINCRÓNICAMENTE (no en segundo plano)
                                        sessionManager.saveSession(
                                            token,
                                            user.getId(),
                                            user.getEmail(),
                                            user.getProfileType() != null ? user.getProfileType() : "user"
                                        );

                                        // Verificar que se guardó correctamente
                                        boolean isLoggedIn = sessionManager.isLoggedIn();
                                        Logger.forceLog(TAG, "Sesión guardada. isLoggedIn: " + isLoggedIn);

                                        Toast.makeText(LoginActivity.this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();

                                        // Navegar a HomeActivity con un pequeño delay
                                        threadManager.executeWithDelay(new Runnable() {
                                            @Override
                                            public void run() {
                                                Logger.forceLog(TAG, "Navegando a HomeActivity...");
                                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }, 300);
                                    } else {
                                        // Manejar mensaje null
                                        String errorMsg = loginResponse.getMessage();
                                        if (errorMsg == null || errorMsg.isEmpty()) {
                                            errorMsg = "Error en el inicio de sesión";
                                        }
                                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    String errorMsg = response.message();
                                    if (errorMsg == null || errorMsg.isEmpty()) {
                                        errorMsg = "Error en el servidor";
                                    }
                                    Toast.makeText(LoginActivity.this, "Error en el login: " + errorMsg, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        // Ejecutar en el hilo principal para actualizar la UI
                        threadManager.executeOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismiss();
                                buttonLogin.setEnabled(true);

                                String errorMsg = t.getMessage();
                                if (errorMsg == null || errorMsg.isEmpty()) {
                                    errorMsg = "Error desconocido";
                                }
                                Toast.makeText(LoginActivity.this, "Error de conexión: " + errorMsg, Toast.LENGTH_LONG).show();
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

