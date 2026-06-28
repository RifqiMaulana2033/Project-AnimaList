package com.animalist.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import retrofit2.Response;

public class AnimeWorker extends Worker {

    public AnimeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Diem-diem nembak API Jikan nyari 1 anime Airing paling rame
            // PAKE .execute() BIAR JALAN SYNCHRONOUS DI BACKGROUND
            Response<JikanResponse> response = ApiClient.getService()
                    .searchAnime(null, "airing", null, "members", "desc", 1, 1)
                    .execute();

            if (response.isSuccessful() && response.body() != null && !response.body().data.isEmpty()) {
                String judulAnime = response.body().data.get(0).title;

                // Munculin Notifikasi!
                tampilkanNotifikasi("Anime Sedang Tayang! 🔥", judulAnime + " lagi rame ditonton nih. Cek episode terbarunya sekarang!");
                return Result.success();
            } else {
                return Result.retry();
            }

        } catch (Exception e) {
            Log.e("AnimeWorker", "Gagal ngecek API di background", e);
            return Result.failure();
        }
    }

    private void tampilkanNotifikasi(String judul, String pesan) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "animalist_update_channel";

        // Syarat wajib Android zaman now, harus bikin "Saluran Notif" dulu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Anime Updates", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        // Biar pas notifnya diklik, langsung buka aplikasi lu
        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round) // Pake icon aplikasi lu
                .setContentTitle(judul)
                .setContentText(pesan)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        manager.notify(1, builder.build());
    }
}