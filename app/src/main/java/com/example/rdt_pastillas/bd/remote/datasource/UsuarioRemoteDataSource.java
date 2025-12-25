package com.example.rdt_pastillas.bd.remote.datasource;

import android.content.Context;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.presion_entity.PresionEntity;
import com.example.rdt_pastillas.Modelo.response.LoginResponse;
import com.example.rdt_pastillas.bd.remote.retrofit.ApiCallback;
import com.example.rdt_pastillas.bd.remote.retrofit.ApiHelper;
import com.example.rdt_pastillas.bd.remote.retrofit.RetrofitClient;
import com.example.rdt_pastillas.bd.remote.api.ApiService;
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.usuario_entity.UsuarioEntity;
import com.example.rdt_pastillas.Modelo.response.ServerResponse;


import retrofit2.Call;
public class UsuarioRemoteDataSource {

    private final ApiService apiService;

    public UsuarioRemoteDataSource() {
        this.apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    public void insertar_usuario(Context context, UsuarioEntity usuario, ApiCallback<ServerResponse> callback) {
        // 1. Prepara la llamada específica para insertar un usuario
        Call<ServerResponse> call = apiService.InsertarUsuario(usuario);

        // 2. Delega la ejecución y el manejo del ProgressDialog al método genérico
        ApiHelper.execute(context, call, callback);
    }

    public void login(Context context, String usuario, String contrasena, ApiCallback<LoginResponse> callback) {
        // Creamos un objeto entidad solo para enviar las credenciales
        UsuarioEntity body = new UsuarioEntity(usuario, contrasena);

        Call<LoginResponse> call = apiService.loginUsuario(body);
        ApiHelper.execute(context, call, callback);
    }

}
