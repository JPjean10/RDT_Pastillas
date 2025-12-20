package com.example.rdt_pastillas.bd.repository;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.usuario_entity.UsuarioEntity;
import com.example.rdt_pastillas.Modelo.response.ServerResponse;
import com.example.rdt_pastillas.bd.local.dao.UsuarioLocalDao;
import com.example.rdt_pastillas.bd.local.database.AppDataBaseControl;
import com.example.rdt_pastillas.bd.remote.datasource.UsuarioRemoteDataSource;
import com.example.rdt_pastillas.bd.remote.retrofit.ApiCallback;
import com.example.rdt_pastillas.util.alert.AlertaAvertencia;
import com.example.rdt_pastillas.util.alert.AlertaError;
import com.example.rdt_pastillas.util.alert.AlertaExitoso;
import com.example.rdt_pastillas.util.sesion.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class UsuarioRepository {

    private final UsuarioLocalDao localDao;
    private final UsuarioRemoteDataSource remote;

    private ExecutorService databaseWriteExecutor;
    private Context context;

    private final SessionManager sessionManager;
    String tag = "UsuarioRepository";


    public UsuarioRepository(Application application) {
        AppDataBaseControl db = AppDataBaseControl.getDatabase(application);
        this.context = application.getApplicationContext();
        localDao = db.usuario_interfaz();
        databaseWriteExecutor = AppDataBaseControl.databaseWriteExecutor;
        remote = new UsuarioRemoteDataSource();
        this.sessionManager = new SessionManager(application);
    }

    // --- Interfaz de Callback para el Login ---
    public interface LoginCallback {
        void onLoginSuccess(int userId);
        void onLoginFailed(String error);
    }

    public interface UserCountCallback {
        void onResult(int count);
    }
    public void insertar_usuario(Context context, UsuarioEntity usuario_entity){
        // 1. Crear y mostrar el ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Cargando...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        remote.insertar_usuario(context, usuario_entity, new ApiCallback<ServerResponse>() {
            @Override
            public void onSuccess(ServerResponse response) {

                try {
                    // 1. Asigna la fecha al objeto ORIGINAL que recibiste.
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String fechaFormateada = sdf.format(new Date());
                    usuario_entity.setFecha_hora_creacion(fechaFormateada);

                    // 2. Ejecuta la inserción en la base de datos local.
                    databaseWriteExecutor.execute(() -> {
                        try {
                            // El método insertUsuario devuelve el 'rowId' (un long).
                            // Si es mayor que 0, la inserción fue exitosa.
                            long idGenerado = localDao.insertUsuario(usuario_entity);

                            if (idGenerado > 0) {
                                // ¡ÉXITO! Imprime un log para confirmarlo.
                                Log.d(tag, "¡ÉXITO! Usuario guardado en BD local con ID: " + idGenerado);
                            } else {
                                // FALLO: La inserción no funcionó.
                                Log.e(tag, "FALLO: No se pudo guardar el usuario en la BD local. ID devuelto: " + idGenerado);
                            }
                        } catch (Exception e) {
                            Log.e(tag, "CRASH: Ocurrió una excepción al guardar en la BD local.", e);
                        }
                    });

                } catch (Exception e) {
                    Log.e(tag, "CRASH: Ocurrió una excepción antes de guardar en la BD local.", e);
                }
                AlertaExitoso.show(context,response.getMensaje());
                progressDialog.dismiss(); // Ocultar siempre al recibir respuesta
            }

            @Override
            public void onError(String errorMessage) {
                AlertaAvertencia.show(context,errorMessage);
                progressDialog.dismiss(); // Ocultar siempre al recibir respuesta
            }

            @Override
            public void onFailure(String failureMessage) {
                AlertaError.show(context,failureMessage);
                progressDialog.dismiss(); // Ocultar siempre al recibir respuesta
            }
        });
    }

    public void login(String usuario, String contrasena, boolean recordar, LoginCallback callback) {
        databaseWriteExecutor.execute(() -> {
            // Buscamos el usuario en la BD local
            UsuarioEntity user = localDao.findUsuarioByCredentials(usuario, contrasena);

            // Volvemos al hilo principal para notificar el resultado
            new Handler(Looper.getMainLooper()).post(() -> {
                if (user != null) {
                    // ¡Usuario encontrado!
                    if (recordar) {
                        // Si el usuario marcó "Mantener sesión", guardamos su ID
                        sessionManager.saveUserSession(user.getId_usuario());
                    }
                    callback.onLoginSuccess(user.getId_usuario());
                } else {
                    // Usuario o contraseña incorrectos
                    callback.onLoginFailed("Usuario o contraseña incorrectos.");
                }
            });
        });
    }

    public void contarUsuarios(UserCountCallback callback) {
        databaseWriteExecutor.execute(() -> {
            int count = localDao.countUsers();
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(count));
        });
    }
}
