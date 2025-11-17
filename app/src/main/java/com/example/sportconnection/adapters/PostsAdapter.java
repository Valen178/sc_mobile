package com.example.sportconnection.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sportconnection.R;
import com.example.sportconnection.model.Post;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {
    private List<Post> posts;
    private OnPostActionListener listener;
    private int currentUserId;
    private boolean showDeleteButton;

    public interface OnPostActionListener {
        void onDeletePost(Post post);
    }

    public PostsAdapter(int currentUserId, boolean showDeleteButton, OnPostActionListener listener) {
        this.posts = new ArrayList<>();
        this.currentUserId = currentUserId;
        this.showDeleteButton = showDeleteButton;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts != null ? posts : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addPost(Post post) {
        this.posts.add(0, post);
        notifyItemInserted(0);
    }

    public void removePost(int postId) {
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId() == postId) {
                posts.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private ShapeableImageView userAvatar;
        private TextView userName;
        private TextView postDate;
        private TextView postText;
        private TextView postUrl;
        private ImageButton btnDelete;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userName = itemView.findViewById(R.id.userName);
            postDate = itemView.findViewById(R.id.postDate);
            postText = itemView.findViewById(R.id.postText);
            postUrl = itemView.findViewById(R.id.postUrl);
            btnDelete = itemView.findViewById(R.id.btnDeletePost);
        }

        public void bind(Post post) {
            // Configurar foto de perfil
            if (post.getUser() != null && post.getUser().getPhotoUrl() != null && !post.getUser().getPhotoUrl().isEmpty()) {
                // Cargar foto de perfil con Glide
                Glide.with(itemView.getContext())
                        .load(post.getUser().getPhotoUrl())
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(userAvatar);
            } else {
                // Mostrar icono por defecto
                userAvatar.setImageResource(R.drawable.ic_profile);
            }

            // Configurar nombre de usuario
            if (post.getUser() != null) {
                userName.setText(post.getUser().getFullName());
            } else {
                userName.setText("Usuario desconocido");
            }

            // Configurar texto del post
            postText.setText(post.getText());

            // Configurar URL si existe
            if (post.getUrl() != null && !post.getUrl().isEmpty()) {
                postUrl.setText(post.getUrl());
                postUrl.setVisibility(View.VISIBLE);
            } else {
                postUrl.setVisibility(View.GONE);
            }

            // Configurar fecha
            postDate.setText(formatDate(post.getCreatedAt()));

            // Configurar botón de eliminar
            if (showDeleteButton && post.getUserId() == currentUserId) {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeletePost(post);
                    }
                });
            } else {
                btnDelete.setVisibility(View.GONE);
            }
        }

        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "";
            }

            try {
                // Normalizar la fecha: remover microsegundos si existen y convertir +00:00 a Z
                String normalizedDate = dateString;

                // Si tiene microsegundos (más de 3 dígitos después del punto), recortarlos
                if (normalizedDate.contains(".")) {
                    int dotIndex = normalizedDate.indexOf(".");
                    int endIndex = dotIndex + 1;

                    // Encontrar el final de los dígitos
                    while (endIndex < normalizedDate.length() &&
                           Character.isDigit(normalizedDate.charAt(endIndex))) {
                        endIndex++;
                    }

                    // Si hay más de 3 dígitos de fracción, recortar a 3
                    int fractionDigits = endIndex - dotIndex - 1;
                    if (fractionDigits > 3) {
                        String beforeDot = normalizedDate.substring(0, dotIndex);
                        String milliseconds = normalizedDate.substring(dotIndex + 1, dotIndex + 4);
                        String afterMillis = normalizedDate.substring(endIndex);
                        normalizedDate = beforeDot + "." + milliseconds + afterMillis;
                    }
                }

                // Intentar múltiples formatos de fecha
                SimpleDateFormat sdf;
                Date date = null;

                // Formato con milisegundos y zona horaria +/-HH:MM
                try {
                    sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
                    date = sdf.parse(normalizedDate);
                } catch (ParseException e) {
                    // Formato sin milisegundos con zona horaria +/-HH:MM
                    try {
                        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
                        date = sdf.parse(normalizedDate);
                    } catch (ParseException e2) {
                        // Formato con milisegundos y Z
                        try {
                            sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                            date = sdf.parse(normalizedDate);
                        } catch (ParseException e3) {
                            // Formato sin milisegundos y Z
                            try {
                                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                date = sdf.parse(normalizedDate);
                            } catch (ParseException e4) {
                                // Si todo falla, devolver la cadena original
                                return dateString;
                            }
                        }
                    }
                }

                if (date == null) {
                    return dateString;
                }

                long diff = System.currentTimeMillis() - date.getTime();
                long seconds = diff / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;

                if (seconds < 0) {
                    // Fecha en el futuro, mostrar fecha completa
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    return outputFormat.format(date);
                } else if (seconds < 60) {
                    return "Hace " + seconds + "s";
                } else if (minutes < 60) {
                    return "Hace " + minutes + "m";
                } else if (hours < 24) {
                    return "Hace " + hours + "h";
                } else if (days < 7) {
                    return "Hace " + days + "d";
                } else if (days < 30) {
                    long weeks = days / 7;
                    return "Hace " + weeks + " sem";
                } else {
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    return outputFormat.format(date);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return dateString;
            }
        }
    }
}

