package com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "GlucosaEntity")
public class GlucosaEntity {

    @PrimaryKey(autoGenerate = true)
    private Integer id_glucosa;

    private int nivel_glucosa;

    private String fecha_hora_creacion;

    private boolean estado;

    public GlucosaEntity(int nivel_glucosa, String fecha_hora_creacion, boolean estado) {
        this.nivel_glucosa = nivel_glucosa;
        this.fecha_hora_creacion = fecha_hora_creacion;
        this.estado = estado;
    }

    public Integer getId_glucosa() {
        return id_glucosa;
    }

    public void setId_glucosa(Integer id_glucosa) {
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
