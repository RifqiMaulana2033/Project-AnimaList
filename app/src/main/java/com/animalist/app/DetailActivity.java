package com.animalist.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private String currentStatus = "none";
    private List<Anime> watchlist;
    private Anime currentAnime;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        ImageView imgPoster = findViewById(R.id.img_detail_poster);
        TextView tvTitle = findViewById(R.id.tv_detail_title);
        TextView tvRating = findViewById(R.id.tv_detail_rating);
        TextView tvStatus = findViewById(R.id.tv_detail_status);
        TextView tvInfoRow = findViewById(R.id.tv_detail_info_row);
        TextView tvGenres = findViewById(R.id.tv_detail_genres);
        TextView tvSynopsis = findViewById(R.id.tv_detail_synopsis);
        fab = findViewById(R.id.fab_bookmark);
        ImageView btnBack = findViewById(R.id.btn_back);

        // Tombol Edit
        ImageView btnEditAdmin = findViewById(R.id.btn_edit_admin);
        if (btnEditAdmin != null) {
            btnEditAdmin.setOnClickListener(v -> showEditPinDialog());
        }

        String title = getIntent().getStringExtra("TITLE");
        String posterUrl = getIntent().getStringExtra("POSTER");
        double rating = getIntent().getDoubleExtra("RATING", 0.0);
        String status = getIntent().getStringExtra("STATUS");
        String synopsis = getIntent().getStringExtra("SYNOPSIS");
        String episodes = getIntent().getStringExtra("EPISODES");
        String year = getIntent().getStringExtra("YEAR");
        String genresString = getIntent().getStringExtra("GENRES");

        ArrayList<String> genresList = getIntent().getStringArrayListExtra("GENRES_LIST");

        currentAnime = new Anime(title, posterUrl, rating, genresList, synopsis, episodes, year, status);

        tvTitle.setText(title);
        tvRating.setText(String.valueOf(rating));
        tvStatus.setText(status);
        tvSynopsis.setText(synopsis);
        tvGenres.setText(genresString);
        tvInfoRow.setText("TV, " + year + "   |   " + status + "   |   " + episodes);
        if (posterUrl != null && !posterUrl.isEmpty()) Glide.with(this).load(posterUrl).into(imgPoster);

        loadWatchlist();
        updateFabUI();

        fab.setOnClickListener(v -> tampilkanMenuModern());
        btnBack.setOnClickListener(v -> finish());
    }

    // === KODE BARU: FUNGSI GEMBOK EDIT ADMIN ===
    private void showEditPinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Admin Access");
        builder.setMessage("Masukkan PIN Admin untuk mengedit:");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(64, 32, 64, 16);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);
        builder.setPositiveButton("Lanjut", (dialog, which) -> {
            // === PASSWORDNYA: admin123 ===
            if (input.getText().toString().equals("admin123")) {

                Intent intent = new Intent(DetailActivity.this, AdminActivity.class);
                intent.putExtra("IS_EDIT_MODE", true);
                intent.putExtra("TITLE", currentAnime.getTitle());
                intent.putExtra("POSTER", currentAnime.getThumbnailUrl());
                intent.putExtra("RATING", currentAnime.getRating());
                intent.putExtra("EPISODES", currentAnime.getEpisodes());
                intent.putExtra("YEAR", currentAnime.getYear());
                intent.putExtra("STATUS", currentAnime.getStatus());
                intent.putExtra("SYNOPSIS", currentAnime.getSynopsis());

                if (currentAnime.getGenres() != null) {
                    intent.putStringArrayListExtra("GENRES_LIST", new ArrayList<>(currentAnime.getGenres()));
                }
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(this, "PIN Salah!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void tampilkanMenuModern() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_kategori, null);
        bottomSheetDialog.setContentView(view);

        Button btnWatching = view.findViewById(R.id.btn_kategori_watching);
        Button btnPlan = view.findViewById(R.id.btn_kategori_plan);
        Button btnDone = view.findViewById(R.id.btn_kategori_done);
        Button btnHapus = view.findViewById(R.id.btn_kategori_hapus);

        int warnaAktifBg = android.graphics.Color.parseColor("#5C9DFF");
        int warnaAktifTeks = android.graphics.Color.WHITE;

        if (currentStatus.equals("Watching")) {
            btnWatching.setBackgroundTintList(android.content.res.ColorStateList.valueOf(warnaAktifBg));
            btnWatching.setTextColor(warnaAktifTeks);
        } else if (currentStatus.equals("Plan")) {
            btnPlan.setBackgroundTintList(android.content.res.ColorStateList.valueOf(warnaAktifBg));
            btnPlan.setTextColor(warnaAktifTeks);
        } else if (currentStatus.equals("Completed")) {
            btnDone.setBackgroundTintList(android.content.res.ColorStateList.valueOf(warnaAktifBg));
            btnDone.setTextColor(warnaAktifTeks);
        }

        if (currentStatus.equals("none")) {
            btnHapus.setVisibility(View.GONE);
        } else {
            btnHapus.setVisibility(View.VISIBLE);
        }

        btnWatching.setOnClickListener(v -> prosesSimpan("Watching", bottomSheetDialog));
        btnPlan.setOnClickListener(v -> prosesSimpan("Plan", bottomSheetDialog));
        btnDone.setOnClickListener(v -> prosesSimpan("Completed", bottomSheetDialog));

        btnHapus.setOnClickListener(v -> {
            hapusDariWatchlist();
            currentStatus = "none";
            Toast.makeText(this, "Removed from list", Toast.LENGTH_SHORT).show();
            updateFabUI();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void prosesSimpan(String statusPilihan, BottomSheetDialog dialog) {
        currentStatus = statusPilihan;
        currentAnime.setUserStatus(statusPilihan);
        simpanKeWatchlist();
        Toast.makeText(this, "Added to: " + statusPilihan, Toast.LENGTH_SHORT).show();
        updateFabUI();
        dialog.dismiss();
    }

    private void loadWatchlist() {
        SharedPreferences prefs = getSharedPreferences("AnimaList_Prefs", Context.MODE_PRIVATE);
        String json = prefs.getString("WATCHLIST", null);
        watchlist = (json == null) ? new ArrayList<>() : new Gson().fromJson(json, new TypeToken<ArrayList<Anime>>(){}.getType());

        currentStatus = "none";
        for (Anime a : watchlist) {
            if (a.getTitle() != null && a.getTitle().equals(currentAnime.getTitle())) {
                currentStatus = (a.getUserStatus() != null) ? a.getUserStatus() : "Watching";
                break;
            }
        }
    }

    private void simpanKeWatchlist() {
        watchlist.removeIf(a -> a.getTitle().equals(currentAnime.getTitle()));
        watchlist.add(0, currentAnime);
        saveToDisk();
    }

    private void hapusDariWatchlist() {
        watchlist.removeIf(a -> a.getTitle().equals(currentAnime.getTitle()));
        saveToDisk();
    }

    private void saveToDisk() {
        SharedPreferences.Editor editor = getSharedPreferences("AnimaList_Prefs", Context.MODE_PRIVATE).edit();
        editor.putString("WATCHLIST", new Gson().toJson(watchlist));
        editor.apply();
    }

    private void updateFabUI() {
        if (currentStatus.equals("none")) {
            fab.setImageResource(R.drawable.list);
            fab.setSupportBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#888888")));
        } else {
            fab.setImageResource(R.drawable.list);
            fab.setSupportBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#016387")));
        }
    }
}