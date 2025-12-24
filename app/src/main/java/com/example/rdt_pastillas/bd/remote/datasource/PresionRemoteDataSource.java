package com.example.rdt_pastillas.bd.remote.datasource;

import android.content.Context;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.presion_entity.PresionEntity;
import com.example.rdt_pastillas.Modelo.response.ServerResponse;
import com.example.rdt_pastillas.bd.remote.api.ApiService;
import com.example.rdt_pastillas.bd.remote.retrofit.ApiCallback;
import com.example.rdt_pastillas.bd.remote.retrofit.ApiHelper;
import com.example.rdt_pastillas.bd.remote.retrofit.RetrofitClient;

import retrofit2.Call;

public class PresionRemoteDataSource {
    private final ApiService apiService;

    public PresionRemoteDataSource() {
        this.apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    public void insertar_presion(Context context, PresionEntity presion, ApiCallback<ServerResponse> callback) {
        // 1. Prepara la llamada específica para insertar un usuario
        Call<ServerResponse> call = apiService.InsertarPresion(presion);

        // 2. Delega la ejecución y el manejo del ProgressDialog al método genérico
        ApiHelper.execute(context, call, callback);
    }

}
