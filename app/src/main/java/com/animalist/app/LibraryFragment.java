package com.animalist.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private RecyclerView rvLibrary;
    private LinearLayout layoutEmpty;
    private AnimeAdapter adapter;

    // Variabel diganti Bahasa Inggris
    private List<Anime> allSavedAnime;
    private List<Anime> displayedAnime;

    // Kenali tombol-tombol di level class biar gampang diakses
    private Button btnAll, btnWatching, btnPlan, btnDone;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        rvLibrary = view.findViewById(R.id.rv_library);
        layoutEmpty = view.findViewById(R.id.layout_empty_library);

        btnAll = view.findViewById(R.id.btn_filter_all);
        btnWatching = view.findViewById(R.id.btn_filter_watching);
        btnPlan = view.findViewById(R.id.btn_filter_plan);
        btnDone = view.findViewById(R.id.btn_filter_done);

        rvLibrary.setLayoutManager(new GridLayoutManager(getContext(), 2));

        allSavedAnime = new ArrayList<>();
        displayedAnime = new ArrayList<>();

        adapter = new AnimeAdapter(displayedAnime);
        rvLibrary.setAdapter(adapter);

        loadDataFromMemory();

        // Oper tombol mana yang lagi diklik ke dalam fungsi filter
        btnAll.setOnClickListener(v -> filterData("All", btnAll));
        btnWatching.setOnClickListener(v -> filterData("Watching", btnWatching));
        btnPlan.setOnClickListener(v -> filterData("Plan", btnPlan));
        btnDone.setOnClickListener(v -> filterData("Completed", btnDone));

        return view;
    }

    private void loadDataFromMemory() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AnimaList_Prefs", Context.MODE_PRIVATE);
        String jsonAnime = sharedPreferences.getString("WATCHLIST", null);

        allSavedAnime.clear();
        if (jsonAnime != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Anime>>() {}.getType();
            List<Anime> localData = gson.fromJson(jsonAnime, type);
            if (localData != null) {
                allSavedAnime.addAll(localData);
            }
        }

        // Panggil default: Tampilkan "All" dan nyalakan highlight di tombol btnAll
        filterData("All", btnAll);
    }

    private void filterData(String selectedCategory, Button activeBtn) {
        // === LOGIKA HIGHLIGHT TOMBOL ===
        Button[] allButtons = {btnAll, btnWatching, btnPlan, btnDone};
        for (Button btn : allButtons) {
            if (btn == activeBtn) {
                // Tombol Aktif: Warna Biru Aksen, Teks Putih
                btn.setBackgroundColor(Color.parseColor("#5C9DFF"));
                btn.setTextColor(Color.WHITE);
            } else {
                // Tombol Pasif: Transparan, Teks Abu-abu
                btn.setBackgroundColor(Color.TRANSPARENT);
                btn.setTextColor(Color.parseColor("#888888"));
            }
        }

        // === LOGIKA SARING DATA ===
        displayedAnime.clear();
        for (Anime anime : allSavedAnime) {
            if (selectedCategory.equals("All") ||
                    (anime.getUserStatus() != null && anime.getUserStatus().equals(selectedCategory))) {
                displayedAnime.add(anime);
            }
        }

        adapter.notifyDataSetChanged();

        if (displayedAnime.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvLibrary.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvLibrary.setVisibility(View.VISIBLE);
        }
    }
}