package com.example.rdt_pastillas.activity.menu_lateral.ui.Presion_fragment.componentes;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.example.rdt_pastillas.R;
import com.google.android.material.textfield.TextInputEditText;

public class PresionInsertDailog {

    private AlertDialog dialog;
    private Context context;

    private insertOnClickedDailog listener;


    public interface insertOnClickedDailog {
        void insertOnClickedDailog(int sys, int dia, int pul);

    }
    public PresionInsertDailog(Context context, insertOnClickedDailog listener) {
        this.context = context;
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

        ImageView btnAceptar = view.findViewById(R.id.btnAceptar);
        ImageView btnCancelar = view.findViewById(R.id.btnCancelar);

        // Configurar el diálogo aquí
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        dialog = builder.create();

        // Configurar el fondo para que sea transparente y se vean las esquinas redondeadas
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Configurar el listener para el botón "Aceptar"
        btnAceptar.setOnClickListener(v -> {
            String sys = txt_sys.getText().toString().trim();
            String dia = txt_dia.getText().toString().trim();
            String pul = txt_pul.getText().toString().trim();


            // Validación simple
            if (sys.isEmpty() || dia.isEmpty() || pul.isEmpty()) {
                if (sys.isEmpty()) {
                    txt_sys.setError("El monto es requerido");
                }
                if (dia.isEmpty()) {
                    txt_dia.setError("El monto es requerido");
                }
                if (pul.isEmpty()) {
                    txt_pul.setError("El monto es requerido");
                }
                return;
            }

            int sys_int = Integer.parseInt(sys);
            int dia_int = Integer.parseInt(dia);
            int pul_int = Integer.parseInt(pul);

            // Llamar al listener con los datos
            if (listener != null) {
                listener.insertOnClickedDailog(sys_int, dia_int, pul_int);
            }

            // Cerrar el diálogo
            dismiss();
        });

        // Configurar el listener para el botón "Cancelar"
        btnCancelar.setOnClickListener(v -> dismiss());

        // Mostrar el diálogo
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
