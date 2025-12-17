package com.example.rdt_pastillas.retrofit.dao;

import android.content.Context;

import com.example.rdt_pastillas.retrofit.ApiCallback;
import com.example.rdt_pastillas.retrofit.Helper.ApiHelper;
import com.example.rdt_pastillas.retrofit.RetrofitClient;
import com.example.rdt_pastillas.retrofit.api_service.ApiService;
import com.example.rdt_pastillas.retrofit.entity.UsuarioEntity;
import com.example.rdt_pastillas.retrofit.model.response.ServerResponse;


import retrofit2.Call;
public class UsuarioDao {

    private final ApiService apiService;

    public UsuarioDao() {
        this.apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    public void insertar_usuario(Context context, UsuarioEntity usuario, ApiCallback<ServerResponse> callback) {
        // 1. Prepara la llamada específica para insertar un usuario
        Call<ServerResponse> call = apiService.InsertarUsuario(usuario);

        // 2. Delega la ejecución y el manejo del ProgressDialog al método genérico
        ApiHelper.execute(context, call, callback);
    }
}
