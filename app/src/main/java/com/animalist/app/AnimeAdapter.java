package com.animalist.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AnimeAdapter extends RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder> {

    private List<Anime> animeList;

    public AnimeAdapter(List<Anime> animeList) {
        this.animeList = animeList;
    }

    @NonNull
    @Override
    public AnimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_anime, parent, false);
        return new AnimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimeViewHolder holder, int position) {
        Anime anime = animeList.get(position);

        holder.tvTitle.setText(anime.getTitle());
        holder.tvRating.setText(String.valueOf(anime.getRating()));

        if (anime.getThumbnailUrl() != null && !anime.getThumbnailUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(anime.getThumbnailUrl())
                    .centerCrop()
                    .into(holder.imgPoster);
        } else {
            holder.imgPoster.setImageResource(android.R.color.darker_gray);
        }

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), DetailActivity.class);

            intent.putExtra("TITLE", anime.getTitle());
            intent.putExtra("POSTER", anime.getThumbnailUrl());
            intent.putExtra("RATING", anime.getRating());
            intent.putExtra("SYNOPSIS", anime.getSynopsis());
            intent.putExtra("STATUS", anime.getStatus());
            intent.putExtra("EPISODES", anime.getEpisodes());
            intent.putExtra("YEAR", anime.getYear());

            // === FIX: BAWA DATA GENRE ASLI (ARRAY) BIAR NGGAK HILANG ===
            if (anime.getGenres() != null) {
                intent.putStringArrayListExtra("GENRES_LIST", new ArrayList<>(anime.getGenres()));
            }

            // Ini cuma buat tampilan teks di layarnya
            String gabunganGenre = "";
            if (anime.getGenres() != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    gabunganGenre = String.join("  •  ", anime.getGenres());
                } else {
                    StringBuilder sb = new StringBuilder();
                    for(int i=0; i<anime.getGenres().size(); i++){
                        sb.append(anime.getGenres().get(i));
                        if(i < anime.getGenres().size()-1) sb.append("  •  ");
                    }
                    gabunganGenre = sb.toString();
                }
            }
            intent.putExtra("GENRES", gabunganGenre);

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return animeList.size();
    }

    public static class AnimeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvRating;
        ImageView imgPoster;

        public AnimeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_anime_title);
            tvRating = itemView.findViewById(R.id.tv_rating_badge);
            imgPoster = itemView.findViewById(R.id.img_poster);
        }
    }
}