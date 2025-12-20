package com.example.rdt_pastillas.bd.remote.api;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.usuario_entity.UsuarioEntity;
import com.example.rdt_pastillas.Modelo.response.LoginResponse;
import com.example.rdt_pastillas.Modelo.response.ServerResponse;
import com.example.rdt_pastillas.util.consts.ApiConst;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ApiService {

    @POST(ApiConst.USUARIO)
    Call<ServerResponse> InsertarUsuario(@Body UsuarioEntity usuario);

    @POST(ApiConst.GLUCOSA)
    Call<ServerResponse> InsertarGlucosa(@Body GlucosaEntity glucosa);

    @POST(ApiConst.GLUCOSA + ApiConst.SINCRONIZAR)
    Call<ServerResponse> sincronizarGlucosa(@Body GlucosaEntity glucosa);

    @PUT(ApiConst.GLUCOSA)
    Call<ServerResponse> EditarGlucosa(@Body GlucosaEntity glucosa);

    @PUT(ApiConst.GLUCOSA + ApiConst.SINCRONIZAR) // Se usa PUT basado en tu última imagen
    Call<ServerResponse> sincronizarGlucosaAc(@Body GlucosaEntity glucosa);

    @POST(ApiConst.USUARIO + ApiConst.LOGIN)
    Call<LoginResponse> loginUsuario(@Body UsuarioEntity usuario);

}
