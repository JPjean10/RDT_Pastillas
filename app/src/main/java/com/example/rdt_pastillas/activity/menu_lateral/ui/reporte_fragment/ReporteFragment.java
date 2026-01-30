package com.example.rdt_pastillas.activity.menu_lateral.ui.reporte_fragment;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.activity.menu_lateral.ui.reporte_fragment.ui.reporte_glucosa.ReporteGlucosaFragment;
import com.example.rdt_pastillas.activity.menu_lateral.ui.reporte_fragment.ui.reporte_presion.ReportePresionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ReporteFragment extends Fragment {

    private BottomNavigationView bottomNav;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reporte, container, false);

        bottomNav = view.findViewById(R.id.nav_reporte);

        bottomNav.setItemIconTintList(null);

        // Configurar el listener de navegación
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_glucosa) {
                reemplazarFragmento(new ReporteGlucosaFragment());
                return true;
            } else if (id == R.id.nav_presion) {
                // Suponiendo que tienes un ReportePresionFragment
                reemplazarFragmento(new ReportePresionFragment());
                return true;
            }
            return false;
        });

        // Cargar el primer fragmento por defecto (Glucosa) al iniciar
        if (savedInstanceState == null) {
            reemplazarFragmento(new ReporteGlucosaFragment());
        }

        return view;
    }

    private void reemplazarFragmento(Fragment fragmentoHijo) {
        // IMPORTANTE: Usar getChildFragmentManager() para fragmentos dentro de fragmentos
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        // Animación opcional para que el cambio se vea suave
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        transaction.replace(R.id.contenedor_reportes, fragmentoHijo);
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Permitir que el sensor gire la pantalla o forzar horizontal al entrar
        // ActivityInfo.SCREEN_ORIENTATION_SENSOR permite que gire según el sensor
        // ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE la fuerza a horizontal siempre
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Bloquear de nuevo a vertical al salir del fragmento
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
