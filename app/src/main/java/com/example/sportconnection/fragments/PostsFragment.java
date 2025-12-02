package com.example.sportconnection.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sportconnection.CreatePostActivity;
import com.example.sportconnection.R;
import com.example.sportconnection.adapters.PostsPagerAdapter;
import com.example.sportconnection.repository.SubscriptionRepository;
import com.example.sportconnection.utils.LoadingDialog;
import com.example.sportconnection.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostsFragment extends Fragment {
    private static final String TAG = "PostsFragment";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabCreatePost;
    private Button btnSubscribePosts;
    private PostsPagerAdapter pagerAdapter;

    private ActivityResultLauncher<Intent> createPostLauncher;
    private SessionManager sessionManager;
    private SubscriptionRepository subscriptionRepository;
    private LoadingDialog loadingDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Registrar el launcher para crear post
        createPostLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Recargar los posts cuando se crea uno nuevo
                        refreshPosts();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar managers y repositorios
        sessionManager = new SessionManager(requireContext());
        subscriptionRepository = new SubscriptionRepository();
        loadingDialog = new LoadingDialog(requireActivity());

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        fabCreatePost = view.findViewById(R.id.fabCreatePost);
        btnSubscribePosts = view.findViewById(R.id.btnSubscribePosts);

        setupViewPager();
        setupFab();
        setupSubscriptionButton();
        updateSubscriptionButton();
    }

    private void setupSubscriptionButton() {
        btnSubscribePosts.setOnClickListener(v -> handleSubscription());
    }

    private void setupViewPager() {
        pagerAdapter = new PostsPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Conectar TabLayout con ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Todas las publicaciones");
                    } else {
                        tab.setText("Mis publicaciones");
                    }
                }
        ).attach();
    }

    private void setupFab() {
        fabCreatePost.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreatePostActivity.class);
            createPostLauncher.launch(intent);
        });
    }

    private void refreshPosts() {
        // Obtener los fragmentos y refrescarlos
        if (pagerAdapter.getAllPostsFragment() != null) {
            pagerAdapter.getAllPostsFragment().refreshPosts();
        }
        if (pagerAdapter.getMyPostsFragment() != null) {
            pagerAdapter.getMyPostsFragment().refreshPosts();
        }
    }

    private void handleSubscription() {
        loadingDialog.show("Verificando suscripción...");

        String token = sessionManager.getToken();
        if (token == null) {
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
                    Log.d(TAG, "Usuario sin suscripción (código " + response.code() + "), mostrando planes");
                    showSubscriptionPlans();
                } else {
                    String errorMsg = "Error al verificar suscripción (Código: " + response.code() + ")";
                    Log.e(TAG, errorMsg);
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.sportconnection.model.SubscriptionStatus> call, Throwable t) {
                loadingDialog.dismiss();
                Log.e(TAG, "Error de conexión al verificar suscripción: " + t.getMessage());
                Toast.makeText(requireContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSubscriptionButton() {
        String token = sessionManager.getToken();
        if (token == null) {
            return;
        }

        subscriptionRepository.getSubscriptionStatus(token, new Callback<com.example.sportconnection.model.SubscriptionStatus>() {
            @Override
            public void onResponse(Call<com.example.sportconnection.model.SubscriptionStatus> call, Response<com.example.sportconnection.model.SubscriptionStatus> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.sportconnection.model.SubscriptionStatus status = response.body();

                    if (status.isActive()) {
                        btnSubscribePosts.setText("Premium ✓");
                        btnSubscribePosts.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        btnSubscribePosts.setText("Suscribirse");
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

        String token = sessionManager.getToken();
        if (token == null) {
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
                Toast.makeText(requireContext(), "Error al cargar planes: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

        String token = sessionManager.getToken();
        if (token == null) {
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
                Toast.makeText(requireContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

        String token = sessionManager.getToken();
        if (token == null) {
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
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
