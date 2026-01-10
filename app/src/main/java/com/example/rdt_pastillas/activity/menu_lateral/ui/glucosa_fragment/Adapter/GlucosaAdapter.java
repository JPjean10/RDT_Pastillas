package com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GlucosaAdapter extends RecyclerView.Adapter<GlucosaAdapter.GlucosaViewHolder> {

    private List<GlucosaEntity> listaGlucosa = new ArrayList<>();
    private EdiOnClickedAdapter listener;

    public interface EdiOnClickedAdapter {
        void onEditClicked(GlucosaEntity medicion);
    }

    public GlucosaAdapter(EdiOnClickedAdapter listener) {
        this.listener = listener;
    }

    public void submitList(List<GlucosaEntity> newList) {
        this.listaGlucosa = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GlucosaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_glucosa, parent, false);
        return new GlucosaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GlucosaViewHolder holder, int position) {
        GlucosaEntity glucosa = listaGlucosa.get(position);

        // 1. Mostrar Nivel de Glucosa
        holder.tvGlucosa.setText(String.valueOf(glucosa.getNivel_glucosa()));

        // 2. Formatear Fecha y Hora (dd/MM/yyyy hh:mm a)
        holder.tvFecha.setText(formatearFechaHora(glucosa.getFecha_hora_creacion()));

        // 3. Configurar CheckBox
        holder.cbEnAyunas.setChecked(glucosa.getEn_ayunas() != null && glucosa.getEn_ayunas());
        holder.cbEnAyunas.setEnabled(false); // Solo lectura

        // 4. Configurar ÚNICAMENTE la tarjeta según el estado de sincronización
        if (glucosa.isEstado()) {
            // ESTADO SINCRONIZADO: Tarjeta limpia y normal
            holder.cardView.setStrokeWidth(0); // Sin borde
            holder.cardView.setCardElevation(4f); // Elevación estándar
        } else {
            // ESTADO NO SINCRONIZADO: Borde rojo y sombra pronunciada
            holder.cardView.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.pure_red));
            holder.cardView.setStrokeWidth(4); // Grosor del borde
            holder.cardView.setCardElevation(12f); // Sombra más intensa para resaltar
        }

        // 5. Lógica del botón Editar (Solo en la posición 0)
        if (position == 0) {
            holder.ivEdit.setVisibility(View.VISIBLE);
            holder.ivEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClicked(glucosa);
            });
        } else {
            holder.ivEdit.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listaGlucosa.size();
    }

    private String formatearFechaHora(String fechaOriginal) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            Date date = input.parse(fechaOriginal);
            return (date != null) ? output.format(date) : fechaOriginal;
        } catch (Exception e) {
            return fechaOriginal;
        }
    }

    static class GlucosaViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.card.MaterialCardView cardView; // Cambiar a MaterialCardView
        TextView tvFecha, tvGlucosa;
        ImageView ivEdit, ivSync;
        CheckBox cbEnAyunas;

        public GlucosaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (com.google.android.material.card.MaterialCardView) itemView; // El root es la card
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            tvGlucosa = itemView.findViewById(R.id.tv_glucosa);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivSync = itemView.findViewById(R.id.iv_icon_glucosa);
            cbEnAyunas = itemView.findViewById(R.id.cb_en_ayunas);
        }
    }
}
