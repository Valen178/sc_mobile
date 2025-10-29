package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class SignupResponse {
    private String message;
    private String token;

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("user")
    private User user;

    // Para mantener compatibilidad con código existente
    public boolean isSuccess() {
        // Si tenemos token o userId, el registro fue exitoso
        return (token != null && !token.isEmpty()) || userId != null || user != null;
    }

    public String getMessage() {
        if (message != null && !message.isEmpty()) {
            return message;
        }
        return isSuccess() ? "Usuario creado exitosamente" : "Error en el registro";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUserId() {
        if (userId != null) {
            return userId;
        }
        if (user != null) {
            return user.id;
        }
        return -1;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Clase interna para manejar el objeto user si viene en la respuesta
    public static class User {
        private int id;
        private String email;

        public int getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }
    }
}

