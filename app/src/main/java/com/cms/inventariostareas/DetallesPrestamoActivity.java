package com.cms.inventariostareas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cms.inventariostareas.models.Prestamo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetallesPrestamoActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private String prestamoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_prestamo);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        prestamoId = getIntent().getStringExtra("prestamo_id");

        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnAprobar = findViewById(R.id.btnAprobar);
        Button btnRechazar = findViewById(R.id.btnRechazar);

        btnVolver.setOnClickListener(v -> finish());

        if (btnAprobar != null && btnRechazar != null) {
            btnAprobar.setOnClickListener(v -> aprobarPrestamo());
            btnRechazar.setOnClickListener(v -> rechazarPrestamo());
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando detalles...");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarDetallesPrestamo();
    }

    private void cargarDetallesPrestamo() {
        if (prestamoId == null) {
            Toast.makeText(this, "Error: ID de préstamo no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressDialog.show();

        databaseReference.child("prestamos").child(prestamoId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Prestamo prestamo = dataSnapshot.getValue(Prestamo.class);
                            if (prestamo != null) {
                                mostrarDetalles(prestamo);
                            }
                        } else {
                            Toast.makeText(DetallesPrestamoActivity.this,
                                    "Préstamo no encontrado", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(DetallesPrestamoActivity.this,
                                "Error al cargar detalles", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarDetalles(Prestamo prestamo) {
        TextView tvEquipo = findViewById(R.id.tvEquipo);
        TextView tvCantidad = findViewById(R.id.tvCantidad);
        TextView tvInstructor = findViewById(R.id.tvInstructor);
        TextView tvFecha = findViewById(R.id.tvFecha);
        TextView tvEstado = findViewById(R.id.tvEstado);
        TextView tvSupervisor = findViewById(R.id.tvSupervisor);

        if (tvEquipo != null) tvEquipo.setText(prestamo.getEquipoNombre());
        if (tvCantidad != null) tvCantidad.setText("Cantidad: " + prestamo.getCantidad());
        if (tvInstructor != null) tvInstructor.setText("Instructor: " + prestamo.getInstructorNombre());
        if (tvFecha != null) tvFecha.setText("Fecha: " + prestamo.getFechaPrestamo());
        if (tvEstado != null) tvEstado.setText("Estado: " + prestamo.getEstado());
        if (tvSupervisor != null && prestamo.getSupervisorNombre() != null) {
            tvSupervisor.setText("Supervisor: " + prestamo.getSupervisorNombre());
        }
    }

    private void aprobarPrestamo() {
        // Lógica para aprobar préstamo
        Toast.makeText(this, "Funcionalidad de aprobación", Toast.LENGTH_SHORT).show();
    }

    private void rechazarPrestamo() {
        // Lógica para rechazar préstamo
        Toast.makeText(this, "Funcionalidad de rechazo", Toast.LENGTH_SHORT).show();
    }
}