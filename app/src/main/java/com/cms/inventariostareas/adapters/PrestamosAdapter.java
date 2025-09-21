package com.cms.inventariostareas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cms.inventariostareas.R;
import com.cms.inventariostareas.models.Prestamo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrestamosAdapter extends RecyclerView.Adapter<PrestamosAdapter.PrestamoViewHolder> {

    private List<Prestamo> listaPrestamos;
    private OnPrestamoClickListener listener;

    // Interfaz para manejar clics
    public interface OnPrestamoClickListener {
        void onPrestamoClick(Prestamo prestamo);
    }

    // Constructor actualizado con listener
    public PrestamosAdapter(List<Prestamo> listaPrestamos, OnPrestamoClickListener listener) {
        this.listaPrestamos = listaPrestamos != null ? listaPrestamos : new ArrayList<>();
        this.listener = listener;
    }

    // Método para actualizar la lista
    public void actualizarLista(List<Prestamo> nuevaLista) {
        this.listaPrestamos = nuevaLista != null ? nuevaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PrestamoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prestamo, parent, false);
        return new PrestamoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrestamoViewHolder holder, int position) {
        Prestamo prestamo = listaPrestamos.get(position);
        if (prestamo != null) {
            holder.bind(prestamo);

            // Configurar el click listener
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPrestamoClick(prestamo);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaPrestamos.size();
    }

    public static class PrestamoViewHolder extends RecyclerView.ViewHolder {
        TextView tvEquipo, tvCantidad, tvEstado, tvFecha;

        public PrestamoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEquipo = itemView.findViewById(R.id.tvEquipoPrestamo);
            tvCantidad = itemView.findViewById(R.id.tvCantidadPrestamo);
            tvEstado = itemView.findViewById(R.id.tvEstadoPrestamo);
            tvFecha = itemView.findViewById(R.id.tvFechaPrestamo);
        }

        public void bind(Prestamo prestamo) {
            if (prestamo == null) return;

            tvEquipo.setText(prestamo.getEquipoNombre());
            tvCantidad.setText("Cantidad: " + prestamo.getCantidad());
            tvEstado.setText(prestamo.getEstado());

            // Formatear fecha para mejor presentación
            try {
                SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date fecha = originalFormat.parse(prestamo.getFechaPrestamo());
                tvFecha.setText(targetFormat.format(fecha));
            } catch (Exception e) {
                tvFecha.setText(prestamo.getFechaPrestamo()); // Fallback si hay error
            }

            // Cambiar color según el estado
            int color;
            switch (prestamo.getEstado()) {
                case "aprobado":
                    color = itemView.getContext().getColor(android.R.color.holo_green_dark);
                    break;
                case "pendiente":
                    color = itemView.getContext().getColor(android.R.color.holo_orange_dark);
                    break;
                case "rechazado":
                    color = itemView.getContext().getColor(android.R.color.holo_red_dark);
                    break;
                case "devuelto":
                    color = itemView.getContext().getColor(android.R.color.holo_blue_dark);
                    break;
                default:
                    color = itemView.getContext().getColor(android.R.color.darker_gray);
            }
            tvEstado.setTextColor(color);
        }
    }
}