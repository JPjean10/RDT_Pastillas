package com.example.rdt_pastillas.Modelo;

public class PastillasModel {
    private int id;
    private String nombre;
    private String hora;
    public PastillasModel(int id, String nombre, String hora) {
        this.id = id;
        this.nombre = nombre;
        this.hora = hora;
    }
    public int getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public String getHora() {
        return hora;
    }
}
