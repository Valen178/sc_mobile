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
import com.example.sportconnection.model.SwipeStatsResponse;
import com.example.sportconnection.model.ContactInfoResponse;
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

    // Premium y swipes
    private boolean isPremium = false;
    private Integer swipesRemaining = null;
    private TextView swipeCounterText;

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
        swipeCounterText = view.findViewById(R.id.swipeCounterText);
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

        // Debug: Log del tipo de perfil
        Log.d(TAG, "Tipo de perfil del usuario: " + userProfileType);

        // Mostrar filtros SOLO para atletas (profileType = "athlete")
        // Nota: "user" significa que no tiene perfil completado aún
        if ("athlete".equals(userProfileType)) {
            Log.d(TAG, "Usuario es atleta - Mostrando filtros");
            filterContainer.setVisibility(View.VISIBLE);
            updateFilterButtons(); // Inicializar el estado visual de los botones
        } else {
            Log.d(TAG, "Usuario NO es atleta (profileType: " + userProfileType + ") - Ocultando filtros");
            filterContainer.setVisibility(View.GONE);
        }

        setupSubscriptionButton();
        updateSubscriptionButton();
        loadSwipeStats();
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
            if (!isPremium) {
                showFilterPaywallDialog();
                return;
            }
            currentFilter = "team";
            updateFilterButtons();
            reloadUsers();
        });

        btnFilterAgents.setOnClickListener(v -> {
            if (!isPremium) {
                showFilterPaywallDialog();
                return;
            }
            currentFilter = "agent";
            updateFilterButtons();
            reloadUsers();
        });

        // Botones de acción
        btnLike.setOnClickListener(v -> handleLike());
        btnDislike.setOnClickListener(v -> handleDislike());
    }

    private void updateFilterButtons() {
        // Colores simples para los botones
        int colorNormal = 0xFFE0E0E0; // Gris claro
        int colorSelected = 0xFF2196F3; // Azul
        int textColorNormal = 0xFF757575; // Gris oscuro
        int textColorSelected = 0xFFFFFFFF; // Blanco

        // Reset todos los botones
        btnFilterAll.setBackgroundColor(colorNormal);
        btnFilterAll.setTextColor(textColorNormal);
        btnFilterTeams.setBackgroundColor(colorNormal);
        btnFilterTeams.setTextColor(textColorNormal);
        btnFilterAgents.setBackgroundColor(colorNormal);
        btnFilterAgents.setTextColor(textColorNormal);

        // Agregar candado si no es premium
        if (!isPremium) {
            btnFilterTeams.setText("Equipos 🔒");
            btnFilterAgents.setText("Agentes 🔒");
        } else {
            btnFilterTeams.setText("Equipos");
            btnFilterAgents.setText("Agentes");
        }

        // Marcar el botón seleccionado
        switch (currentFilter) {
            case "both":
                btnFilterAll.setBackgroundColor(colorSelected);
                btnFilterAll.setTextColor(textColorSelected);
                break;
            case "team":
                btnFilterTeams.setBackgroundColor(colorSelected);
                btnFilterTeams.setTextColor(textColorSelected);
                break;
            case "agent":
                btnFilterAgents.setBackgroundColor(colorSelected);
                btnFilterAgents.setTextColor(textColorSelected);
                break;
        }
    }

    private void loadUsers() {
        loadingDialog.show("Cargando usuarios...");

        // Solo aplicar filtro si es atleta (profileType = "athlete") y (es premium O el filtro es "both")
        String filter = null;
        if ("athlete".equals(userProfileType)) {
            if (isPremium || "both".equals(currentFilter)) {
                filter = currentFilter;
            } else {
                // Usuario no premium intentando usar filtros avanzados
                filter = "both";
            }
        }

        Call<DiscoverResponse> call = apiService.getDiscoverUsers("Bearer " + token, filter, 20);
        call.enqueue(new Callback<DiscoverResponse>() {
            @Override
            public void onResponse(Call<DiscoverResponse> call, Response<DiscoverResponse> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    DiscoverResponse discoverResponse = response.body();

                    // IMPORTANTE: Actualizar el profileType desde el backend
                    String actualProfileType = discoverResponse.getUserProfileType();
                    if (actualProfileType != null && !actualProfileType.isEmpty()) {
                        Log.d(TAG, "ProfileType actualizado desde API: " + actualProfileType + " (anterior: " + userProfileType + ")");
                        userProfileType = actualProfileType;

                        // Actualizar visibilidad de filtros según el profileType real
                        if ("athlete".equals(userProfileType)) {
                            filterContainer.setVisibility(View.VISIBLE);
                            updateFilterButtons();
                        } else {
                            filterContainer.setVisibility(View.GONE);
                        }
                    }

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
        Button btnDirectContact = cardView.findViewById(R.id.btnDirectContactCard);

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

        // Botón de Contacto Directo Premium
        if (isPremium) {
            btnDirectContact.setText("📱 Contacto Directo");
            btnDirectContact.setVisibility(View.VISIBLE);
            btnDirectContact.setOnClickListener(v -> handleDirectContactFromCard(user.getUserId()));
        } else {
            btnDirectContact.setText("📱 Contacto Directo 🔒");
            btnDirectContact.setVisibility(View.VISIBLE);
            btnDirectContact.setOnClickListener(v -> showPremiumRequiredDialog());
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

                if (response.code() == 403) {
                    // Límite de swipes alcanzado
                    swipesRemaining = 0;
                    updateSwipeCounter();
                    showSwipeLimitDialog();
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    SwipeResponse swipeResponse = response.body();

                    // Actualizar contador de swipes
                    swipesRemaining = swipeResponse.getSwipesRemaining();
                    isPremium = swipeResponse.isPremium();
                    updateSwipeCounter();

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

    private void loadSwipeStats() {
        apiService.getSwipeStats("Bearer " + token).enqueue(new Callback<SwipeStatsResponse>() {
            @Override
            public void onResponse(Call<SwipeStatsResponse> call, Response<SwipeStatsResponse> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    SwipeStatsResponse stats = response.body();
                    isPremium = stats.isPremium();
                    swipesRemaining = stats.getSwipesRemaining();
                    updateSwipeCounter();
                }
            }

            @Override
            public void onFailure(Call<SwipeStatsResponse> call, Throwable t) {
                Log.e(TAG, "Error loading swipe stats", t);
            }
        });
    }

    private void updateSwipeCounter() {
        if (!isAdded() || getContext() == null) return;

        if (isPremium) {
            swipeCounterText.setText("✨ Premium - Swipes ilimitados");
            swipeCounterText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            swipeCounterText.setVisibility(View.VISIBLE);
        } else if (swipesRemaining != null) {
            if (swipesRemaining > 0) {
                swipeCounterText.setText("⚡ " + swipesRemaining + " swipes restantes hoy");
                swipeCounterText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                swipeCounterText.setVisibility(View.VISIBLE);
            } else {
                swipeCounterText.setText("⚠ Límite de swipes alcanzado. ¡Mejora a Premium!");
                swipeCounterText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                swipeCounterText.setVisibility(View.VISIBLE);
            }
        } else {
            swipeCounterText.setVisibility(View.GONE);
        }
    }

    private void showSwipeLimitDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("¡Límite alcanzado! 🎯");

        String message = "Has usado todos tus swipes de hoy.\n\n" +
                        "Con Premium obtienes:\n" +
                        "✅ Swipes ilimitados\n" +
                        "✅ Filtros avanzados\n" +
                        "✅ Contacto directo sin match";

        builder.setMessage(message);
        builder.setPositiveButton("Ver Planes Premium", (dialog, which) -> showSubscriptionPlans());
        builder.setNegativeButton("Volver mañana", null);
        builder.show();
    }

    private void showFilterPaywallDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Filtros Avanzados 🔍");

        String message = "Los filtros avanzados requieren una suscripción premium.\n\n" +
                        "Con Premium puedes:\n" +
                        "✅ Filtrar por equipos o agentes\n" +
                        "✅ Encontrar exactamente lo que buscas\n" +
                        "✅ Ahorrar tiempo en tu búsqueda";

        builder.setMessage(message);
        builder.setPositiveButton("Ver Planes Premium", (dialog, which) -> showSubscriptionPlans());
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void showPremiumRequiredDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Contacto Directo 📱");

        String message = "El contacto directo requiere una suscripción premium.\n\n" +
                        "Con Premium puedes:\n" +
                        "✅ Acceder a información de contacto sin match\n" +
                        "✅ Swipes ilimitados\n" +
                        "✅ Filtros avanzados";

        builder.setMessage(message);
        builder.setPositiveButton("Ver Planes Premium", (dialog, which) -> showSubscriptionPlans());
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void handleDirectContactFromCard(int targetUserId) {
        if (!isPremium) {
            showPremiumRequiredDialog();
            return;
        }

        // Usuario premium - obtener información de contacto
        loadingDialog.show("Obteniendo información de contacto...");

        apiService.getDirectContact("Bearer " + token, targetUserId).enqueue(new Callback<ContactInfoResponse>() {
            @Override
            public void onResponse(Call<ContactInfoResponse> call, Response<ContactInfoResponse> response) {
                if (!isAdded() || getContext() == null) return;

                loadingDialog.dismiss();

                if (response.code() == 403) {
                    showPremiumRequiredDialog();
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    showContactInfoDialog(response.body());
                } else {
                    Toast.makeText(getContext(), "Error al obtener información de contacto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ContactInfoResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;

                loadingDialog.dismiss();
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showContactInfoDialog(ContactInfoResponse contactInfo) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Información de Contacto");

        StringBuilder message = new StringBuilder();
        message.append("Nombre: ").append(contactInfo.getName());
        if (contactInfo.getLastName() != null) {
            message.append(" ").append(contactInfo.getLastName());
        }
        message.append("\n");

        ContactInfoResponse.ContactInfo contact = contactInfo.getContactInfo();

        if (contact.getPhone() != null && !contact.getPhone().isEmpty()) {
            message.append("\nTeléfono: ").append(contact.getPhone());
        }

        if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
            message.append("\nEmail: ").append(contact.getEmail());
        }

        if (contact.getInstagram() != null && !contact.getInstagram().isEmpty()) {
            message.append("\nInstagram: ").append(contact.getInstagram());
        }

        if (contact.getTwitter() != null && !contact.getTwitter().isEmpty()) {
            message.append("\nTwitter: ").append(contact.getTwitter());
        }

        builder.setMessage(message.toString());

        // Agregar botones de acción
        if (contact.getPhone() != null && !contact.getPhone().isEmpty()) {
            final String phoneNumber = contact.getPhone();
            builder.setPositiveButton("Abrir WhatsApp", (dialog, which) -> openWhatsApp(phoneNumber));
        }

        builder.setNeutralButton("Cerrar", null);
        builder.show();
    }

    private void openWhatsApp(String phoneNumber) {
        try {
            String cleanNumber = phoneNumber.replaceAll("[^0-9+]", "");
            String url = "https://wa.me/" + cleanNumber;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show();
        }
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
