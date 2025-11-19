package com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaDia;
import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GlucosaAdapter extends RecyclerView.Adapter<GlucosaAdapter.ViewHolder> {

    private List<GlucosaDia> listaDias = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_glucosa , parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GlucosaDia item = listaDias.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return listaDias.size();
    }

    // Método para actualizar la lista de datos del adaptador
    public void submitList(List<GlucosaDia> newList) {
        listaDias.clear();
        listaDias.addAll(newList);
        notifyDataSetChanged(); // Notifica al RecyclerView que los datos cambiaron
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Vista principal
        TextView tvFecha;    // Vistas para cada una de las 3 mediciones
        View medicionView1, medicionView2, medicionView3;
        View divider1, divider2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tv_fecha);

            // Referencias a las vistas incluidas
            medicionView1 = itemView.findViewById(R.id.medicion1);
            medicionView2 = itemView.findViewById(R.id.medicion2);
            medicionView3 = itemView.findViewById(R.id.medicion3);

            // Referencias a los divisores
            divider1 = itemView.findViewById(R.id.divider1);
            divider2 = itemView.findViewById(R.id.divider2);
        }

        public void bind(final GlucosaDia item) {
            tvFecha.setText(formatearFechaSimple(item.getFecha()));
            List<GlucosaEntity> mediciones = item.getMediciones();

            // Limpiar y ocultar todas las vistas antes de empezar
            medicionView1.setVisibility(View.INVISIBLE);
            medicionView2.setVisibility(View.INVISIBLE);
            medicionView3.setVisibility(View.INVISIBLE);
            divider1.setVisibility(View.GONE);
            divider2.setVisibility(View.GONE);

            // Configurar la primera medición (si existe)
            if (mediciones.size() >= 1) {
                configurarMedicionView(medicionView1, mediciones.get(0));
            }

            // Configurar la segunda medición (si existe)
            if (mediciones.size() >= 2) {
                configurarMedicionView(medicionView2, mediciones.get(1));
                divider1.setVisibility(View.VISIBLE);
            }

            // Configurar la tercera medición (si existe)
            if (mediciones.size() >= 3) {
                configurarMedicionView(medicionView3, mediciones.get(2));
                divider2.setVisibility(View.VISIBLE);
            }
        }

        private void configurarMedicionView(View medicionView, GlucosaEntity medicion) {
            medicionView.setVisibility(View.VISIBLE);

            TextView tvGlucosa = medicionView.findViewById(R.id.tv_glucosa);
            TextView tvHora = medicionView.findViewById(R.id.tv_hora);
            ImageView ivSync = medicionView.findViewById(R.id.iv_sync);

            tvGlucosa.setText(String.valueOf(medicion.getNivel_glucosa()));
            tvHora.setText(formatearHora(medicion.getFecha_hora_creacion()));

            if (medicion.isEstado()) { // true = sincronizado
                ivSync.setImageResource(R.drawable.ic_cloud_24);
            } else { // false = no sincronizado
                ivSync.setImageResource(R.drawable.ic_cloud_off_24);
            }
        }

        private String formatearHora(String fechaHoraCompleta) {
            // Tu formato de guardado: "yyyy/MM/dd hh:mm a"
            SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault());
            try {
                Date date = formatoOriginal.parse(fechaHoraCompleta);
                // La salida ya tiene AM/PM, no hace falta formatear de nuevo a "hh:mm a"
                return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);
            } catch (Exception e) {
                return fechaHoraCompleta.length() > 11 ? fechaHoraCompleta.substring(11) : "";
            }
        }

        private String formatearFechaSimple(String fechaOriginal) {
            SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            SimpleDateFormat formatoDeseado = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date date = formatoOriginal.parse(fechaOriginal);
                return formatoDeseado.format(date);
            } catch (Exception e) { return fechaOriginal; }
        }
    }
}
