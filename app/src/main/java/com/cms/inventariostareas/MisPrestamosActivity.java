package com.cms.inventariostareas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.List;

public class MisPrestamosActivity extends AppCompatActivity implements PrestamosAdapter.OnPrestamoClickListener {

    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private FirebaseUser currentUser;
    private RecyclerView recyclerView;
    private PrestamosAdapter adapter;
    private List<Prestamo> listaPrestamos;
    private List<Prestamo> listaPrestamosCompleta;
    private TextView tvEmpty;
    private Spinner spinnerFiltro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_prestamos);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        listaPrestamos = new ArrayList<>();
        listaPrestamosCompleta = new ArrayList<>();

        Button btnVolver = findViewById(R.id.btnVolver);
        spinnerFiltro = findViewById(R.id.spinnerFiltroEstado);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView = findViewById(R.id.recyclerViewPrestamos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // CORREGIDO: Pasar this como listener
        adapter = new PrestamosAdapter(listaPrestamos, this);
        recyclerView.setAdapter(adapter);

        btnVolver.setOnClickListener(v -> finish());

        // Configurar spinner de filtro
        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filtro = parent.getItemAtPosition(position).toString();
                filtrarPrestamos(filtro);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarTodosPrestamos();
    }

    // Implementar el método del listener
    @Override
    public void onPrestamoClick(Prestamo prestamo) {
        // Aquí decides qué hacer cuando se hace clic en un préstamo
        // Por ejemplo, mostrar detalles o acciones disponibles
        mostrarOpcionesPrestamo(prestamo);
    }

    private void mostrarOpcionesPrestamo(Prestamo prestamo) {
        // Implementar lógica según el estado del préstamo
        switch (prestamo.getEstado()) {
            case "aprobado":
                // Mostrar opción para registrar devolución
                Toast.makeText(this, "Préstamo activo: " + prestamo.getEquipoNombre(), Toast.LENGTH_SHORT).show();
                break;
            case "pendiente":
                Toast.makeText(this, "Préstamo pendiente de aprobación", Toast.LENGTH_SHORT).show();
                break;
            case "devuelto":
                Toast.makeText(this, "Préstamo ya devuelto", Toast.LENGTH_SHORT).show();
                break;
            case "rechazado":
                Toast.makeText(this, "Préstamo rechazado", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void cargarTodosPrestamos() {
        progressDialog.show();
        if (currentUser != null) {
            databaseReference.child("prestamos")
                    .orderByChild("instructorId")
                    .equalTo(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            listaPrestamosCompleta.clear();
                            listaPrestamos.clear();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Prestamo prestamo = snapshot.getValue(Prestamo.class);
                                if (prestamo != null) {
                                    prestamo.setId(snapshot.getKey());
                                    listaPrestamosCompleta.add(prestamo);
                                    listaPrestamos.add(prestamo); // Mostrar todos inicialmente
                                }
                            }

                            adapter.notifyDataSetChanged();
                            progressDialog.dismiss();

                            // Mostrar mensaje si no hay préstamos
                            if (listaPrestamos.isEmpty()) {
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
                            Toast.makeText(MisPrestamosActivity.this, "Error al cargar préstamos", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void filtrarPrestamos(String filtro) {
        listaPrestamos.clear();

        if (filtro.equals("Todos")) {
            listaPrestamos.addAll(listaPrestamosCompleta);
        } else {
            for (Prestamo prestamo : listaPrestamosCompleta) {
                if (filtro.equalsIgnoreCase(prestamo.getEstado())) {
                    listaPrestamos.add(prestamo);
                }
            }
        }

        adapter.notifyDataSetChanged();

        // Mostrar mensaje si no hay préstamos después del filtro
        if (listaPrestamos.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar préstamos al volver a la actividad
        cargarTodosPrestamos();
    }
}