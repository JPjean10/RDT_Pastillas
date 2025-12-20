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
     * @param listaParaAgrupar La lista de mediciones, ordenada de más antiguo a más reciente.
     * @return Una lista de objetos GlucosaDia, invertida para mostrar los más recientes arriba.
     */
    private List<GlucosaDia> agruparGlucosaPorDia(List<GlucosaEntity> listaParaAgrupar) {
        if (listaParaAgrupar == null || listaParaAgrupar.isEmpty()) {
            return new ArrayList<>();
        }

        List<GlucosaDia> listaFinal = new ArrayList<>();
        SimpleDateFormat formatoClave = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        // Mapa para agrupar mediciones por día
        Map<String, List<GlucosaEntity>> medicionesPorDia = new LinkedHashMap<>();
        for (GlucosaEntity medicion : listaParaAgrupar) {
            try {
                Date fechaCompleta = formatoEntrada.parse(medicion.getFecha_hora_creacion());
                String fechaKey = formatoClave.format(fechaCompleta);
                medicionesPorDia.putIfAbsent(fechaKey, new ArrayList<>());
                medicionesPorDia.get(fechaKey).add(medicion);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Construir las tarjetas para cada día
        for (Map.Entry<String, List<GlucosaEntity>> entry : medicionesPorDia.entrySet()) {
            String fechaKey = entry.getKey();
            List<GlucosaEntity> medicionesDelDia = entry.getValue(); // Ej: [99, 96, 100]

            // Iterar sobre la lista ASCENDENTE, tomándolas de 2 en 2
            for (int i = 0; i < medicionesDelDia.size(); i += 2) {
                GlucosaDia grupo = new GlucosaDia(fechaKey);

                // Añadir el primer elemento del par (el más antiguo del par). Ej: 99
                grupo.addMedicion(medicionesDelDia.get(i));

                // Si existe un segundo elemento, añadirlo. Ej: 96
                if (i + 1 < medicionesDelDia.size()) {
                    grupo.addMedicion(medicionesDelDia.get(i + 1));
                }
                listaFinal.add(grupo);
            }
        }

        Collections.reverse(listaFinal);

        return listaFinal;
    }
}
