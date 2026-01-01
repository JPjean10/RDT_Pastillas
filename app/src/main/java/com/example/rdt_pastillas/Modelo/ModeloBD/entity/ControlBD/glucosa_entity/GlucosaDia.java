package com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity;

import java.util.ArrayList;
import java.util.List;

public class GlucosaDia {

    private String fecha;
    private List<GlucosaEntity> mediciones;

    // Solo necesitas este constructor
    public GlucosaDia(String fecha, List<GlucosaEntity> mediciones) {
        this.fecha = fecha;
        this.mediciones = mediciones;
    }

    // --- Métodos Getters ---
    public String getFecha() {
        return fecha;
    }

    public List<GlucosaEntity> getMediciones() {
        return mediciones;
    }
}
