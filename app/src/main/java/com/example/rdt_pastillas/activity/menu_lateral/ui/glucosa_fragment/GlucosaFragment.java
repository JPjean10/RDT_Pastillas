package com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.Adapter.GlucosaAdapter;
import com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.ViewModel.GlucosaViewModel;
import com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.componentes.dailog.GlucosaInsertDailog;
import com.example.rdt_pastillas.basedata.servicio.glucosa_bd.GlucosaServicio;
import com.example.rdt_pastillas.util.dailog.AnioMesDialog;
import java.util.Locale;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GlucosaFragment extends Fragment implements
        GlucosaInsertDailog.insertOnClickedDailog {

    private String fechaGuardada;
    //variables del activity
    EditText btn_fecha;
    Button btn_agregar;

    private GlucosaViewModel glucosaViewModel;
    private GlucosaAdapter adapter;
    private RecyclerView recyclerView;

    //-------------------
    GlucosaServicio servicio;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_glucosa, container, false);

        servicio = new GlucosaServicio(requireActivity().getApplication());

        btn_fecha = view.findViewById(R.id.btn_seleccionar_fecha);
        btn_agregar = view.findViewById(R.id.btn_agregar);

        recyclerView = view.findViewById(R.id.recycler_view_glucosa);

        // Configurar RecyclerView
        setupRecyclerView();

        // Inicializar ViewModel
        glucosaViewModel = new ViewModelProvider(this).get(GlucosaViewModel.class);

        // Observar los datos
        observarDatos();

//        establecerFechaInicial();

        /*feha_inicial();*/

        btn_fecha.setOnClickListener(v -> {
            //_________________________________________________________
            AbrirDailogFecha(btn_fecha, () -> {
            });
            //_________________________________________________________
        });

        btn_agregar.setOnClickListener(v -> {
            GlucosaInsertDailog dialog = new GlucosaInsertDailog(getContext(), GlucosaFragment.this);
            dialog.show();
        });
        // =====================================================================

//______________no tocar______________________________________________
        return view;
    }
    //______________no tocar______________________________________________
    //________________________________________________________________________________
    // --- NUEVOS MÉTODOS ---
    private void setupRecyclerView() {
        adapter = new GlucosaAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void observarDatos() {
        glucosaViewModel.getListaGlucosaAgrupada().observe(getViewLifecycleOwner(), listaAgrupada -> {
            // Cada vez que los datos cambian en la BD, este código se ejecuta.
            if (listaAgrupada != null && !listaAgrupada.isEmpty()) {
                adapter.submitList(listaAgrupada);
            } else {
                // Opcional: mostrar un mensaje de que no hay datos
            }
        });
    }

    private void establecerFechaInicial() {
        // Formato para guardar y filtrar (ej: "2025/11")
        SimpleDateFormat formatoGuardado = new SimpleDateFormat("yyyy/MM", Locale.getDefault());
        // Formato para mostrar al usuario (ej: "Noviembre 2025")
        SimpleDateFormat formatoMostrado = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

        Calendar calendario = Calendar.getInstance();

        fechaGuardada = formatoGuardado.format(calendario.getTime());
        String fechaMostrada = formatoMostrado.format(calendario.getTime());

        // Capitalizar la primera letra del mes
        fechaMostrada = fechaMostrada.substring(0, 1).toUpperCase() + fechaMostrada.substring(1);

        btn_fecha.setText(fechaMostrada);

        // Establecer el filtro inicial en el ViewModel
        glucosaViewModel.setFiltroFecha(fechaGuardada);
    }
    // -------------------------
    // --- MÉTODOS DE LA INTERFAZ DEL DIÁLOGO ---
    @Override
    public void insertOnClickedDailog(int nivel_glucosa) {
        servicio.insert(nivel_glucosa);
    }
    //________________________________________________________________________________
    private void AbrirDailogFecha(EditText btnFecha, Runnable metodo){

        Integer initialYear = null;
        if (fechaGuardada != null && fechaGuardada.contains("/")) {
            try {
                initialYear = Integer.parseInt(fechaGuardada.split("/")[0]);
            } catch (NumberFormatException e) {
                initialYear = null; // en caso de error, usamos null para que use el año actual
            }
        }

        AnioMesDialog dialog = new AnioMesDialog((fechaSeleccionada, fechaMostrada) -> {
            this.fechaGuardada = fechaSeleccionada; // Guardar como "2023/04"
            btnFecha.setText(fechaMostrada);


            metodo.run();
        }, initialYear);
        dialog.show(getFragmentManager(), "AnioMesDialog");
    }
    //____________________________________________________________________________________________________________
//______________________no tocar______________________________________________
}
