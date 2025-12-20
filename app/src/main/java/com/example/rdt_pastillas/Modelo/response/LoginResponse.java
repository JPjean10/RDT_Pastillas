package com.example.rdt_pastillas.Modelo.response;


import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.usuario_entity.UsuarioEntity;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("userMsg")
    private String userMsg;

    @SerializedName("data")
    private List<UsuarioEntity> data;

    public boolean isStatus() { return status; }
    public String getUserMsg() { return userMsg; }
    public List<UsuarioEntity> getData() { return data; }
}