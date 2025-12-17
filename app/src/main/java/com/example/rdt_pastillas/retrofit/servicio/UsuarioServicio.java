package com.example.rdt_pastillas.retrofit.servicio;

import android.content.Context;
import android.widget.Toast;

import com.example.rdt_pastillas.retrofit.ApiCallback;
import com.example.rdt_pastillas.retrofit.dao.UsuarioDao;
import com.example.rdt_pastillas.retrofit.entity.UsuarioEntity;
import com.example.rdt_pastillas.retrofit.model.response.ServerResponse;

public class UsuarioServicio {

    private UsuarioDao usuarioDao;
    public void insertar_usuario(Context context, UsuarioEntity usuario){
        usuarioDao = new UsuarioDao();

        usuarioDao.insertar_usuario(context, usuario, new ApiCallback<ServerResponse>() {
            @Override
            public void onSuccess(ServerResponse response) {
                // La API respondió con éxito. Muestra un mensaje de éxito.
                Toast.makeText(context, response.getMensaje(), Toast.LENGTH_SHORT).show();
                // Aquí puedes, por ejemplo, navegar a la pantalla de Login.
                // startActivity(new Intent(RegistroActivity.this, LoginActivity.class));
            }

            @Override
            public void onError(String errorMessage) {
                // La API devolvió un error (4xx o 5xx). Muestra el mensaje.
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String failureMessage) {
                // Hubo un problema de red. Muestra el mensaje de fallo.
                Toast.makeText(context,failureMessage, Toast.LENGTH_LONG).show();
            }
        });

    }
}
