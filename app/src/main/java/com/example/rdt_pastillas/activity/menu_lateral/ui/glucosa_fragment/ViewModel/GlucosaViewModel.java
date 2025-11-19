package com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.ViewModel;


import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.rdt_pastillas.basedata.app_database.glucosa_bd.AppDataBaseGlucosa;
import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaDia;
import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.basedata.interfaz.glucosa_bd.GlucosaInterfaz;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GlucosaViewModel extends AndroidViewModel {

    private final GlucosaInterfaz glucosaInterfaz;
    private final MutableLiveData<String> filtroFecha = new MutableLiveData<>(); // Formato "yyyy/MM"
    private final LiveData<List<GlucosaDia>> listaGlucosaAgrupada;

    public GlucosaViewModel(@NonNull Application application) {
        super(application);
        glucosaInterfaz = AppDataBaseGlucosa.getDatabase(application).glucosa_interfaz();

        // 1. Se usa switchMap para reaccionar a los cambios en 'filtroFecha'.
        // Cada vez que 'filtroFecha' cambia, se ejecuta una nueva consulta a la BD.
        LiveData<List<GlucosaEntity>> listaFiltradaDesdeBD = Transformations.switchMap(filtroFecha, fecha -> {
            if (fecha == null || fecha.isEmpty()) {
                // Si no hay filtro, devuelve una lista vacía para evitar errores.
                MutableLiveData<List<GlucosaEntity>> emptyList = new MutableLiveData<>();
                emptyList.setValue(new ArrayList<>());
                return emptyList;
            }
            // ¡Aquí se usa tu consulta seleccionada!
            return glucosaInterfaz.getGlucosaFiltradaPorMes(fecha);
        });

        // 2. La lógica de agrupación se aplica sobre la lista que ya viene filtrada desde la BD.
        listaGlucosaAgrupada = Transformations.map(listaFiltradaDesdeBD, this::agruparGlucosaPorDia);
    }

    /**
     * Este es el método que el Fragment llamará para pasar el string con la fecha.
     */
    public void setFiltroFecha(String fecha) { // Recibe "yyyy/MM"
        filtroFecha.setValue(fecha);
    }

    /**
     * El Fragment observa este LiveData para obtener la lista final.
     */
    public LiveData<List<GlucosaDia>> getListaGlucosaAgrupada() {
        return listaGlucosaAgrupada;
    }

    /**
     * Agrupa una lista (ya filtrada) en objetos GlucosaDia (hasta 3 mediciones por tarjeta).
     */
    private List<GlucosaDia> agruparGlucosaPorDia(List<GlucosaEntity> listaParaAgrupar) {
        if (listaParaAgrupar == null) {
            return new ArrayList<>();
        }
        Map<String, List<GlucosaDia>> mapaAgrupado = new LinkedHashMap<>();
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault());
        SimpleDateFormat formatoClave = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

        for (GlucosaEntity medicion : listaParaAgrupar) {
            try {
                Date fechaCompleta = formatoEntrada.parse(medicion.getFecha_hora_creacion());
                String fechaKey = formatoClave.format(fechaCompleta);

                mapaAgrupado.putIfAbsent(fechaKey, new ArrayList<>());
                List<GlucosaDia> gruposDelDia = mapaAgrupado.get(fechaKey);
                GlucosaDia grupoActual;
                if (gruposDelDia.isEmpty() || gruposDelDia.get(gruposDelDia.size() - 1).getMedicionesCount() == 2) {
                    grupoActual = new GlucosaDia(fechaKey);
                    gruposDelDia.add(grupoActual);
                } else {
                    grupoActual = gruposDelDia.get(gruposDelDia.size() - 1);
                }
                grupoActual.addMedicion(medicion);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        List<GlucosaDia> listaFinal = new ArrayList<>();
        for (List<GlucosaDia> grupos : mapaAgrupado.values()) {
            listaFinal.addAll(grupos);
        }
        return listaFinal;
    }
}
