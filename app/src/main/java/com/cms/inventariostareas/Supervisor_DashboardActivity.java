package com.cms.inventariostareas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

public class Supervisor_DashboardActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private FirebaseUser currentUser;

    private TextView tvBienvenida, tvPrestamosPendientes, tvPrestamosRevisados;
    private RecyclerView recyclerViewPrestamos;
    private PrestamosAdapter adapter;
    private List<Prestamo> listaPrestamosPendientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_dashboard);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas
        tvBienvenida = findViewById(R.id.tvBienvenida);
        tvPrestamosPendientes = findViewById(R.id.tvPrestamosPendientes);
        tvPrestamosRevisados = findViewById(R.id.tvPrestamosRevisados);

        Button btnAprobarPrestamos = findViewById(R.id.btnAprobarPrestamos);
        Button btnHistorial = findViewById(R.id.btnHistorialPrestamos);
        Button btnReportes = findViewById(R.id.btnReportesSupervisor);
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        // Configurar RecyclerView
        recyclerViewPrestamos = findViewById(R.id.recyclerViewPrestamos);
        recyclerViewPrestamos.setLayoutManager(new LinearLayoutManager(this));
        listaPrestamosPendientes = new ArrayList<>();
        adapter = new PrestamosAdapter(listaPrestamosPendientes, this::onPrestamoClick);
        recyclerViewPrestamos.setAdapter(adapter);

        // Listeners de botones
        btnAprobarPrestamos.setOnClickListener(v -> {
            startActivity(new Intent(Supervisor_DashboardActivity.this, AprobarPrestamosActivity.class));
        });

        btnHistorial.setOnClickListener(v -> {
            startActivity(new Intent(Supervisor_DashboardActivity.this, HistorialPrestamosActivity.class));
        });

        btnReportes.setOnClickListener(v -> {
            startActivity(new Intent(Supervisor_DashboardActivity.this, ReportesSupervisorActivity.class));
        });

        btnCerrarSesion.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(Supervisor_DashboardActivity.this, MainActivity.class));
            finish();
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando dashboard...");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarDatosUsuario();
        cargarPrestamosPendientes();
        cargarEstadisticas();
    }

    private void cargarDatosUsuario() {
        if (currentUser != null) {
            databaseReference.child("usuarios").child(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String nombre = dataSnapshot.child("nombre").getValue(String.class);
                                String apellido = dataSnapshot.child("apellido").getValue(String.class);
                                tvBienvenida.setText("Supervisor: " + nombre + " " + apellido);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(Supervisor_DashboardActivity.this,
                                    "Error al cargar datos de usuario", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void cargarPrestamosPendientes() {
        databaseReference.child("prestamos")
                .orderByChild("estado")
                .equalTo("pendiente")
                .limitToLast(5)
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
                        tvPrestamosPendientes.setText("Pendientes: " + listaPrestamosPendientes.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(Supervisor_DashboardActivity.this,
                                "Error al cargar préstamos pendientes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarEstadisticas() {
        // Cargar estadísticas de préstamos revisados (aprobados + rechazados)
        databaseReference.child("prestamos")
                .orderByChild("supervisorId")
                .equalTo(currentUser != null ? currentUser.getUid() : "")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int revisados = 0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Prestamo prestamo = snapshot.getValue(Prestamo.class);
                            if (prestamo != null && !"pendiente".equals(prestamo.getEstado())) {
                                revisados++;
                            }
                        }
                        tvPrestamosRevisados.setText("Revisados: " + revisados);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(Supervisor_DashboardActivity.this,
                                "Error al cargar estadísticas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onPrestamoClick(Prestamo prestamo) {
        // Al hacer clic en un préstamo pendiente, ir a aprobación
        Intent intent = new Intent(this, AprobarPrestamosActivity.class);
        intent.putExtra("prestamo_id", prestamo.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPrestamosPendientes();
        cargarEstadisticas();
    }
}