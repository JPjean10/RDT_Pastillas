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

import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.componentes.dailog.GlucosaInsertDailog;
import com.example.rdt_pastillas.basedata.servicio.glucosa_bd.GlucosaServicio;
import com.example.rdt_pastillas.util.dailog.AnioMesDialog;

public class GlucosaFragment extends Fragment implements
        GlucosaInsertDailog.insertOnClickedDailog {

    private String fechaGuardada;
    //variables del activity
    EditText btn_fecha;
    Button btn_agregar;

    //-------------------
    GlucosaServicio servicio;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_glucosa, container, false);

        servicio = new GlucosaServicio(requireActivity().getApplication());

        btn_fecha = view.findViewById(R.id.btn_seleccionar_fecha);
        btn_agregar = view.findViewById(R.id.btn_agregar);

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
    // --- MÉTODOS DE LA INTERFAZ DEL DIÁLOGO ---
    @Override
    public void insertOnClickedDailog(int nivel_glucosa) {
        Toast.makeText(getContext(), "Monto: " + nivel_glucosa, Toast.LENGTH_SHORT).show();
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
