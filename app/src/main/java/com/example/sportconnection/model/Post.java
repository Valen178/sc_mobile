package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class Post {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("text")
    private String text;

    @SerializedName("url")
    private String url;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("user")
    private PostUser user;

    // Constructor vacío
    public Post() {
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public PostUser getUser() {
        return user;
    }

    public void setUser(PostUser user) {
        this.user = user;
    }

    // Clase interna para el usuario del post
    public static class PostUser {
        @SerializedName("id")
        private int id;

        @SerializedName("email")
        private String email;

        @SerializedName("role")
        private String role;

        @SerializedName("name")
        private String name;

        @SerializedName("last_name")
        private String lastName;

        @SerializedName("photo_url")
        private String photoUrl;

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

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
        }

        // Método auxiliar para obtener el nombre completo
        public String getFullName() {
            if (name != null && !name.trim().isEmpty() && lastName != null && !lastName.trim().isEmpty()) {
                return name + " " + lastName;
            } else if (name != null && !name.trim().isEmpty()) {
                return name;
            } else if (lastName != null && !lastName.trim().isEmpty()) {
                return lastName;
            } else if (email != null && !email.trim().isEmpty()) {
                // Extraer un nombre del email (parte antes del @)
                String username = email.split("@")[0];
                // Capitalizar primera letra y reemplazar puntos/guiones con espacios
                username = username.replace(".", " ").replace("_", " ").replace("-", " ");
                // Capitalizar cada palabra
                String[] words = username.split(" ");
                StringBuilder result = new StringBuilder();
                for (String word : words) {
                    if (word.length() > 0) {
                        result.append(Character.toUpperCase(word.charAt(0)));
                        if (word.length() > 1) {
                            result.append(word.substring(1).toLowerCase());
                        }
                        result.append(" ");
                    }
                }
                return result.toString().trim();
            } else {
                return "Usuario desconocido";
            }
        }
    }
}

