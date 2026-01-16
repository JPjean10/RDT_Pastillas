package com.example.rdt_pastillas.activity.menu_lateral.ui.reporte_fragment;

import android.graphics.Color; // Importación correcta para los colores
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// Imports de la Base de Datos y Entidades
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.bd.local.database.AppDataBaseControl;

// Imports de MPAndroidChart (Gráficos)
import com.example.rdt_pastillas.util.sesion.SessionManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

// Imports de Java Standard (Listas)
import java.util.ArrayList;
import java.util.List;

public class ReporteFragment extends Fragment {

    private LineChart lineChart;
    private TextView tvRangoFechas;
    private ImageButton btnRetroceder, btnAdelantar;
    private SessionManager sessionManager;

    private int currentOffset = 0; // Controla la página actual
    private int totalRegistros = 0;
    private final int LIMIT = 7;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reporte, container, false);
        sessionManager = new SessionManager(requireContext());

        lineChart = view.findViewById(R.id.chartGlucosa);
        tvRangoFechas = view.findViewById(R.id.tvRangoFechas);
        btnRetroceder = view.findViewById(R.id.btnRetroceder);
        btnAdelantar = view.findViewById(R.id.btnAdelantar);

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

        configurarEsteticaGrafico();
        cargarDatos();
        return view;
    }

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
    }

    private void cargarDatos() {
        AppDataBaseControl.databaseWriteExecutor.execute(() -> {
            long idUser = sessionManager.getUserId();
            // Cambiamos a las últimas 7 tomas para asegurar que siempre haya datos en el gráfico
            totalRegistros = AppDataBaseControl.getDatabase(getContext()).glucosa_interfaz().getTotalRegistros(idUser);

            List<GlucosaEntity> lista = AppDataBaseControl.getDatabase(getContext())
                    .glucosa_interfaz().getGlucosaPaginadaGraficos(idUser, currentOffset);

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
        LineDataSet dataSet = new LineDataSet(entries, "");

        // Estilo 📈 (Curva suave y colores)
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(Color.parseColor("#6200EE"));
        dataSet.setCircleColor(Color.parseColor("#BB86FC"));
        dataSet.setLineWidth(3f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#6200EE"));
        dataSet.setFillAlpha(40);
        dataSet.setValueTextSize(10f);

        // Eje X con las fechas de la semana
        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < fechasX.size()) ? fechasX.get(index) : "";
            }
        });

        LineData data = new LineData(dataSet);
        lineChart.setData(data);
        lineChart.animateY(1000);
        lineChart.invalidate();
        XAxis xAxis = lineChart.getXAxis();
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
}
