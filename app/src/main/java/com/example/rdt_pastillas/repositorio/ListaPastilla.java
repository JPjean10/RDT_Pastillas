package com.example.rdt_pastillas.repositorio;

import com.example.rdt_pastillas.Modelo.PastillasModel;

import java.util.ArrayList;
import java.util.List;

public class ListaPastilla {

    public static List<PastillasModel> getPastillas() {
        // Inicializa la lista
        ArrayList<PastillasModel> pastillas = new ArrayList<>();


        //en la mañana
        pastillas.add(new PastillasModel(
                1,
                "insulina",
                "06:30 AM"));
        pastillas.add(new PastillasModel(
                2,
                "Alprazolam",
                "07:40 AM"));
        pastillas.add(new PastillasModel(
                3,
                "Carvedilol 1/2",
                "07:40 AM"));
        pastillas.add(new PastillasModel(
                4,
                "Gabapentina",
                "07:40 AM"));
        pastillas.add(new PastillasModel(
                5,
                "Metformina",
                "07:40 AM"));
        // en tarde
        pastillas.add(new PastillasModel(
                6,
                "Metformina",
                "01:30 PM"));
        pastillas.add(new PastillasModel(
                7,
                "Dapagliflozina",
                "01:30 PM"));
        // en la noche
        pastillas.add(new PastillasModel(
                8,
                "insulina",
                "06:30 PM"));
        pastillas.add(new PastillasModel(
                9,
                "Alprazolam",
                "07:40 PM"));
        pastillas.add(new PastillasModel(
                10,
                "Carvedilol 1/2 pastillas",
                "07:40 PM"));
        pastillas.add(new PastillasModel(
                11,
                "Gabapentina",
                "07:40 PM"));
        pastillas.add(new PastillasModel(
                12,
                "Amlodipina",
                "07:40 PM"));
        pastillas.add(new PastillasModel(
                13,
                "Atorvastatina",
                "07:40 PM"));
        pastillas.add(new PastillasModel(
                14,
                "Metformina",
                "07:40 PM"));
        // pruevas

        return pastillas;
    }

}
