package com.example.sportconnection;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.sportconnection.fragments.ConnectFragment;
import com.example.sportconnection.fragments.ConnectionsFragment;
import com.example.sportconnection.fragments.MapFragment;
import com.example.sportconnection.fragments.PostsFragment;
import com.example.sportconnection.fragments.ProfileFragment;
import com.example.sportconnection.utils.Logger;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;
    private ProgressBar loadingOverlay;
    private boolean isFragmentLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        fragmentManager = getSupportFragmentManager();

        // Cargar el fragmento inicial (Conectar)
        if (savedInstanceState == null) {
            loadFragment(new ConnectFragment());
        }

        // Configurar el listener para la barra de navegación
        bottomNavigation.setOnItemSelectedListener(item -> {
            // Bloquear navegación si hay un fragmento cargando
            if (isFragmentLoading) {
                Logger.d(TAG, "Navegación bloqueada - Fragmento en proceso de carga");
                return false;
            }

            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_connect) {
                fragment = new ConnectFragment();
            } else if (itemId == R.id.nav_connections) {
                fragment = new ConnectionsFragment();
            } else if (itemId == R.id.nav_posts) {
                fragment = new PostsFragment();
            } else if (itemId == R.id.nav_map) {
                fragment = new MapFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        Logger.d(TAG, "Iniciando carga de fragmento: " + fragment.getClass().getSimpleName());
        setFragmentLoading(true);
        
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commitNow(); // Usar commitNow para esperar a que termine
        
        // Dar un pequeño delay para que el fragmento se inicialice
        bottomNavigation.postDelayed(() -> {
            setFragmentLoading(false);
            Logger.d(TAG, "Fragmento cargado: " + fragment.getClass().getSimpleName());
        }, 500);
    }
    
    public void setFragmentLoading(boolean loading) {
        isFragmentLoading = loading;
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        bottomNavigation.setEnabled(!loading);
        Logger.d(TAG, "Estado de carga: " + (loading ? "BLOQUEADO" : "DESBLOQUEADO"));
    }
}

