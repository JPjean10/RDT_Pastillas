package com.example.rdt_pastillas.activity.menu_lateral.ui.reporte_fragment.ui.exportar_pdf_fragment;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.presion_entity.PresionEntity;
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
    private MaterialButton btn_exportar;
    private RadioGroup rgTipoReporte;
    private RadioButton rbGlucosa, rbPresion;

    private long minDateMillis = 0;
    private long maxDateMillis = System.currentTimeMillis();

    private SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat sdfShort = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat sdfOutput = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
    private SimpleDateFormat sdfVisual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exportar_pdf, container, false);

        // CONFIGURACIÓN CRUCIAL: Forzar UTC para evitar desfases de días
        sdfFull.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdfShort.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdfVisual.setTimeZone(TimeZone.getTimeZone("UTC"));

        sessionManager = new SessionManager(requireContext());

        btn_FechaInicio = view.findViewById(R.id.btn_FechaInicio);
        btn_FechaFin = view.findViewById(R.id.btn_FechaFin);
        btn_exportar = view.findViewById(R.id.btn_exportar);
        rgTipoReporte = view.findViewById(R.id.rgTipoReporte);
        rbGlucosa = view.findViewById(R.id.rbGlucosa);
        rbPresion = view.findViewById(R.id.rbPresion);

        // Al cambiar de Glucosa a Presión, recalculamos los límites de las fechas
        rgTipoReporte.setOnCheckedChangeListener((group, checkedId) -> {
            btn_FechaInicio.setText("");
            btn_FechaFin.setText("");
            obtenerLimitesDeBaseDeDatos();
        });

        obtenerLimitesDeBaseDeDatos();

        btn_FechaInicio.setOnClickListener(v -> mostrarDatePicker(btn_FechaInicio));
        btn_FechaFin.setOnClickListener(v -> mostrarDatePicker(btn_FechaFin));

        btn_exportar.setOnClickListener(v -> {
            String inicioVisual = btn_FechaInicio.getText().toString();
            String finVisual = btn_FechaFin.getText().toString();

            if (inicioVisual.isEmpty() || finVisual.isEmpty()) {
                Toast.makeText(getContext(), "Por favor seleccione ambas fechas", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    // Convertimos el formato visual (dd/MM/yyyy) al formato de base de datos (yyyy-MM-dd)
                    Date d1 = sdfVisual.parse(inicioVisual);
                    Date d2 = sdfVisual.parse(finVisual);

                    String inicioDB = sdfShort.format(d1);
                    String finDB = sdfShort.format(d2);

                    if (rbGlucosa.isChecked()) {
                        generarPDFGlucosa(inicioDB, finDB);
                    } else {
                        generarPDFPresion(inicioDB, finDB);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }

    private void obtenerLimitesDeBaseDeDatos() {
        new Thread(() -> {
            try {
                long idUser = sessionManager.getUserId();
                String fechaMinStr, fechaMaxStr;

                if (rbGlucosa.isChecked()) {
                    fechaMinStr = AppDataBaseControl.getDatabase(getContext()).glucosa_interfaz().getFechaMinima(idUser);
                    fechaMaxStr = AppDataBaseControl.getDatabase(getContext()).glucosa_interfaz().getFechaMaxima(idUser);
                } else {
                    fechaMinStr = AppDataBaseControl.getDatabase(getContext()).presion_interfaz().getFechaMinima(idUser);
                    fechaMaxStr = AppDataBaseControl.getDatabase(getContext()).presion_interfaz().getFechaMaxima(idUser);
                }

                if (fechaMinStr != null) {
                    Date dMin = sdfFull.parse(fechaMinStr);
                    if (dMin != null) {
                        // NORMALIZAR AL INICIO DEL DÍA (00:00:00)
                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        cal.setTime(dMin);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        minDateMillis = cal.getTimeInMillis();
                    }
                }

                if (fechaMaxStr != null) {
                    Date dMax = sdfFull.parse(fechaMaxStr);
                    if (dMax != null) {
                        // NORMALIZAR AL FINAL DEL DÍA (23:59:59)
                        // Esto asegura que el último día sea seleccionable
                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        cal.setTime(dMax);
                        cal.set(Calendar.HOUR_OF_DAY, 23);
                        cal.set(Calendar.MINUTE, 59);
                        cal.set(Calendar.SECOND, 59);
                        cal.set(Calendar.MILLISECOND, 999);
                        maxDateMillis = cal.getTimeInMillis();
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void mostrarDatePicker(TextInputEditText et) {
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();

        if (minDateMillis > 0 && maxDateMillis > 0) {
            // Establecer los límites de navegación del calendario
            constraintsBuilder.setStart(minDateMillis);
            constraintsBuilder.setEnd(maxDateMillis);

            // CREAR VALIDADOR COMPUESTO: Bloquea antes del mínimo Y después del máximo
            CalendarConstraints.DateValidator minValidator = DateValidatorPointForward.from(minDateMillis);
            CalendarConstraints.DateValidator maxValidator = com.google.android.material.datepicker.DateValidatorPointBackward.before(maxDateMillis);

            // Combinamos ambos validadores
            java.util.ArrayList<CalendarConstraints.DateValidator> listValidators = new java.util.ArrayList<>();
            listValidators.add(minValidator);
            listValidators.add(maxValidator);

            constraintsBuilder.setValidator(com.google.android.material.datepicker.CompositeDateValidator.allOf(listValidators));
        }

        // Determinar selección inicial (si el campo tiene algo, usarlo; si no, usar el mínimo)
        long selection = maxDateMillis > 0 ? maxDateMillis : MaterialDatePicker.todayInUtcMilliseconds();
        try {
            String actual = et.getText().toString();
            if (!actual.isEmpty()) {
                Date d = sdfShort.parse(actual);
                if (d != null) selection = d.getTime();
            }
        } catch (Exception e) {}

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Seleccione una fecha")
                .setSelection(selection)
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        datePicker.addOnPositiveButtonClickListener(selectionMillis -> {
            // Formatear la fecha seleccionada usando UTC
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selectionMillis);

            // CAMBIO AQUÍ: Usamos el formato visual dd/MM/yyyy
            String fechaParaMostrar = sdfVisual.format(calendar.getTime());
            et.setText(fechaParaMostrar);
        });

        datePicker.show(getChildFragmentManager(), "MATERIAL_DATE_PICKER");
    }

    private void generarPDFGlucosa(String fechaInicio, String fechaFin) {
        new Thread(() -> {
            try {
                // 1. Obtener datos de la base de datos
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

                // 2. Configuración del Documento PDF
                PdfDocument document = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();

                // --- ENCABEZADOS ---
                paint.setTextSize(18f);
                paint.setFakeBoldText(true);
                canvas.drawText("REPORTE DE SALUD: GLUCOSA", 50, 50, paint);

                String periodoText = "Periodo: ";
                try {
                    Date dIni = sdfShort.parse(fechaInicio);
                    Date dFin = sdfShort.parse(fechaFin);
                    periodoText += sdfVisual.format(dIni) + " al " + sdfVisual.format(dFin);
                } catch (ParseException e) {
                    periodoText += fechaInicio + " al " + fechaFin; // Respaldo
                }

                paint.setTextSize(12f);
                paint.setFakeBoldText(false);
                // Mostrar el periodo en formato dd/MM/yyyy en el encabezado del PDF
                canvas.drawText(periodoText, 50, 80, paint);
                canvas.drawText(periodoText, 50, 80, paint);
                canvas.drawLine(50, 95, 545, 95, paint);

                int yPos = 130;
                paint.setFakeBoldText(true);
                canvas.drawText("FECHA / HORA", 50, yPos, paint);
                canvas.drawText("NIVEL", 250, yPos, paint);
                canvas.drawText("ESTADO", 380, yPos, paint);

                // --- CUERPO DEL REPORTE ---
                paint.setFakeBoldText(false);
                yPos += 30;

                // Importante: No forzar UTC aquí para que la hora se lea tal cual se guardó
                SimpleDateFormat sdfEntrada = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat sdfSalida = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());

                for (GlucosaEntity glucosa : listaGlucosa) {
                    // Control de salto de página
                    if (yPos > 800) {
                        document.finishPage(page);
                        pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        yPos = 50;
                    }

                    String fechaHoraOriginal = glucosa.getFecha_hora_creacion();
                    String fechaParaMostrar = fechaHoraOriginal;
                    String estadoFinal = "";

                    try {
                        // Convertir el String de la BD a objeto Date
                        Date dateObj = sdfEntrada.parse(fechaHoraOriginal);
                        if (dateObj != null) {
                            // Formatear a: 13/01/2026 06:30 a.m.
                            fechaParaMostrar = sdfSalida.format(dateObj)
                                    .replace("AM", "a.m.")
                                    .replace("PM", "p.m.");

                            // Lógica de Clasificación por Horario
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(dateObj);
                            int hora24 = cal.get(Calendar.HOUR_OF_DAY);
                            int minutos = cal.get(Calendar.MINUTE);
                            int tiempoTotalMinutos = (hora24 * 60) + minutos;
                            boolean esAyunas = glucosa.getEn_ayunas();

                            // Desayuno: 06:00 a 08:30
                            if (tiempoTotalMinutos >= (6 * 60) && tiempoTotalMinutos <= (8 * 60 + 30)) {
                                estadoFinal = esAyunas ? "Antes del desayuno" : "Después del desayuno";
                            }
                            // Almuerzo: 12:00 a 13:30
                            else if (tiempoTotalMinutos >= (12 * 60) && tiempoTotalMinutos <= (13 * 60 + 30)) {
                                estadoFinal = esAyunas ? "Antes del almuerzo" : "Después del almuerzo";
                            }
                            // Lonche/Cena: 18:00 a 19:40
                            else if (tiempoTotalMinutos >= (18 * 60) && tiempoTotalMinutos <= (19 * 60 + 40)) {
                                estadoFinal = esAyunas ? "Antes del lonche" : "Después del lonche";
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    // Dibujar fila en el PDF
                    canvas.drawText(fechaParaMostrar, 50, yPos, paint);
                    canvas.drawText(glucosa.getNivel_glucosa() + " mg/dL", 250, yPos, paint);
                    canvas.drawText(estadoFinal, 380, yPos, paint);

                    yPos += 25; // Espacio entre filas
                }

                document.finishPage(page);
                guardarArchivo(document, "Reporte_Glucosa");
            } catch (IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error al crear el archivo PDF", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void generarPDFPresion(String fechaInicio, String fechaFin) {
        new Thread(() -> {
            try {
                // 1. Obtener datos de la base de datos
                List<PresionEntity> lista = AppDataBaseControl.getDatabase(getContext())
                        .presion_interfaz().obtenerPorRango(
                                sessionManager.getUserId(),
                                fechaInicio.trim() + " 00:00:00",
                                fechaFin.trim() + " 23:59:59"
                        );

                if (lista == null || lista.isEmpty()) {
                    mostrarToast("No hay datos de Presión en este rango");
                    return;
                }

                // 2. Configuración del Documento PDF
                PdfDocument document = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();

                // --- ENCABEZADOS ---
                paint.setTextSize(18f);
                paint.setFakeBoldText(true);
                canvas.drawText("REPORTE DE PRESIÓN ARTERIAL", 50, 50, paint);

                String periodoFormateado = "Periodo: ";
                try {
                    Date dIni = sdfShort.parse(fechaInicio);
                    Date dFin = sdfShort.parse(fechaFin);
                    periodoFormateado += sdfVisual.format(dIni) + " al " + sdfVisual.format(dFin);
                } catch (ParseException e) {
                    periodoFormateado += fechaInicio + " al " + fechaFin; // Respaldo por si falla
                }

                paint.setTextSize(12f);
                paint.setFakeBoldText(false);
                canvas.drawText(periodoFormateado, 50, 80, paint);
                canvas.drawLine(50, 95, 545, 95, paint);

                int yPos = 130;
                paint.setFakeBoldText(true);
                canvas.drawText("FECHA / HORA", 50, yPos, paint);
                canvas.drawText("SYS/DIA (mmHg)", 250, yPos, paint);
                canvas.drawText("PULSO (lpm)", 450, yPos, paint);

                // --- CUERPO DEL REPORTE ---
                paint.setFakeBoldText(false);
                yPos += 30;

                // Definimos formatos locales para evitar el desfase de horas (No forzar UTC)
                SimpleDateFormat sdfEntrada = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat sdfSalida = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());

                for (PresionEntity p : lista) {
                    // Control de salto de página
                    if (yPos > 800) {
                        document.finishPage(page);
                        pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        yPos = 50;
                    }

                    String fechaHoraOriginal = p.getFecha_hora_creacion();
                    String fechaParaMostrar = fechaHoraOriginal;

                    try {
                        // Parsear y formatear para que coincida con la hora del celular
                        Date dateObj = sdfEntrada.parse(fechaHoraOriginal);
                        if (dateObj != null) {
                            fechaParaMostrar = sdfSalida.format(dateObj)
                                    .replace("AM", "a.m.")
                                    .replace("PM", "p.m.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    canvas.drawText(fechaParaMostrar, 50, yPos, paint);
                    canvas.drawText(p.getSys() + "/" + p.getDia(), 250, yPos, paint);
                    canvas.drawText(p.getPul() + "", 450, yPos, paint);

                    yPos += 25; // Espacio entre filas
                }

                document.finishPage(page);

                // 3. Guardar el archivo
                String fileName = "Reporte_Presion.pdf";
                guardarArchivo(document, "Reporte_Presion");

            } catch (Exception e) {
                e.printStackTrace();
                mostrarToast("Error al generar PDF de Presión");
            }
        }).start();
    }

    private void guardarArchivo(PdfDocument doc, String prefix) throws IOException {
        String fileName = prefix + ".pdf";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        FileOutputStream fos = new FileOutputStream(file);
        doc.writeTo(fos);
        fos.flush(); fos.close(); doc.close();
        mostrarToast("Archivo guardado: " + fileName);
    }

    private void mostrarToast(String msg) {
        if(getActivity() != null) getActivity().runOnUiThread(() -> Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
    }

    @Override public void onResume() { super.onResume(); if(getActivity()!=null) getActivity().setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT); }
    @Override public void onPause() { super.onPause(); if(getActivity()!=null) getActivity().setRequestedOrientation(SCREEN_ORIENTATION_SENSOR); }
}
