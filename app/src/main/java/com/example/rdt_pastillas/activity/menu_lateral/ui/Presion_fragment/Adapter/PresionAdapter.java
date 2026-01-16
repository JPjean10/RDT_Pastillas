package com.example.rdt_pastillas.activity.menu_lateral.ui.Presion_fragment.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.presion_entity.PresionEntity;
import com.example.rdt_pastillas.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PresionAdapter extends RecyclerView.Adapter<PresionAdapter.PresionViewHolder> {

    private List<PresionEntity> listaPresion = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(PresionEntity presion);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setListaPresion(List<PresionEntity> listaPresion) {
        this.listaPresion = listaPresion;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PresionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_presion, parent, false);
        return new PresionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PresionViewHolder holder, int position) {
        PresionEntity presion = listaPresion.get(position);

        // Formatear valores
        holder.tvSys.setText(String.valueOf(presion.getSys()));
        holder.tvDia.setText(String.valueOf(presion.getDia()));
        holder.tvPul.setText(String.valueOf(presion.getPul()));
        // --- CAMBIO DE FORMATO DE FECHA ---
        String fechaOriginal = presion.getFecha_hora_creacion(); // Viene como "yyyy-MM-dd HH:mm:ss"
        String fechaFormateada = fechaOriginal; // Valor por defecto en caso de error

        try {
            // 1. Formato de entrada (como está guardado en la BD)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            // 2. Formato de salida (Día/Mes/Año Hora:Minuto AM/PM)
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());

            Date date = inputFormat.parse(fechaOriginal);
            if (date != null) {
                fechaFormateada = outputFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvFecha.setText(fechaFormateada);

        // 4. Configurar ÚNICAMENTE la tarjeta según el estado de sincronización
        if (presion.isEstado()) {
            // ESTADO SINCRONIZADO: Tarjeta limpia y normal
            holder.cardView.setStrokeWidth(0); // Sin borde
            holder.cardView.setCardElevation(4f); // Elevación estándar
        } else {
            // ESTADO NO SINCRONIZADO: Borde rojo y sombra pronunciada
            holder.cardView.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.pure_red));
            holder.cardView.setStrokeWidth(4); // Grosor del borde
            holder.cardView.setCardElevation(12f); // Sombra más intensa para resaltar
        }

        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(presion);
            }
        });

        // CAMBIO AQUÍ: Mostrar solo en la primera posición (índice 0)
        if (position == 0) {
            holder.btnEditar.setVisibility(View.VISIBLE);
        } else {
            holder.btnEditar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listaPresion.size();
    }

    static class PresionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSys, tvDia, tvPul, tvFecha;
        ImageView btnEditar;
        // Referencia a MaterialCardView
        com.google.android.material.card.MaterialCardView cardView;

        public PresionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            tvSys = itemView.findViewById(R.id.tv_sys_valor);
            tvDia = itemView.findViewById(R.id.tv_dia_valor);
            tvPul = itemView.findViewById(R.id.tv_pul_valor);
            btnEditar = itemView.findViewById(R.id.btn_editar);

            // Ahora el cast funcionará porque el XML ya tiene MaterialCardView
            cardView = (com.google.android.material.card.MaterialCardView) itemView;
        }
    }
}
