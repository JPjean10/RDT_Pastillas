package com.example.rdt_pastillas.activity.menu_lateral.ui.Presion_fragment.componentes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.presion_entity.PresionEntity;
import com.example.rdt_pastillas.R;
import com.google.android.material.textfield.TextInputEditText;

public class PresionEditDailog {

    private AlertDialog dialog;
    private Context context;
    private final PresionEntity presion;
    private EditOnClickedDailog listener;

    public interface EditOnClickedDailog {
        void EditOnClickedDailog(PresionEntity presion, int sys, int dia, int pul);

    }
    public PresionEditDailog(Context context, PresionEntity presion, EditOnClickedDailog listener) {
        this.context = context;
        this.presion = presion;
        this.listener = listener;
    }

    public void show() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dailog_presion, null);

        final TextInputEditText txt_sys = view.findViewById(R.id.txt_sys);
        final TextInputEditText txt_dia = view.findViewById(R.id.txt_dia);
        final TextInputEditText txt_pul = view.findViewById(R.id.txt_pul);

        txt_sys.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(3)});
        txt_dia.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(3)});
        txt_pul.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(3)});

        txt_sys.setText(String.valueOf(presion.getSys()));
        txt_dia.setText(String.valueOf(presion.getDia()));
        txt_pul.setText(String.valueOf(presion.getPul()));

        ImageView btnAceptar = view.findViewById(R.id.btnAceptar);
        ImageView btnCancelar = view.findViewById(R.id.btnCancelar);
        // Construir el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        dialog = builder.create();

        // Configurar el fondo para que sea transparente y se vean las esquinas redondeadas
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Configurar el listener para el botón "Aceptar"
        btnAceptar.setOnClickListener(v -> {
            String Edi_sys = txt_sys.getText().toString().trim();
            String Edi_dia = txt_dia.getText().toString().trim();
            String Edi_pul = txt_pul.getText().toString().trim();

            // Validación simple
            if (Edi_sys.isEmpty() || Edi_dia.isEmpty() || Edi_pul.isEmpty()) {
                if (Edi_sys.isEmpty()) {
                    txt_sys.setError("El monto es requerido");
                }
                if (Edi_dia.isEmpty()) {
                    txt_dia.setError("El monto es requerido");
                }
                if (Edi_pul.isEmpty()) {
                    txt_pul.setError("El monto es requerido");
                }
                return;
            }

            int sys = Integer.parseInt(Edi_sys);
            int dia = Integer.parseInt(Edi_dia);
            int pul = Integer.parseInt(Edi_pul);

            // Llamar al listener con los datos
            if (listener != null) {
                listener.EditOnClickedDailog(presion, sys, dia, pul);
            }

            // Cerrar el diálogo
            dismiss();
        });

        // Configurar el listener para el botón "Cancelar"
        btnCancelar.setOnClickListener(v -> dismiss());

        // Mostrar el diálogo
        dialog.show();

    }

    // Método para cerrar el diálogo
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

}
