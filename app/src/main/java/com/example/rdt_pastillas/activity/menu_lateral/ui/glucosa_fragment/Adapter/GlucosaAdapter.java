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
import com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.componentes.dailog.GlucosaInsertDailog;
import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaDia;
import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GlucosaAdapter extends RecyclerView.Adapter<GlucosaAdapter.ViewHolder> {

    private List<GlucosaDia> listaDias = new ArrayList<>();
    private EdiOnClickedAdapter listener;


    public interface EdiOnClickedAdapter {
        void EdiOnClickedAdapter(GlucosaEntity medicion);
    }

    public GlucosaAdapter(EdiOnClickedAdapter listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asegúrate de que el layout inflado sea 'item_glucosa.xml' que tiene 2 o 3 slots.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_glucosa, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GlucosaDia item = listaDias.get(position);
        // Pasamos la posición actual al ViewHolder para la lógica del icono de editar
        holder.bind(item, position, listener);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvFecha;
        ImageView ivEdit; // Referencia al icono de editar
        View medicionView1, medicionView2; // Asumiendo 2 slots según tu último XML
        View divider1;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            ivEdit = itemView.findViewById(R.id.iv_edit); // Obtener la referencia del icono

            medicionView1 = itemView.findViewById(R.id.medicion1);
            medicionView2 = itemView.findViewById(R.id.medicion2);
            divider1 = itemView.findViewById(R.id.divider1);
        }

        // MÉTODO BIND CORREGIDO PARA MOSTRAR EL ICONO DE EDITAR
        public void bind(final GlucosaDia item, int position,EdiOnClickedAdapter listener) {
            tvFecha.setText(formatearFechaSimple(item.getFecha()));
            List<GlucosaEntity> mediciones = item.getMediciones();
            int count = mediciones.size();

            // --- LÓGICA PARA MOSTRAR/OCULTAR EL ICONO DE EDITAR ---
            // Como la lista viene ordenada DESC, el item más reciente está en la posición 0.
            if (position == 0) {
                ivEdit.setVisibility(View.VISIBLE);
                if (count > 0) {
                    final GlucosaEntity medicionMasReciente = mediciones.get(0);

                    // --- CORRECCIÓN 2: La llamada al listener va DENTRO del OnClickListener ---
                    ivEdit.setOnClickListener(v -> {
                        if (listener != null) {
                            // Llama al método con el nombre corregido
//                            listene.onEditClicked(medicionMasReciente);
                            listener.EdiOnClickedAdapter(medicionMasReciente);
                        }
                    });
                }
            } else {
                ivEdit.setVisibility(View.INVISIBLE);
                ivEdit.setOnClickListener(null); // Buena práctica: limpiar el listener
            }
            // --------------------------------------------------------

            // Limpiar vistas antes de configurar
            medicionView1.setVisibility(View.INVISIBLE);
            medicionView2.setVisibility(View.INVISIBLE);
            divider1.setVisibility(View.GONE);

            // Lógica para 1 o 2 mediciones
            switch (count) {
                case 1:
                    // Si solo hay uno, lo mostramos en el primer slot
                    configurarMedicionView(medicionView1, mediciones.get(0));
                    break;
                case 2:
                    // Si hay dos, llenamos los dos slots
                    // El más antiguo (get(1)) a la izquierda y el más nuevo (get(0)) a la derecha
                    configurarMedicionView(medicionView1, mediciones.get(1));
                    configurarMedicionView(medicionView2, mediciones.get(0));
                    divider1.setVisibility(View.VISIBLE);
                    break;
                // Si necesitaras 3, aquí iría el case 3
            }
        }

        private void configurarMedicionView(View medicionView, GlucosaEntity medicion) {
            medicionView.setVisibility(View.VISIBLE);
            TextView tvGlucosa = medicionView.findViewById(R.id.tv_glucosa);
            TextView tvHora = medicionView.findViewById(R.id.tv_hora);
            ImageView ivSync = medicionView.findViewById(R.id.iv_sync);

            tvGlucosa.setText(String.valueOf(medicion.getNivel_glucosa()));
            tvHora.setText(formatearHora(medicion.getFecha_hora_creacion()));

            if (medicion.isEstado()) {
                ivSync.setImageResource(R.drawable.ic_cloud_24);
            } else {
                ivSync.setImageResource(R.drawable.ic_cloud_off_24);
            }
        }

        private String formatearHora(String fechaHoraCompleta) {
            try {
                SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault());
                SimpleDateFormat formatoDeseado = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Date date = formatoOriginal.parse(fechaHoraCompleta);
                return formatoDeseado.format(date);
            } catch (Exception e) {
                return ""; // Evitar devolver texto basura si hay error
            }
        }

        private String formatearFechaSimple(String fechaOriginal) {
            try {
                SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                SimpleDateFormat formatoDeseado = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = formatoOriginal.parse(fechaOriginal);
                return formatoDeseado.format(date);
            } catch (Exception e) {
                return fechaOriginal; // Devolver la original si falla el parseo
            }
        }
    }
}
