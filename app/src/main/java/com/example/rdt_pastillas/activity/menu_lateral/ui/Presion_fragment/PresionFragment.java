package com.example.rdt_pastillas.activity.menu_lateral.ui.Presion_fragment;

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
import androidx.recyclerview.widget.RecyclerView;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.presion_entity.PresionEntity;
import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.activity.menu_lateral.ui.Presion_fragment.Adapter.PresionAdapter;
import com.example.rdt_pastillas.activity.menu_lateral.ui.Presion_fragment.componentes.PresionEditDailog;
import com.example.rdt_pastillas.activity.menu_lateral.ui.Presion_fragment.componentes.PresionInsertDailog;
import com.example.rdt_pastillas.bd.repository.PresionRepository;
import com.example.rdt_pastillas.util.dailog.AnioMesDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PresionFragment extends Fragment implements
        PresionAdapter.OnItemClickListener,
        PresionInsertDailog.insertOnClickedDailog,
        PresionEditDailog.EditOnClickedDailog {

    private String fechaGuardada; // Formato "yyyy/MM"
    private EditText btn_fecha;
    private Button btn_agregar;
    private RecyclerView recyclerView;
    private PresionRepository servicio;
    private PresionAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_presion, container, false);

        servicio = new PresionRepository(requireActivity().getApplication());

        btn_fecha = view.findViewById(R.id.btn_seleccionar_fecha);
        btn_agregar = view.findViewById(R.id.btn_agregar);
        recyclerView = view.findViewById(R.id.recycler_view_presion);

        establecerFechaInicial();

        btn_fecha.setOnClickListener(v -> AbrirDailogFecha());

        btn_agregar.setOnClickListener(v ->{
            PresionInsertDailog dialog = new PresionInsertDailog(getContext(), PresionFragment.this);
            dialog.show();
        });

        adapter = new PresionAdapter();
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);

        return view;
    }
    // MÉTODO_______________________________________________________________________________________
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
        cargarDatos(fechaGuardada);
    }

    private void cargarDatos(String fechaFiltro) {
        // Observamos el LiveData del repositorio
        // Nota: El formato en BD es "yyyy-MM-dd...", así que filtramos por "yyyy-MM"
        // Asegúrate de que tu DAO usa LIKE :filtro || '%' correctamente
        servicio.obtenerPresionPorMes(fechaFiltro).observe(getViewLifecycleOwner(), lista -> {
            adapter.setListaPresion(lista);
        });
    }

    //_______________________________________________________________________________________________
    // dailog________________________________________________________________________________________
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
            cargarDatos(fechaGuardada);
        }, initialYear);

        dialog.show(getParentFragmentManager(), "AnioMesDialog");
    }

    @Override
    public void insertOnClickedDailog(int sys, int dia, int pul) {
        servicio.insert(sys,dia,pul);
    }
    @Override
    public void EditOnClickedDailog(PresionEntity presion, int sys, int dia, int pul) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        // 2. Crea el String con la fecha actual formateada.
        String fechaFormateada = sdf.format(new Date());

        PresionEntity presionParaActualizar = presion;

        // 2. Actualiza solo los campos que cambiaron.
        presionParaActualizar.setSys(sys);
        presionParaActualizar.setDia(dia);
        presionParaActualizar.setPul(pul);
        presionParaActualizar.setFecha_hora_creacion(fechaFormateada);
        presionParaActualizar.setEstado(false);

        servicio.edit(presionParaActualizar);
    }
    //______________________________________________________________________________________________
    // adapter______________________________________________________________________________________
    @Override
    public void onEditClick(PresionEntity presion) {
       PresionEditDailog dialog = new PresionEditDailog(getContext(), presion, PresionFragment.this);
        dialog.show();
    }
    //______________________________________________________________________________________________
}
