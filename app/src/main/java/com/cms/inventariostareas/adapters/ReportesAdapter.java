package com.cms.inventariostareas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cms.inventariostareas.R;
import com.cms.inventariostareas.models.Reporte;

import java.util.List;

public class ReportesAdapter extends RecyclerView.Adapter<ReportesAdapter.ReporteViewHolder> {

    private List<Reporte> listaReportes;

    public ReportesAdapter(List<Reporte> listaReportes) {
        this.listaReportes = listaReportes;
    }

    // MÉTODO DE ACTUALIZACIÓN AGREGADO
    public void actualizarLista(List<Reporte> nuevaLista) {
        this.listaReportes = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReporteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reporte, parent, false);
        return new ReporteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReporteViewHolder holder, int position) {
        Reporte reporte = listaReportes.get(position);
        holder.bind(reporte);
    }

    @Override
    public int getItemCount() {
        return listaReportes.size();
    }

    public static class ReporteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo, tvDescripcion, tvDetalles, tvFecha;

        public ReporteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tvTipoReporte);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionReporte);
            tvDetalles = itemView.findViewById(R.id.tvDetallesReporte);
            tvFecha = itemView.findViewById(R.id.tvFechaReporte);
        }

        public void bind(Reporte reporte) {
            tvTipo.setText(reporte.getTipo());
            tvDescripcion.setText(reporte.getDescripcion());
            tvDetalles.setText(reporte.getDetalles()); // ✅ CORRECTO: getDetalles()
            tvFecha.setText(reporte.getFecha());

            // Cambiar color según el tipo de reporte
            int color;
            switch (reporte.getTipo()) {
                case "Inventario":
                    color = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_blue_dark);
                    break;
                case "Préstamo":
                    color = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark);
                    break;
                case "Auditoría":
                    color = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_purple);
                    break;
                default:
                    color = ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray);
            }
            tvTipo.setTextColor(color);
        }
    }
}