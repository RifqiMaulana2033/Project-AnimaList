package com.animalist.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("AnimaList_Prefs", Context.MODE_PRIVATE);

        // 1. Terapkan Tema
        int savedTheme = prefs.getInt("selected_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedTheme);

        // 2. Terapkan Bahasa Global
        String lang = prefs.getString("selected_language", "en");
        java.util.Locale locale = new java.util.Locale(lang);
        java.util.Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragmentYangDipilih = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragmentYangDipilih = new HomeFragment();
            } else if (itemId == R.id.nav_search) {
                fragmentYangDipilih = new SearchFragment();
            } else if (itemId == R.id.nav_library) {
                fragmentYangDipilih = new LibraryFragment();
            }

            if (fragmentYangDipilih != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragmentYangDipilih)
                        .commit();
            }
            return true;
        });

        // --- JALANKAN SISTEM NOTIFIKASI BACKGROUND (WORKMANAGER) ---
        // Pekerja ini bakal jalan di background tiap 12 jam sekali buat ngecek Jikan API
        PeriodicWorkRequest animeWorkRequest =
                new PeriodicWorkRequest.Builder(AnimeWorker.class, 12, TimeUnit.HOURS)
                        .build();

        // Daftarin pekerja ke sistem Android biar jalan terus walau aplikasi ditutup
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "AnimeUpdateWork",
                ExistingPeriodicWorkPolicy.KEEP, // Biar ga numpuk/dobel kalau user buka app berkali-kali
                animeWorkRequest
        );
    }
}