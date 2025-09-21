package com.cms.inventariostareas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cms.inventariostareas.R;
import com.cms.inventariostareas.models.Equipo;

import java.util.List;

public class EquiposInstructorAdapter extends RecyclerView.Adapter<EquiposInstructorAdapter.EquipoViewHolder> {

    private List<Equipo> listaEquipos;

    public EquiposInstructorAdapter(List<Equipo> listaEquipos) {
        this.listaEquipos = listaEquipos;
    }

    public void actualizarLista(List<Equipo> nuevaLista) {
        this.listaEquipos = nuevaLista != null ? nuevaLista : new java.util.ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EquipoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_equipo_instructor, parent, false);
        return new EquipoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipoViewHolder holder, int position) {
        Equipo equipo = listaEquipos.get(position);
        if (equipo != null) {
            holder.bind(equipo);
        }
    }

    @Override
    public int getItemCount() {
        return listaEquipos.size();
    }

    public static class EquipoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCategoria, tvMarcaModelo, tvStock, tvDisponibles, tvEstado, tvUbicacion;

        public EquipoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvMarcaModelo = itemView.findViewById(R.id.tvMarcaModelo);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvDisponibles = itemView.findViewById(R.id.tvDisponibles);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvUbicacion = itemView.findViewById(R.id.tvUbicacion);
        }

        public void bind(Equipo equipo) {
            tvNombre.setText(equipo.getNombre());
            tvCategoria.setText(equipo.getCategoria());
            tvMarcaModelo.setText(equipo.getMarca() + " " + equipo.getModelo());
            tvStock.setText("Total: " + equipo.getCantidad());
            tvDisponibles.setText("Disponibles: " + equipo.getDisponibles());
            tvEstado.setText("Estado: " + equipo.getEstado());
            tvUbicacion.setText(equipo.getUbicacion());

            // Cambiar color según disponibilidad
            if (equipo.getDisponibles() == 0) {
                tvDisponibles.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
            } else if (equipo.getDisponibles() < 3) {
                tvDisponibles.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark));
            } else {
                tvDisponibles.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
            }

            // Cambiar color según estado
            switch (equipo.getEstado().toLowerCase()) {
                case "disponible":
                    tvEstado.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
                    break;
                case "mantenimiento":
                    tvEstado.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark));
                    break;
                case "dañado":
                    tvEstado.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
                    break;
                default:
                    tvEstado.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
            }
        }
    }
}