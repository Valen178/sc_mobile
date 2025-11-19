package com.example.sportconnection.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sportconnection.R;
import com.example.sportconnection.model.Match;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchViewHolder> {

    private Context context;
    private List<Match> matches;
    private OnMatchClickListener listener;

    public interface OnMatchClickListener {
        void onMatchClick(Match match);
    }

    public MatchesAdapter(Context context, List<Match> matches, OnMatchClickListener listener) {
        this.context = context;
        this.matches = matches;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matches.get(position);
        Match.MatchProfile profile = match.getOtherUser().getProfile();

        // Nombre
        holder.userName.setText(profile.getName());

        // Trabajo
        if (profile.getJob() != null && !profile.getJob().isEmpty()) {
            holder.userJob.setText(profile.getJob());
            holder.userJob.setVisibility(View.VISIBLE);
        } else {
            holder.userJob.setVisibility(View.GONE);
        }

        // Fecha del match
        holder.matchDate.setText(getTimeAgo(match.getCreatedAt()));

        // Foto de perfil
        if (profile.getPhotoUrl() != null && !profile.getPhotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(profile.getPhotoUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(holder.userPhoto);
        }

        // Click en el nombre/info para ver perfil
        holder.userInfoContainer.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMatchClick(match);
            }
        });

        // Click en WhatsApp
        holder.whatsappButton.setOnClickListener(v -> {
            String phoneNumber = profile.getPhoneNumber();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                openWhatsApp(phoneNumber);
            } else {
                Toast.makeText(context, "Este usuario no tiene número de teléfono", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    private void openWhatsApp(String phoneNumber) {
        try {
            // Limpiar el número de teléfono
            String cleanNumber = phoneNumber.replaceAll("[^0-9+]", "");

            // Intentar abrir WhatsApp con el número
            String url = "https://wa.me/" + cleanNumber;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }

    private String getTimeAgo(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = sdf.parse(dateString);
            if (date == null) return "Recientemente";

            long timeAgo = System.currentTimeMillis() - date.getTime();
            long days = TimeUnit.MILLISECONDS.toDays(timeAgo);
            long hours = TimeUnit.MILLISECONDS.toHours(timeAgo);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(timeAgo);

            if (days > 0) {
                return "Match hace " + days + (days == 1 ? " día" : " días");
            } else if (hours > 0) {
                return "Match hace " + hours + (hours == 1 ? " hora" : " horas");
            } else if (minutes > 0) {
                return "Match hace " + minutes + (minutes == 1 ? " minuto" : " minutos");
            } else {
                return "Match reciente";
            }
        } catch (ParseException e) {
            return "Recientemente";
        }
    }

    public void updateMatches(List<Match> newMatches) {
        this.matches = newMatches;
        notifyDataSetChanged();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        ImageView userPhoto;
        TextView userName;
        TextView userJob;
        TextView matchDate;
        ImageButton whatsappButton;
        LinearLayout userInfoContainer;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            userPhoto = itemView.findViewById(R.id.matchUserPhoto);
            userName = itemView.findViewById(R.id.matchUserName);
            userJob = itemView.findViewById(R.id.matchUserJob);
            matchDate = itemView.findViewById(R.id.matchDate);
            whatsappButton = itemView.findViewById(R.id.btnWhatsApp);
            userInfoContainer = itemView.findViewById(R.id.userInfoContainer);
        }
    }
}

