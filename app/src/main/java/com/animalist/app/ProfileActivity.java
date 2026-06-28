package com.animalist.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AnimaList_Prefs";
    private TextView tvProfileName, tvCurrentTheme, tvCurrentLanguage;
    private ShapeableImageView imgProfilePic;
    private SharedPreferences sharedPreferences;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Terapkan bahasa sebelum layar digambar
        String lang = sharedPreferences.getString("selected_language", "en");
        java.util.Locale locale = new java.util.Locale(lang);
        java.util.Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        tvProfileName = findViewById(R.id.tv_profile_name);
        tvCurrentLanguage = findViewById(R.id.tv_current_language);
        tvCurrentTheme = findViewById(R.id.tv_current_theme);
        imgProfilePic = findViewById(R.id.img_profile_pic);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        sharedPreferences.edit().putString("USER_PHOTO", imageUri.toString()).apply();
                        imgProfilePic.setImageTintList(null);
                        Glide.with(this).load(imageUri).into(imgProfilePic);
                    }
                }
        );

        loadSavedSettings();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.layout_setting_language).setOnClickListener(v -> showLanguageDialog());
        findViewById(R.id.layout_setting_theme).setOnClickListener(v -> showThemeDialog());
        findViewById(R.id.btn_edit_profile).setOnClickListener(v -> showEditOptionsDialog());
        findViewById(R.id.layout_setting_help).setOnClickListener(v -> openHelpEmail());
        findViewById(R.id.layout_setting_about).setOnClickListener(v -> showAboutDialog());

        // KODE BARU: Tombol Admin pakai gembok
        View adminLayout = findViewById(R.id.layout_setting_admin);
        if (adminLayout != null) {
            adminLayout.setOnClickListener(v -> showAdminPinDialog());
        }
    }

    private void loadSavedSettings() {
        tvProfileName.setText(sharedPreferences.getString("USER_NAME", "User"));

        String savedPhoto = sharedPreferences.getString("USER_PHOTO", null);
        if (savedPhoto != null) {
            imgProfilePic.setImageTintList(null);
            Glide.with(this).load(Uri.parse(savedPhoto)).into(imgProfilePic);
        }

        boolean isI = sharedPreferences.getString("selected_language", "en").equals("in");
        tvCurrentLanguage.setText(isI ? "Indonesia >" : "English >");

        int mode = sharedPreferences.getInt("selected_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (mode == AppCompatDelegate.MODE_NIGHT_NO) tvCurrentTheme.setText(isI ? "Terang >" : "Light >");
        else if (mode == AppCompatDelegate.MODE_NIGHT_YES) tvCurrentTheme.setText(isI ? "Gelap >" : "Dark >");
        else tvCurrentTheme.setText(isI ? "Sistem >" : "System >");
    }

    private void showEditOptionsDialog() {
        boolean isI = sharedPreferences.getString("selected_language", "en").equals("in");
        String[] options = isI ? new String[]{"Ubah Nama", "Ubah Foto Profil"} : new String[]{"Change Name", "Change Profile Photo"};

        new AlertDialog.Builder(this)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showEditNameDialog();
                    else openGallery();
                }).show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void showLanguageDialog() {
        boolean isI = sharedPreferences.getString("selected_language", "en").equals("in");
        String[] langs = isI ? new String[]{"Inggris", "Indonesia"} : new String[]{"English", "Indonesia"};
        int current = isI ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle(isI ? "Pilih Bahasa" : "Select Language")
                .setSingleChoiceItems(langs, current, (dialog, which) -> {
                    String code = (which == 1) ? "in" : "en";
                    sharedPreferences.edit().putString("selected_language", code).apply();
                    restartAppStack();
                    dialog.dismiss();
                }).show();
    }

    private void showThemeDialog() {
        boolean isI = sharedPreferences.getString("selected_language", "en").equals("in");
        String[] themes = isI ? new String[]{"Terang", "Gelap", "Sistem"} : new String[]{"Light", "Dark", "System"};
        int currentMode = sharedPreferences.getInt("selected_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        int checked = (currentMode == AppCompatDelegate.MODE_NIGHT_NO) ? 0 :
                (currentMode == AppCompatDelegate.MODE_NIGHT_YES) ? 1 : 2;

        new AlertDialog.Builder(this)
                .setTitle(isI ? "Pilih Tema" : "Select Theme")
                .setSingleChoiceItems(themes, checked, (dialog, which) -> {
                    int mode = (which == 0) ? AppCompatDelegate.MODE_NIGHT_NO :
                            (which == 1) ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    sharedPreferences.edit().putInt("selected_theme", mode).apply();
                    AppCompatDelegate.setDefaultNightMode(mode);
                    restartAppStack();
                    dialog.dismiss();
                }).show();
    }

    private void restartAppStack() {
        Intent intentMain = new Intent(this, MainActivity.class);
        intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentMain);
        Intent intentProfile = new Intent(this, ProfileActivity.class);
        startActivity(intentProfile);
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }

    private void showEditNameDialog() {
        boolean isI = sharedPreferences.getString("selected_language", "en").equals("in");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isI ? "Ubah Nama" : "Edit Name");

        final EditText input = new EditText(this);
        input.setText(tvProfileName.getText().toString());
        input.setSingleLine(true);

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(64, 32, 64, 16);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);
        builder.setPositiveButton(isI ? "Simpan" : "Save", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                sharedPreferences.edit().putString("USER_NAME", name).apply();
                tvProfileName.setText(name);
            }
        });
        builder.setNegativeButton(isI ? "Batal" : "Cancel", null);
        builder.show();
    }

    private void openHelpEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:rifqimaulana2033@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "AnimaList Support");
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Tidak ada aplikasi email ditemukan", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.about_msg))
                .setPositiveButton("OK", null)
                .show();
    }

    // === KODE BARU: FUNGSI GEMBOK ADMIN ===
    private void showAdminPinDialog() {
        boolean isI = sharedPreferences.getString("selected_language", "en").equals("in");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isI ? "Akses Admin" : "Admin Access");
        builder.setMessage(isI ? "Masukkan PIN Rahasia:" : "Enter Secret PIN:");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(64, 32, 64, 16);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);
        builder.setPositiveButton(isI ? "Masuk" : "Login", (dialog, which) -> {
            String pin = input.getText().toString();
            // === PASSWORDNYA: admin123 ===
            if (pin.equals("admin123")) {
                startActivity(new Intent(ProfileActivity.this, AdminActivity.class));
            } else {
                Toast.makeText(this, isI ? "PIN Salah!" : "Incorrect PIN!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(isI ? "Batal" : "Cancel", null);
        builder.show();
    }
}