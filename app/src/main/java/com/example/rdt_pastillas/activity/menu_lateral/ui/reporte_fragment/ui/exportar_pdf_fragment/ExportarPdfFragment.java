package com.example.rdt_pastillas.activity.menu_lateral.ui.reporte_fragment.ui.exportar_pdf_fragment;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR;

import android.app.DatePickerDialog;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.R;

import com.example.rdt_pastillas.bd.local.database.AppDataBaseControl;
import com.example.rdt_pastillas.util.sesion.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ExportarPdfFragment extends Fragment {

    private TextInputEditText btn_FechaInicio, btn_FechaFin;
    private SessionManager sessionManager;
    private MaterialButton btnExportar;

    // Variables para el rango de fechas de la DB
    private long minDateMillis = 0;
    private long maxDateMillis = System.currentTimeMillis();
    private SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat sdfShort = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    // Agrega esta línea junto a las otras declaraciones de SimpleDateFormat
    private SimpleDateFormat sdfOutput = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exportar_pdf, container, false);

        sessionManager = new SessionManager(requireContext());

        btn_FechaInicio = view.findViewById(R.id.btn_FechaInicio);
        btn_FechaFin = view.findViewById(R.id.btn_FechaFin);
        btnExportar = view.findViewById(R.id.btnExportar);

        // 1. Cargar los límites desde la base de datos al iniciar
        obtenerLimitesDeBaseDeDatos();

        // 2. Configurar selectores de fecha
        btn_FechaInicio.setOnClickListener(v -> mostrarDatePicker(btn_FechaInicio));
        btn_FechaFin.setOnClickListener(v -> mostrarDatePicker(btn_FechaFin));

        // 3. Acción de exportar
        btnExportar.setOnClickListener(v -> {
            String inicio = btn_FechaInicio.getText().toString();
            String fin = btn_FechaFin.getText().toString();

            if (inicio.isEmpty() || fin.isEmpty()) {
                Toast.makeText(getContext(), "Por favor seleccione ambas fechas", Toast.LENGTH_SHORT).show();
            } else {
                generarPDF(inicio, fin);
            }
        });

        return view;
    }

    private void obtenerLimitesDeBaseDeDatos() {
        // Ejecutamos en un hilo para no bloquear la UI (O usa Coroutines si prefieres)
        new Thread(() -> {
            try {

                long idUser = sessionManager.getUserId();
                String fechaMaxStr = AppDataBaseControl.getDatabase(getContext()).glucosa_interfaz().getFechaMaxima(idUser);
                String fechaMinStr = AppDataBaseControl.getDatabase(getContext()).glucosa_interfaz().getFechaMinima(idUser);

                if (fechaMinStr != null) {
                    Date dMin = sdfFull.parse(fechaMinStr);
                    if (dMin != null) minDateMillis = dMin.getTime();
                }
                if (fechaMaxStr != null) {
                    Date dMax = sdfFull.parse(fechaMaxStr);
                    if (dMax != null) maxDateMillis = dMax.getTime();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void mostrarDatePicker(TextInputEditText et) {
        // 1. Configurar las restricciones de fecha
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();

        // Establecer el rango permitido (Desde minDateMillis hasta maxDateMillis)
        if (minDateMillis > 0) {
            constraintsBuilder.setStart(minDateMillis);
            constraintsBuilder.setEnd(maxDateMillis);
            // El Validator impide seleccionar días fuera del rango (se ponen en gris)
            constraintsBuilder.setValidator(DateValidatorPointForward.from(minDateMillis));
            // Nota: Para limitar también el final, puedes usar un validador compuesto si es necesario,
            // pero setEnd() suele manejar la vista del calendario.
        }

        // 2. Determinar qué fecha mostrar al abrir el calendario
        long selection = MaterialDatePicker.todayInUtcMilliseconds();
        try {
            String fechaActual = et.getText().toString();
            if (!fechaActual.isEmpty()) {
                Date date = sdfShort.parse(fechaActual);
                if (date != null) selection = date.getTime();
            } else if (minDateMillis > 0) {
                selection = minDateMillis; // Empezar desde el primer registro si el campo está vacío
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 3. Crear el constructor del MaterialDatePicker
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Seleccione una fecha")
                .setSelection(selection)
                .setCalendarConstraints(constraintsBuilder.build())
                // Esto activa el modo de selección de año rápido al tocar el encabezado
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .build();

        // 4. Configurar el listener cuando el usuario acepta (OK)
        datePicker.addOnPositiveButtonClickListener(selectionMillis -> {
            // MaterialDatePicker trabaja en UTC, formateamos a nuestro huso local
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selectionMillis);

            String fechaSeleccionada = sdfShort.format(calendar.getTime());
            et.setText(fechaSeleccionada);
        });

        datePicker.show(getChildFragmentManager(), "MATERIAL_DATE_PICKER");
    }

    private void generarPDF(String fechaInicio, String fechaFin) {
        new Thread(() -> {
            try {
                List<GlucosaEntity> listaGlucosa = AppDataBaseControl.getDatabase(getContext())
                        .glucosa_interfaz().obtenerPorRango(
                                sessionManager.getUserId(),
                                fechaInicio.trim() + " 00:00:00",
                                fechaFin.trim() + " 23:59:59"
                        );

                if (listaGlucosa == null || listaGlucosa.isEmpty()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "No hay datos en este rango", Toast.LENGTH_SHORT).show());
                    return;
                }

                PdfDocument document = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();

                // --- ENCABEZADOS ---
                paint.setTextSize(18f);
                paint.setFakeBoldText(true);
                canvas.drawText("REPORTE DE GLUCOSA", 50, 50, paint);

                paint.setTextSize(12f);
                paint.setFakeBoldText(false);
                canvas.drawText("Periodo: " + fechaInicio + " al " + fechaFin, 50, 80, paint);
                canvas.drawLine(50, 95, 545, 95, paint);

                int yPos = 130;
                paint.setFakeBoldText(true);
                canvas.drawText("FECHA / HORA", 50, yPos, paint);
                canvas.drawText("NIVEL", 250, yPos, paint); // Ajustado margen para etiquetas largas
                canvas.drawText("ESTADO", 380, yPos, paint);

                // --- CUERPO DINÁMICO ---
                paint.setFakeBoldText(false);
                yPos += 30;

                for (GlucosaEntity glucosa : listaGlucosa) {
                    if (yPos > 800) {
                        document.finishPage(page);
                        pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        yPos = 50;
                    }

                    // 1. Formatear Fecha (Ej: 19/12/2025 07:40 a.m.)
                    String fechaFormateada = glucosa.getFecha_hora_creacion();
                    Date fechaDate = null;
                    try {
                        fechaDate = sdfFull.parse(glucosa.getFecha_hora_creacion());
                        if (fechaDate != null) {
                            fechaFormateada = sdfOutput.format(fechaDate)
                                    .replace("AM", "a.m.")
                                    .replace("PM", "p.m.");
                        }
                    } catch (ParseException e) { e.printStackTrace(); }

                    canvas.drawText(fechaFormateada, 50, yPos, paint);
                    canvas.drawText(glucosa.getNivel_glucosa() + " mg/dL", 250, yPos, paint);

                    // 2. Lógica de Estado Personalizada
                    String estadoFinal = "";
                    if (fechaDate != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(fechaDate);
                        int hora24 = cal.get(Calendar.HOUR_OF_DAY);
                        int minutos = cal.get(Calendar.MINUTE);
                        int tiempoTotalMin = (hora24 * 60) + minutos;
                        boolean esAyunas = glucosa.getEn_ayunas();

                        // Rango Desayuno: 06:00 a 07:40
                        if (tiempoTotalMin >= (6 * 60) && tiempoTotalMin <= (8 * 60 + 30)) {
                            estadoFinal = esAyunas ? "Antes del desayuno" : "Después del desayuno";
                        }
                        // Rango Almuerzo: 12:00 a 13:30
                        else if (tiempoTotalMin >= (12 * 60) && tiempoTotalMin <= (13 * 60 + 30)) {
                            estadoFinal = esAyunas ? "Antes del almuerzo" : "Después del almuerzo";
                        }
                        // Rango Cena/Lonche: 18:00 a 19:40
                        else if (tiempoTotalMin >= (18 * 60) && tiempoTotalMin <= (19 * 60 + 40)) {
                            estadoFinal = esAyunas ? "Antes del lonche" : "Después del lonche";
                        }
                        else {
                            // Por si está fuera de los rangos específicos
                            estadoFinal = esAyunas ? "" : "";
                        }
                    } else {
                        estadoFinal = glucosa.getEn_ayunas() ? "" : "";
                    }

                    canvas.drawText(estadoFinal, 380, yPos, paint);
                    yPos += 25;
                }

                document.finishPage(page);

                String fileName = "Reporte_Glucosa" + ".pdf";
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

                FileOutputStream fos = new FileOutputStream(file);
                document.writeTo(fos);
                fos.flush();
                fos.close();
                document.close();

                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "PDF Guardado en Descargas: " + fileName, Toast.LENGTH_LONG).show());

            } catch (IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error al crear PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Forzar orientación vertical al entrar al fragmento
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Al salir, devolvemos el control al sensor o al comportamiento por defecto de la Activity
        // Si tu ReporteFragment principal permite rotar, usa SCREEN_ORIENTATION_SENSOR
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(SCREEN_ORIENTATION_SENSOR);
        }
    }
}
