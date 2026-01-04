package com.example.rdt_pastillas.receiver;

import android.app.Notification;
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
import com.example.rdt_pastillas.activity.menu_lateral.ui.pastillas_fragment.PastillasFragment;
import com.example.rdt_pastillas.activity.alarm_activity.service.AlarmService;
import com.example.rdt_pastillas.repositorio.ListaPastilla;

import java.util.ArrayList;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "alarm_channel_group"; // Cambiamos el ID para evitar conflictos
    public static final String NOTIFICATION = "NOTIFICATION";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Obtenemos los datos de la pastilla que disparó la alarma
        int pillId = intent.getIntExtra("PILL_ID", -1);
        String pillName = intent.getStringExtra("PILL_NAME");
        String pillHour = intent.getStringExtra("PILL_HOUR");

        // Si no hay datos, no podemos continuar.
        if (pillId == -1 || pillName == null || pillHour == null) {
            Log.e("AlarmReceiver", "Faltan datos en el Intent. No se puede procesar la alarma.");
            return;
        }

        // --- PASO 1: REPROGRAMAR LA ALARMA PARA EL PRÓXIMO DÍA ---
        PastillasModel pastillaActual = new PastillasModel(pillId, pillName, pillHour);
        PastillasFragment.programarAlarma(context, pastillaActual, false); // false = no es snooze
        Log.d("AlarmReceiver", "Alarma para '" + pillName + "' reprogramada para el próximo día.");

        // --- PASO 2: CREAR UN IDENTIFICADOR ÚNICO PARA EL GRUPO DE HORA ---
        // Convertimos la hora (ej. "07:40 PM") en un número entero.
        // Esto agrupará todas las notificaciones de la misma hora bajo un solo ID.
        int notificationId = generarIdDesdeHora(pillHour);

        // --- PASO 3: BUSCAR TODAS LAS PASTILLAS PARA ESA HORA Y AGRUPAR NOMBRES ---
        List<PastillasModel> todasLasPastillas = ListaPastilla.getPastillas();
        List<String> nombresPastillasGrupo = new ArrayList<>();
        for (PastillasModel p : todasLasPastillas) {
            if (p.getHora().equals(pillHour)) {
                nombresPastillasGrupo.add(p.getNombre());
            }
        }
        // Creamos un solo String con los nombres, separados por comas.
        String nombresAgrupados = String.join(", ", nombresPastillasGrupo);


        // --- PASO 4: CREAR Y CONFIGURAR LA NOTIFICACIÓN ÚNICA ---
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // El Intent que se abre al tocar la notificación. Siempre llevará al mismo sitio.
        Intent notificationIntent = new Intent(context, AlarmActivity.class);
        notificationIntent.putExtra("PILL_HOUR", pillHour);
        notificationIntent.putExtra("PILL_ID", pillId);
        notificationIntent.putExtra("PILL_NAME", pillName);
        notificationIntent.putExtra("NOTIFICATION_ID", notificationId);
        notificationIntent.putExtra("GROUPED_NAMES", nombresAgrupados);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                notificationIntent,
                // CAMBIA 'FLAG_UPDATE_CURRENT' POR 'FLAG_CANCEL_CURRENT'
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Configuramos el texto de la notificación para mostrar los nombres agrupados
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_pastilla)
                .setContentTitle("RDT_pastillas")
                .setContentText("Tomar pastillas") // Muestra "Tomar: gabapentina, metformina,
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pendingIntent, true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Alarmas de Medicamentos (Agrupadas)", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Canal para mostrar una sola notificación para alarmas a la misma hora.");
            channel.setSound(null, null); // El sonido lo gestiona el servicio
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = builder.build();

        // --- PASO 5: INICIAR EL SERVICIO DE SONIDO ---
        // Le pasamos la notificación y el ID de grupo.
        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra(NOTIFICATION, notification);
        serviceIntent.putExtra(NOTIFICATION_ID, notificationId);

        // El servicio se iniciará (si no está activo) o se actualizará, pero no se duplicará.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    /**
     * Genera un ID numérico consistente a partir de un String de hora (ej. "07:40 PM").
     * Esto asegura que "07:40 PM" siempre genere el mismo ID.
     */
    private int generarIdDesdeHora(String hora) {
        String horaNumerica = hora.replaceAll("[^0-9]", "");
        try {
            return Integer.parseInt(horaNumerica);
        } catch (NumberFormatException e) {
            return 12345;
        }
    }
}
