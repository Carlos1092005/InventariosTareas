package com.cms.inventariostareas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.cms.inventariostareas.R;
import com.cms.inventariostareas.models.Equipo;

import java.util.List;

public class EquiposAdapter extends RecyclerView.Adapter<EquiposAdapter.EquipoViewHolder> {

    private List<Equipo> listaEquipos;
    private OnEquipoClickListener listener;

    public interface OnEquipoClickListener {
        void onEquipoClick(Equipo equipo);
        void onEliminarEquipo(Equipo equipo);
    }

    public EquiposAdapter(List<Equipo> listaEquipos, OnEquipoClickListener listener) {
        this.listaEquipos = listaEquipos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EquipoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_equipo, parent, false);
        return new EquipoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipoViewHolder holder, int position) {
        Equipo equipo = listaEquipos.get(position);
        holder.bind(equipo, listener);
    }

    @Override
    public int getItemCount() {
        return listaEquipos.size();
    }

    public static class EquipoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvMarcaModelo, tvStock, tvDisponibles, tvEstado;
        ImageButton btnEditar, btnEliminar;
        DatabaseReference databaseReference;

        public EquipoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvMarcaModelo = itemView.findViewById(R.id.tvMarcaModelo);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvDisponibles = itemView.findViewById(R.id.tvDisponibles);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);

            databaseReference = FirebaseDatabase.getInstance().getReference("equipos");
        }

        public void bind(Equipo equipo, OnEquipoClickListener listener) {
            tvNombre.setText(equipo.getNombre());
            tvMarcaModelo.setText(equipo.getMarca() + " " + equipo.getModelo());
            tvStock.setText("Stock: " + equipo.getCantidad());
            tvDisponibles.setText("Disponibles: " + equipo.getDisponibles());
            tvEstado.setText("Estado: " + equipo.getEstado());

            // Editar equipo
            btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEquipoClick(equipo);
                }
            });

            // Eliminar equipo
            btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEliminarEquipo(equipo);
                }
            });
        }
    }
}