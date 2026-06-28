package com.animalist.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    private TextView greetingText;
    private ImageView flagImage;
    private ProgressBar loadingSpinner;
    private Button nextButton;
    private ActivityResultLauncher<String[]> requestLocationLauncher;

    private final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 0. WAJIB DI SINI: Panggil Splash Screen minimalis bawaan Android 12+
        androidx.core.splashscreen.SplashScreen.installSplashScreen(this);

        SharedPreferences prefs = getSharedPreferences("AnimaList_Prefs", Context.MODE_PRIVATE);

        // 1. Terapkan Tema DULU
        int savedTheme = prefs.getInt("selected_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedTheme);

        // 2. Terapkan Bahasa DULU (SEBELUM LAYAR DIGAMBAR!)
        String lang = prefs.getString("selected_language", "en");
        java.util.Locale locale = new java.util.Locale(lang);
        java.util.Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        super.onCreate(savedInstanceState);

        // 3. BARU GAMBAR LAYARNYA (Biar string.xml kebaca sesuai bahasa)
        setContentView(R.layout.activity_splash);

        // ... (sisanya biarin sama persis kayak kodingan lu sebelumnya ke bawah) ...

        android.view.Window window = getWindow();
        int warnaBg = ContextCompat.getColor(this, R.color.warna_bg_utama);
        window.setBackgroundDrawable(new ColorDrawable(warnaBg));
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(warnaBg);

        androidx.core.view.WindowInsetsControllerCompat windowInsetsController = new androidx.core.view.WindowInsetsControllerCompat(window, window.getDecorView());
        int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        windowInsetsController.setAppearanceLightStatusBars(nightModeFlags != android.content.res.Configuration.UI_MODE_NIGHT_YES);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        greetingText = findViewById(R.id.greeting_text);
        flagImage = findViewById(R.id.flag_image);
        loadingSpinner = findViewById(R.id.loading_spinner);
        nextButton = findViewById(R.id.next_button);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        flagImage.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.GONE);

        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        requestLocationLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean fineGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    if (fineGranted || permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                        detectLocation();
                    } else {
                        handleLocationError("Akses lokasi ditolak.");
                    }
                });

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            detectLocation();
        } else {
            requestLocationLauncher.launch(LOCATION_PERMISSIONS);
        }
    }

    private void detectLocation() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            handleLocationError("Please activate location services");
            return;
        }

        greetingText.setText(getString(R.string.loading_location));
        loadingSpinner.setVisibility(View.VISIBLE);

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) reverseGeocode(location);
                    else handleLocationError("Sinyal GPS lemah.");
                })
                .addOnFailureListener(e -> handleLocationError("Gagal mendapat lokasi."));
    }

    private void handleLocationError(String msg) {
        loadingSpinner.setVisibility(View.GONE);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        updateGreeting("??");
    }

    private void reverseGeocode(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) updateGreeting(addresses.get(0).getCountryCode());
            else handleLocationError("Negara tidak diketahui");
        } catch (IOException e) {
            handleLocationError("Sistem gagal menerjemahkan lokasi");
        }
    }

    private void updateGreeting(String countryCode) {
        loadingSpinner.setVisibility(View.GONE);
        String finalGreeting;
        int flagResourceId;

        if (nextButton != null) nextButton.setVisibility(View.VISIBLE);
        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.gentle_enter, R.anim.gentle_exit);
            finish();
        });

        if (countryCode == null) countryCode = "??";

        switch (countryCode.toUpperCase()) {
            case "ID": finalGreeting = "Halo! \uD83C\uDDEE\uD83C\uDDE9"; flagResourceId = R.drawable.ic_flag_id; break;
            case "JP": finalGreeting = "Konnichiwa! \uD83C\uDDEF\uD83C\uDDF5"; flagResourceId = R.drawable.ic_flag_jp; break;
            case "US": finalGreeting = "Hello! \uD83C\uDDFA\uD83C\uDDF8"; flagResourceId = R.drawable.ic_flag_us; break;
            default: finalGreeting = "Welcome! 👋"; flagResourceId = R.drawable.flag_default; break;
        }

        greetingText.setText(finalGreeting);

        try {
            flagImage.setImageResource(flagResourceId);
        } catch (Exception e) {
            flagImage.setImageResource(R.drawable.flag_default);
        }
        flagImage.setVisibility(View.VISIBLE);
    }
}