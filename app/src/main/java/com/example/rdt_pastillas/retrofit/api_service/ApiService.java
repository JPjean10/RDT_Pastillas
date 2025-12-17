package com.example.rdt_pastillas.retrofit.api_service;

import com.example.rdt_pastillas.retrofit.entity.UsuarioEntity;
import com.example.rdt_pastillas.retrofit.model.response.ServerResponse;
import com.example.rdt_pastillas.util.consts.ApiConst;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST(ApiConst.USUARIO)
    Call<ServerResponse> InsertarUsuario(@Body UsuarioEntity usuario);
}
