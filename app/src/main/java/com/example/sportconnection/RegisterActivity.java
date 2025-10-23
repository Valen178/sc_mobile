package com.example.sportconnection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sportconnection.model.SignupResponse;
import com.example.sportconnection.repository.AuthRepository;
import com.example.sportconnection.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonContinue;
    private ProgressBar progressBar;

    private AuthRepository authRepository;
    private SessionManager sessionManager;

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

        // Inicializar repositorio y session manager
        authRepository = new AuthRepository();
        sessionManager = new SessionManager(this);

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
    }

    private void registerUser(String email, String password) {
        buttonContinue.setEnabled(false);

        authRepository.signup(email, password, new Callback<SignupResponse>() {
            @Override
            public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
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

                    // Si la respuesta es exitosa (código HTTP 200-299), navegar a la siguiente pantalla
                    // Guardar token temporalmente
                    String token = signupResponse.getToken();

                    // Pasar a la siguiente pantalla para seleccionar tipo de perfil
                    Intent intent = new Intent(RegisterActivity.this, SelectProfileActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    intent.putExtra("token", token);
                    intent.putExtra("userId", signupResponse.getUserId());
                    startActivity(intent);
                    finish();
                } else {
                    android.util.Log.e("RegisterActivity", "Response code: " + response.code());
                    android.util.Log.e("RegisterActivity", "Response message: " + response.message());
                    Toast.makeText(RegisterActivity.this, "Error en el registro: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SignupResponse> call, Throwable t) {
                buttonContinue.setEnabled(true);
                android.util.Log.e("RegisterActivity", "Error de conexión", t);
                Toast.makeText(RegisterActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

