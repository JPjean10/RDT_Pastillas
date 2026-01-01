package com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.presion_entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "PresionEntity")
public class PresionEntity {

    private long id_usuario;
    @PrimaryKey(autoGenerate = true)
    private long id_presion;
    private int sys;
    private int dia;
    private int pul;
    private String fecha_hora_creacion;
    private boolean estado;

    public PresionEntity(long id_usuario, int sys, int dia, int pul) {
        this.id_usuario = id_usuario;
        this.sys = sys;
        this.dia = dia;
        this.pul = pul;
        this.fecha_hora_creacion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        this.estado = false;
    }

    @Ignore
    public PresionEntity(long id_usuario,long id_presion, int sys, int dia, int pul) {
        this.id_usuario = id_usuario;
        this.id_presion = id_presion;
        this.sys = sys;
        this.dia = dia;
        this.pul = pul;
        this.fecha_hora_creacion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        this.estado = true;
    }

    @Ignore
    public PresionEntity(long id_usuario, int sys, int dia, int pul, String fecha_hora_creacion, boolean estado) {
        this.id_usuario = id_usuario;
        this.sys = sys;
        this.dia = dia;
        this.pul = pul;
        this.fecha_hora_creacion = fecha_hora_creacion;
        this.estado = estado;
    }

    public long getId_usuario() { return id_usuario; }
    public void setId_usuario(long id_usuario) { this.id_usuario = id_usuario; }
    public long getId_presion() { return id_presion; }
    public void setId_presion(long id_presion) { this.id_presion = id_presion; }
    public int getSys() { return sys; }
    public void setSys(int sys) { this.sys = sys; }
    public int getDia() { return dia; }
    public void setDia(int dia) { this.dia = dia; }
    public int getPul() { return pul; }
    public void setPul(int pul) { this.pul = pul; }
    public String getFecha_hora_creacion() { return fecha_hora_creacion; }
    public void setFecha_hora_creacion(String fecha_hora_creacion) { this.fecha_hora_creacion = fecha_hora_creacion; }
    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }

}
