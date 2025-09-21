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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cms.inventariostareas.adapters.PrestamosAdapter;
import com.cms.inventariostareas.models.Prestamo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class DevolucionEquipoActivity extends AppCompatActivity implements PrestamosAdapter.OnPrestamoClickListener {

    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private FirebaseUser currentUser;
    private RecyclerView recyclerView;
    private PrestamosAdapter adapter;
    private List<Prestamo> listaPrestamosActivos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devolucion_equipo);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        listaPrestamosActivos = new ArrayList<>();

        Button btnVolver = findViewById(R.id.btnVolver);
        TextView tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView = findViewById(R.id.recyclerViewPrestamos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // CORREGIDO: Pasar el listener de clics
        adapter = new PrestamosAdapter(listaPrestamosActivos, this);
        recyclerView.setAdapter(adapter);

        btnVolver.setOnClickListener(v -> finish());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarPrestamosActivos();
    }

    // CORREGIDO: Implementar la interfaz del listener
    @Override
    public void onPrestamoClick(Prestamo prestamo) {
        mostrarDialogoDevolucion(prestamo);
    }

    private void cargarPrestamosActivos() {
        progressDialog.show();
        if (currentUser != null) {
            databaseReference.child("prestamos")
                    .orderByChild("instructorId")
                    .equalTo(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            listaPrestamosActivos.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Prestamo prestamo = snapshot.getValue(Prestamo.class);
                                if (prestamo != null && "aprobado".equals(prestamo.getEstado())) {
                                    prestamo.setId(snapshot.getKey());
                                    listaPrestamosActivos.add(prestamo);
                                }
                            }

                            adapter.notifyDataSetChanged();
                            progressDialog.dismiss();

                            if (listaPrestamosActivos.isEmpty()) {
                                findViewById(R.id.tvEmpty).setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                findViewById(R.id.tvEmpty).setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressDialog.dismiss();
                            Toast.makeText(DevolucionEquipoActivity.this, "Error al cargar préstamos", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void mostrarDialogoDevolucion(Prestamo prestamo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_devolucion, null);
        builder.setView(view);

        TextView tvEquipo = view.findViewById(R.id.tvEquipoDevolucion);
        EditText etCondicion = view.findViewById(R.id.etCondicionEquipo);

        // CORREGIDO: Remover spinner innecesario
        // Spinner spinnerEstado = view.findViewById(R.id.spinnerEstadoDevolucion);

        tvEquipo.setText("Devolver: " + prestamo.getEquipoNombre());

        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnCancelarDevolucion).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnConfirmarDevolucion).setOnClickListener(v -> {
            String condicion = etCondicion.getText().toString().trim();

            if (condicion.isEmpty()) {
                Toast.makeText(this, "Describa la condición del equipo", Toast.LENGTH_SHORT).show();
                return;
            }

            registrarDevolucion(prestamo, condicion);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void registrarDevolucion(Prestamo prestamo, String condicion) {
        progressDialog.setMessage("Registrando devolución...");
        progressDialog.show();

        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "devuelto");
        updates.put("condicionEquipo", condicion);
        updates.put("fechaDevolucion", fecha);

        // Actualizar préstamo
        databaseReference.child("prestamos").child(prestamo.getId()).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // CORREGIDO: Actualizar inventario correctamente
                        actualizarInventario(prestamo.getEquipoId(), prestamo.getCantidad());
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error al registrar devolución", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarInventario(String equipoId, int cantidadDevuelta) {
        // Obtener el valor actual de disponibles y aumentarlo
        databaseReference.child("equipos").child(equipoId).child("disponibles")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Integer disponiblesActuales = dataSnapshot.getValue(Integer.class);
                            if (disponiblesActuales != null) {
                                int nuevosDisponibles = disponiblesActuales + cantidadDevuelta;

                                // Actualizar el valor
                                databaseReference.child("equipos").child(equipoId)
                                        .child("disponibles").setValue(nuevosDisponibles)
                                        .addOnCompleteListener(task -> {
                                            progressDialog.dismiss();
                                            if (task.isSuccessful()) {
                                                Toast.makeText(DevolucionEquipoActivity.this,
                                                        "Devolución registrada exitosamente",
                                                        Toast.LENGTH_SHORT).show();
                                                cargarPrestamosActivos(); // Recargar lista
                                            } else {
                                                Toast.makeText(DevolucionEquipoActivity.this,
                                                        "Error al actualizar inventario",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(DevolucionEquipoActivity.this,
                                "Error al actualizar inventario",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}