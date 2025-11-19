package com.example.sportconnection.repository;

import android.util.Log;

import com.example.sportconnection.model.CreatePostRequest;
import com.example.sportconnection.model.CreatePostResponse;
import com.example.sportconnection.model.MyPostsResponse;
import com.example.sportconnection.model.Post;
import com.example.sportconnection.network.ApiClient;
import com.example.sportconnection.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostRepository {
    private static final String TAG = "PostRepository";
    private final ApiService apiService;

    public PostRepository() {
        this.apiService = ApiClient.getApiService();
    }

    public interface PostsCallback {
        void onSuccess(List<Post> posts);
        void onError(String message);
    }

    public interface CreatePostCallback {
        void onSuccess(Post post);
        void onError(String message);
    }

    public interface DeletePostCallback {
        void onSuccess();
        void onError(String message);
    }

    // Obtener todas las publicaciones
    public void getAllPosts(PostsCallback callback) {
        apiService.getAllPosts().enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al obtener las publicaciones");
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    // Obtener mis publicaciones
    public void getMyPosts(String token, PostsCallback callback) {
        apiService.getMyPosts("Bearer " + token).enqueue(new Callback<MyPostsResponse>() {
            @Override
            public void onResponse(Call<MyPostsResponse> call, Response<MyPostsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getPosts());
                } else {
                    callback.onError("Error al obtener tus publicaciones");
                }
            }

            @Override
            public void onFailure(Call<MyPostsResponse> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    // Crear una publicación
    public void createPost(String token, String text, String url, CreatePostCallback callback) {
        CreatePostRequest request = new CreatePostRequest(text, url);
        apiService.createPost("Bearer " + token, request).enqueue(new Callback<CreatePostResponse>() {
            @Override
            public void onResponse(Call<CreatePostResponse> call, Response<CreatePostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getPost());
                } else {
                    callback.onError("Error al crear la publicación");
                }
            }

            @Override
            public void onFailure(Call<CreatePostResponse> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    // Eliminar una publicación
    public void deletePost(String token, int postId, DeletePostCallback callback) {
        apiService.deletePost("Bearer " + token, postId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error al eliminar la publicación");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }
}

