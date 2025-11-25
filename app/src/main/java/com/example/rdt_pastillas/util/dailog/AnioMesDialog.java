package com.example.rdt_pastillas.util.dailog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.rdt_pastillas.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale; // Para String.format

public class AnioMesDialog extends DialogFragment {

    private final OnFechaSeleccionadaListener listener;
    private final Integer initialYear; // Año previamente seleccionado, si existe

    private final List<String> months = Arrays.asList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    );

    private Integer selectedYear;
    private Integer selectedMonthIndex;
    private View selectedMonthView;

    // Constructor
    public AnioMesDialog(OnFechaSeleccionadaListener listener, @Nullable Integer initialYear) {
        this.listener = listener;
        this.initialYear = initialYear;
    }

    public AnioMesDialog(OnFechaSeleccionadaListener listener) {
        this(listener, null); // Constructor para cuando no hay initialYear
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dailog_mes_anio, null);

        GridView gridYears = view.findViewById(R.id.gridYears);
        ListView listMonths = view.findViewById(R.id.listMonths);
        Button btnAccept = view.findViewById(R.id.btnAccept);
        Button btnCancelar = view.findViewById(R.id.btnCancel);
        ViewFlipper viewFlipper = view.findViewById(R.id.viewFlipper);
        TextView tvSelectedYear = view.findViewById(R.id.tvSelectedYear);

        Calendar calendarInstance = Calendar.getInstance();
        int currentSystemYear = calendarInstance.get(Calendar.YEAR);

        List<Integer> years = new ArrayList<>();
        for (int i = currentSystemYear; i <= currentSystemYear + 10; i++) {
            years.add(i);
        }

        if (initialYear != null) {
            selectedYear = initialYear;
        } else {
            selectedYear = currentSystemYear;
        }

        // Adaptadores
        ArrayAdapter<Integer> yearsAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_grid_text, R.id.textItem, years);
        gridYears.setAdapter(yearsAdapter);

        ArrayAdapter<String> monthsAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_grid_text, R.id.textItem, months);
        listMonths.setAdapter(monthsAdapter);

        // Si el año está dentro del rango, mostrar meses directamente
        if (years.contains(selectedYear)) { // Verificar si el año seleccionado está en la lista generada
            tvSelectedYear.setText(String.valueOf(selectedYear));
            tvSelectedYear.setVisibility(View.VISIBLE);
            viewFlipper.setDisplayedChild(1); // Asumiendo que los meses son el segundo hijo (índice 1)
        }


        gridYears.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = years.get(position);
                tvSelectedYear.setText(String.valueOf(selectedYear));
                tvSelectedYear.setVisibility(View.VISIBLE);
                viewFlipper.setDisplayedChild(1); // Mostrar el siguiente (meses)
            }
        });

        tvSelectedYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMonthSelection(listMonths);
                selectedMonthIndex = null;
                // selectedMonthView ya se resetea en resetMonthSelection
                btnAccept.setVisibility(View.GONE);
                tvSelectedYear.setVisibility(View.GONE);
                viewFlipper.setDisplayedChild(0); // Mostrar el anterior (años)
            }
        });

        listMonths.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewItem, int position, long id) {
                selectedMonthIndex = position + 1; // Los meses son base 1 (Enero = 1)
                String monthName = months.get(position);

                if (selectedMonthView != null) {
                    selectedMonthView.setBackgroundResource(android.R.color.transparent);
                }
                viewItem.setBackgroundResource(R.drawable.dg_selected_month); // Asegúrate que este drawable exista
                selectedMonthView = viewItem;

                tvSelectedYear.setText(String.format(Locale.getDefault(), "%d / %s", selectedYear, monthName));
                btnAccept.setVisibility(View.VISIBLE);
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedYear != null && selectedMonthIndex != null) {
                    String monthName = months.get(selectedMonthIndex - 1); // El índice de la lista es base 0
                    String formattedMonth = String.format(Locale.getDefault(), "%02d", selectedMonthIndex);
                    String fechaParaMostrar = String.format(Locale.getDefault(), "%d/%s", selectedYear, monthName);
                    String fechaParaGuardar = String.format(Locale.getDefault(), "%d-%s", selectedYear, formattedMonth);

                    if (listener != null) {
                        listener.onFechaSeleccionada(fechaParaGuardar, fechaParaMostrar);
                    }
                    dismiss();
                }
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(view);
        return builder.create();
    }

    private void resetMonthSelection(ListView list) {
        for (int i = 0; i < list.getChildCount(); i++) {
            View child = list.getChildAt(i);
            if (child != null) {
                child.setBackgroundResource(android.R.color.transparent);
            }
        }
        selectedMonthView = null; // También resetear la referencia
    }

    public interface OnFechaSeleccionadaListener {
        void onFechaSeleccionada(String fechaGuardada, String fechaMostrada);
    }
}
