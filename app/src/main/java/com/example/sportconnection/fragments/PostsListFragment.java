package com.example.sportconnection.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportconnection.R;
import com.example.sportconnection.adapters.PostsAdapter;
import com.example.sportconnection.model.GetProfileResponse;
import com.example.sportconnection.model.Post;
import com.example.sportconnection.network.ApiClient;
import com.example.sportconnection.repository.AuthRepository;
import com.example.sportconnection.repository.PostRepository;
import com.example.sportconnection.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostsListFragment extends Fragment implements PostsAdapter.OnPostActionListener {
    private static final String ARG_SHOW_MY_POSTS = "show_my_posts";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private PostsAdapter adapter;
    private PostRepository postRepository;
    private AuthRepository authRepository;
    private SessionManager sessionManager;
    private boolean showMyPosts;

    public static PostsListFragment newInstance(boolean showMyPosts) {
        PostsListFragment fragment = new PostsListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_MY_POSTS, showMyPosts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            showMyPosts = getArguments().getBoolean(ARG_SHOW_MY_POSTS, false);
        }
        postRepository = new PostRepository();
        authRepository = new AuthRepository();
        sessionManager = new SessionManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posts_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewPosts);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);

        setupRecyclerView();
        loadPosts();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        int userId = sessionManager.getUserId();
        adapter = new PostsAdapter(userId, showMyPosts, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadPosts() {
        showLoading(true);

        if (showMyPosts) {
            // Cargar mis posts
            String token = sessionManager.getToken();
            if (token != null) {
                // Primero obtener el perfil del usuario para tener su nombre
                authRepository.getProfile(token, new Callback<GetProfileResponse>() {
                    @Override
                    public void onResponse(Call<GetProfileResponse> call, Response<GetProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            GetProfileResponse profileResponse = response.body();
                            GetProfileResponse.ProfileData profileData = profileResponse.getProfile();

                            // Crear objeto PostUser con la información del perfil
                            Post.PostUser currentUser = new Post.PostUser();
                            currentUser.setId(sessionManager.getUserId());
                            currentUser.setEmail(sessionManager.getEmail());
                            currentUser.setRole(sessionManager.getProfileType());

                            if (profileData != null) {
                                currentUser.setName(profileData.getName());
                                currentUser.setLastName(profileData.getLastName());
                                currentUser.setPhotoUrl(profileData.getPhotoUrl());
                            }

                            // Ahora cargar los posts y agregar el usuario
                            postRepository.getMyPosts(token, new PostRepository.PostsCallback() {
                                @Override
                                public void onSuccess(List<Post> posts) {
                                    if (isAdded()) {
                                        // Agregar información del usuario a cada post
                                        for (Post post : posts) {
                                            post.setUser(currentUser);
                                        }
                                        showLoading(false);
                                        adapter.setPosts(posts);
                                        updateEmptyState(posts.isEmpty());
                                    }
                                }

                                @Override
                                public void onError(String message) {
                                    if (isAdded()) {
                                        showLoading(false);
                                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                        updateEmptyState(true);
                                    }
                                }
                            });
                        } else {
                            // Si no se puede obtener el perfil, cargar los posts sin información del usuario
                            loadMyPostsWithoutProfile(token);
                        }
                    }

                    @Override
                    public void onFailure(Call<GetProfileResponse> call, Throwable t) {
                        // Si falla, cargar los posts sin información del usuario
                        loadMyPostsWithoutProfile(token);
                    }
                });
            } else {
                showLoading(false);
                updateEmptyState(true);
            }
        } else {
            // Cargar todos los posts
            postRepository.getAllPosts(new PostRepository.PostsCallback() {
                @Override
                public void onSuccess(List<Post> posts) {
                    if (isAdded()) {
                        // Mostrar los posts inmediatamente con la información disponible
                        showLoading(false);
                        adapter.setPosts(posts);
                        updateEmptyState(posts.isEmpty());

                        // Obtener el token para las peticiones de perfil
                        String token = sessionManager.getToken();
                        if (token != null && !posts.isEmpty()) {
                            // Cargar los perfiles de los usuarios en segundo plano
                            loadUserProfilesForPosts(posts, token);
                        }
                    }
                }

                @Override
                public void onError(String message) {
                    if (isAdded()) {
                        showLoading(false);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        updateEmptyState(true);
                    }
                }
            });
        }
    }

    private void loadMyPostsWithoutProfile(String token) {
        postRepository.getMyPosts(token, new PostRepository.PostsCallback() {
            @Override
            public void onSuccess(List<Post> posts) {
                if (isAdded()) {
                    showLoading(false);
                    adapter.setPosts(posts);
                    updateEmptyState(posts.isEmpty());
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    showLoading(false);
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    updateEmptyState(true);
                }
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        emptyText.setText(showMyPosts ? "No tienes publicaciones" : "No hay publicaciones");
    }

    @Override
    public void onDeletePost(Post post) {
        String token = sessionManager.getToken();
        if (token != null) {
            postRepository.deletePost(token, post.getId(), new PostRepository.DeletePostCallback() {
                @Override
                public void onSuccess() {
                    if (isAdded()) {
                        adapter.removePost(post.getId());
                        Toast.makeText(requireContext(), "Publicación eliminada", Toast.LENGTH_SHORT).show();
                        updateEmptyState(adapter.getItemCount() == 0);
                    }
                }

                @Override
                public void onError(String message) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void refreshPosts() {
        loadPosts();
    }

    public void addNewPost(Post post) {
        if (showMyPosts) {
            adapter.addPost(post);
            updateEmptyState(false);
        } else {
            // Recargar todos los posts para incluir el nuevo
            loadPosts();
        }
    }

    private void loadUserProfilesForPosts(List<Post> posts, String token) {
        android.util.Log.d("PostsListFragment", "Iniciando carga de perfiles. Total posts: " + posts.size());

        if (posts.isEmpty()) {
            showLoading(false);
            adapter.setPosts(posts);
            updateEmptyState(true);
            return;
        }

        // Obtener lista de IDs únicos de usuarios
        java.util.Set<Integer> uniqueUserIds = new java.util.HashSet<>();
        for (Post post : posts) {
            if (post.getUser() != null) {
                uniqueUserIds.add(post.getUser().getId());
            }
        }

        android.util.Log.d("PostsListFragment", "IDs únicos de usuarios: " + uniqueUserIds);

        if (uniqueUserIds.isEmpty()) {
            showLoading(false);
            adapter.setPosts(posts);
            updateEmptyState(posts.isEmpty());
            return;
        }

        // Map para guardar los perfiles cargados
        final java.util.Map<Integer, GetProfileResponse.ProfileData> profilesCache = new java.util.HashMap<>();

        // Contador para saber cuándo se han cargado todos los perfiles únicos
        final int[] loadedProfiles = {0};
        final int totalUniqueUsers = uniqueUserIds.size();
        final int currentUserId = sessionManager.getUserId();

        android.util.Log.d("PostsListFragment", "Cargando " + totalUniqueUsers + " perfiles únicos. Usuario actual: " + currentUserId);

        for (Integer userId : uniqueUserIds) {
            // Si el userId es el del usuario actual, cargar con /profile/me
            if (userId == currentUserId) {
                android.util.Log.d("PostsListFragment", "Usuario " + userId + " es el usuario actual, usando /profile/me");

                authRepository.getProfile(token, new Callback<GetProfileResponse>() {
                    @Override
                    public void onResponse(Call<GetProfileResponse> call, Response<GetProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            GetProfileResponse profileResponse = response.body();
                            GetProfileResponse.ProfileData profileData = profileResponse.getProfile();

                            if (profileData != null) {
                                android.util.Log.d("PostsListFragment", "Perfil propio cargado - Name: " + profileData.getName() +
                                    ", PhotoURL: " + profileData.getPhotoUrl());
                                profilesCache.put(userId, profileData);
                            } else {
                                android.util.Log.w("PostsListFragment", "ProfileData es null para usuario actual " + userId);
                            }
                        } else {
                            android.util.Log.e("PostsListFragment", "Error al cargar perfil propio - Code: " + response.code());
                        }

                        loadedProfiles[0]++;
                        android.util.Log.d("PostsListFragment", "Progreso: " + loadedProfiles[0] + "/" + totalUniqueUsers);

                        if (loadedProfiles[0] >= totalUniqueUsers && isAdded()) {
                            android.util.Log.d("PostsListFragment", "Todos los perfiles cargados. Actualizando posts...");
                            updatePostsWithProfiles(posts, profilesCache);
                        }
                    }

                    @Override
                    public void onFailure(Call<GetProfileResponse> call, Throwable t) {
                        android.util.Log.e("PostsListFragment", "Error al cargar perfil propio: " + t.getMessage());
                        loadedProfiles[0]++;
                        if (loadedProfiles[0] >= totalUniqueUsers && isAdded()) {
                            updatePostsWithProfiles(posts, profilesCache);
                        }
                    }
                });
                continue; // Saltar al siguiente userId
            }

            // Para otros usuarios, usar /profile/{userId}
            Call<GetProfileResponse> call = ApiClient.getApiService()
                    .getUserProfile("Bearer " + token, userId);

            call.enqueue(new Callback<GetProfileResponse>() {
                @Override
                public void onResponse(Call<GetProfileResponse> call, Response<GetProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        GetProfileResponse profileResponse = response.body();
                        GetProfileResponse.ProfileData profileData = profileResponse.getProfile();

                        if (profileData != null) {
                            android.util.Log.d("PostsListFragment", "Perfil cargado para usuario " + userId +
                                " - Name: " + profileData.getName() +
                                ", PhotoURL: " + profileData.getPhotoUrl());
                            // Guardar el perfil en el cache
                            profilesCache.put(userId, profileData);
                        } else {
                            android.util.Log.w("PostsListFragment", "ProfileData es null para usuario " + userId);
                        }
                    } else {
                        android.util.Log.e("PostsListFragment", "Error al cargar perfil de usuario " + userId +
                            " - Code: " + response.code() +
                            ", Message: " + response.message());
                    }

                    // Incrementar contador
                    loadedProfiles[0]++;
                    android.util.Log.d("PostsListFragment", "Progreso: " + loadedProfiles[0] + "/" + totalUniqueUsers);

                    // Cuando todos los perfiles estén cargados, actualizar los posts
                    if (loadedProfiles[0] >= totalUniqueUsers && isAdded()) {
                        android.util.Log.d("PostsListFragment", "Todos los perfiles cargados. Actualizando posts...");
                        updatePostsWithProfiles(posts, profilesCache);
                    }
                }

                @Override
                public void onFailure(Call<GetProfileResponse> call, Throwable t) {
                    android.util.Log.e("PostsListFragment", "Error al cargar perfil de usuario " + userId + ": " + t.getMessage());

                    // Incrementar contador incluso si falla
                    loadedProfiles[0]++;

                    if (loadedProfiles[0] >= totalUniqueUsers && isAdded()) {
                        android.util.Log.d("PostsListFragment", "Todos los perfiles procesados (con errores). Actualizando posts...");
                        updatePostsWithProfiles(posts, profilesCache);
                    }
                }
            });
        }
    }

    private void updatePostsWithProfiles(List<Post> posts, java.util.Map<Integer, GetProfileResponse.ProfileData> profilesCache) {
        // Actualizar los nombres y fotos en todos los posts
        android.util.Log.d("PostsListFragment", "Actualizando posts con perfiles. Posts: " + posts.size() + ", Perfiles en cache: " + profilesCache.size());

        boolean hasUpdates = false;

        for (Post post : posts) {
            if (post.getUser() != null) {
                GetProfileResponse.ProfileData profileData = profilesCache.get(post.getUser().getId());
                if (profileData != null) {
                    android.util.Log.d("PostsListFragment", "Usuario ID " + post.getUser().getId() +
                        " - Name: " + profileData.getName() +
                        ", PhotoURL: " + profileData.getPhotoUrl());

                    post.getUser().setName(profileData.getName());
                    post.getUser().setLastName(profileData.getLastName());
                    post.getUser().setPhotoUrl(profileData.getPhotoUrl());
                    hasUpdates = true;
                } else {
                    android.util.Log.w("PostsListFragment", "No se encontró perfil para usuario ID: " + post.getUser().getId());
                }
            }
        }

        // Notificar al adaptador que los datos han cambiado
        if (hasUpdates) {
            android.util.Log.d("PostsListFragment", "Notificando cambios al adaptador");
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                });
            }
        }
    }
}

