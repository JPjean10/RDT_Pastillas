package com.example.rdt_pastillas.activity.login.componentes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.example.rdt_pastillas.R;
import com.google.android.material.textfield.TextInputEditText;

public class UsuarioInsertDailog {

    private AlertDialog dialog;
    private Context context;
    private insertOnClickedDailog listener;

    public interface insertOnClickedDailog {
        void insertOnClickedDailog(String usuario, String contrasena);

    }

    public UsuarioInsertDailog(Context context, insertOnClickedDailog listener) {
        this.context = context;
        this.listener = listener;
    }

    public void show() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dailog_insert_usuario, null);

        final TextInputEditText txtUsuario = view.findViewById(R.id.txt_usuario);
        final TextInputEditText txtContrasena = view.findViewById(R.id.txt_contrasena);

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
            String usuario = txtUsuario.getText().toString().trim();
            String contrasena = txtContrasena.getText().toString().trim();

            // Validación simple
            if (usuario.isEmpty()) {
                txtUsuario.setError("El usuario es requerido");
                return;
            }
            if (contrasena.isEmpty()) {
                txtContrasena.setError("La contraseña es requerida");
                return;
            }

            // Llamar al listener con los datos
            if (listener != null) {
                listener.insertOnClickedDailog(usuario, contrasena);
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
