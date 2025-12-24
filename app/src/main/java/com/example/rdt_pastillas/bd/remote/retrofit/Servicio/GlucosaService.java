package com.example.rdt_pastillas.bd.remote.retrofit.Servicio;

import android.content.Context;
import android.util.Log;

import com.example.rdt_pastillas.Modelo.response.ServerResponse;
import com.example.rdt_pastillas.bd.local.dao.GlucosaLocalDao;
import com.example.rdt_pastillas.bd.local.database.AppDataBaseControl;
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.bd.remote.datasource.UsuarioRemoteDataSource;
import com.example.rdt_pastillas.bd.remote.retrofit.ApiCallback;
import com.example.rdt_pastillas.bd.servicio.txt_servicio.TxtServicioUsuario;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlucosaService {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String TAG = "GlucosaService";

    public static void insertarGlucosa(Context context, long id, int id_usuario ,GlucosaEntity entidad) {

        GlucosaEntity nuevoGlucosa = new GlucosaEntity(id_usuario,id,entidad.getNivel_glucosa());

        UsuarioRemoteDataSource remote = new UsuarioRemoteDataSource();
        remote.insertar_glucosa(context, nuevoGlucosa, new ApiCallback<ServerResponse>() {
            @Override
            public void onSuccess(ServerResponse response) {
                AppDataBaseControl.databaseWriteExecutor.execute(() -> {
                    GlucosaLocalDao dao = AppDataBaseControl.getDatabase(context.getApplicationContext()).glucosa_interfaz();
                    dao.actualizarEstado(id);
                    // El TxtServicioUsuario también puede ir aquí, ya que es una operación de I/O.
                    TxtServicioUsuario.ActualizarEstadoEnTxt(id);
                    Log.d(TAG, "Sincronización exitosa con el servidor remoto");
                });
            }
            public void onError(String errorMessage) {
                Log.d(TAG, errorMessage);
            }
            @Override
            public void onFailure(String failureMessage) {
                Log.d(TAG, failureMessage);
            }
        });
    }

    public static void editarGlucosa(Context context,GlucosaEntity entidad) {
        UsuarioRemoteDataSource remote = new UsuarioRemoteDataSource();

        // 2. Sincronizar la edición con el servidor remoto usando Retrofit
        remote.editar_glucosa(context, entidad, new ApiCallback<ServerResponse>() {
            @Override
            public void onSuccess(ServerResponse response) {
                AppDataBaseControl.databaseWriteExecutor.execute(() -> {
                    GlucosaLocalDao dao = AppDataBaseControl.getDatabase(context.getApplicationContext()).glucosa_interfaz();
                    dao.actualizarEstado(entidad.getId_glucosa());
                    // El TxtServicioUsuario también puede ir aquí, ya que es una operación de I/O.
                    TxtServicioUsuario.ActualizarEstadoEnTxt(entidad.getId_glucosa());

                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("GlucosaRepository", "Error de API al editar glucosa: " + errorMessage);
            }

            @Override
            public void onFailure(String failureMessage) {
                Log.e("GlucosaRepository", "Fallo de red al editar glucosa: " + failureMessage);
            }
        });
    }

}
