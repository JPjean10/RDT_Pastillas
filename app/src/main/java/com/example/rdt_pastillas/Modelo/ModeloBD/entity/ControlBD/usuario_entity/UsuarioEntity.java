package com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.usuario_entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "UsuarioEntity")
public class UsuarioEntity {

    @PrimaryKey
    @SerializedName("id_usuario")
    private int id_usuario;
    @SerializedName("usuario")
    private String usuario;
    @SerializedName("contrasena")
    private String contrasena;
    @SerializedName("nombre")
    private String nombre;
    @ColumnInfo(name = "fecha_hora_creacion")
    @SerializedName("fechaHoraCreacion")
    String fecha_hora_creacion;

    public UsuarioEntity() {
    }

    @Ignore
    public UsuarioEntity(String usuario, String contrasena, String nombre){
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.nombre = nombre;
    }

    @Ignore
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

    public String getNombre() {return nombre;}

    public void setNombre(String nombre) {this.nombre = nombre;}

    public String getFecha_hora_creacion() {
        return fecha_hora_creacion;
    }

    public void setFecha_hora_creacion(String fecha_hora_creacion) {
        this.fecha_hora_creacion = fecha_hora_creacion;
    }
}
