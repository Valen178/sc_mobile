package com.example.sportconnection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SelectProfileActivity extends AppCompatActivity {

    private CardView cardAthlete, cardAgent, cardTeam;
    private String email, password, token;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.titleSelectProfile), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener datos del Intent
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        token = getIntent().getStringExtra("token");
        userId = getIntent().getIntExtra("userId", -1);

        // Inicializar vistas
        cardAthlete = findViewById(R.id.cardAthlete);
        cardAgent = findViewById(R.id.cardAgent);
        cardTeam = findViewById(R.id.cardTeam);

        // Configurar listeners para las tarjetas
        cardAthlete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {navigateToProfileForm("athlete");}
        });

        cardAgent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToProfileForm("agent");
            }
        });

        cardTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToProfileForm("team");
            }
        });
    }

    private void navigateToProfileForm(String profileType) {
        Intent intent = new Intent(SelectProfileActivity.this, ProfileFormActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        intent.putExtra("token", token);
        intent.putExtra("userId", userId);
        intent.putExtra("profileType", profileType);
        startActivity(intent);
    }
}

