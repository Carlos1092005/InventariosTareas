package com.cms.inventariostareas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cms.inventariostareas.R;
import com.cms.inventariostareas.models.MovimientoInventario;

import java.util.List;

public class MovimientosInventarioAdapter extends RecyclerView.Adapter<MovimientosInventarioAdapter.MovimientoViewHolder> {

    private List<MovimientoInventario> listaMovimientos;

    public MovimientosInventarioAdapter(List<MovimientoInventario> listaMovimientos) {
        this.listaMovimientos = listaMovimientos;
    }

    @NonNull
    @Override
    public MovimientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movimiento_inventario, parent, false);
        return new MovimientoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovimientoViewHolder holder, int position) {
        MovimientoInventario movimiento = listaMovimientos.get(position);
        holder.bind(movimiento);
    }

    @Override
    public int getItemCount() {
        return listaMovimientos.size();
    }

    public static class MovimientoViewHolder extends RecyclerView.ViewHolder {
        TextView tvEquipo, tvTipo, tvCantidad, tvResponsable, tvFecha;

        public MovimientoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEquipo = itemView.findViewById(R.id.tvEquipo);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvResponsable = itemView.findViewById(R.id.tvResponsable);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }

        public void bind(MovimientoInventario movimiento) {
            tvEquipo.setText(movimiento.getEquipoNombre());
            tvTipo.setText(movimiento.getTipo().equals("entrada") ? "Entrada" : "Salida");
            tvCantidad.setText(String.valueOf(movimiento.getCantidad()));
            tvResponsable.setText(movimiento.getResponsable());
            tvFecha.setText(movimiento.getFecha());

            // Cambiar color según el tipo
            int color = movimiento.getTipo().equals("entrada") ?
                    itemView.getContext().getColor(android.R.color.holo_green_dark) :
                    itemView.getContext().getColor(android.R.color.holo_red_dark);
            tvTipo.setTextColor(color);
        }
    }
}