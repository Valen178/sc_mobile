package com.example.sportconnection.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.sportconnection.HomeActivity;
import com.example.sportconnection.R;
import com.example.sportconnection.model.DiscoverResponse;
import com.example.sportconnection.model.SwipeRequest;
import com.example.sportconnection.model.SwipeResponse;
import com.example.sportconnection.model.SwipeUser;
import com.example.sportconnection.network.ApiClient;
import com.example.sportconnection.network.ApiService;
import com.example.sportconnection.repository.SubscriptionRepository;
import com.example.sportconnection.utils.LoadingDialog;
import com.example.sportconnection.utils.SwipeCardHelper;
import com.example.sportconnection.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnectFragment extends Fragment {

    private static final String TAG = "ConnectFragment";

    // UI Components
    private LinearLayout filterContainer;
    private Button btnFilterAll, btnFilterTeams, btnFilterAgents;
    private Button btnSubscribeConnect;
    private FrameLayout cardContainer;
    private LinearLayout buttonContainer;
    private ImageButton btnLike, btnDislike;
    private LinearLayout emptyStateContainer;
    private ProgressBar progressBar;

    // Data
    private List<SwipeUser> userList;
    private int currentUserIndex = 0;
    private String currentFilter = "both"; // both, team, agent
    private String userProfileType;
    private String token;

    // API
    private ApiService apiService;
    private SessionManager sessionManager;
    private SubscriptionRepository subscriptionRepository;
    private LoadingDialog loadingDialog;

    // Current card view
    private View currentCardView;
    private SwipeCardHelper currentSwipeHelper;

    // API Calls tracking
    private Call<DiscoverResponse> currentDiscoverCall;
    private Call<SwipeResponse> currentSwipeCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, container, false);

        initializeViews(view);
        initializeData();
        setupListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // La notificación a HomeActivity se hace en el callback de loadUsers()
    }

    private void initializeViews(View view) {
        filterContainer = view.findViewById(R.id.filterContainer);
        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterTeams = view.findViewById(R.id.btnFilterTeams);
        btnFilterAgents = view.findViewById(R.id.btnFilterAgents);
        btnSubscribeConnect = view.findViewById(R.id.btnSubscribeConnect);
        cardContainer = view.findViewById(R.id.cardContainer);
        buttonContainer = view.findViewById(R.id.buttonContainer);
        btnLike = view.findViewById(R.id.btnLike);
        btnDislike = view.findViewById(R.id.btnDislike);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void initializeData() {
        sessionManager = new SessionManager(requireContext());
        token = sessionManager.getToken();
        userProfileType = sessionManager.getProfileType();
        loadingDialog = new LoadingDialog(requireContext());
        subscriptionRepository = new SubscriptionRepository();

        if (token == null) {
            token = "";
        }
        if (userProfileType == null) {
            userProfileType = "";
        }

        apiService = ApiClient.getApiService();
        userList = new ArrayList<>();

        // Mostrar filtros solo para atletas
        if ("athlete".equals(userProfileType)) {
            filterContainer.setVisibility(View.VISIBLE);
        } else {
            filterContainer.setVisibility(View.GONE);
        }

        setupSubscriptionButton();
        updateSubscriptionButton();
        loadUsers();
    }

    private void setupSubscriptionButton() {
        btnSubscribeConnect.setOnClickListener(v -> handleSubscription());
    }

    private void setupListeners() {
        // Filtros
        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "both";
            updateFilterButtons();
            reloadUsers();
        });

        btnFilterTeams.setOnClickListener(v -> {
            currentFilter = "team";
            updateFilterButtons();
            reloadUsers();
        });

        btnFilterAgents.setOnClickListener(v -> {
            currentFilter = "agent";
            updateFilterButtons();
            reloadUsers();
        });

        // Botones de acción
        btnLike.setOnClickListener(v -> handleLike());
        btnDislike.setOnClickListener(v -> handleDislike());
    }

    private void updateFilterButtons() {
        // Reset todos los botones
        btnFilterAll.setBackgroundResource(R.drawable.filter_button_background);
        btnFilterAll.setTextColor(getResources().getColor(android.R.color.darker_gray));
        btnFilterTeams.setBackgroundResource(R.drawable.filter_button_background);
        btnFilterTeams.setTextColor(getResources().getColor(android.R.color.darker_gray));
        btnFilterAgents.setBackgroundResource(R.drawable.filter_button_background);
        btnFilterAgents.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // Marcar el botón seleccionado
        switch (currentFilter) {
            case "both":
                btnFilterAll.setBackgroundResource(R.drawable.filter_button_selected);
                btnFilterAll.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "team":
                btnFilterTeams.setBackgroundResource(R.drawable.filter_button_selected);
                btnFilterTeams.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "agent":
                btnFilterAgents.setBackgroundResource(R.drawable.filter_button_selected);
                btnFilterAgents.setTextColor(getResources().getColor(android.R.color.white));
                break;
        }
    }

    private void loadUsers() {
        loadingDialog.show("Cargando usuarios...");

        String filter = "athlete".equals(userProfileType) ? currentFilter : null;

        Call<DiscoverResponse> call = apiService.getDiscoverUsers("Bearer " + token, filter, 20);
        call.enqueue(new Callback<DiscoverResponse>() {
            @Override
            public void onResponse(Call<DiscoverResponse> call, Response<DiscoverResponse> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    DiscoverResponse discoverResponse = response.body();
                    userList.clear();
                    userList.addAll(discoverResponse.getUsers());
                    currentUserIndex = 0;

                    if (userList.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                        showNextCard();
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }

                // Notificar que el fragmento está listo
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).setFragmentLoading(false);
                }
            }

            @Override
            public void onFailure(Call<DiscoverResponse> call, Throwable t) {
                loadingDialog.dismiss();
                Log.e(TAG, "Error loading users", t);
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                showEmptyState();

                // Notificar que el fragmento está listo (aunque haya error)
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).setFragmentLoading(false);
                }
            }
        });
    }

    private void reloadUsers() {
        cardContainer.removeAllViews();
        currentUserIndex = 0;
        loadUsers();
    }

    private void showNextCard() {
        if (currentUserIndex >= userList.size()) {
            // No hay más usuarios, cargar más
            loadUsers();
            return;
        }

        SwipeUser user = userList.get(currentUserIndex);

        // Inflar la card
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        currentCardView = inflater.inflate(R.layout.item_swipe_card, cardContainer, false);

        // Llenar datos
        populateCard(currentCardView, user);

        // Agregar al contenedor
        cardContainer.addView(currentCardView);

        // Configurar gestos de swipe
        currentSwipeHelper = new SwipeCardHelper(currentCardView, new SwipeCardHelper.SwipeListener() {
            @Override
            public void onSwipeRight() {
                performSwipe(user.getUserId(), "like");
            }

            @Override
            public void onSwipeLeft() {
                performSwipe(user.getUserId(), "dislike");
            }

            @Override
            public void onCardExited() {
                currentUserIndex++;
                showNextCard();
            }
        });
    }

    private void populateCard(View cardView, SwipeUser user) {
        ImageView userPhoto = cardView.findViewById(R.id.userPhoto);
        TextView userName = cardView.findViewById(R.id.userName);
        TextView userProfileType = cardView.findViewById(R.id.userProfileType);
        TextView userJob = cardView.findViewById(R.id.userJob);
        TextView userLocation = cardView.findViewById(R.id.userLocation);
        TextView userSport = cardView.findViewById(R.id.userSport);
        TextView userDescription = cardView.findViewById(R.id.userDescription);

        // Nombre
        userName.setText(user.getName());

        // Tipo de perfil
        userProfileType.setText(user.getProfileTypeDisplayName());

        // Trabajo
        if (user.getJob() != null && !user.getJob().isEmpty()) {
            userJob.setText(user.getJob());
            userJob.setVisibility(View.VISIBLE);
        } else {
            userJob.setVisibility(View.GONE);
        }

        // Ubicación
        if (user.getLocation() != null) {
            String location = user.getLocation().getCity() + ", " + user.getLocation().getCountry();
            userLocation.setText(location);
        }

        // Deporte
        if (user.getSport() != null) {
            userSport.setText(user.getSport().getName());
        }

        // Descripción
        if (user.getDescription() != null && !user.getDescription().isEmpty()) {
            userDescription.setText(user.getDescription());
            userDescription.setVisibility(View.VISIBLE);
        } else {
            userDescription.setVisibility(View.GONE);
        }

        // Foto
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            Glide.with(requireContext())
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(userPhoto);
        }
    }

    private void handleLike() {
        if (currentUserIndex < userList.size() && currentSwipeHelper != null) {
            currentSwipeHelper.animateProgrammaticSwipeRight();
        }
    }

    private void handleDislike() {
        if (currentUserIndex < userList.size() && currentSwipeHelper != null) {
            currentSwipeHelper.animateProgrammaticSwipeLeft();
        }
    }

    private void performSwipe(int swipedUserId, String action) {
        SwipeRequest request = new SwipeRequest(swipedUserId, action);

        currentSwipeCall = apiService.swipe("Bearer " + token, request);
        currentSwipeCall.enqueue(new Callback<SwipeResponse>() {
            @Override
            public void onResponse(Call<SwipeResponse> call, Response<SwipeResponse> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    SwipeResponse swipeResponse = response.body();

                    if (swipeResponse.isMatch()) {
                        Toast.makeText(getContext(), "¡Match! " + swipeResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<SwipeResponse> call, Throwable t) {
                // Silenciar errores si el fragmento ya no está adjunto
            }
        });
    }

    private void showEmptyState() {
        emptyStateContainer.setVisibility(View.VISIBLE);
        buttonContainer.setVisibility(View.GONE);
        cardContainer.removeAllViews();
    }

    private void hideEmptyState() {
        emptyStateContainer.setVisibility(View.GONE);
        currentSwipeHelper = null;
    }

    private void handleSubscription() {
        loadingDialog.show("Verificando suscripción...");

        if (token == null || token.isEmpty()) {
            loadingDialog.dismiss();
            Toast.makeText(requireContext(), "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        subscriptionRepository.getSubscriptionStatus(token, new Callback<com.example.sportconnection.model.SubscriptionStatus>() {
            @Override
            public void onResponse(Call<com.example.sportconnection.model.SubscriptionStatus> call, Response<com.example.sportconnection.model.SubscriptionStatus> response) {
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
            public void onFailure(Call<com.example.sportconnection.model.SubscriptionStatus> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSubscriptionButton() {
        if (token == null || token.isEmpty()) {
            return;
        }

        subscriptionRepository.getSubscriptionStatus(token, new Callback<com.example.sportconnection.model.SubscriptionStatus>() {
            @Override
            public void onResponse(Call<com.example.sportconnection.model.SubscriptionStatus> call, Response<com.example.sportconnection.model.SubscriptionStatus> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.sportconnection.model.SubscriptionStatus status = response.body();

                    if (status.isActive()) {
                        btnSubscribeConnect.setText("Premium ✓");
                        btnSubscribeConnect.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        btnSubscribeConnect.setText("Suscribirse");
                    }
                }
            }

            @Override
            public void onFailure(Call<com.example.sportconnection.model.SubscriptionStatus> call, Throwable t) {
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

        subscriptionRepository.getSubscriptionPlans(token, new Callback<List<com.example.sportconnection.model.SubscriptionPlan>>() {
            @Override
            public void onResponse(Call<List<com.example.sportconnection.model.SubscriptionPlan>> call, Response<List<com.example.sportconnection.model.SubscriptionPlan>> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<com.example.sportconnection.model.SubscriptionPlan> plans = response.body();
                    displaySubscriptionPlansDialog(plans);
                } else {
                    Toast.makeText(requireContext(), "No hay planes disponibles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<com.example.sportconnection.model.SubscriptionPlan>> call, Throwable t) {
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

        subscriptionRepository.createCheckoutSession(token, planId, new Callback<com.example.sportconnection.model.CheckoutSessionResponse>() {
            @Override
            public void onResponse(Call<com.example.sportconnection.model.CheckoutSessionResponse> call, Response<com.example.sportconnection.model.CheckoutSessionResponse> response) {
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
            public void onFailure(Call<com.example.sportconnection.model.CheckoutSessionResponse> call, Throwable t) {
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

        // Cancelar llamadas API pendientes
        if (currentDiscoverCall != null && !currentDiscoverCall.isCanceled()) {
            currentDiscoverCall.cancel();
        }
        if (currentSwipeCall != null && !currentSwipeCall.isCanceled()) {
            currentSwipeCall.cancel();
        }

        // Cerrar el diálogo si está abierto
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }

        // Limpiar vistas
        if (cardContainer != null) {
            cardContainer.removeAllViews();
        }
        currentCardView = null;
        currentSwipeHelper = null;
    }
}
