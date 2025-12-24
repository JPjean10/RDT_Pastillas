package com.example.rdt_pastillas.bd.remote.retrofit.Servicio;

import android.content.Context;
import android.util.Log;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.presion_entity.PresionEntity;
import com.example.rdt_pastillas.Modelo.response.ServerResponse;
import com.example.rdt_pastillas.bd.local.dao.PresionLocalDao;
import com.example.rdt_pastillas.bd.local.database.AppDataBaseControl;
import com.example.rdt_pastillas.bd.remote.datasource.PresionRemoteDataSource;
import com.example.rdt_pastillas.bd.remote.retrofit.ApiCallback;
import com.example.rdt_pastillas.bd.servicio.txt_servicio.TxtSrvicioPresion;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PresionServicio {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String TAG = "PresionService";

    public static void insertarPresion(Context context, long id, int id_usuario , PresionEntity entidad) {
        PresionEntity nuevaPresion = new PresionEntity(id_usuario,id,entidad.getSys(),entidad.getDia(), entidad.getPul());

        PresionRemoteDataSource remote = new PresionRemoteDataSource();

        remote.insertar_presion(context, nuevaPresion, new ApiCallback<ServerResponse>() {
            @Override
            public void onSuccess(ServerResponse response) {
                AppDataBaseControl.databaseWriteExecutor.execute(() -> {
                    PresionLocalDao dao = AppDataBaseControl.getDatabase(context.getApplicationContext()).presion_interfaz();
                    dao.actualizarEstado(id);
                    // El TxtServicioUsuario también puede ir aquí, ya que es una operación de I/O.
                    TxtSrvicioPresion.ActualizarEstadoEnTxt(id);
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
}
