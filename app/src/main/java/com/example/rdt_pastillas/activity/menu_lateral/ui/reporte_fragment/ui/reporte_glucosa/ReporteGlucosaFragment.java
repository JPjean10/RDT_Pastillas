package com.example.rdt_pastillas.activity.menu_lateral.ui.reporte_fragment.ui.reporte_glucosa;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color; // Importación correcta para los colores
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// Imports de la Base de Datos y Entidades
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.bd.local.database.AppDataBaseControl;

// Imports de MPAndroidChart (Gráficos)
import com.example.rdt_pastillas.util.dailog.RangoFechasDialog;
import com.example.rdt_pastillas.util.sesion.SessionManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;

// Imports de Java Standard (Listas)
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class ReporteGlucosaFragment extends Fragment implements
     RangoFechasDialog.OnRangoSeleccionadoListener {

    // VARIABLE AÑADIDA PARA CORREGIR EL ERROR
    private int orientacionOriginal = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    private LineChart lineChart;
    private TextView tvRangoFechas;
    private ImageButton btnRetroceder, btnAdelantar;
    private SessionManager sessionManager;
    private MaterialButton btnExportarPDF;


    private int currentOffset = 0; // Controla la página actual
    private int totalRegistros = 0;
    private final int LIMIT = 15;

    // Formatos de fecha necesarios
    private SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat sdfShort = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat sdfVisual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // ... (variables existentes)
    private long minDateMillis = 0;
    private long maxDateMillis = System.currentTimeMillis();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reporte_glucosa, container, false);
        sessionManager = new SessionManager(requireContext());

        // Configurar TimeZones para evitar desfases en cálculos de calendario
        sdfFull.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdfShort.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdfVisual.setTimeZone(TimeZone.getTimeZone("UTC"));

        lineChart = view.findViewById(R.id.chartGlucosa);
        tvRangoFechas = view.findViewById(R.id.tvRangoFechas);
        btnRetroceder = view.findViewById(R.id.btnRetroceder);
        btnAdelantar = view.findViewById(R.id.btnAdelantar);
        btnExportarPDF = view.findViewById(R.id.btn_exportar);

        btnRetroceder.setOnClickListener(v -> {
            currentOffset += LIMIT;
            cargarDatos();
        });

        btnAdelantar.setOnClickListener(v -> {
            if (currentOffset >= LIMIT) {
                currentOffset -= LIMIT;
                cargarDatos();
            }
        });

        btnExportarPDF.setOnClickListener(v -> {
            new RangoFechasDialog(
                    requireContext(),
                    getChildFragmentManager(),
                    new RangoFechasDialog.DaoProvider() {
                        @Override
                        public String getMin(long userId) {
                            return AppDataBaseControl.getDatabase(getContext()).glucosa_interfaz().getFechaMinima(userId);
                        }

                        @Override
                        public String getMax(long userId) {
                            return AppDataBaseControl.getDatabase(getContext()).glucosa_interfaz().getFechaMaxima(userId);
                        }
                    },
                    this
            ).show();
        });

        configurarEsteticaGrafico();
        cargarDatos();
        return view;
    }

    // Reemplaza tu método abrirSelectorRango() por este:

    private void configurarEsteticaGrafico() {
        if (lineChart == null) return;

        lineChart.getDescription().setEnabled(false);
        lineChart.setNoDataText("No hay datos para mostrar el gráfico");
        lineChart.animateX(1500);

        // Configurar Eje X (Fechas)
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        lineChart.setExtraBottomOffset(20f);

        // --- CONFIGURACIÓN EJE Y (Rango 0 - 300 de 50 en 50) ---
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);      // Valor mínimo
        leftAxis.setAxisMaximum(300f);    // Valor máximo
        leftAxis.setLabelCount(7, true);  // 7 etiquetas: 0, 50, 100, 150, 200, 250, 300
        leftAxis.setGranularity(50f);     // Intervalo fijo de 50

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(300f);
        rightAxis.setLabelCount(7, true);
        rightAxis.setGranularity(50f);
    }

    private void cargarDatos() {
        AppDataBaseControl.databaseWriteExecutor.execute(() -> {
            long idUser = sessionManager.getUserId();
            // Cambiamos a las últimas 15 tomas para asegurar que siempre haya datos en el gráfico
            totalRegistros = AppDataBaseControl.getDatabase(getContext()).glucosa_interfaz().getTotalRegistros(idUser);

            List<GlucosaEntity> lista = AppDataBaseControl.getDatabase(getContext())
                    .glucosa_interfaz().getGlucosaPaginadaGraficos(idUser,LIMIT,currentOffset);

            getActivity().runOnUiThread(() -> {
                actualizarBotones();

                if (lista != null && !lista.isEmpty()) {
                    actualizarRangoFechas(lista);

                    List<Entry> entries = new ArrayList<>();
                    List<String> fechasX = new ArrayList<>();

                    for (int i = 0; i < lista.size(); i++) {
                        GlucosaEntity item = lista.get(i);
                        entries.add(new Entry(i, (float) item.getNivel_glucosa()));

                        try {
                            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            java.util.Date date = parser.parse(item.getFecha_hora_creacion());
                            SimpleDateFormat sdfDia = new SimpleDateFormat("EEE d", new Locale("es", "ES"));
                            fechasX.add(sdfDia.format(date));
                        } catch (Exception e) { fechasX.add("S/D"); }
                    }
                    configurarGraficoFinal(entries, fechasX);
                } else {
                    lineChart.clear();
                    tvRangoFechas.setText("Sin datos");
                }
            });
        });
    }

    private void configurarGraficoFinal(List<Entry> entries, List<String> fechasX) {
        LineDataSet dataSet = new LineDataSet(entries, "glucosa");

        // Estilo 📈 (Curva suave y colores)
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(Color.parseColor("#6200EE"));
        dataSet.setCircleColor(Color.parseColor("#BB86FC"));
        dataSet.setLineWidth(3f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#6200EE"));
        dataSet.setFillAlpha(40);
        dataSet.setValueTextSize(10f);

        XAxis xAxis = lineChart.getXAxis();

        // Eje X con las fechas de la semana
        xAxis.setLabelCount(fechasX.size(), true);

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < fechasX.size()) {
                    return fechasX.get(index);
                }
                return "";
            }
        });


        LineData data = new LineData(dataSet);
        lineChart.setData(data);
        lineChart.animateY(1000);
        lineChart.invalidate();
        xAxis.setLabelRotationAngle(0f);
    }

    private void actualizarBotones() {
        // Ocultar Adelantar si estamos en los registros más nuevos (offset 0)
        btnAdelantar.setVisibility(currentOffset > 0 ? View.VISIBLE : View.INVISIBLE);

        // Ocultar Retroceder si ya no hay más registros viejos
        btnRetroceder.setVisibility((currentOffset + LIMIT) < totalRegistros ? View.VISIBLE : View.INVISIBLE);
    }

    private void actualizarRangoFechas(List<GlucosaEntity> lista) {
        try {
            // La lista viene ordenada ASC por la query del DAO (el primero es el más viejo de la página)
            String fechaInicioRaw = lista.get(0).getFecha_hora_creacion();
            String fechaFinRaw = lista.get(lista.size() - 1).getFecha_hora_creacion();

            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat display = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            String f1 = display.format(parser.parse(fechaInicioRaw));
            String f2 = display.format(parser.parse(fechaFinRaw));

            tvRangoFechas.setText(f1 + " - " + f2);
        } catch (Exception e) {
            tvRangoFechas.setText("---");
        }
    }

    @Override
    public void onRangoAceptado(String inicioDB, String finDB) {
        generarPDFGlucosa(inicioDB, finDB);
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Guardar la orientación que tenía la actividad antes de entrar
        if (getActivity() != null) {
            orientacionOriginal = getActivity().getRequestedOrientation();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Forzar horizontal
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Al salir, volver a la orientación que tenía antes (o forzar vertical)
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(orientacionOriginal);
        }
    }
}
