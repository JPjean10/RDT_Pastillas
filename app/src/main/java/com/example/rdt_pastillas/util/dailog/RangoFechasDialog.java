package com.example.rdt_pastillas.util.dailog;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.util.sesion.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RangoFechasDialog {

    public interface OnRangoSeleccionadoListener {
        void onRangoAceptado(String inicioDB, String finDB);
    }

    public interface DaoProvider {
        String getMin(long userId);
        String getMax(long userId);
    }

    private final Context context;
    private final FragmentManager fragmentManager;
    private final DaoProvider daoProvider;
    private final SessionManager sessionManager;
    private final OnRangoSeleccionadoListener listener;

    private long minDateMillis = 0;
    private long maxDateMillis = System.currentTimeMillis();

    private final SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat sdfShort = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat sdfVisual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public RangoFechasDialog(Context context, FragmentManager fragmentManager, DaoProvider daoProvider, OnRangoSeleccionadoListener listener) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.daoProvider = daoProvider;
        this.listener = listener;
        this.sessionManager = new SessionManager(context);

        sdfFull.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdfShort.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdfVisual.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void show() {
        new Thread(() -> {
            try {
                long idUser = sessionManager.getUserId();
                String fechaMinStr = daoProvider.getMin(idUser);
                String fechaMaxStr = daoProvider.getMax(idUser);

                if (fechaMinStr != null) {
                    Date dMin = sdfFull.parse(fechaMinStr);
                    if (dMin != null) {
                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        cal.setTime(dMin);
                        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
                        minDateMillis = cal.getTimeInMillis();
                    }
                }

                if (fechaMaxStr != null) {
                    Date dMax = sdfFull.parse(fechaMaxStr);
                    if (dMax != null) {
                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        cal.setTime(dMax);
                        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
                        maxDateMillis = cal.getTimeInMillis();
                    }
                }

                new android.os.Handler(android.os.Looper.getMainLooper()).post(this::crearDialogoUI);

            } catch (ParseException e) { e.printStackTrace(); }
        }).start();
    }

    private void crearDialogoUI() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_rango_fechas, null);
        TextInputEditText etInicio = view.findViewById(R.id.btn_FechaInicio);
        TextInputEditText etFin = view.findViewById(R.id.btn_FechaFin);
        MaterialButton btnCancelar = view.findViewById(R.id.btnCancelar);
        MaterialButton btnDescargar = view.findViewById(R.id.btn_descargar);

        // --- LÓGICA DE FECHAS POR DEFECTO ---
        if (maxDateMillis > 0) {
            // 1. "Hasta" siempre es el último registro
            Calendar calHasta = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calHasta.setTimeInMillis(maxDateMillis);
            etFin.setText(sdfVisual.format(calHasta.getTime()));

            // 2. "Desde" calculado (3 meses atrás desde el máximo)
            Calendar calDesde = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calDesde.setTimeInMillis(maxDateMillis);
            calDesde.add(Calendar.MONTH, -3); // Restamos 3 meses

            long tresMesesAtrasMillis = calDesde.getTimeInMillis();

            // Si 3 meses atrás es menor que el primer registro que existe (minDateMillis),
            // entonces usamos el primer registro como el más cercano.
            if (tresMesesAtrasMillis < minDateMillis) {
                calDesde.setTimeInMillis(minDateMillis);
            }

            etInicio.setText(sdfVisual.format(calDesde.getTime()));
        }

        // --- CONFIGURACIÓN DE CLICKS ---
        etInicio.setOnClickListener(v -> mostrarPicker(etInicio));
        etFin.setOnClickListener(v -> mostrarPicker(etFin));

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnDescargar.setOnClickListener(v -> {
            String inicioVis = etInicio.getText().toString();
            String finVis = etFin.getText().toString();

            if (!inicioVis.isEmpty() && !finVis.isEmpty()) {
                try {
                    Date d1 = sdfVisual.parse(inicioVis);
                    Date d2 = sdfVisual.parse(finVis);
                    if (d1 != null && d2 != null) {
                        listener.onRangoAceptado(sdfShort.format(d1), sdfShort.format(d2));
                        dialog.dismiss();
                    }
                } catch (ParseException e) { e.printStackTrace(); }
            } else {
                Toast.makeText(context, "Seleccione ambas fechas", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void mostrarPicker(TextInputEditText et) {
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();

        if (minDateMillis > 0 && maxDateMillis > 0) {
            constraintsBuilder.setStart(minDateMillis);
            constraintsBuilder.setEnd(maxDateMillis);
            List<CalendarConstraints.DateValidator> validators = new ArrayList<>();
            validators.add(DateValidatorPointForward.from(minDateMillis));
            validators.add(DateValidatorPointBackward.before(maxDateMillis));
            constraintsBuilder.setValidator(CompositeDateValidator.allOf(validators));
        }

        // Para el picker, intentamos abrirlo en la fecha que ya tiene el campo escrito
        long selection = maxDateMillis;
        try {
            String fechaActualEnCampo = et.getText().toString();
            if (!fechaActualEnCampo.isEmpty()) {
                Date d = sdfVisual.parse(fechaActualEnCampo);
                if (d != null) selection = d.getTime();
            }
        } catch (Exception e) { e.printStackTrace(); }

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Seleccione fecha")
                .setSelection(selection)
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        picker.addOnPositiveButtonClickListener(selectionMillis -> {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(selectionMillis);
            et.setText(sdfVisual.format(cal.getTime()));
        });

        picker.show(fragmentManager, "DATE_PICKER");
    }
}
