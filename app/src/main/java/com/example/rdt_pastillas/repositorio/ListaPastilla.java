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
                "alprasolan",
                "07:40 AM"));
        pastillas.add(new PastillasModel(
                3,
                "carrelidol 1/2 pastillas",
                "07:40 AM"));
        pastillas.add(new PastillasModel(
                4,
                "gabapentina",
                "07:40 AM"));
        pastillas.add(new PastillasModel(
                5,
                "meformina",
                "07:40 AM"));
        // en tarde
        pastillas.add(new PastillasModel(
                6,
                "meformina",
                "01:30 PM"));
        pastillas.add(new PastillasModel(
                7,
                "dapaglifozina",
                "01:30 PM"));
        // en la noche
        pastillas.add(new PastillasModel(
                8,
                "insulina",
                "06:30 PM"));
        pastillas.add(new PastillasModel(
                9,
                "aprasolan",
                "07:40 PM"));
        pastillas.add(new PastillasModel(
                10,
                "carrelidol 1/2 pastillas",
                "07:40 PM"));
        pastillas.add(new PastillasModel(
                11,
                "gabapentina",
                "07:40 PM"));
        pastillas.add(new PastillasModel(
                12,
                "almiodipilina",
                "07:40 PM"));
        pastillas.add(new PastillasModel(
                13,
                "atorrastatina",
                "07:40 PM"));
        pastillas.add(new PastillasModel(
                14,
                "meformina",
                "07:40 PM"));

        return pastillas;
    }

}
