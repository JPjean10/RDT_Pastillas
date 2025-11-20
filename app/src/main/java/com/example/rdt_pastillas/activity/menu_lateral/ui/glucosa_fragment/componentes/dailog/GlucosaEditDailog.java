package com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.componentes.dailog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaEntity;
import com.google.android.material.textfield.TextInputEditText;

public class GlucosaEditDailog {
    private AlertDialog dialog;
    private Context context;
    private final GlucosaEntity glucosa;

    private EditOnClickedDailog listener;


    public interface EditOnClickedDailog {
        void EditOnClickedDailog(int id,int nivel_glucosa);

    }
    public GlucosaEditDailog(Context context, GlucosaEntity glucosa, EditOnClickedDailog listener) {
        this.context = context;
        this.glucosa = glucosa;
        this.listener = listener;
    }
    public void show() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dailog_edit_glucosa, null);

        final TextInputEditText txtGlucosa = view.findViewById(R.id.txt_glucosa);
        final TextInputEditText txtGlucosaNoEditable = view.findViewById(R.id.txt_glucosa_no_edit);

        txtGlucosaNoEditable.setText(String.valueOf(glucosa.getNivel_glucosa()));

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
            String Edi_glucosa = txtGlucosa.getText().toString().trim();

            // Validación simple
            if (Edi_glucosa.isEmpty()) {
                txtGlucosa.setError("El monto es requerido");
                return;
            }

            int nivel_glucosa = Integer.parseInt(Edi_glucosa);


            // Llamar al listener con los datos
            if (listener != null) {
                listener.EditOnClickedDailog(glucosa.getId_glucosa(),nivel_glucosa);
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
