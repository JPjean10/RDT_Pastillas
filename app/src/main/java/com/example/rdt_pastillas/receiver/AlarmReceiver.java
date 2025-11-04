package com.example.rdt_pastillas.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.rdt_pastillas.Modelo.PastillasModel;
import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.activity.alarm_activity.AlarmActivity;
// ¡¡IMPORTANTE!! Añadir el import del HomeFragment
import com.example.rdt_pastillas.activity.menu_lateral.ui.HomeFragment;
import com.example.rdt_pastillas.service.AlarmService;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "alarm_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Obtenemos TODOS los datos de la pastilla
        int pillId = intent.getIntExtra("PILL_ID", -1);
        String pillName = intent.getStringExtra("PILL_NAME");
        String pillHour = intent.getStringExtra("PILL_HOUR"); // Esencial para reprogramar

        // --- PASO 1: REPROGRAMAR LA ALARMA PARA EL PRÓXIMO DÍA ---
        if (pillId != -1 && pillName != null && pillHour != null) {
            PastillasModel pastilla = new PastillasModel(pillId, pillName, pillHour);
            // Llamamos al método estático en HomeFragment para programar la alarma para el día siguiente.
            HomeFragment.scheduleAlarmForPill(context, pastilla, false); // false = no es snooze
            Log.d("AlarmReceiver", "Alarma recibida y REPROGRAMADA para el próximo día.");
        } else {
            Log.e("AlarmReceiver", "No se pudo reprogramar la alarma, faltan datos en el Intent.");
        }

        // --- PASO 2: INICIAR SONIDO Y NOTIFICACIÓN ---
        Intent serviceIntent = new Intent(context, AlarmService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, AlarmActivity.class);
        // Pasamos todos los datos a la Activity para que la suspensión también funcione
        notificationIntent.putExtras(intent.getExtras());
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                pillId,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // AQUÍ ES DONDE SE USA EL NOMBRE DE LA PASTILLA
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_pastilla)
                .setContentTitle("Hora de tu pastilla: " + pillName) // <-- El nombre se muestra aquí
                .setContentText("¡No olvides tomar tu medicamento!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pendingIntent, true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // La creación del canal es mejor en MainActivity, pero si lo dejas aquí, también funciona.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Alarm Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Canal para las alarmas de medicamentos");
            channel.setSound(null, null); // El sonido lo controla el servicio
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(pillId, builder.build());
    }
}
