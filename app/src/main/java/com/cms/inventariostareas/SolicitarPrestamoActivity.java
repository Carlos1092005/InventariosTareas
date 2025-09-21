package com.cms.inventariostareas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import com.cms.inventariostareas.models.Equipo;
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
import java.util.UUID;

public class SolicitarPrestamoActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private FirebaseUser currentUser;
    private List<Equipo> listaEquiposDisponibles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitar_prestamo);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        listaEquiposDisponibles = new ArrayList<>();

        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnSolicitar = findViewById(R.id.btnSolicitarPrestamo);

        btnVolver.setOnClickListener(v -> finish());
        btnSolicitar.setOnClickListener(v -> mostrarDialogoSolicitud());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarEquiposDisponibles();
    }

    private void cargarEquiposDisponibles() {
        progressDialog.show();
        databaseReference.child("equipos")
                .orderByChild("estado").equalTo("Disponible")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        listaEquiposDisponibles.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Equipo equipo = snapshot.getValue(Equipo.class);
                            if (equipo != null && equipo.getDisponibles() > 0) {
                                equipo.setId(snapshot.getKey());
                                listaEquiposDisponibles.add(equipo);
                            }
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(SolicitarPrestamoActivity.this, "Error al cargar equipos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarDialogoSolicitud() {
        if (listaEquiposDisponibles.isEmpty()) {
            Toast.makeText(this, "No hay equipos disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_solicitar_prestamo, null);
        builder.setView(view);

        Spinner spinnerEquipos = view.findViewById(R.id.spinnerEquipos);
        EditText etCantidad = view.findViewById(R.id.etCantidadPrestamo);
        TextView tvDisponibles = view.findViewById(R.id.tvDisponibles);

        // Configurar spinner
        ArrayAdapter<Equipo> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listaEquiposDisponibles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEquipos.setAdapter(adapter);

        // Mostrar disponibles del primer equipo
        if (!listaEquiposDisponibles.isEmpty()) {
            tvDisponibles.setText("Disponibles: " + listaEquiposDisponibles.get(0).getDisponibles());
        }

        spinnerEquipos.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Equipo equipo = (Equipo) parent.getSelectedItem();
                tvDisponibles.setText("Disponibles: " + equipo.getDisponibles());
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnCancelarPrestamo).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnConfirmarPrestamo).setOnClickListener(v -> {
            Equipo equipo = (Equipo) spinnerEquipos.getSelectedItem();
            String cantidadStr = etCantidad.getText().toString().trim();

            if (validarSolicitud(equipo, cantidadStr)) {
                int cantidad = Integer.parseInt(cantidadStr);
                solicitarPrestamo(equipo, cantidad);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean validarSolicitud(Equipo equipo, String cantidadStr) {
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
            if (cantidad > equipo.getDisponibles()) {
                Toast.makeText(this, "No hay suficientes unidades disponibles", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void solicitarPrestamo(Equipo equipo, int cantidad) {
        progressDialog.setMessage("Enviando solicitud...");
        progressDialog.show();

        String prestamoId = UUID.randomUUID().toString();
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Obtener nombre del instructor
        databaseReference.child("usuarios").child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String nombreInstructor = dataSnapshot.child("nombre").getValue(String.class) + " " +
                                dataSnapshot.child("apellido").getValue(String.class);

                        Prestamo prestamo = new Prestamo(
                                prestamoId, equipo.getId(), equipo.getNombre(), cantidad,
                                currentUser.getUid(), nombreInstructor, fecha,
                                "", "pendiente", null, null, null, ""
                        );

                        databaseReference.child("prestamos").child(prestamoId).setValue(prestamo)
                                .addOnCompleteListener(task -> {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SolicitarPrestamoActivity.this,
                                                "Solicitud enviada para aprobación", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(SolicitarPrestamoActivity.this,
                                                "Error al enviar solicitud", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(SolicitarPrestamoActivity.this, "Error al obtener datos", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}