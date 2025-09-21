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
import android.widget.Toast;

import com.cms.inventariostareas.adapters.EquiposAdapter;
import com.cms.inventariostareas.models.Equipo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GestionEquiposActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EquiposAdapter adapter;
    private List<Equipo> listaEquipos;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;

    // Declarar los botones como variables globales
    private Button btnVolver, btnAgregarEquipo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_equipos);

        // INICIALIZAR LOS BOTONES CORRECTAMENTE
        btnVolver = findViewById(R.id.btnVolver);
        btnAgregarEquipo = findViewById(R.id.btnAgregarEquipo);

        databaseReference = FirebaseDatabase.getInstance().getReference("equipos");
        listaEquipos = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerViewEquipos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EquiposAdapter(listaEquipos, new EquiposAdapter.OnEquipoClickListener() {
            @Override
            public void onEquipoClick(Equipo equipo) {
                mostrarDialogoEditarEquipo(equipo);
            }

            @Override
            public void onEliminarEquipo(Equipo equipo) {
                mostrarDialogoConfirmarEliminacion(equipo);
            }
        });

        recyclerView.setAdapter(adapter);

        // LISTENERS DE BOTONES
        btnVolver.setOnClickListener(v -> finish());
        btnAgregarEquipo.setOnClickListener(v -> mostrarDialogoAgregarEquipo());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarEquipos();
    }

    private void cargarEquipos() {
        progressDialog.show();
        databaseReference.addValueEventListener(new ValueEventListener() {
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
                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(GestionEquiposActivity.this, "Error al cargar equipos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoAgregarEquipo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_equipo, null);
        builder.setView(view);

        // INICIALIZAR LAS VISTAS CORRECTAMENTE - verifica que estos IDs existan en dialog_agregar_equipo.xml
        EditText etNombre = view.findViewById(R.id.etNombreEquipo);
        EditText etMarca = view.findViewById(R.id.etMarcaEquipo);
        EditText etModelo = view.findViewById(R.id.etModeloEquipo);
        EditText etCantidad = view.findViewById(R.id.etCantidadEquipo);
        EditText etUbicacion = view.findViewById(R.id.etUbicacionEquipo);
        Spinner spinnerCategoria = view.findViewById(R.id.spinnerCategoriaEquipo);
        Spinner spinnerEstado = view.findViewById(R.id.spinnerEstadoEquipo);

        // Configurar spinners
        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(this,
                R.array.categorias_array, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(catAdapter);

        ArrayAdapter<CharSequence> estAdapter = ArrayAdapter.createFromResource(this,
                R.array.estados_array, android.R.layout.simple_spinner_item);
        estAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(estAdapter);

        AlertDialog dialog = builder.create();

        // BOTONES DEL DIÁLOGO - verifica que estos IDs existan
        Button btnCancelar = view.findViewById(R.id.btnCancelarEquipo);
        Button btnCrear = view.findViewById(R.id.btnCrearEquipo);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnCrear.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String marca = etMarca.getText().toString().trim();
            String modelo = etModelo.getText().toString().trim();
            String cantidadStr = etCantidad.getText().toString().trim();
            String ubicacion = etUbicacion.getText().toString().trim();
            String categoria = spinnerCategoria.getSelectedItem().toString();
            String estado = spinnerEstado.getSelectedItem().toString();

            if (validarDatos(nombre, marca, cantidadStr)) {
                int cantidad = Integer.parseInt(cantidadStr);
                crearEquipo(nombre, marca, modelo, cantidad, ubicacion, categoria, estado);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean validarDatos(String nombre, String marca, String cantidadStr) {
        if (nombre.isEmpty() || marca.isEmpty() || cantidadStr.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            int cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                Toast.makeText(this, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void crearEquipo(String nombre, String marca, String modelo, int cantidad,
                             String ubicacion, String categoria, String estado) {
        progressDialog.setMessage("Creando equipo...");
        progressDialog.show();

        String id = UUID.randomUUID().toString();
        Equipo equipo = new Equipo(
                id, nombre, categoria, marca, modelo, estado, ubicacion,
                cantidad, cantidad, "" // QR vacío por ahora
        );

        databaseReference.child(id).setValue(equipo)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Equipo creado exitosamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al crear equipo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarDialogoEditarEquipo(Equipo equipo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_editar_equipo, null);
        builder.setView(view);

        // INICIALIZAR VISTAS DEL DIÁLOGO DE EDICIÓN
        EditText etNombre = view.findViewById(R.id.etNombreEquipo);
        EditText etMarca = view.findViewById(R.id.etMarcaEquipo);
        EditText etModelo = view.findViewById(R.id.etModeloEquipo);
        EditText etCantidad = view.findViewById(R.id.etCantidadEquipo);
        EditText etUbicacion = view.findViewById(R.id.etUbicacionEquipo);
        Spinner spinnerCategoria = view.findViewById(R.id.spinnerCategoriaEquipo);
        Spinner spinnerEstado = view.findViewById(R.id.spinnerEstadoEquipo);

        // Llenar campos con datos actuales
        etNombre.setText(equipo.getNombre());
        etMarca.setText(equipo.getMarca());
        etModelo.setText(equipo.getModelo());
        etCantidad.setText(String.valueOf(equipo.getCantidad()));
        etUbicacion.setText(equipo.getUbicacion());

        // Configurar spinners
        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(this,
                R.array.categorias_array, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(catAdapter);

        ArrayAdapter<CharSequence> estAdapter = ArrayAdapter.createFromResource(this,
                R.array.estados_array, android.R.layout.simple_spinner_item);
        estAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(estAdapter);

        // Seleccionar valores actuales
        int catPosition = catAdapter.getPosition(equipo.getCategoria());
        if (catPosition >= 0) spinnerCategoria.setSelection(catPosition);

        int estPosition = estAdapter.getPosition(equipo.getEstado());
        if (estPosition >= 0) spinnerEstado.setSelection(estPosition);

        AlertDialog dialog = builder.create();

        // BOTONES DEL DIÁLOGO DE EDICIÓN - usar IDs correctos
        Button btnCancelar = view.findViewById(R.id.btnCancelarEquipo);
        Button btnGuardar = view.findViewById(R.id.btnGuardarEquipo);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = etNombre.getText().toString().trim();
            String nuevaMarca = etMarca.getText().toString().trim();
            String nuevoModelo = etModelo.getText().toString().trim();
            String nuevaCantidadStr = etCantidad.getText().toString().trim();
            String nuevaUbicacion = etUbicacion.getText().toString().trim();
            String nuevaCategoria = spinnerCategoria.getSelectedItem().toString();
            String nuevoEstado = spinnerEstado.getSelectedItem().toString();

            if (validarDatosEdicion(nuevoNombre, nuevaMarca, nuevaCantidadStr)) {
                int nuevaCantidad = Integer.parseInt(nuevaCantidadStr);
                actualizarEquipo(equipo, nuevoNombre, nuevaMarca, nuevoModelo, nuevaCantidad,
                        nuevaUbicacion, nuevaCategoria, nuevoEstado);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // Método de validación para edición
    private boolean validarDatosEdicion(String nombre, String marca, String cantidadStr) {
        if (nombre.isEmpty() || marca.isEmpty() || cantidadStr.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            int cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                Toast.makeText(this, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void actualizarEquipo(Equipo equipo, String nombre, String marca, String modelo,
                                  int cantidad, String ubicacion, String categoria, String estado) {
        progressDialog.setMessage("Actualizando equipo...");
        progressDialog.show();

        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("marca", marca);
        updates.put("modelo", modelo);
        updates.put("cantidad", cantidad);
        updates.put("disponibles", cantidad); // Reset disponibles
        updates.put("ubicacion", ubicacion);
        updates.put("categoria", categoria);
        updates.put("estado", estado);

        databaseReference.child(equipo.getId()).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Equipo actualizado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarDialogoConfirmarEliminacion(Equipo equipo) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de eliminar el equipo " + equipo.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarEquipo(equipo);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarEquipo(Equipo equipo) {
        progressDialog.setMessage("Eliminando equipo...");
        progressDialog.show();

        databaseReference.child(equipo.getId()).removeValue()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Equipo eliminado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}