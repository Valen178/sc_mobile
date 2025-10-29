package com.example.sportconnection;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.sportconnection.fragments.ConnectFragment;
import com.example.sportconnection.fragments.ConnectionsFragment;
import com.example.sportconnection.fragments.MapFragment;
import com.example.sportconnection.fragments.PostsFragment;
import com.example.sportconnection.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        fragmentManager = getSupportFragmentManager();

        // Cargar el fragmento inicial (Conectar)
        if (savedInstanceState == null) {
            loadFragment(new ConnectFragment());
        }

        // Configurar el listener para la barra de navegación
        bottomNavigation.setOnItemSelectedListener(item -> {
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
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}

