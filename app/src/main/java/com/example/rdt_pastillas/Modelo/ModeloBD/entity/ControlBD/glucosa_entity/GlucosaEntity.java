package com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "GlucosaEntity")
public class GlucosaEntity {

    private long id_usuario;
    @PrimaryKey(autoGenerate = true)
    private long id_glucosa;
    private int nivel_glucosa;
    private String fecha_hora_creacion;
    private boolean estado;


    @Ignore
    public GlucosaEntity(long id_usuario,long id_glucosa,int nivel_glucosa) {
        this.id_usuario = id_usuario;
        this.id_glucosa = id_glucosa;
        this.nivel_glucosa = nivel_glucosa;
    }

    public GlucosaEntity(long id_usuario,int nivel_glucosa, String fecha_hora_creacion, boolean estado) {
        this.id_usuario = id_usuario;
        this.nivel_glucosa = nivel_glucosa;
        this.fecha_hora_creacion = fecha_hora_creacion;
        this.estado = estado;
    }

    public long getId_glucosa() {return id_glucosa;}

    public long getId_usuario() {return id_usuario;}

    public void setId_usuario(long id_usuario) {this.id_usuario = id_usuario;}

    public void setId_glucosa(long id_glucosa) {
        this.id_glucosa = id_glucosa;
    }

    public int getNivel_glucosa() {
        return nivel_glucosa;
    }

    public void setNivel_glucosa(int nivel_glucosa) {
        this.nivel_glucosa = nivel_glucosa;
    }

    public String getFecha_hora_creacion() {
        return fecha_hora_creacion;
    }

    public void setFecha_hora_creacion(String fecha_hora_creacion) {
        this.fecha_hora_creacion = fecha_hora_creacion;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

}
