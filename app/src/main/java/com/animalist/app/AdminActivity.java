package com.animalist.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private EditText etTitle, etThumb, etRating, etGenres, etEpisodes, etYear, etStatus, etSynopsis;
    private FirebaseFirestore db;

    // Variabel buat nandain ini lagi ngedit atau nambah baru
    private boolean isEditMode = false;
    private String originalTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();

        TextView tvAdminTitle = findViewById(R.id.tv_admin_title);
        etTitle = findViewById(R.id.et_admin_title);
        etThumb = findViewById(R.id.et_admin_thumb);
        etRating = findViewById(R.id.et_admin_rating);
        etGenres = findViewById(R.id.et_admin_genres);
        etEpisodes = findViewById(R.id.et_admin_episodes);
        etYear = findViewById(R.id.et_admin_year);
        etStatus = findViewById(R.id.et_admin_status);
        etSynopsis = findViewById(R.id.et_admin_synopsis);

        Button btnSave = findViewById(R.id.btn_admin_save);
        Button btnDelete = findViewById(R.id.btn_admin_delete);

        findViewById(R.id.btn_back_admin).setOnClickListener(v -> finish());

        // === KUNCI EDIT MODE: Cek sinyal dari DetailActivity ===
        isEditMode = getIntent().getBooleanExtra("IS_EDIT_MODE", false);

        if (isEditMode) {
            originalTitle = getIntent().getStringExtra("TITLE");

            // Ubah Tampilan UI jadi mode Edit
            if (tvAdminTitle != null) tvAdminTitle.setText(R.string.admin_edit_title);
            if (btnSave != null) btnSave.setText(R.string.btn_update_anime);
            if (btnDelete != null) btnDelete.setVisibility(View.VISIBLE);

            // Isi Form secara otomatis dengan data lama
            etTitle.setText(originalTitle);
            etThumb.setText(getIntent().getStringExtra("POSTER"));
            etRating.setText(String.valueOf(getIntent().getDoubleExtra("RATING", 0.0)));
            etEpisodes.setText(getIntent().getStringExtra("EPISODES"));
            etYear.setText(getIntent().getStringExtra("YEAR"));
            etStatus.setText(getIntent().getStringExtra("STATUS"));
            etSynopsis.setText(getIntent().getStringExtra("SYNOPSIS"));

            // Gabungin array genre jadi teks berkoma
            ArrayList<String> genresList = getIntent().getStringArrayListExtra("GENRES_LIST");
            if (genresList != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < genresList.size(); i++) {
                    sb.append(genresList.get(i));
                    if (i < genresList.size() - 1) sb.append(", ");
                }
                etGenres.setText(sb.toString());
            }

            // Pasang Fungsi Delete
            if (btnDelete != null) btnDelete.setOnClickListener(v -> konfirmasiHapus());
        }

        // Kalau tombol simpan/update ditekan
        if (btnSave != null) btnSave.setOnClickListener(v -> simpanDataKeFirebase());
    }

    private void konfirmasiHapus() {
        new AlertDialog.Builder(this)
                .setTitle("Peringatan")
                .setMessage("Yakin mau menghapus anime ini secara permanen dari Database?")
                .setPositiveButton("Hapus", (dialog, which) -> hapusDariFirebase())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void hapusDariFirebase() {
        // Cari Anime di Firebase berdasarkan Judul aslinya, lalu hapus dokumennya
        db.collection("Anime").whereEqualTo("title", originalTitle).get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String docId = query.getDocuments().get(0).getId();
                        db.collection("Anime").document(docId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Anime Terhapus!", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                    }
                });
    }

    private void simpanDataKeFirebase() {
        String title = etTitle.getText().toString().trim();
        String thumb = etThumb.getText().toString().trim();
        String ratingStr = etRating.getText().toString().trim();
        String genresStr = etGenres.getText().toString().trim();
        String episodes = etEpisodes.getText().toString().trim();
        String year = etYear.getText().toString().trim();
        String status = etStatus.getText().toString().trim();
        String synopsis = etSynopsis.getText().toString().trim();

        if (title.isEmpty() || ratingStr.isEmpty() || genresStr.isEmpty()) {
            Toast.makeText(this, "Title, Rating, dan Genres wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        double rating = 0.0;
        try {
            rating = Double.parseDouble(ratingStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Format rating salah! Gunakan titik (contoh: 8.5)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pecah teks "Action, Fantasy" jadi List/Array
        List<String> genresList = new ArrayList<>();
        String[] splitGenres = genresStr.split(",");
        for (String g : splitGenres) {
            genresList.add(g.trim());
        }

        // Siapin paket data buat dikirim ke Firestore
        Map<String, Object> animeData = new HashMap<>();
        animeData.put("title", title);
        animeData.put("thumbnail_url", thumb);
        animeData.put("rating", rating);
        animeData.put("genres", genresList);
        animeData.put("episodes", episodes);
        animeData.put("year", year);
        animeData.put("status", status);
        animeData.put("synopsis", synopsis);

        if (isEditMode) {
            // MODE UPDATE: Cari dokumen lama, lalu timpa isinya
            db.collection("Anime").whereEqualTo("title", originalTitle).get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            String docId = query.getDocuments().get(0).getId();
                            db.collection("Anime").document(docId).set(animeData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Data Berhasil Diperbarui!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                        }
                    });
        } else {
            // MODE BIKIN BARU: Langsung tambah data baru ke Firestore
            db.collection("Anime").add(animeData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Sukses ditambah ke Database!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}