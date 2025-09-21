package com.cms.inventariostareas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.cms.inventariostareas.R;
import com.cms.inventariostareas.models.Usuario;

import java.util.List;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder> {

    private List<Usuario> listaUsuarios;
    private OnUsuarioClickListener listener;

    public interface OnUsuarioClickListener {
        void onUsuarioClick(Usuario usuario);
        void onEliminarUsuario(Usuario usuario);
    }

    public UsuariosAdapter(List<Usuario> listaUsuarios, OnUsuarioClickListener listener) {
        this.listaUsuarios = listaUsuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);
        holder.bind(usuario, listener);
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCorreo, tvRol;
        Switch switchActivo;
        ImageButton btnEditar, btnEliminar;
        DatabaseReference databaseReference;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvCorreo = itemView.findViewById(R.id.tvCorreo);
            tvRol = itemView.findViewById(R.id.tvRol);
            switchActivo = itemView.findViewById(R.id.switchActivo);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);

            databaseReference = FirebaseDatabase.getInstance().getReference("usuarios");
        }

        public void bind(Usuario usuario, OnUsuarioClickListener listener) {
            tvNombre.setText(usuario.getNombre() + " " + usuario.getApellido());
            tvCorreo.setText(usuario.getCorreo());
            tvRol.setText(usuario.getRol());
            switchActivo.setChecked(usuario.isActivo());

            // Cambiar estado activo/inactivo
            switchActivo.setOnCheckedChangeListener((buttonView, isChecked) -> {
                databaseReference.child(usuario.getUid()).child("activo").setValue(isChecked)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String estado = isChecked ? "activado" : "desactivado";
                                Toast.makeText(itemView.getContext(), "Usuario " + estado, Toast.LENGTH_SHORT).show();
                            }
                        });
            });

            // Editar usuario
            btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUsuarioClick(usuario);
                }
            });

            // Eliminar usuario
            btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEliminarUsuario(usuario);
                }
            });
        }
    }
}