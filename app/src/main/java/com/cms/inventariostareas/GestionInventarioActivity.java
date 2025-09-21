package com.cms.inventariostareas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cms.inventariostareas.adapters.MovimientosInventarioAdapter;
import com.cms.inventariostareas.models.Equipo;
import com.cms.inventariostareas.models.MovimientoInventario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class GestionInventarioActivity extends AppCompatActivity {

    private DatabaseReference dbEquipos, dbInventario;
    private ProgressDialog progressDialog;
    private List<Equipo> listaEquipos;
    private List<MovimientoInventario> listaMovimientos;
    private RecyclerView recyclerView;
    private MovimientosInventarioAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_inventario);

        dbEquipos = FirebaseDatabase.getInstance().getReference("equipos");
        dbInventario = FirebaseDatabase.getInstance().getReference("inventario");
        listaEquipos = new ArrayList<>();
        listaMovimientos = new ArrayList<>();

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewMovimientos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MovimientosInventarioAdapter(listaMovimientos);
        recyclerView.setAdapter(adapter);

        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnEntrada = findViewById(R.id.btnEntradaInventario);
        Button btnSalida = findViewById(R.id.btnSalidaInventario);

        btnVolver.setOnClickListener(v -> finish());
        btnEntrada.setOnClickListener(v -> mostrarDialogoMovimiento("entrada"));
        btnSalida.setOnClickListener(v -> mostrarDialogoMovimiento("salida"));

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarEquipos();
        cargarMovimientos();
    }

    private void cargarEquipos() {
        progressDialog.show();
        dbEquipos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaEquipos.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Equipo equipo = snapshot.getValue(Equipo.class);
                    if (equipo != null) {
                        equipo.setId(snapshot.getKey());
                        listaEquipos.add(equipo);
                    }
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(GestionInventarioActivity.this, "Error al cargar equipos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarMovimientos() {
        dbInventario.orderByChild("fecha").limitToLast(50)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        listaMovimientos.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            MovimientoInventario movimiento = snapshot.getValue(MovimientoInventario.class);
                            if (movimiento != null) {
                                listaMovimientos.add(0, movimiento); // Add at beginning for reverse chronological order
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(GestionInventarioActivity.this, "Error al cargar movimientos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarDialogoMovimiento(String tipo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_movimiento_inventario, null);
        builder.setView(view);

        Spinner spinnerEquipos = view.findViewById(R.id.spinnerEquipos);
        EditText etCantidad = view.findViewById(R.id.etCantidad);
        EditText etProveedor = view.findViewById(R.id.etProveedor);
        EditText etResponsable = view.findViewById(R.id.etResponsable);
        TextView tvTitulo = view.findViewById(R.id.tvTituloDialogo);

        String titulo = tipo.equals("entrada") ? "Registrar Entrada" : "Registrar Salida";
        tvTitulo.setText(titulo);

        // Ocultar proveedor para salidas
        if (tipo.equals("salida")) {
            view.findViewById(R.id.layoutProveedor).setVisibility(View.GONE);
        }

        // Configurar spinner de equipos
        ArrayAdapter<Equipo> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listaEquipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEquipos.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        Button btnCancelar = view.findViewById(R.id.btnCancelar);
        Button btnConfirmar = view.findViewById(R.id.btnConfirmar);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnConfirmar.setOnClickListener(v -> {
            Equipo equipoSeleccionado = (Equipo) spinnerEquipos.getSelectedItem();
            String cantidadStr = etCantidad.getText().toString().trim();
            String proveedor = etProveedor.getText().toString().trim();
            String responsable = etResponsable.getText().toString().trim();

            if (equipoSeleccionado != null && validarCantidad(cantidadStr, equipoSeleccionado, tipo)) {
                int cantidad = Integer.parseInt(cantidadStr);

                if (tipo.equals("entrada")) {
                    registrarEntrada(equipoSeleccionado, cantidad, proveedor, responsable);
                } else {
                    registrarSalida(equipoSeleccionado, cantidad, responsable);
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean validarCantidad(String cantidadStr, Equipo equipo, String tipo) {
        if (cantidadStr.isEmpty()) {
            Toast.makeText(this, "Ingrese la cantidad", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            int cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                Toast.makeText(this, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Validar stock para salidas
            if (tipo.equals("salida") && cantidad > equipo.getDisponibles()) {
                Toast.makeText(this, "Stock insuficiente. Disponibles: " + equipo.getDisponibles(), Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void registrarEntrada(Equipo equipo, int cantidad, String proveedor, String responsable) {
        progressDialog.setMessage("Registrando entrada...");
        progressDialog.show();

        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String movimientoId = UUID.randomUUID().toString();

        MovimientoInventario movimiento = new MovimientoInventario(
                movimientoId, equipo.getId(), equipo.getNombre(), "entrada",
                cantidad, proveedor, responsable, fecha
        );

        // Actualizar stock del equipo
        int nuevoStock = equipo.getCantidad() + cantidad;
        int nuevosDisponibles = equipo.getDisponibles() + cantidad;

        Map<String, Object> updatesEquipo = new HashMap<>();
        updatesEquipo.put("cantidad", nuevoStock);
        updatesEquipo.put("disponibles", nuevosDisponibles);

        // Ejecutar ambas operaciones
        dbEquipos.child(equipo.getId()).updateChildren(updatesEquipo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dbInventario.child(movimientoId).setValue(movimiento)
                                .addOnCompleteListener(task2 -> {
                                    progressDialog.dismiss();
                                    if (task2.isSuccessful()) {
                                        Toast.makeText(this, "Entrada registrada exitosamente", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Error al registrar movimiento", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error al actualizar stock", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registrarSalida(Equipo equipo, int cantidad, String responsable) {
        progressDialog.setMessage("Registrando salida...");
        progressDialog.show();

        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String movimientoId = UUID.randomUUID().toString();

        MovimientoInventario movimiento = new MovimientoInventario(
                movimientoId, equipo.getId(), equipo.getNombre(), "salida",
                cantidad, "", responsable, fecha
        );

        // Actualizar stock del equipo
        int nuevoStock = equipo.getCantidad() - cantidad;
        int nuevosDisponibles = equipo.getDisponibles() - cantidad;

        Map<String, Object> updatesEquipo = new HashMap<>();
        updatesEquipo.put("cantidad", nuevoStock);
        updatesEquipo.put("disponibles", nuevosDisponibles);

        // Ejecutar ambas operaciones
        dbEquipos.child(equipo.getId()).updateChildren(updatesEquipo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dbInventario.child(movimientoId).setValue(movimiento)
                                .addOnCompleteListener(task2 -> {
                                    progressDialog.dismiss();
                                    if (task2.isSuccessful()) {
                                        Toast.makeText(this, "Salida registrada exitosamente", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Error al registrar movimiento", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error al actualizar stock", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}