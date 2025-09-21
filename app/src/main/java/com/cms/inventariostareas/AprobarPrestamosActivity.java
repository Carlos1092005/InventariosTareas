package com.cms.inventariostareas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class AprobarPrestamosActivity extends AppCompatActivity implements PrestamosAdapter.OnPrestamoClickListener {

    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private FirebaseUser currentUser;
    private RecyclerView recyclerView;
    private PrestamosAdapter adapter;
    private List<Prestamo> listaPrestamosPendientes;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aprobar_prestamos);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Button btnVolver = findViewById(R.id.btnVolver);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView = findViewById(R.id.recyclerViewPrestamos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaPrestamosPendientes = new ArrayList<>();
        adapter = new PrestamosAdapter(listaPrestamosPendientes, this);
        recyclerView.setAdapter(adapter);

        btnVolver.setOnClickListener(v -> finish());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando préstamos pendientes...");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarPrestamosPendientes();
    }

    private void cargarPrestamosPendientes() {
        progressDialog.show();

        databaseReference.child("prestamos")
                .orderByChild("estado")
                .equalTo("pendiente")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        listaPrestamosPendientes.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Prestamo prestamo = snapshot.getValue(Prestamo.class);
                            if (prestamo != null) {
                                prestamo.setId(snapshot.getKey());
                                listaPrestamosPendientes.add(prestamo);
                            }
                        }

                        adapter.notifyDataSetChanged();
                        progressDialog.dismiss();

                        if (listaPrestamosPendientes.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(AprobarPrestamosActivity.this,
                                "Error al cargar préstamos: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onPrestamoClick(Prestamo prestamo) {
        mostrarDialogoAprobacion(prestamo);
    }

    private void mostrarDialogoAprobacion(Prestamo prestamo) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Revisar Préstamo");
        builder.setMessage("¿Qué acción deseas realizar con el préstamo de " + prestamo.getEquipoNombre() + "?");

        builder.setPositiveButton("Aprobar", (dialog, which) -> {
            aprobarPrestamo(prestamo);
        });

        builder.setNegativeButton("Rechazar", (dialog, which) -> {
            rechazarPrestamo(prestamo);
        });

        builder.setNeutralButton("Cancelar", null);

        builder.show();
    }

    private void aprobarPrestamo(Prestamo prestamo) {
        progressDialog.setMessage("Aprobando préstamo...");
        progressDialog.show();

        String fechaAprobacion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "aprobado");
        updates.put("supervisorId", currentUser.getUid());
        updates.put("supervisorNombre", "Supervisor"); // Se actualizará con el nombre real
        updates.put("fechaAprobacion", fechaAprobacion);

        databaseReference.child("prestamos").child(prestamo.getId()).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        actualizarInventarioAprobacion(prestamo);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error al aprobar préstamo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarInventarioAprobacion(Prestamo prestamo) {
        databaseReference.child("equipos").child(prestamo.getEquipoId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Integer disponibles = dataSnapshot.child("disponibles").getValue(Integer.class);
                            if (disponibles != null && disponibles >= prestamo.getCantidad()) {
                                int nuevosDisponibles = disponibles - prestamo.getCantidad();

                                databaseReference.child("equipos").child(prestamo.getEquipoId())
                                        .child("disponibles").setValue(nuevosDisponibles)
                                        .addOnCompleteListener(task -> {
                                            progressDialog.dismiss();
                                            if (task.isSuccessful()) {
                                                Toast.makeText(AprobarPrestamosActivity.this,
                                                        "Préstamo aprobado exitosamente", Toast.LENGTH_SHORT).show();
                                                cargarPrestamosPendientes();
                                            } else {
                                                Toast.makeText(AprobarPrestamosActivity.this,
                                                        "Error al actualizar inventario", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(AprobarPrestamosActivity.this,
                                        "No hay suficientes equipos disponibles", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                    }
                });
    }

    private void rechazarPrestamo(Prestamo prestamo) {
        progressDialog.setMessage("Rechazando préstamo...");
        progressDialog.show();

        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "rechazado");
        updates.put("supervisorId", currentUser.getUid());
        updates.put("supervisorNombre", "Supervisor");

        databaseReference.child("prestamos").child(prestamo.getId()).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Préstamo rechazado", Toast.LENGTH_SHORT).show();
                        cargarPrestamosPendientes();
                    } else {
                        Toast.makeText(this, "Error al rechazar préstamo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPrestamosPendientes();
    }
}