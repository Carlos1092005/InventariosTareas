package com.cms.inventariostareas;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Administrador_DashboardActivity extends AppCompatActivity {
    // Views
    LinearLayout lyGestionUsuarios, lyGestionEquipos, lyGestionInventario, lyReportes, lyConfiguracionSistema;
    Button adminVolver;
    TextView tvTotalUsuarios, tvTotalEquipos, tvPrestamosActivos, tvEquiposDisponibles;

    // Firebase
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    // Variables
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrador_dashboard);

        // Inicializar Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas
        inicializarVistas();

        // Configurar listeners
        configurarListeners();

        // Cargar estadísticas
        cargarEstadisticas();
    }

    private void inicializarVistas() {
        adminVolver = findViewById(R.id.adminVolver);
        lyGestionUsuarios = findViewById(R.id.lyGestionUsuarios);
        lyGestionEquipos = findViewById(R.id.lyGestionEquipos);
        lyGestionInventario = findViewById(R.id.lyGestionInventario);
        lyReportes = findViewById(R.id.lyReportes);
        lyConfiguracionSistema = findViewById(R.id.lyConfiguracionSistema);

        // TextViews para estadísticas
        tvTotalUsuarios = findViewById(R.id.tvTotalUsuarios);
        tvTotalEquipos = findViewById(R.id.tvTotalEquipos);
        tvPrestamosActivos = findViewById(R.id.tvPrestamosActivos);
        tvEquiposDisponibles = findViewById(R.id.tvEquiposDisponibles);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando");
        progressDialog.setMessage("Espere por favor...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void configurarListeners() {
        // Cerrar sesión
        adminVolver.setOnClickListener(v -> {
            firebaseAuth.signOut();
            startActivity(new Intent(Administrador_DashboardActivity.this, MainActivity.class));
            finish();
        });

        // Navegación a diferentes módulos
        lyGestionUsuarios.setOnClickListener(v -> {
            startActivity(new Intent(Administrador_DashboardActivity.this, GestionUsuariosActivity.class));
        });

        lyGestionEquipos.setOnClickListener(v -> {
            startActivity(new Intent(Administrador_DashboardActivity.this, GestionEquiposActivity.class));
        });

        lyGestionInventario.setOnClickListener(v -> {
            startActivity(new Intent(Administrador_DashboardActivity.this, GestionInventarioActivity.class));
        });

        lyReportes.setOnClickListener(v -> {
            startActivity(new Intent(Administrador_DashboardActivity.this, ReportesActivity.class));
        });

        lyConfiguracionSistema.setOnClickListener(v -> {
            startActivity(new Intent(Administrador_DashboardActivity.this, ConfiguracionSistemaActivity.class));
        });
    }

    private void cargarEstadisticas() {
        progressDialog.show();

        // Cargar total de usuarios
        databaseReference.child("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long totalUsuarios = dataSnapshot.getChildrenCount();
                tvTotalUsuarios.setText(String.valueOf(totalUsuarios));

                // Cargar total de equipos
                cargarTotalEquipos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(Administrador_DashboardActivity.this,
                        "Error al cargar usuarios: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarTotalEquipos() {
        databaseReference.child("equipos").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long totalEquipos = 0;
                long equiposDisponibles = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Integer cantidad = snapshot.child("cantidad").getValue(Integer.class);
                    Integer disponibles = snapshot.child("disponibles").getValue(Integer.class);

                    if (cantidad != null) totalEquipos += cantidad;
                    if (disponibles != null) equiposDisponibles += disponibles;
                }

                tvTotalEquipos.setText(String.valueOf(totalEquipos));
                tvEquiposDisponibles.setText(String.valueOf(equiposDisponibles));

                // Cargar préstamos activos - CORREGIDO
                cargarPrestamosActivos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(Administrador_DashboardActivity.this,
                        "Error al cargar equipos: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarPrestamosActivos() {
        // CORRECCIÓN: usar "aprobado" en lugar de "activo"
        databaseReference.child("prestamos").orderByChild("estado").equalTo("aprobado")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long prestamosActivos = dataSnapshot.getChildrenCount();
                        tvPrestamosActivos.setText(String.valueOf(prestamosActivos));
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(Administrador_DashboardActivity.this,
                                "Error al cargar préstamos: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar estadísticas cuando la actividad se reanude
        cargarEstadisticas();
    }
}