package com.animalist.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private AnimeAdapter adapterTopRated, adapterBaruRilis, adapterRekomendasi;
    private List<Anime> listTopRated, listBaruRilis, listRekomendasi;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ShapeableImageView icProfile;
    private TextView tvRekomendasiTitle;
    private RecyclerView rvRekomendasi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView tvGreetingTime = view.findViewById(R.id.tv_greeting_time);
        RecyclerView rvTopRated = view.findViewById(R.id.rv_top_rated);
        RecyclerView rvBaruRilis = view.findViewById(R.id.rv_baru_rilis);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_home);
        icProfile = view.findViewById(R.id.ic_profile);

        rvRekomendasi = view.findViewById(R.id.rv_rekomendasi);
        tvRekomendasiTitle = view.findViewById(R.id.tv_rekomendasi_title);

        aturSapaanPintar(tvGreetingTime);

        rvTopRated.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBaruRilis.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRekomendasi.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        listTopRated = new ArrayList<>();
        listBaruRilis = new ArrayList<>();
        listRekomendasi = new ArrayList<>();

        adapterTopRated = new AnimeAdapter(listTopRated);
        adapterBaruRilis = new AnimeAdapter(listBaruRilis);
        adapterRekomendasi = new AnimeAdapter(listRekomendasi);

        rvTopRated.setAdapter(adapterTopRated);
        rvBaruRilis.setAdapter(adapterBaruRilis);
        rvRekomendasi.setAdapter(adapterRekomendasi);

        muatSemuaData();

        swipeRefreshLayout.setOnRefreshListener(this::muatSemuaData);

        icProfile.setOnClickListener(v -> startActivity(new Intent(getActivity(), ProfileActivity.class)));

        return view;
    }

    private void muatSemuaData() {
        swipeRefreshLayout.setRefreshing(true);
        ambilDataTopRated();
        ambilDataBaruRilis();
        analisaDanTarikRekomendasi();
    }

    // 1. Tarik Top Rated (Diurutkan berdasarkan Score Tertinggi)
    private void ambilDataTopRated() {
        ApiClient.getService().searchAnime(null, null, null, "score", "desc", 10, 1)
                .enqueue(new Callback<JikanResponse>() {
                    @Override
                    public void onResponse(Call<JikanResponse> call, Response<JikanResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            listTopRated.clear();
                            for (JikanResponse.JikanAnime data : response.body().data) {
                                listTopRated.add(mapJikanToAnime(data));
                            }
                            adapterTopRated.notifyDataSetChanged();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(Call<JikanResponse> call, Throwable t) {
                        swipeRefreshLayout.setRefreshing(false);
                        Log.e("API_ERROR", "Gagal Top Rated: " + t.getMessage());
                    }
                });
    }

    // 2. Tarik Baru Rilis (Sedang Tayang & Paling Ramai)
    private void ambilDataBaruRilis() {
        ApiClient.getService().searchAnime(null, "airing", null, "members", "desc", 10, 1)
                .enqueue(new Callback<JikanResponse>() {
                    @Override
                    public void onResponse(Call<JikanResponse> call, Response<JikanResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            listBaruRilis.clear();
                            for (JikanResponse.JikanAnime data : response.body().data) {
                                listBaruRilis.add(mapJikanToAnime(data));
                            }
                            adapterBaruRilis.notifyDataSetChanged();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(Call<JikanResponse> call, Throwable t) {
                        swipeRefreshLayout.setRefreshing(false);
                        Log.e("API_ERROR", "Gagal Baru Rilis: " + t.getMessage());
                    }
                });
    }

    // 3. Tarik Rekomendasi berdasarkan Histori Watchlist
    private void analisaDanTarikRekomendasi() {
        SharedPreferences prefs = getContext().getSharedPreferences("AnimaList_Prefs", Context.MODE_PRIVATE);
        String json = prefs.getString("WATCHLIST", null);

        if (json == null || json.isEmpty()) {
            sembunyikanRekomendasi();
            return;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Anime>>() {}.getType();
        List<Anime> watchlistLokal = gson.fromJson(json, type);

        if (watchlistLokal == null || watchlistLokal.isEmpty()) {
            sembunyikanRekomendasi();
            return;
        }

        HashMap<String, Integer> hitungGenre = new HashMap<>();
        for (Anime anime : watchlistLokal) {
            if (anime.getGenres() != null) {
                for (String genre : anime.getGenres()) {
                    hitungGenre.put(genre, hitungGenre.getOrDefault(genre, 0) + 1);
                }
            }
        }

        String genreFavorit = "";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : hitungGenre.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                genreFavorit = entry.getKey();
            }
        }

        String apiGenreId = getGenreId(genreFavorit);

        if (!genreFavorit.isEmpty() && apiGenreId != null) {
            boolean isIndo = prefs.getString("selected_language", "en").equals("in");
            String rekTeks = isIndo ? "Karena kamu menyimpan " + genreFavorit : "Because you saved " + genreFavorit;

            tvRekomendasiTitle.setText(rekTeks);
            tvRekomendasiTitle.setVisibility(View.VISIBLE);
            rvRekomendasi.setVisibility(View.VISIBLE);

            // Tembak API berdasarkan Genre ID Favorit (Diurutin dari yang paling populer)
            ApiClient.getService().searchAnime(null, null, apiGenreId, "members", "desc", 10, 1)
                    .enqueue(new Callback<JikanResponse>() {
                        @Override
                        public void onResponse(Call<JikanResponse> call, Response<JikanResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                listRekomendasi.clear();
                                for (JikanResponse.JikanAnime data : response.body().data) {
                                    listRekomendasi.add(mapJikanToAnime(data));
                                }
                                adapterRekomendasi.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(Call<JikanResponse> call, Throwable t) {
                            Log.e("API_ERROR", "Gagal Rekomendasi: " + t.getMessage());
                        }
                    });
        } else {
            sembunyikanRekomendasi();
        }
    }

    private void sembunyikanRekomendasi() {
        tvRekomendasiTitle.setVisibility(View.GONE);
        rvRekomendasi.setVisibility(View.GONE);
    }

    // Alat penerjemah Teks JSON dari Jikan ke Objek Anime kita
    private Anime mapJikanToAnime(JikanResponse.JikanAnime data) {
        String title = data.title != null ? data.title : "Unknown";
        String thumb = (data.images != null && data.images.jpg != null) ? data.images.jpg.imageUrl : "";
        double rating = data.score != null ? data.score : 0.0;
        String synopsis = data.synopsis != null ? data.synopsis : "Sinopsis tidak tersedia.";
        String episodes = data.episodes != null ? data.episodes + " ep" : "? ep";
        String year = data.year != null ? String.valueOf(data.year) : "?";
        String status = data.status != null ? data.status : "Unknown";

        List<String> genres = new ArrayList<>();
        if (data.genres != null) {
            for (JikanResponse.JikanGenre g : data.genres) {
                genres.add(g.name);
            }
        }
        return new Anime(title, thumb, rating, genres, synopsis, episodes, year, status);
    }

    // Alat penerjemah Teks Genre ke ID MyAnimeList
    private String getGenreId(String genreName) {
        switch (genreName) {
            case "Action": return "1";
            case "Adventure": return "2";
            case "Comedy": return "4";
            case "Drama": return "8";
            case "Fantasy": return "10";
            case "Horror": return "14";
            case "Isekai": return "62";
            case "Mecha": return "18";
            case "Mystery": return "7";
            case "Psychological": return "40";
            case "Romance": return "22";
            case "Sci-Fi": return "24";
            case "Slice of Life": return "36";
            case "Sports": return "30";
            case "Supernatural": return "37";
            case "Thriller": return "41";
            default: return null;
        }
    }

    private void aturSapaanPintar(TextView textView) {
        int jamSekarang = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (jamSekarang >= 0 && jamSekarang < 12) textView.setText(R.string.greeting_morning);
        else if (jamSekarang >= 12 && jamSekarang < 15) textView.setText(R.string.greeting_afternoon);
        else if (jamSekarang >= 15 && jamSekarang < 18) textView.setText(R.string.greeting_evening);
        else textView.setText(R.string.greeting_night);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            TextView tvHiUser = getView().findViewById(R.id.tv_hi_user);
            SharedPreferences prefs = getContext().getSharedPreferences("AnimaList_Prefs", Context.MODE_PRIVATE);

            boolean isI = prefs.getString("selected_language", "en").equals("in");
            String username = prefs.getString("USER_NAME", "User");
            tvHiUser.setText(isI ? "Halo " + username + "," : "Hi " + username + ",");

            String savedPhoto = prefs.getString("USER_PHOTO", null);
            if (savedPhoto != null) {
                icProfile.setImageTintList(null);
                Glide.with(this).load(Uri.parse(savedPhoto)).into(icProfile);
            }

            // Panggil rekomendasi ulang tiap balik ke Home (siapa tahu baru nambahin anime ke watchlist)
            analisaDanTarikRekomendasi();
        }
    }
}