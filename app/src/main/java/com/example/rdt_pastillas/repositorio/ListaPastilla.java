package com.example.rdt_pastillas.repositorio;

import com.example.rdt_pastillas.Modelo.PastillasModel;

import java.util.ArrayList;
import java.util.List;

public class ListaPastilla {

    public static List<PastillasModel> getPastillas() {
        // Inicializa la lista
        ArrayList<PastillasModel> pastillas = new ArrayList<>();


        pastillas.add(new PastillasModel(
                1,
                "Paracetamol",
                "10:52 AM"));
        pastillas.add(new PastillasModel(
                2,
                "Paracetamol",
                "10:53 AM"));
/*
        pastillas.add(new PastillasModel(2, "Ibuprofeno", "12:30 PM"));
        pastillas.add(new PastillasModel(3, "Vitamina C", "09:00 AM"));
        pastillas.add(new PastillasModel(4, "Amoxicilina", "10:00 PM"));
        pastillas.add(new PastillasModel(5, "Omeprazol", "07:00 AM"));
        pastillas.add(new PastillasModel(6, "Loratadina", "Cada 24 horas"));
        pastillas.add(new PastillasModel(7, "Aspirina", "Después de comer"));
        pastillas.add(new PastillasModel(8, "Complejo B", "En ayunas"));
*/

        return pastillas;
    }

}
