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

import com.example.rdt_pastillas.Modelo.PastillasModel;
import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.receiver.AlarmReceiver;
import com.example.rdt_pastillas.repositorio.ListaPastilla;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PastillasFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Directamente programamos/actualizamos las alarmas basadas en la lista actual.
        // Ya no es necesario el paso de limpieza.
        scheduleOrUpdateAlarms();

        return view;
    }

    private void scheduleOrUpdateAlarms() {
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
