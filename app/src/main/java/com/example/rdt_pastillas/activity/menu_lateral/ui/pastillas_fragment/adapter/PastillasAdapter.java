package com.example.rdt_pastillas.activity.menu_lateral.ui.pastillas_fragment.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rdt_pastillas.Modelo.PastillasModel;
import com.example.rdt_pastillas.R;

import java.util.List;

public class PastillasAdapter extends RecyclerView.Adapter<PastillasAdapter.PastillaViewHolder> {

    private List<PastillasModel> listaPastillas;

    public PastillasAdapter(List<PastillasModel> listaPastillas) {
        this.listaPastillas = listaPastillas;
    }

    @NonNull
    @Override
    public PastillaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Crea la vista para cada elemento de la lista usando el layout item_pastilla.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pastilla, parent, false);
        return new PastillaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PastillaViewHolder holder, int position) {
        // Obtiene la pastilla en la posición actual
        PastillasModel pastilla = listaPastillas.get(position);
        // Asigna los datos a las vistas dentro del ViewHolder
        holder.nombrePastilla.setText(pastilla.getNombre());
        holder.horaPastilla.setText(pastilla.getHora());
    }

    @Override
    public int getItemCount() {
        // Devuelve el número total de elementos en la lista
        return listaPastillas.size();
    }

    // El ViewHolder contiene las referencias a las vistas de cada elemento de la lista
    public static class PastillaViewHolder extends RecyclerView.ViewHolder {
        TextView nombrePastilla;
        TextView horaPastilla;

        public PastillaViewHolder(@NonNull View itemView) {
            super(itemView);
            nombrePastilla = itemView.findViewById(R.id.tv_nombre_pastilla);
            horaPastilla = itemView.findViewById(R.id.tv_hora_pastilla);
        }
    }
}
