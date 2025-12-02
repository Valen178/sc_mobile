package com.example.sportconnection.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.sportconnection.R;
import com.example.sportconnection.model.Venue;
import com.example.sportconnection.model.VenuesResponse;
import com.example.sportconnection.network.ApiClient;
import com.example.sportconnection.network.ApiService;
import com.example.sportconnection.repository.SubscriptionRepository;
import com.example.sportconnection.utils.LoadingDialog;
import com.example.sportconnection.utils.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ApiService apiService;
    private SessionManager sessionManager;
    private SubscriptionRepository subscriptionRepository;
    private LoadingDialog loadingDialog;
    private String authToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Inicializar API service
        apiService = ApiClient.getClient().create(ApiService.class);

        // Obtener token
        sessionManager = new SessionManager(requireContext());
        authToken = sessionManager.getToken();

        // Inicializar repositorios y utilidades
        subscriptionRepository = new SubscriptionRepository();
        loadingDialog = new LoadingDialog(requireActivity());

        // Inicializar location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Configurar botón de suscripción
        Button btnSubscribeMap = view.findViewById(R.id.btnSubscribeMap);
        btnSubscribeMap.setOnClickListener(v -> handleSubscription());
        updateSubscriptionButton(btnSubscribeMap);

        // Inicializar mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(requireContext());

        if (status != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Google Play Services error: " + status);
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(requireActivity(), status, 2404).show();
            }
            return false;
        }

        Log.d(TAG, "Google Play Services disponible y actualizado");
        return true;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        Log.d(TAG, "onMapReady llamado - GoogleMap recibido");

        try {
            // Configurar UI del mapa
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setMapToolbarEnabled(true);

            // Configurar tipo de mapa
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            Log.d(TAG, "Configuración de UI del mapa completada");

            // Habilitar ubicación si hay permisos
            enableMyLocation();

            // Cargar venues desde la API
            loadVenues();

            Log.d(TAG, "Mapa inicializado correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error al configurar el mapa", e);
            Toast.makeText(requireContext(), "Error al configurar el mapa: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            if (googleMap != null) {
                try {
                    googleMap.setMyLocationEnabled(true);

                    // Obtener ubicación actual y mover cámara
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(requireActivity(), location -> {
                                if (location != null) {
                                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12f));
                                    Log.d(TAG, "Ubicación del usuario obtenida: " + currentLocation.toString());
                                } else {
                                    // Ubicación por defecto (Buenos Aires)
                                    setDefaultLocation();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al obtener ubicación", e);
                                setDefaultLocation();
                            });
                } catch (SecurityException e) {
                    Log.e(TAG, "SecurityException al habilitar ubicación", e);
                }
            }
        } else {
            // Sin permisos, usar ubicación por defecto
            Log.d(TAG, "Sin permisos de ubicación, usando ubicación por defecto");
            setDefaultLocation();

            // Solicitar permisos
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void setDefaultLocation() {
        if (googleMap != null) {
            // Ubicación por defecto (Buenos Aires, Argentina)
            LatLng defaultLocation = new LatLng(-34.599722222222, -58.381944444444);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));
            Log.d(TAG, "Usando ubicación por defecto: Buenos Aires");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, habilitar ubicación
                enableMyLocation();
                Toast.makeText(requireContext(), "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show();
            } else {
                // Permiso denegado, usar ubicación por defecto
                Toast.makeText(requireContext(), "El mapa funcionará sin tu ubicación actual", Toast.LENGTH_SHORT).show();
                setDefaultLocation();
            }
        }
    }

    private void loadVenues() {
        if (authToken == null) {
            Log.e(TAG, "No auth token available");
            return;
        }

        Call<VenuesResponse> call = apiService.getVenues("Bearer " + authToken);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<VenuesResponse> call, @NonNull Response<VenuesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Venue> venues = response.body().getData();
                    if (venues != null && !venues.isEmpty()) {
                        displayVenuesOnMap(venues);
                        Log.d(TAG, "Loaded " + venues.size() + " venues");
                    } else {
                        Log.d(TAG, "No venues found");
                        Toast.makeText(requireContext(), "No hay lugares deportivos disponibles", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Error loading venues: " + response.code());
                    Toast.makeText(requireContext(), "Error al cargar lugares deportivos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<VenuesResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error loading venues", t);
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayVenuesOnMap(List<Venue> venues) {
        if (googleMap == null) {
            Log.e(TAG, "GoogleMap es null, no se pueden mostrar venues");
            return;
        }

        Log.d(TAG, "Mostrando " + venues.size() + " venues en el mapa");

        for (Venue venue : venues) {
            LatLng position = new LatLng(venue.getLat(), venue.getLng());

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(venue.getName())
                    .snippet(venue.getAddress());

            googleMap.addMarker(markerOptions);
            Log.d(TAG, "Marcador agregado: " + venue.getName() + " en " + position.toString());
        }

        // Si hay venues, mover la cámara al primero (solo si no se ha movido a ubicación del usuario)
        if (!venues.isEmpty() && !hasMovedCameraToUserLocation()) {
            Venue firstVenue = venues.get(0);
            LatLng firstPosition = new LatLng(firstVenue.getLat(), firstVenue.getLng());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstPosition, 10f));
            Log.d(TAG, "Cámara movida al primer venue: " + firstVenue.getName());
        }
    }

    private boolean hasMovedCameraToUserLocation() {
        // Si el usuario tiene permisos de ubicación, asumimos que ya movimos la cámara
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void handleSubscription() {
        loadingDialog.show("Verificando suscripción...");

        if (authToken == null) {
            loadingDialog.dismiss();
            Toast.makeText(requireContext(), "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        subscriptionRepository.getSubscriptionStatus("Bearer " + authToken, new Callback<com.example.sportconnection.model.SubscriptionStatus>() {
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

    private void updateSubscriptionButton(Button btnSubscribe) {
        if (authToken == null) {
            return;
        }

        subscriptionRepository.getSubscriptionStatus("Bearer " + authToken, new Callback<com.example.sportconnection.model.SubscriptionStatus>() {
            @Override
            public void onResponse(Call<com.example.sportconnection.model.SubscriptionStatus> call, Response<com.example.sportconnection.model.SubscriptionStatus> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.sportconnection.model.SubscriptionStatus status = response.body();

                    if (status.isActive()) {
                        btnSubscribe.setText("Premium ✓");
                        btnSubscribe.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        btnSubscribe.setText("Suscribirse");
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

        if (authToken == null) {
            loadingDialog.dismiss();
            Toast.makeText(requireContext(), "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        subscriptionRepository.getSubscriptionPlans("Bearer " + authToken, new Callback<List<com.example.sportconnection.model.SubscriptionPlan>>() {
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

        if (authToken == null) {
            loadingDialog.dismiss();
            Toast.makeText(requireContext(), "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        subscriptionRepository.createCheckoutSession("Bearer " + authToken, planId, new Callback<com.example.sportconnection.model.CheckoutSessionResponse>() {
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

        if (authToken == null) {
            loadingDialog.dismiss();
            Toast.makeText(requireContext(), "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        subscriptionRepository.cancelSubscription("Bearer " + authToken, new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Suscripción cancelada exitosamente", Toast.LENGTH_LONG).show();
                    
                    // Actualizar el botón de suscripción
                    Button btnSubscribe = getView() != null ? getView().findViewById(R.id.btnSubscribeMap) : null;
                    if (btnSubscribe != null) {
                        updateSubscriptionButton(btnSubscribe);
                    }
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
}

