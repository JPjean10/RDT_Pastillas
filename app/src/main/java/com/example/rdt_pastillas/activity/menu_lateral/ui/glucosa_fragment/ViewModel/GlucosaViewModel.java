package com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.rdt_pastillas.bd.local.dao.GlucosaLocalDao;
import com.example.rdt_pastillas.bd.local.database.AppDataBaseControl;
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaDia;
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GlucosaViewModel extends AndroidViewModel {

    private final GlucosaLocalDao glucosaInterfaz;
    private final MutableLiveData<String> filtroFecha = new MutableLiveData<>(); // Formato "yyyy/MM"
    private final LiveData<List<GlucosaDia>> listaGlucosaAgrupada;

    public GlucosaViewModel(@NonNull Application application) {
        super(application);
        glucosaInterfaz = AppDataBaseControl.getDatabase(application).glucosa_interfaz();

        // 1. Se usa switchMap para reaccionar a los cambios en 'filtroFecha'.
        // Cada vez que 'filtroFecha' cambia, se ejecuta la nueva consulta a la BD.
        LiveData<List<GlucosaEntity>> listaFiltradaDesdeBD = Transformations.switchMap(filtroFecha, fecha -> {
            if (fecha == null || fecha.isEmpty()) {
                // Si no hay filtro, devuelve una lista vacía para evitar errores.
                MutableLiveData<List<GlucosaEntity>> emptyList = new MutableLiveData<>();
                emptyList.setValue(new ArrayList<>());
                return emptyList;
            }
            // Llama a la consulta que ahora ordena ASC (del más antiguo al más reciente)
            return glucosaInterfaz.getGlucosaFiltradaPorMes(fecha);
        });

        // 2. La lógica de agrupación se aplica sobre la lista que ya viene en el orden de inserción (ASC).
        listaGlucosaAgrupada = Transformations.map(listaFiltradaDesdeBD, this::agruparGlucosaPorDia);
    }

    /**
     * El Fragment llama a este método para pasar el string con la fecha.
     */
    public void setFiltroFecha(String fecha) { // Recibe "yyyy/MM"
        filtroFecha.setValue(fecha);
    }

    /**
     * El Fragment observa este LiveData para obtener la lista final para el RecyclerView.
     */
    public LiveData<List<GlucosaDia>> getListaGlucosaAgrupada() {
        return listaGlucosaAgrupada;
    }

    /**
     * Agrupa una lista (ya filtrada y en orden ASCENDENTE) en objetos GlucosaDia,
     * con un máximo de 2 mediciones por tarjeta, y luego invierte el resultado final
     * para la visualización.
     * @param mediciones La lista de mediciones, ordenada de más antiguo a más reciente.
     * @return Una lista de objetos GlucosaDia, invertida para mostrar los más recientes arriba.
     */
    private List<GlucosaDia> agruparGlucosaPorDia(List<GlucosaEntity> mediciones) {
        if (mediciones == null || mediciones.isEmpty()) {
            return new ArrayList<>();
        }

        // Usamos LinkedHashMap para mantener el orden de inserción (por fecha)
        Map<String, List<GlucosaEntity>> medicionesPorDia = new LinkedHashMap<>();

        // 1. Agrupar todas las mediciones por su día (extrayendo "yyyy-MM-dd" del string)
        for (GlucosaEntity medicion : mediciones) {
            String fechaKey = medicion.getFecha_hora_creacion().substring(0, 10);
            medicionesPorDia.computeIfAbsent(fechaKey, k -> new ArrayList<>()).add(medicion);
        }

        List<GlucosaDia> listaAgrupada = new ArrayList<>();

        // 2. Convertir el mapa en una lista de objetos GlucosaDia
        for (Map.Entry<String, List<GlucosaEntity>> entry : medicionesPorDia.entrySet()) {
            // El orden de las mediciones dentro del día ya es ASC (viene así de la BD)
            GlucosaDia dia = new GlucosaDia(entry.getKey(), entry.getValue());
            listaAgrupada.add(dia);
        }

        // 3. Invertir la lista final para que los días más recientes aparezcan arriba
        Collections.reverse(listaAgrupada);

        return listaAgrupada;
    }
}
