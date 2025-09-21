package com.cms.inventariostareas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cms.inventariostareas.adapters.PrestamosVisualizacionAdapter;
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

public class Instructor_DashboardActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private FirebaseUser currentUser;

    private TextView tvBienvenida, tvPrestamosActivos, tvPrestamosPendientes;
    private RecyclerView recyclerViewPrestamos;
    private PrestamosVisualizacionAdapter adapter; // CAMBIADO
    private List<Prestamo> listaPrestamos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_dashboard);

        // Inicializar Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas
        tvBienvenida = findViewById(R.id.tvBienvenida);
        tvPrestamosActivos = findViewById(R.id.tvPrestamosActivos);
        tvPrestamosPendientes = findViewById(R.id.tvPrestamosPendientes);

        Button btnSolicitarPrestamo = findViewById(R.id.btnSolicitarPrestamo);
        Button btnMisPrestamos = findViewById(R.id.btnMisPrestamos);
        Button btnInventario = findViewById(R.id.btnInventario);
        Button btnDevoluciones = findViewById(R.id.btnDevoluciones);
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        // Configurar RecyclerView - CORREGIDO
        recyclerViewPrestamos = findViewById(R.id.recyclerViewPrestamos);
        recyclerViewPrestamos.setLayoutManager(new LinearLayoutManager(this));
        listaPrestamos = new ArrayList<>();
        adapter = new PrestamosVisualizacionAdapter(listaPrestamos); // NUEVO ADAPTER
        recyclerViewPrestamos.setAdapter(adapter);

        // Listeners
        btnSolicitarPrestamo.setOnClickListener(v -> {
            startActivity(new Intent(Instructor_DashboardActivity.this, SolicitarPrestamoActivity.class));
        });

        btnMisPrestamos.setOnClickListener(v -> {
            startActivity(new Intent(Instructor_DashboardActivity.this, MisPrestamosActivity.class));
        });

        btnInventario.setOnClickListener(v -> {
            startActivity(new Intent(Instructor_DashboardActivity.this, InventarioConsultaActivity.class));
        });

        btnDevoluciones.setOnClickListener(v -> {
            startActivity(new Intent(Instructor_DashboardActivity.this, DevolucionEquipoActivity.class));
        });

        btnCerrarSesion.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(Instructor_DashboardActivity.this, MainActivity.class));
            finish();
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarDatosUsuario();
        cargarPrestamosRecientes();
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
                                tvBienvenida.setText("Bienvenido, " + nombre + " " + apellido);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(Instructor_DashboardActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void cargarPrestamosRecientes() {
        progressDialog.show();

        if (currentUser != null) {
            databaseReference.child("prestamos")
                    .orderByChild("instructorId")
                    .equalTo(currentUser.getUid())
                    .limitToLast(5)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            listaPrestamos.clear();
                            int activos = 0;
                            int pendientes = 0;

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Prestamo prestamo = snapshot.getValue(Prestamo.class);
                                if (prestamo != null) {
                                    prestamo.setId(snapshot.getKey());
                                    listaPrestamos.add(prestamo);

                                    // Contar por estado
                                    if ("aprobado".equals(prestamo.getEstado())) {
                                        activos++;
                                    } else if ("pendiente".equals(prestamo.getEstado())) {
                                        pendientes++;
                                    }
                                }
                            }

                            tvPrestamosActivos.setText("Activos: " + activos);
                            tvPrestamosPendientes.setText("Pendientes: " + pendientes);
                            adapter.actualizarLista(listaPrestamos); // ACTUALIZAR CON NUEVO MÉTODO
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressDialog.dismiss();
                            Toast.makeText(Instructor_DashboardActivity.this, "Error al cargar préstamos", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar datos cuando la actividad se reanude
        cargarPrestamosRecientes();
    }
}