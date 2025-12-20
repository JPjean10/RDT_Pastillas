package com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.Adapter.GlucosaAdapter;
import com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.ViewModel.GlucosaViewModel;
import com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.componentes.dailog.GlucosaEditDailog;
import com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.componentes.dailog.GlucosaInsertDailog;
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.bd.repository.GlucosaRepository;
import com.example.rdt_pastillas.util.dailog.AnioMesDialog;

import java.util.ArrayList; // Asegúrate de tener este import  OnEditClickLister,
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GlucosaFragment extends Fragment implements
        GlucosaAdapter.EdiOnClickedAdapter,
        GlucosaInsertDailog.insertOnClickedDailog,
        GlucosaEditDailog.EditOnClickedDailog {

    private String fechaGuardada; // Formato "yyyy/MM"
    private EditText btn_fecha;
    private Button btn_agregar;

    private GlucosaViewModel glucosaViewModel;
    private GlucosaAdapter adapter;
    private RecyclerView recyclerView;

    private GlucosaRepository servicio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_glucosa, container, false);

        servicio = new GlucosaRepository(requireActivity().getApplication());

        btn_fecha = view.findViewById(R.id.btn_seleccionar_fecha);
        btn_agregar = view.findViewById(R.id.btn_agregar);
        recyclerView = view.findViewById(R.id.recycler_view_glucosa);

        // 1. Configurar el RecyclerView y su adaptador
        setupRecyclerView();

        // 2. Inicializar el ViewModel
        glucosaViewModel = new ViewModelProvider(this).get(GlucosaViewModel.class);

        // 3. Observar los datos del ViewModel
        observarDatos();

        // 4. DESCOMENTAR ESTA LÍNEA: Es crucial para el funcionamiento inicial
        establecerFechaInicial();

        // 5. Configurar listener para el botón de fecha
        btn_fecha.setOnClickListener(v -> AbrirDailogFecha());

        // 6. Configurar listener para el botón de agregar
        btn_agregar.setOnClickListener(v -> {
            GlucosaInsertDailog dialog = new GlucosaInsertDailog(getContext(), GlucosaFragment.this);
            dialog.show();
        });

        return view;
    }
    // MÉTODO_______________________________________________________________________________________
    private void setupRecyclerView() {
        adapter = new GlucosaAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void observarDatos() {
        glucosaViewModel.getListaGlucosaAgrupada().observe(getViewLifecycleOwner(), listaAgrupada -> {
            // Este bloque ahora maneja todos los casos
            if (listaAgrupada != null && !listaAgrupada.isEmpty()) {
                adapter.submitList(listaAgrupada);
            } else {
                // ¡AQUÍ ESTÁ LA CORRECCIÓN CLAVE!
                // Si la lista es nula o vacía, le pasamos una lista vacía al adaptador para que se limpie.
                adapter.submitList(new ArrayList<>());
            }
        });
    }

    private void establecerFechaInicial() {
        SimpleDateFormat formatoGuardado = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat formatoMostrado = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        Calendar calendario = Calendar.getInstance();

        fechaGuardada = formatoGuardado.format(calendario.getTime());
        String fechaMostrada = formatoMostrado.format(calendario.getTime());

        // Capitalizar la primera letra del mes
        if (fechaMostrada.length() > 0) {
            fechaMostrada = fechaMostrada.substring(0, 1).toUpperCase() + fechaMostrada.substring(1);
        }

        btn_fecha.setText(fechaMostrada);


        // Llama al método del ViewModel para aplicar el filtro inicial
        glucosaViewModel.setFiltroFecha(fechaGuardada);
    }
   //_______________________________________________________________________________________________
   // dailog________________________________________________________________________________________
    @Override
    public void insertOnClickedDailog(int nivel_glucosa) {
        servicio.insert(nivel_glucosa);
    }

    @Override
    public void EditOnClickedDailog(GlucosaEntity glucosa,int nivel_glucosa) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        // 2. Crea el String con la fecha actual formateada.
        String fechaFormateada = sdf.format(new Date());

        GlucosaEntity glucosaParaActualizar = glucosa;

        // 2. Actualiza solo los campos que cambiaron.
        glucosaParaActualizar.setNivel_glucosa(nivel_glucosa);
        glucosaParaActualizar.setFecha_hora_creacion(fechaFormateada);
        glucosaParaActualizar.setEstado(false);

        servicio.edit(glucosaParaActualizar);

    }

    private void AbrirDailogFecha() {
        Integer initialYear = null;
        if (fechaGuardada != null && fechaGuardada.contains("-")) {
            try {
                initialYear = Integer.parseInt(fechaGuardada.split("-")[0]);
            } catch (NumberFormatException e) {
                initialYear = null;
            }
        }

        AnioMesDialog dialog = new AnioMesDialog((fechaSeleccionada, fechaMostrada) -> {
            // 1. Actualizar la UI
            this.fechaGuardada = fechaSeleccionada;
            this.btn_fecha.setText(fechaMostrada);

            // 2. Notificar al ViewModel el nuevo filtro
            glucosaViewModel.setFiltroFecha(fechaSeleccionada);

        }, initialYear);

        dialog.show(getParentFragmentManager(), "AnioMesDialog");
    }
    //______________________________________________________________________________________________
    // adapter______________________________________________________________________________________
    @Override
    public void EdiOnClickedAdapter(GlucosaEntity medicion) {
        GlucosaEditDailog dialog = new GlucosaEditDailog(getContext(), medicion ,GlucosaFragment.this);
        dialog.show();;
    }
    //______________________________________________________________________________________________
}
