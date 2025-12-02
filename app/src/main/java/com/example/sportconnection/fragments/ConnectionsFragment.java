package com.example.sportconnection.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportconnection.HomeActivity;
import com.example.sportconnection.R;
import com.example.sportconnection.ViewProfileActivity;
import com.example.sportconnection.adapters.MatchesAdapter;
import com.example.sportconnection.model.Match;
import com.example.sportconnection.model.MatchesResponse;
import com.example.sportconnection.network.ApiClient;
import com.example.sportconnection.network.ApiService;
import com.example.sportconnection.repository.SubscriptionRepository;
import com.example.sportconnection.utils.LoadingDialog;
import com.example.sportconnection.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnectionsFragment extends Fragment {

    private static final String TAG = "ConnectionsFragment";

    // UI Components
    private RecyclerView recyclerView;
    private TextView matchesCount;
    private Button btnSubscribeConnections;
    private LinearLayout emptyState;
    private ProgressBar progressBar;

    // Data
    private MatchesAdapter adapter;
    private List<Match> matches;
    private String token;

    // API
    private ApiService apiService;
    private SessionManager sessionManager;
    private SubscriptionRepository subscriptionRepository;
    private LoadingDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connections, container, false);

        initializeViews(view);
        initializeData();
        loadMatches();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar matches cuando volvemos al fragmento
        loadMatches();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.matchesRecyclerView);
        matchesCount = view.findViewById(R.id.matchesCount);
        btnSubscribeConnections = view.findViewById(R.id.btnSubscribeConnections);
        emptyState = view.findViewById(R.id.emptyStateConnections);
        progressBar = view.findViewById(R.id.progressBarConnections);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void initializeData() {
        sessionManager = new SessionManager(requireContext());
        token = sessionManager.getToken();
        loadingDialog = new LoadingDialog(requireContext());
        subscriptionRepository = new SubscriptionRepository();

        if (token == null) {
            token = "";
        }

        apiService = ApiClient.getApiService();
        matches = new ArrayList<>();

        adapter = new MatchesAdapter(requireContext(), matches, match -> {
            if (!isAdded() || getContext() == null) return;

            // Al hacer click en un match, abrir el perfil completo
            Intent intent = new Intent(getContext(), ViewProfileActivity.class);
            intent.putExtra("user_id", match.getOtherUser().getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        setupSubscriptionButton();
        updateSubscriptionButton();
    }

    private void setupSubscriptionButton() {
        btnSubscribeConnections.setOnClickListener(v -> handleSubscription());
    }

    private void loadMatches() {
        loadingDialog.show("Cargando conexiones...");

        Call<MatchesResponse> call = apiService.getMatches("Bearer " + token);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<MatchesResponse> call, @NonNull Response<MatchesResponse> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    MatchesResponse matchesResponse = response.body();
                    matches.clear();
                    matches.addAll(matchesResponse.getMatches());
                    adapter.notifyDataSetChanged();

                    // Actualizar contador
                    int count = matches.size();
                    matchesCount.setText(count + (count == 1 ? " conexión" : " conexiones"));

                    // Mostrar/ocultar empty state
                    if (matches.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al cargar conexiones", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }

                // Notificar que el fragmento está listo
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).setFragmentLoading(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MatchesResponse> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                showEmptyState();

                // Notificar que el fragmento está listo (aunque haya error)
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).setFragmentLoading(false);
                }
            }
        });
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void handleSubscription() {
        loadingDialog.show("Verificando suscripción...");

        if (token == null || token.isEmpty()) {
            loadingDialog.dismiss();
            Toast.makeText(requireContext(), "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        subscriptionRepository.getSubscriptionStatus(token, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<com.example.sportconnection.model.SubscriptionStatus> call, @NonNull Response<com.example.sportconnection.model.SubscriptionStatus> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    com.example.sportconnection.model.SubscriptionStatus status = response.body();

                    if (status.isActive()) {
                        showSubscriptionDetails(status);
                    } else {
                        showSubscriptionPlans();
                    }
                } else if (response.code() == 404 || response.code() == 500) {
                    Log.d(TAG, "Usuario sin suscripción, mostrando planes");
                    showSubscriptionPlans();
                } else {
                    Toast.makeText(requireContext(), "Error al verificar suscripción", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.example.sportconnection.model.SubscriptionStatus> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSubscriptionButton() {
        if (token == null || token.isEmpty()) {
            return;
        }

        subscriptionRepository.getSubscriptionStatus(token, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<com.example.sportconnection.model.SubscriptionStatus> call, @NonNull Response<com.example.sportconnection.model.SubscriptionStatus> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.sportconnection.model.SubscriptionStatus status = response.body();

                    if (status.isActive()) {
                        btnSubscribeConnections.setText("Premium ✓");
                        btnSubscribeConnections.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        btnSubscribeConnections.setText("Suscribirse");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.example.sportconnection.model.SubscriptionStatus> call, @NonNull Throwable t) {
                // Mantener texto por defecto
            }
        });
    }

    private void showSubscriptionDetails(com.example.sportconnection.model.SubscriptionStatus status) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Suscripción Activa");

        com.example.sportconnection.model.SubscriptionStatus.SubscriptionDetails details = status.getSubscriptionDetails();
        String message = "Plan: " + details.getPlanName() + "\n" +
                        "Estado: " + details.getStatus() + "\n" +
                        "Válida hasta: " + formatSubscriptionDate(details.getEndDate());

        builder.setMessage(message);
        builder.setPositiveButton("Aceptar", null);
        builder.setNeutralButton("Renovar", (dialog, which) -> showSubscriptionPlans());
        builder.setNegativeButton("Cancelar Suscripción", (dialog, which) -> showCancelSubscriptionConfirmation());
        builder.show();
    }

    private void showSubscriptionPlans() {
        loadingDialog.show("Cargando planes...");

        if (token == null || token.isEmpty()) {
            loadingDialog.dismiss();
            Toast.makeText(requireContext(), "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        subscriptionRepository.getSubscriptionPlans(token, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<com.example.sportconnection.model.SubscriptionPlan>> call, @NonNull Response<List<com.example.sportconnection.model.SubscriptionPlan>> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<com.example.sportconnection.model.SubscriptionPlan> plans = response.body();
                    displaySubscriptionPlansDialog(plans);
                } else {
                    Toast.makeText(requireContext(), "No hay planes disponibles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<com.example.sportconnection.model.SubscriptionPlan>> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(requireContext(), "Error al cargar planes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displaySubscriptionPlansDialog(List<com.example.sportconnection.model.SubscriptionPlan> plans) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Selecciona un Plan Premium");

        String[] planNames = new String[plans.size()];
        for (int i = 0; i < plans.size(); i++) {
            planNames[i] = plans.get(i).toString();
        }

        builder.setItems(planNames, (dialog, which) -> {
            com.example.sportconnection.model.SubscriptionPlan selectedPlan = plans.get(which);
            createCheckoutSession(selectedPlan.getId());
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void createCheckoutSession(int planId) {
        loadingDialog.show("Preparando pago...");

        if (token == null || token.isEmpty()) {
            loadingDialog.dismiss();
            Toast.makeText(requireContext(), "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        subscriptionRepository.createCheckoutSession(token, planId, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<com.example.sportconnection.model.CheckoutSessionResponse> call, @NonNull Response<com.example.sportconnection.model.CheckoutSessionResponse> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    com.example.sportconnection.model.CheckoutSessionResponse checkoutResponse = response.body();
                    String checkoutUrl = checkoutResponse.getCheckoutUrl();
                    openStripeCheckout(checkoutUrl);
                } else {
                    String errorMessage = "Error al crear sesión de pago";
                    if (response.code() == 400) {
                        errorMessage = "Ya tienes una suscripción activa";
                    } else if (response.code() == 500) {
                        errorMessage = "Error del servidor. Las claves de Stripe pueden no estar configuradas.";
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.example.sportconnection.model.CheckoutSessionResponse> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openStripeCheckout(String checkoutUrl) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl));
            startActivity(browserIntent);
            Toast.makeText(requireContext(), "Completa el pago en el navegador", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error al abrir el navegador", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatSubscriptionDate(String isoDate) {
        if (isoDate != null && isoDate.length() >= 10) {
            String datePart = isoDate.substring(0, 10);
            String[] parts = datePart.split("-");
            if (parts.length == 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
        }
        return isoDate;
    }

    private void showCancelSubscriptionConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Cancelar Suscripción");
        builder.setMessage("¿Estás seguro que deseas cancelar tu suscripción? Perderás acceso a todas las funciones premium inmediatamente.");

        builder.setPositiveButton("Sí, Cancelar", (dialog, which) -> cancelSubscription());
        builder.setNegativeButton("No, Mantener", null);
        builder.show();
    }

    private void cancelSubscription() {
        loadingDialog.show("Cancelando suscripción...");

        if (token == null || token.isEmpty()) {
            loadingDialog.dismiss();
            Toast.makeText(requireContext(), "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        subscriptionRepository.cancelSubscription(token, new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Suscripción cancelada exitosamente", Toast.LENGTH_LONG).show();
                    updateSubscriptionButton();
                } else {
                    String errorMessage = "Error al cancelar suscripción";
                    if (response.code() == 404) {
                        errorMessage = "No se encontró una suscripción activa";
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(requireContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

