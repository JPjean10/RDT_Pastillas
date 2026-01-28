package com.example.rdt_pastillas.activity.menu_lateral.ui.reporte_fragment.ui.reporte_presion;

import android.graphics.Canvas;
import android.graphics.Color;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.presion_entity.PresionEntity;
import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.bd.local.database.AppDataBaseControl;
import com.example.rdt_pastillas.util.dailog.RangoFechasDialog;
import com.example.rdt_pastillas.util.sesion.SessionManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ReportePresionFragment extends Fragment implements OnChartValueSelectedListener,
        RangoFechasDialog.OnRangoSeleccionadoListener{

    private LineChart lineChart;
    private TextView tvRangoFechas;
    private ImageButton btnRetroceder, btnAdelantar;
    private SessionManager sessionManager;

    private int currentOffset = 0;
    private int totalRegistros = 0;
    private final int LIMIT = 15;

    private MaterialButton btnExportarPDF;

    // Formatos de fecha necesarios
    private SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat sdfShort = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat sdfVisual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reporte_presion, container, false);

        // Configurar TimeZones para evitar desfases en cálculos de calendario
        sdfFull.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdfShort.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdfVisual.setTimeZone(TimeZone.getTimeZone("UTC"));

        lineChart = view.findViewById(R.id.chartPresion);
        tvRangoFechas = view.findViewById(R.id.tvRangoFechas);
        btnRetroceder = view.findViewById(R.id.btnRetroceder);
        btnAdelantar = view.findViewById(R.id.btnAdelantar);
        sessionManager = new SessionManager(requireContext());
        btnExportarPDF = view.findViewById(R.id.btn_exportar);

        // Configurar listener de selección
        lineChart.setOnChartValueSelectedListener(this);

        // Al tocar cualquier parte vacía, restaurar todo
        view.setOnClickListener(v -> restaurarTransparencias());

        configurarEjes();
        cargarDatos();

        btnRetroceder.setOnClickListener(v -> { currentOffset += LIMIT; cargarDatos(); });
        btnAdelantar.setOnClickListener(v -> { if (currentOffset >= LIMIT) { currentOffset -= LIMIT; cargarDatos(); } });

        btnExportarPDF.setOnClickListener(v -> {
            new RangoFechasDialog(
                    requireContext(),
                    getChildFragmentManager(),
                    new RangoFechasDialog.DaoProvider() {
                        @Override
                        public String getMin(long userId) {
                            return AppDataBaseControl.getDatabase(getContext()).presion_interfaz().getFechaMinima(userId);
                        }

                        @Override
                        public String getMax(long userId) {
                            return AppDataBaseControl.getDatabase(getContext()).presion_interfaz().getFechaMaxima(userId);
                        }
                    },
                    this
            ).show();
        });

        return view;
    }

    private void configurarEjes() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setExtraBottomOffset(15f);
        lineChart.setTouchEnabled(true);
        lineChart.setHighlightPerTapEnabled(true);

        // --- CAMBIO AQUÍ: Desactivar zoom por doble toque, mantener zoom por dedos ---
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setPinchZoom(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        // Configuración Eje Izquierdo (0 a 300)
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(300f);
        leftAxis.setLabelCount(7, true);

        // Configuración Eje Derecho (Muestra valores 50, 100, 150... al final de la línea)
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(true);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(300f);
        rightAxis.setLabelCount(7, true);
        rightAxis.setDrawGridLines(false);
    }

    private void cargarDatos() {
        AppDataBaseControl.databaseWriteExecutor.execute(() -> {
            long idUser = sessionManager.getUserId();
            totalRegistros = AppDataBaseControl.getDatabase(getContext()).presion_interfaz().getTotalRegistros(idUser);
            List<PresionEntity> lista = AppDataBaseControl.getDatabase(getContext()).presion_interfaz().getPresionPaginadaGraficosBase(idUser, LIMIT, currentOffset);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    actualizarBotones();
                    if (lista != null && !lista.isEmpty()) {
                        actualizarRangoFechas(lista);
                        pintarGrafico(lista);
                    } else {
                        lineChart.clear();
                        tvRangoFechas.setText("Sin datos");
                    }
                });
            }
        });
    }

    private void pintarGrafico(List<PresionEntity> lista) {
        ArrayList<Entry> entriesSys = new ArrayList<>();
        ArrayList<Entry> entriesDia = new ArrayList<>();
        ArrayList<Entry> entriesPul = new ArrayList<>();
        ArrayList<String> fechasX = new ArrayList<>();

        for (int i = 0; i < lista.size(); i++) {
            PresionEntity p = lista.get(i);
            entriesSys.add(new Entry(i, (float) p.getSys()));
            entriesDia.add(new Entry(i, (float) p.getDia()));
            entriesPul.add(new Entry(i, (float) p.getPul()));
            fechasX.add(formatearFechaEjeX(p.getFecha_hora_creacion()));
        }

        LineDataSet setSys = crearDataSet(entriesSys, "SYS", Color.RED);
        LineDataSet setDia = crearDataSet(entriesDia, "DIA", Color.BLUE);
        LineDataSet setPul = crearDataSet(entriesPul, "PUL", Color.parseColor("#2E7D32"));

        LineData data = new LineData(setSys, setDia, setPul);

        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                return (idx >= 0 && idx < fechasX.size()) ? fechasX.get(idx) : "";
            }
        });

        lineChart.getXAxis().setLabelCount(fechasX.size(), true);
        lineChart.setData(data);
        lineChart.animateY(1000);
        lineChart.invalidate();
    }

    private LineDataSet crearDataSet(ArrayList<Entry> entries, String label, int color) {
        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(color);
        set.setCircleColor(color);
        set.setLineWidth(2.5f);
        set.setCircleRadius(4f);
        set.setDrawValues(true);
        set.setValueTextSize(9f);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setHighlightEnabled(true);
        set.setDrawHighlightIndicators(false);
        return set;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        int indexSeleccionado = h.getDataSetIndex();
        LineData data = lineChart.getData();

        if (data == null) return;

        for (int i = 0; i < data.getDataSetCount(); i++) {
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(i);
            if (i == indexSeleccionado) {
                // LA SELECCIONADA: Resalta
                set.setFillAlpha(255);
                set.setDrawCircles(true);
                set.setDrawValues(true);
                set.setLineWidth(3.0f);
            } else {
                // LAS OTRAS: Se vuelven sombras para no tapar el resultado
                set.setFillAlpha(30);
                set.setDrawCircles(false);
                set.setDrawValues(false);
                set.setLineWidth(1.0f);
            }
        }
        lineChart.invalidate();
    }

    @Override
    public void onNothingSelected() {
        restaurarTransparencias();
    }

    private void restaurarTransparencias() {
        LineData data = lineChart.getData();
        if (data == null) return;

        for (int i = 0; i < data.getDataSetCount(); i++) {
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(i);
            set.setFillAlpha(255);
            set.setLineWidth(2.5f);
            set.setDrawCircles(true);
            set.setDrawValues(true);
        }
        lineChart.invalidate();
    }

    private String formatearFechaEjeX(String raw) {
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat formatter = new SimpleDateFormat("EEE d", new Locale("es", "ES"));
            return formatter.format(parser.parse(raw));
        } catch (Exception e) { return ""; }
    }

    private void actualizarBotones() {
        btnAdelantar.setVisibility(currentOffset > 0 ? View.VISIBLE : View.INVISIBLE);
        btnRetroceder.setVisibility((currentOffset + LIMIT) < totalRegistros ? View.VISIBLE : View.INVISIBLE);
    }

    private void actualizarRangoFechas(List<PresionEntity> lista) {
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat display = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String f1 = display.format(parser.parse(lista.get(0).getFecha_hora_creacion()));
            String f2 = display.format(parser.parse(lista.get(lista.size()-1).getFecha_hora_creacion()));
            tvRangoFechas.setText(f1 + " - " + f2);
        } catch (Exception e) { tvRangoFechas.setText("---"); }
    }

    @Override
    public void onRangoAceptado(String inicioDB, String finDB) {
        generarPDFPresion(inicioDB, finDB);
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
}
