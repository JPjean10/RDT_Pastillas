package com.example.rdt_pastillas.activity.alarm_activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rdt_pastillas.Modelo.PastillasModel;
import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.activity.menu_lateral.ui.pastillas_fragment.PastillasFragment;
import com.example.rdt_pastillas.activity.alarm_activity.service.AlarmService;
import com.example.rdt_pastillas.repositorio.ListaPastilla;

import java.util.ArrayList;
import java.util.List;

public class AlarmActivity extends AppCompatActivity {


    private int pillId;
    private String pillName;
    private String pillHour;
    private int notificationId;
    private String TAG = "AlarmActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);


        pillId = getIntent().getIntExtra("PILL_ID", -1);
        notificationId = getIntent().getIntExtra("NOTIFICATION_ID", 0);
        String nombresAgrupados = getIntent().getStringExtra("GROUPED_NAMES");
        pillName = getIntent().getStringExtra("PILL_NAME");
        pillHour = getIntent().getStringExtra("PILL_HOUR");

        // Opcional: Mostrar en pantalla qué pastillas toca tomar
        TextView txtNombres = findViewById(R.id.txt_nombre); // Asegúrate de tener este ID en tu XML o quita estas líneas
        if (txtNombres != null && nombresAgrupados != null) {
            String listaVertical = nombresAgrupados.replace(", ", "\n");

            txtNombres.setText(listaVertical);
        } else {
            if(txtNombres != null) txtNombres.setText(pillName);
        }

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
        // 1. Detener el sonido
        detener_alarma();

        if (pillHour == null || pillHour.isEmpty()) {
            Toast.makeText(this, "Error: No se pudo identificar la hora de la alarma", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Error: No se pudo identificar la hora de la alarma");
            finish();
            return;
        }

        // 3. Buscar todas las pastillas de esa hora
        List<PastillasModel> todasLasPastillas = ListaPastilla.getPastillas();
        List<PastillasModel> grupoSuspender = new ArrayList<>();

        for (PastillasModel p : todasLasPastillas) {
            if (p.getHora().equals(pillHour)) {
                grupoSuspender.add(p);
            }
        }

        // 4. Reprogramar cada una para dentro de 1 minuto (Snooze)
        for (PastillasModel p : grupoSuspender) {
            PastillasFragment.programarAlarma(this, p, true);
        }
        Toast.makeText(this, "Alarma pospuesta 30 minutos", Toast.LENGTH_SHORT).show();

    }
}
