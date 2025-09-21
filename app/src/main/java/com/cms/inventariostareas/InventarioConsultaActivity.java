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

import com.cms.inventariostareas.adapters.EquiposInstructorAdapter;
import com.cms.inventariostareas.adapters.MovimientosInventarioAdapter;
import com.cms.inventariostareas.models.Equipo;
import com.cms.inventariostareas.models.MovimientoInventario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InventarioConsultaActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private FirebaseUser currentUser;

    private RecyclerView recyclerViewEquipos, recyclerViewMovimientos;
    private EquiposInstructorAdapter equiposAdapter;
    private MovimientosInventarioAdapter movimientosAdapter;
    private List<Equipo> listaEquipos;
    private List<MovimientoInventario> listaMovimientos;

    private TextView tvTotalEquipos, tvDisponibles, tvEmptyEquipos, tvEmptyMovimientos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario_consulta);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas
        Button btnVolver = findViewById(R.id.btnVolver);
        tvTotalEquipos = findViewById(R.id.tvTotalEquipos);
        tvDisponibles = findViewById(R.id.tvDisponibles);
        tvEmptyEquipos = findViewById(R.id.tvEmptyEquipos);
        tvEmptyMovimientos = findViewById(R.id.tvEmptyMovimientos);

        // Configurar RecyclerView para equipos
        recyclerViewEquipos = findViewById(R.id.recyclerViewEquipos);
        recyclerViewEquipos.setLayoutManager(new LinearLayoutManager(this));
        listaEquipos = new ArrayList<>();
        equiposAdapter = new EquiposInstructorAdapter(listaEquipos);
        recyclerViewEquipos.setAdapter(equiposAdapter);

        // Configurar RecyclerView para movimientos
        recyclerViewMovimientos = findViewById(R.id.recyclerViewMovimientos);
        recyclerViewMovimientos.setLayoutManager(new LinearLayoutManager(this));
        listaMovimientos = new ArrayList<>();
        movimientosAdapter = new MovimientosInventarioAdapter(listaMovimientos);
        recyclerViewMovimientos.setAdapter(movimientosAdapter);

        btnVolver.setOnClickListener(v -> finish());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando inventario");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarInventario();
        cargarMovimientosRecientes();
    }

    private void cargarInventario() {
        progressDialog.show();

        databaseReference.child("equipos")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        listaEquipos.clear();
                        int totalEquipos = 0;
                        int totalDisponibles = 0;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Equipo equipo = snapshot.getValue(Equipo.class);
                            if (equipo != null) {
                                equipo.setId(snapshot.getKey());
                                listaEquipos.add(equipo);

                                totalEquipos += equipo.getCantidad();
                                totalDisponibles += equipo.getDisponibles();
                            }
                        }

                        // Actualizar estadísticas
                        tvTotalEquipos.setText("Total equipos: " + totalEquipos);
                        tvDisponibles.setText("Disponibles: " + totalDisponibles);

                        equiposAdapter.actualizarLista(listaEquipos);

                        if (listaEquipos.isEmpty()) {
                            tvEmptyEquipos.setVisibility(View.VISIBLE);
                            recyclerViewEquipos.setVisibility(View.GONE);
                        } else {
                            tvEmptyEquipos.setVisibility(View.GONE);
                            recyclerViewEquipos.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(InventarioConsultaActivity.this,
                                "Error al cargar inventario", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarMovimientosRecientes() {
        databaseReference.child("movimientos")
                .orderByChild("fecha")
                .limitToLast(10) // Últimos 10 movimientos
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        listaMovimientos.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            MovimientoInventario movimiento = snapshot.getValue(MovimientoInventario.class);
                            if (movimiento != null) {
                                movimiento.setId(snapshot.getKey());
                                listaMovimientos.add(0, movimiento); // Añadir al inicio para orden inverso
                            }
                        }

                        movimientosAdapter.notifyDataSetChanged();
                        progressDialog.dismiss();

                        if (listaMovimientos.isEmpty()) {
                            tvEmptyMovimientos.setVisibility(View.VISIBLE);
                            recyclerViewMovimientos.setVisibility(View.GONE);
                        } else {
                            tvEmptyMovimientos.setVisibility(View.GONE);
                            recyclerViewMovimientos.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(InventarioConsultaActivity.this,
                                "Error al cargar movimientos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos al volver a la actividad
        cargarInventario();
        cargarMovimientosRecientes();
    }
}