package com.example.rdt_pastillas.retrofit.model.response;

import com.google.gson.annotations.SerializedName;

public class ServerResponse
{
    private boolean success;

    @SerializedName("userMssg") // <- Muy importante
    private String mensaje;

    public boolean isSuccess() {
        return success;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
