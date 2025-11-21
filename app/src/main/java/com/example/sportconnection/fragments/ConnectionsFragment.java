package com.example.sportconnection.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private LinearLayout emptyState;
    private ProgressBar progressBar;

    // Data
    private MatchesAdapter adapter;
    private List<Match> matches;
    private String token;

    // API
    private ApiService apiService;
    private SessionManager sessionManager;
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
        // La notificación a HomeActivity se hace en el callback de loadMatches()
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.matchesRecyclerView);
        matchesCount = view.findViewById(R.id.matchesCount);
        emptyState = view.findViewById(R.id.emptyStateConnections);
        progressBar = view.findViewById(R.id.progressBarConnections);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void initializeData() {
        sessionManager = new SessionManager(requireContext());
        token = sessionManager.getToken();
        loadingDialog = new LoadingDialog(requireContext());

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
    }

    private void loadMatches() {
        loadingDialog.show("Cargando conexiones...");

        Call<MatchesResponse> call = apiService.getMatches("Bearer " + token);
        call.enqueue(new Callback<MatchesResponse>() {
            @Override
            public void onResponse(Call<MatchesResponse> call, Response<MatchesResponse> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    MatchesResponse matchesResponse = response.body();
                    matches.clear();
                    matches.addAll(matchesResponse.getMatches());
                    adapter.notifyDataSetChanged();

                    // Actualizar contador
                    int count = matchesResponse.getCount();
                    matchesCount.setText(count + (count == 1 ? " conexión" : " conexiones"));

                    // Mostrar estado vacío si no hay matches
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
            public void onFailure(Call<MatchesResponse> call, Throwable t) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Cerrar el diálogo si está abierto
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}

