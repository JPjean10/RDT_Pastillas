package com.example.rdt_pastillas.activity.alarm_activity.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.activity.alarm_activity.AlarmActivity;
import com.example.rdt_pastillas.receiver.AlarmReceiver;

public class AlarmService extends Service {
    private MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "alarm_channel_service";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializar el MediaPlayer
        Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm_buzzer);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(true); // Para que el sonido se repita

        // Configurar para que suene con el volumen de la alarma
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
        );

        try {
            mediaPlayer.setDataSource(getApplicationContext(), soundUri);
            mediaPlayer.prepareAsync(); // Preparar de forma asíncrona
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Asegurarnos de que el canal existe
        createNotificationChannel();

        // 1. INTENTAR OBTENER LA NOTIFICACIÓN QUE VIENE DEL RECEIVER
        // Esta es la parte crucial. AlarmReceiver ya creó una notificación que tiene
        // el Intent con los extras (PILL_HOUR, PILL_NAME). Debemos usar esa misma.
        Notification notification = null;
        int notificationId = 1;

        if (intent != null) {
            notification = intent.getParcelableExtra(AlarmReceiver.NOTIFICATION);
            notificationId = intent.getIntExtra(AlarmReceiver.NOTIFICATION_ID, 1);
        }

        // 2. SI NO LLEGA (FALLBACK), CREAMOS UNA DE EMERGENCIA
        // Esto solo ocurre si algo falla gravemente o si inicias el servicio manualmente.
        if (notification == null) {
            Log.w("AlarmService", "La notificación llegó nula, creando una genérica de respaldo.");

            Intent notificationIntent = new Intent(this, AlarmActivity.class);
            // Nota: Esta notificación de respaldo NO tendrá los datos de la pastilla,
            // pero evitará que el servicio se caiga.
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
            );

            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Alarma de Pastilla Activa")
                    .setContentText("Tomando tu medicamento.")
                    .setSmallIcon(R.drawable.logo_pastilla)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();
        }

        // 3. INICIAR EL SERVICIO EN PRIMER PLANO CON LA NOTIFICACIÓN CORRECTA
        startForeground(notificationId, notification);

        // Iniciar la reproducción cuando esté listo
        mediaPlayer.setOnPreparedListener(mp -> {
            if (!mp.isPlaying()) {
                mp.start();
            }
        });

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarm Service Channel",
                    NotificationManager.IMPORTANCE_LOW // Menos intrusivo que el canal de la notificación principal
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}