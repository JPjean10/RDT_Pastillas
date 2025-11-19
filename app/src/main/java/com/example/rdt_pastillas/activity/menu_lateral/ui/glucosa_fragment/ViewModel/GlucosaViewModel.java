package com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

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
    private final LiveData<List<GlucosaEntity>> lista_glucosa; // Datos originales sin filtrar
    private final MediatorLiveData<List<GlucosaDia>> listaGlucosaAgrupada = new MediatorLiveData<>();
    private final MutableLiveData<String> filtroFecha = new MutableLiveData<>(); // Formato "yyyy/MM"

    public GlucosaViewModel(@NonNull Application application) {
        super(application);
        glucosaInterfaz = AppDataBaseGlucosa.getDatabase(application).glucosa_interfaz();

        // 1. Obtenemos todos los datos de la BD una sola vez.
        lista_glucosa = glucosaInterfaz.getAllGlucosaOrderByDate();

        // 2. El MediatorLiveData observará tanto los datos originales como el filtro.
        listaGlucosaAgrupada.addSource(lista_glucosa, glucosaEntities -> {
            // Cuando los datos de la BD cambian, volvemos a filtrar y agrupar.
            List<GlucosaEntity> listaFiltrada = filtrarLista(glucosaEntities, filtroFecha.getValue());
            listaGlucosaAgrupada.setValue(agruparGlucosaPorDia(listaFiltrada));
        });

        listaGlucosaAgrupada.addSource(filtroFecha, fecha -> {
            // Cuando el filtro cambia, volvemos a filtrar y agrupar.
            List<GlucosaEntity> listaFiltrada = filtrarLista(lista_glucosa.getValue(), fecha);
            listaGlucosaAgrupada.setValue(agruparGlucosaPorDia(listaFiltrada));
        });
    }

    // El Fragment llamará a este método para pasar la nueva fecha seleccionada.
    public void setFiltroFecha(String fecha) { // Recibe "yyyy/MM"
        filtroFecha.setValue(fecha);
    }

    // El Fragment observará este LiveData para obtener la lista final.
    public LiveData<List<GlucosaDia>> getListaGlucosaAgrupada() {
        return listaGlucosaAgrupada;
    }

    // Nueva función para filtrar la lista en memoria
    private List<GlucosaEntity> filtrarLista(List<GlucosaEntity> listaOriginal, String filtro) {
        if (listaOriginal == null) {
            return new ArrayList<>();
        }
        if (filtro == null || filtro.isEmpty()) {
            return listaOriginal; // Si no hay filtro, devuelve la lista completa.
        }

        List<GlucosaEntity> listaFiltrada = new ArrayList<>();
        for (GlucosaEntity medicion : listaOriginal) {
            // Extraemos "yyyy/MM" de "yyyy/MM/dd hh:mm a"
            if (medicion.getFecha_hora_creacion() != null && medicion.getFecha_hora_creacion().startsWith(filtro)) {
                listaFiltrada.add(medicion);
            }
        }
        return listaFiltrada;
    }

    // Tu método de agrupación se mantiene igual, solo le pasamos la lista ya filtrada.
    private List<GlucosaDia> agruparGlucosaPorDia(List<GlucosaEntity> listaParaAgrupar) {
        if (listaParaAgrupar == null) {
            return new ArrayList<>();
        }

        // LinkedHashMap mantiene el orden de inserción (días más recientes primero).
        Map<String, List<GlucosaDia>> mapaAgrupado = new LinkedHashMap<>();
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault());
        SimpleDateFormat formatoClave = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

        for (GlucosaEntity medicion : listaParaAgrupar) {
            try {
                Date fechaCompleta = formatoEntrada.parse(medicion.getFecha_hora_creacion());
                String fechaKey = formatoClave.format(fechaCompleta);

                // Obtener la lista de grupos para esta fecha, o crearla si no existe.
                mapaAgrupado.putIfAbsent(fechaKey, new ArrayList<>());
                List<GlucosaDia> gruposDelDia = mapaAgrupado.get(fechaKey);

                // Buscar un grupo existente con espacio o crear uno nuevo.
                GlucosaDia grupoActual;
                if (gruposDelDia.isEmpty() || gruposDelDia.get(gruposDelDia.size() - 1).getMedicionesCount() == 3) {
                    // Si no hay grupos o el último está lleno, crear uno nuevo.
                    grupoActual = new GlucosaDia(fechaKey);
                    gruposDelDia.add(grupoActual);
                } else {
                    // Usar el último grupo que todavía tiene espacio.
                    grupoActual = gruposDelDia.get(gruposDelDia.size() - 1);
                }
                grupoActual.addMedicion(medicion);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Aplanar la lista de listas en una sola lista de GlucosaDia
        List<GlucosaDia> listaFinal = new ArrayList<>();
        for (List<GlucosaDia> grupos : mapaAgrupado.values()) {
            listaFinal.addAll(grupos);
        }
        return listaFinal;
    }
}
