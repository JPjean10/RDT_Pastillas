package com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaDia;
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class GlucosaAdapter extends RecyclerView.Adapter<GlucosaAdapter.ViewHolder> {

    private List<GlucosaDia> listaDias = new ArrayList<>();
    private EdiOnClickedAdapter listener;

    // --- Interfaz para los clics ---
    public interface EdiOnClickedAdapter {
        void onEditClicked(GlucosaEntity medicion);
    }

    public GlucosaAdapter(EdiOnClickedAdapter listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_glucosa, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GlucosaDia item = listaDias.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return listaDias.size();
    }

    public void submitList(List<GlucosaDia> newList) {
        listaDias.clear();
        listaDias.addAll(newList);
        notifyDataSetChanged();
    }


    // --- CLASE VIEWHOLDER ---
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvFecha;
        ImageView ivEdit;
        ImageButton btnToggleAyunas;
        View medicionView1, medicionView2;
        View divider1;

        private boolean mostrarAyunas = false; // Estado de visibilidad, local para cada ViewHolder

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            btnToggleAyunas = itemView.findViewById(R.id.btn_toggle_ayunas);

            medicionView1 = itemView.findViewById(R.id.medicion1);
            medicionView2 = itemView.findViewById(R.id.medicion2);
            divider1 = itemView.findViewById(R.id.divider1);
        }

        public void bind(final GlucosaDia item, EdiOnClickedAdapter listener) {
            tvFecha.setText(formatearFechaSimple(item.getFecha()));
            List<GlucosaEntity> mediciones = item.getMediciones();

            // --- Lógica del botón de Edición ---
            // Solo se muestra si hay mediciones ese día
            if (mediciones.isEmpty()) {
                ivEdit.setVisibility(View.INVISIBLE);
                ivEdit.setOnClickListener(null);
            } else {
                ivEdit.setVisibility(View.VISIBLE);
                final GlucosaEntity medicionMasReciente = mediciones.get(mediciones.size() - 1);
                ivEdit.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEditClicked(medicionMasReciente);
                    }
                });
            }

            // --- Lógica del botón de Visibilidad ---
            btnToggleAyunas.setOnClickListener(v -> {
                mostrarAyunas = !mostrarAyunas; // Cambia el estado de visibilidad
                // Vuelve a aplicar la configuración a las vistas para reflejar el cambio
                actualizarVistasMediciones(mediciones, mostrarAyunas);
                // Cambia el ícono del botón
                btnToggleAyunas.setImageResource(
                        mostrarAyunas ? R.drawable.ic_ocultar : R.drawable.ic_mostrar
                );
            });

            // Configuración inicial de las vistas
            actualizarVistasMediciones(mediciones, mostrarAyunas);
        }

        private void actualizarVistasMediciones(List<GlucosaEntity> mediciones, boolean mostrarAyunas) {
            // Ocultar todo por defecto
            medicionView1.setVisibility(View.INVISIBLE);
            medicionView2.setVisibility(View.INVISIBLE);
            divider1.setVisibility(View.GONE);

            // Configurar la primera medición si existe
            if (mediciones.size() >= 1) {
                configurarMedicionView(medicionView1, mediciones.get(0), mostrarAyunas);
            }

            // Configurar la segunda medición si existe
            if (mediciones.size() >= 2) {
                configurarMedicionView(medicionView2, mediciones.get(1), mostrarAyunas);
                divider1.setVisibility(View.VISIBLE);
            }
        }

        private void configurarMedicionView(View medicionView, GlucosaEntity medicion, boolean mostrarAyunas) {
            medicionView.setVisibility(View.VISIBLE);
            TextView tvGlucosa = medicionView.findViewById(R.id.tv_glucosa);
            TextView tvHora = medicionView.findViewById(R.id.tv_hora);
            ImageView ivSync = medicionView.findViewById(R.id.iv_sync);
            CheckBox cbEnAyunas = medicionView.findViewById(R.id.cb_en_ayunas);

            tvGlucosa.setText(String.valueOf(medicion.getNivel_glucosa()));
            tvHora.setText(formatearHora(medicion.getFecha_hora_creacion()));

            // Estado de sincronización
            ivSync.setImageResource(medicion.isEstado() ? R.drawable.ic_cloud_24 : R.drawable.ic_cloud_off_24);

            // Lógica de visibilidad y estado del CheckBox
            cbEnAyunas.setVisibility(mostrarAyunas ? View.VISIBLE : View.GONE);
            if (medicion.getEn_ayunas() != null) {
                cbEnAyunas.setChecked(medicion.getEn_ayunas());
            } else {
                cbEnAyunas.setChecked(false);
            }
            cbEnAyunas.setEnabled(false); // No se puede editar desde la lista
        }

        // --- Métodos para formatear fecha y hora (sin cambios) ---
        private String formatearHora(String fechaHoraCompleta) {
            try {
                SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat formatoDeseado = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Date date = formatoOriginal.parse(fechaHoraCompleta);
                return (date != null) ? formatoDeseado.format(date) : "";
            } catch (Exception e) { return ""; }
        }

        private String formatearFechaSimple(String fechaOriginal) {
            try {
                SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat formatoDeseado = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = formatoOriginal.parse(fechaOriginal);
                return (date != null) ? formatoDeseado.format(date) : fechaOriginal;
            } catch (Exception e) { return fechaOriginal; }
        }
    }
}
