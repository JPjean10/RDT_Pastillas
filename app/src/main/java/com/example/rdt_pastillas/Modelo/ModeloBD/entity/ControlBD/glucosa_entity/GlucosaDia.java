package com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity;

import java.util.ArrayList;
import java.util.List;

public class GlucosaDia {

    private String fecha;
    // Usamos una lista para más flexibilidad
    private List<GlucosaEntity> mediciones;

    public GlucosaDia(String fecha) {
        this.fecha = fecha;
        this.mediciones = new ArrayList<>();
    }

    public String getFecha() {
        return fecha;
    }

    public List<GlucosaEntity> getMediciones() {
        return mediciones;
    }

    public void addMedicion(GlucosaEntity medicion) {
        if (mediciones.size() < 2) {
            mediciones.add(medicion);
        }
    }

    public int getMedicionesCount() {
        return mediciones.size();
    }
}
