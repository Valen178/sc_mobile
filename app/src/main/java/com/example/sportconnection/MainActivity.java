package com.example.sportconnection;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sportconnection.utils.Logger;
import com.example.sportconnection.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Log forzado que SIEMPRE se muestra
        Logger.forceLog(TAG, "========== INICIANDO MAINACTIVITY ==========");
        Logger.d(TAG, "onCreate() llamado");

        // Inicializar SessionManager primero
        sessionManager = new SessionManager(this);
        Logger.d(TAG, "SessionManager inicializado");

        // Verificar sesión ANTES de cargar el layout
        Logger.d(TAG, "Verificando sesión...");
        boolean isLoggedIn = sessionManager.isLoggedIn();
        Logger.forceLog(TAG, "isLoggedIn: " + isLoggedIn);

        if (isLoggedIn) {
            // Si ya está logueado, ir directamente a HomeActivity SIN cargar el layout
            Logger.forceLog(TAG, "Usuario YA ESTÁ LOGUEADO, navegando a HomeActivity");
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return; // Importante: salir del onCreate sin hacer nada más
        }

        // Solo si NO está logueado, cargar el layout y mostrar botones
        Logger.forceLog(TAG, "Usuario NO LOGUEADO, cargando interfaz de login");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Logger.d(TAG, "Inicializando botones");
        initializeButtons();
    }

    private void initializeButtons() {
        Logger.d(TAG, "Configurando botones de Login y Registro");
        // Inicializar los botones
        Button buttonLogin = findViewById(R.id.button1);
        Button buttonRegister = findViewById(R.id.button2);

        // Configurar el botón de Iniciar Sesión
        buttonLogin.setOnClickListener(v -> {
            Logger.d(TAG, "Botón LOGIN presionado, navegando a LoginActivity");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Configurar el botón de Registrarse
        buttonRegister.setOnClickListener(v -> {
            Logger.d(TAG, "Botón REGISTRO presionado, navegando a RegisterActivity");
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        Logger.d(TAG, "Botones configurados exitosamente");
    }
}

