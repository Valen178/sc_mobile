package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private String token;
    private UserData user;

    @SerializedName("requiresProfile")
    private boolean requiresProfile;

    // Para mantener compatibilidad con código existente
    public boolean isSuccess() {
        // Si tenemos token y user, el login fue exitoso
        return token != null && !token.isEmpty() && user != null;
    }

    public String getMessage() {
        // Mensaje por defecto
        return isSuccess() ? "Login exitoso" : "Error en el login";
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    public boolean isRequiresProfile() {
        return requiresProfile;
    }

    public void setRequiresProfile(boolean requiresProfile) {
        this.requiresProfile = requiresProfile;
    }

    // Método helper para obtener el userId directamente
    public int getUserId() {
        return user != null ? user.getId() : 0;
    }

    public static class UserData {
        private int id;
        private String email;

        // El backend envía "role" pero lo mapeamos a "profileType"
        @SerializedName("role")
        private String profileType;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getProfileType() {
            return profileType;
        }

        public void setProfileType(String profileType) {
            this.profileType = profileType;
        }

        // Método adicional por si necesitamos acceder como "role"
        public String getRole() {
            return profileType;
        }
    }
}

