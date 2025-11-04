package com.example.rdt_pastillas.activity.alarm_activity;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.receiver.AlarmReceiver;
import com.example.rdt_pastillas.activity.menu_lateral.ui.pastillas_fragment.service.AlarmService;

import java.util.Calendar;

public class AlarmActivity extends AppCompatActivity {

    private int pillId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        // Recuperar el ID de la pastilla del intent
        pillId = getIntent().getIntExtra("PILL_ID", -1);

        Button btn_desactivar = findViewById(R.id.btn_desactivar);
        Button btn_suspender = findViewById(R.id.btn_suspender);

        btn_desactivar.setOnClickListener(v -> {
            detener_alarma();
            // No canceles la alarma principal para que suene al día siguiente.
            // Si quisieras cancelarla permanentemente, usarías el método cancelRepeatingAlarm().
            finish();
        });

        btn_suspender.setOnClickListener(v -> {
            suspender();
            finish();
        });
    }

    private void detener_alarma() {
        // Detiene el servicio de sonido
        Intent serviceIntent = new Intent(this, AlarmService.class);
        stopService(serviceIntent);

        // Opcional: Cierra la notificación
        if (pillId != -1) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(pillId);
        }
    }

    private void suspender() {
        // Primero, detiene el sonido actual
        detener_alarma();

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        // Mantener los extras para la próxima notificación
        intent.putExtras(getIntent().getExtras());

        // El PendingIntent debe ser el mismo que el original para que la alarma se reprograme
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                pillId, // Usar el ID de la pastilla
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30); // Suspender por 30 minutos

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Toast.makeText(this, "Alarma suspendida por 30 minutos", Toast.LENGTH_SHORT).show();
    }

    // Este método es por si necesitas un botón para cancelar TODAS las futuras repeticiones.
    private void cancelRepeatingAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                pillId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        detener_alarma(); // También detiene el sonido actual
    }
}
