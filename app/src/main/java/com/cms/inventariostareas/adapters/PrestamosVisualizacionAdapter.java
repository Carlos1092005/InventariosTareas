package com.cms.inventariostareas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cms.inventariostareas.R;
import com.cms.inventariostareas.models.Prestamo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrestamosVisualizacionAdapter extends RecyclerView.Adapter<PrestamosVisualizacionAdapter.PrestamoViewHolder> {

    private List<Prestamo> listaPrestamos;

    public PrestamosVisualizacionAdapter(List<Prestamo> listaPrestamos) {
        this.listaPrestamos = listaPrestamos;
    }

    public void actualizarLista(List<Prestamo> nuevaLista) {
        this.listaPrestamos = nuevaLista;
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
        }
    }

    @Override
    public int getItemCount() {
        return listaPrestamos.size();
    }

    public static class PrestamoViewHolder extends RecyclerView.ViewHolder {
        TextView tvEquipo, tvInstructor, tvCantidad, tvEstado, tvFecha;

        public PrestamoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEquipo = itemView.findViewById(R.id.tvEquipoPrestamo);
            tvInstructor = itemView.findViewById(R.id.tvInstructorPrestamo);
            tvCantidad = itemView.findViewById(R.id.tvCantidadPrestamo);
            tvEstado = itemView.findViewById(R.id.tvEstadoPrestamo);
            tvFecha = itemView.findViewById(R.id.tvFechaPrestamo);
        }

        public void bind(Prestamo prestamo) {
            // Información básica
            tvEquipo.setText(prestamo.getEquipoNombre());
            tvInstructor.setText("Instructor: " + prestamo.getInstructorNombre());
            tvCantidad.setText("Cantidad: " + prestamo.getCantidad());
            tvEstado.setText(prestamo.getEstado().toUpperCase());

            // Formatear fecha de préstamo
            try {
                SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date fecha = originalFormat.parse(prestamo.getFechaPrestamo());
                tvFecha.setText("Solicitado: " + targetFormat.format(fecha));
            } catch (Exception e) {
                tvFecha.setText("Solicitado: " + prestamo.getFechaPrestamo());
            }

            // Cambiar color según el estado
            int color;
            switch (prestamo.getEstado().toLowerCase()) {
                case "aprobado":
                    color = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark);
                    break;
                case "pendiente":
                    color = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark);
                    break;
                case "rechazado":
                    color = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark);
                    break;
                case "devuelto":
                    color = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_blue_dark);
                    break;
                default:
                    color = ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray);
            }
            tvEstado.setTextColor(color);

            // Opcional: cambiar fondo del estado si quieres
            // tvEstado.setBackgroundResource(R.drawable.bg_estado_prestamo);
        }
    }
}