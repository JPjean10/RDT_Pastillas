package com.example.rdt_pastillas.retrofit.entity;

import com.google.gson.annotations.SerializedName;

public class UsuarioEntity {

    int id_usuario;
    @SerializedName("usuario")
    String usuario;
    @SerializedName("contrasena")
    String contrasena;
    String fecha_hora_creacion;

    public UsuarioEntity(String usuario, String contrasena){
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    public int getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getFecha_hora_creacion() {
        return fecha_hora_creacion;
    }

    public void setFecha_hora_creacion(String fecha_hora_creacion) {
        this.fecha_hora_creacion = fecha_hora_creacion;
    }
}
