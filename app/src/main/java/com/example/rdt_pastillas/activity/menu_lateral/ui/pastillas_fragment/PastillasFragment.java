package com.example.rdt_pastillas.activity.menu_lateral.ui.pastillas_fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rdt_pastillas.Modelo.PastillasModel;
import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.activity.menu_lateral.ui.pastillas_fragment.adapter.PastillasAdapter;
import com.example.rdt_pastillas.receiver.AlarmReceiver;
import com.example.rdt_pastillas.repositorio.ListaPastilla;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PastillasFragment extends Fragment {

    private RecyclerView recyclerView;
    private PastillasAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pastillas, container, false);

        // --- Configuración del RecyclerView ---
        recyclerView = view.findViewById(R.id.recycler_view_pastillas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 1. Obtiene la lista completa de pastillas.
        List<PastillasModel> todasLasPastillas = ListaPastilla.getPastillas();

        // 2. Filtra la lista para obtener solo las próximas pastillas del día.
        List<PastillasModel> pastillasProximas = filtrarProximoGrupoDePastillas(todasLasPastillas);

        // 3. Crea y asigna el adaptador con la lista ya filtrada.
        adapter = new PastillasAdapter(pastillasProximas);
        recyclerView.setAdapter(adapter);

        ProgramarOActualizarAlarmas();

        return view;
    }


    private List<PastillasModel> filtrarProximoGrupoDePastillas(List<PastillasModel> todasLasPastillas) {
        Calendar ahora = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
        String proximaHora = null;

        // --- PASO 1: Encontrar la próxima hora de alarma más cercana ---
        // Primero ordenamos la lista por hora para facilitar la búsqueda.
        Collections.sort(todasLasPastillas, new Comparator<PastillasModel>() {
            @Override
            public int compare(PastillasModel p1, PastillasModel p2) {
                try {
                    Date hora1 = sdf.parse(p1.getHora());
                    Date hora2 = sdf.parse(p2.getHora());
                    return hora1.compareTo(hora2);
                } catch (ParseException e) {
                    return 0;
                }
            }
        });

        for (PastillasModel pastilla : todasLasPastillas) {
            try {
                Date horaPastillaDate = sdf.parse(pastilla.getHora());
                Calendar calendarioPastilla = Calendar.getInstance();
                calendarioPastilla.setTime(horaPastillaDate);
                calendarioPastilla.set(Calendar.YEAR, ahora.get(Calendar.YEAR));
                calendarioPastilla.set(Calendar.MONTH, ahora.get(Calendar.MONTH));
                calendarioPastilla.set(Calendar.DAY_OF_MONTH, ahora.get(Calendar.DAY_OF_MONTH));

                // Si la hora de la pastilla es después de la hora actual,
                // hemos encontrado la próxima hora de alarma.
                if (calendarioPastilla.after(ahora)) {
                    proximaHora = pastilla.getHora();
                    break; // Salimos del bucle en cuanto encontramos la primera hora futura.
                }

            } catch (ParseException e) {
                Log.e("FiltradoPastillas", "Formato de hora inválido: " + pastilla.getHora(), e);
            }
        }

        // --- PASO 2: Recolectar todas las pastillas que coincidan con esa próxima hora ---
        List<PastillasModel> pastillasFiltradas = new ArrayList<>();
        if (proximaHora != null) {
            for (PastillasModel pastilla : todasLasPastillas) {
                if (pastilla.getHora().equals(proximaHora)) {
                    pastillasFiltradas.add(pastilla);
                }
            }
        } else {
            // Si no se encontró ninguna hora futura, significa que todas las alarmas de hoy ya pasaron.
            Toast.makeText(getContext(), "No hay más pastillas programadas para hoy.", Toast.LENGTH_LONG).show();
        }

        return pastillasFiltradas;
    }
    private void ProgramarOActualizarAlarmas() {
        List<PastillasModel> pastillas = ListaPastilla.getPastillas();
        if (pastillas.isEmpty()) {
            Toast.makeText(getContext(), "No hay pastillas para programar.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (PastillasModel pastilla : pastillas) {
            programarAlarma(requireContext(), pastilla, false);
        }
        Toast.makeText(getContext(), "Alarmas programadas/actualizadas.", Toast.LENGTH_SHORT).show();
    }

    public static void programarAlarma(Context context, PastillasModel pastilla, boolean isSnooze) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("PILL_ID", pastilla.getId());
        intent.putExtra("PILL_NAME", pastilla.getNombre());
        intent.putExtra("PILL_HOUR", pastilla.getHora()); // Crucial para que el Receiver pueda reprogramar

        Log.d("AlarmScheduling", "Alarma SUSPENDIDA para '" + pastilla.getNombre() + "' a las: " + pastilla.getHora() + " (Snooze)");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                pastilla.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();

        if (isSnooze) {
            // Lógica para suspender: programar para X minutos en el futuro.
            calendar.add(Calendar.MINUTE, 1); // Puedes cambiar a 30 min.
            Log.d("AlarmScheduling", "Alarma SUSPENDIDA para '" + pastilla.getNombre() + "' en 1 minuto.");
        } else {
            // Lógica para programar una alarma normal.
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            Date alarmDate;
            try {
                alarmDate = sdf.parse(pastilla.getHora());
            } catch (ParseException e) {
                Log.e("AlarmScheduling", "Formato de hora inválido: " + pastilla.getHora());
                return; // No se puede programar si la hora es incorrecta.
            }

            Calendar now = Calendar.getInstance();
            calendar.setTime(alarmDate);
            calendar.set(Calendar.YEAR, now.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, now.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
            calendar.set(Calendar.SECOND, 0);

            // Si la hora de hoy ya pasó, se programa para mañana.
            if (calendar.before(now)) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        // Comprobación de permiso para Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w("AlarmScheduling", "La app no tiene permiso para programar alarmas exactas. La alarma podría no ser precisa.");
        }

        // LA LÍNEA MÁS IMPORTANTE: Usamos setExactAndAllowWhileIdle para la máxima precisión.
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

Log.d("AlarmScheduling", "Alarma programada para '" + pastilla.getNombre() + "' a las: " + calendar.getTime());
    }
}
