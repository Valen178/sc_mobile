package com.example.sportconnection.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sportconnection.MainActivity;
import com.example.sportconnection.R;
import com.example.sportconnection.utils.LoadingDialog;
import com.example.sportconnection.utils.SessionManager;
import com.example.sportconnection.utils.ThreadManager;

public class ProfileFragment extends Fragment {

    private TextView textUserEmail;
    private TextView textUserType;
    private Button buttonLogout;
    private SessionManager sessionManager;
    private LoadingDialog loadingDialog;
    private ThreadManager threadManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inicializar vistas
        textUserEmail = view.findViewById(R.id.textUserEmail);
        textUserType = view.findViewById(R.id.textUserType);
        buttonLogout = view.findViewById(R.id.buttonLogout);

        // Inicializar utilidades
        sessionManager = new SessionManager(requireContext());
        loadingDialog = new LoadingDialog(requireContext());
        threadManager = ThreadManager.getInstance();

        // Cargar información del usuario en segundo plano
        loadUserData();

        // Configurar botón de cerrar sesión
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        return view;
    }

    private void loadUserData() {
        loadingDialog.show("Cargando perfil...");

        threadManager.executeInBackground(new Runnable() {
            @Override
            public void run() {
                // Obtener información de la sesión
                final String email = sessionManager.getEmail();
                final String profileType = sessionManager.getProfileType();

                // Actualizar UI en el hilo principal
                threadManager.executeOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();

                        textUserEmail.setText("Email: " + (email != null ? email : "No disponible"));

                        // Traducir el tipo de perfil
                        String profileTypeText = "No disponible";
                        if (profileType != null) {
                            switch (profileType.toLowerCase()) {
                                case "athlete":
                                    profileTypeText = "Atleta";
                                    break;
                                case "agent":
                                    profileTypeText = "Agente";
                                    break;
                                case "team":
                                    profileTypeText = "Equipo";
                                    break;
                                default:
                                    profileTypeText = profileType;
                            }
                        }
                        textUserType.setText("Tipo: " + profileTypeText);
                    }
                });
            }
        });
    }

    private void logoutUser() {
        loadingDialog.show("Cerrando sesión...");
        buttonLogout.setEnabled(false);

        threadManager.executeInBackground(new Runnable() {
            @Override
            public void run() {
                // Cerrar sesión
                sessionManager.logout();

                // Pequeño delay para dar feedback visual
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Redirigir a MainActivity en el hilo principal
                threadManager.executeOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();

                        Intent intent = new Intent(requireActivity(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cerrar el diálogo si está abierto
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}

