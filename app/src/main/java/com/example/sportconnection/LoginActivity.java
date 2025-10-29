package com.example.sportconnection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

    private AuthRepository authRepository;
    private SessionManager sessionManager;
    private LoadingDialog loadingDialog;
    private ThreadManager threadManager;

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

        // Inicializar repositorio, session manager y utilidades
        authRepository = new AuthRepository();
        sessionManager = new SessionManager(this);
        loadingDialog = new LoadingDialog(this);
        threadManager = ThreadManager.getInstance();

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

