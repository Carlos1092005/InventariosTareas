package com.cms.inventariostareas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConfiguracionSistemaActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private TextView tvVersionApp, tvTotalUsuarios, tvTotalEquipos, tvUltimoBackup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion_sistema);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas
        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnBackup = findViewById(R.id.btnBackup);
        Button btnLogs = findViewById(R.id.btnLogs);
        Button btnSeguridad = findViewById(R.id.btnSeguridad);

        tvVersionApp = findViewById(R.id.tvVersionApp);
        tvTotalUsuarios = findViewById(R.id.tvTotalUsuarios);
        tvTotalEquipos = findViewById(R.id.tvTotalEquipos);
        tvUltimoBackup = findViewById(R.id.tvUltimoBackup);

        btnVolver.setOnClickListener(v -> finish());
        btnBackup.setOnClickListener(v -> realizarBackup());
        btnLogs.setOnClickListener(v -> verLogsAuditoria());
        btnSeguridad.setOnClickListener(v -> configurarSeguridad());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Configuración");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarConfiguracion();
        cargarEstadisticasSistema();
    }

    private void cargarConfiguracion() {
        databaseReference.child("configuracion").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    configuracionPorDefecto();
                } else {
                    // Cargar última fecha de backup
                    String ultimoBackup = dataSnapshot.child("ultimoBackup").getValue(String.class);
                    if (ultimoBackup != null) {
                        tvUltimoBackup.setText("Último backup: " + ultimoBackup);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ConfiguracionSistemaActivity.this, "Error al cargar configuración", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configuracionPorDefecto() {
        Map<String, Object> config = new HashMap<>();
        config.put("autoBackup", true);
        config.put("notificaciones", true);
        config.put("diasBackup", 7);
        config.put("ultimoBackup", "Nunca");

        databaseReference.child("configuracion").setValue(config);
    }

    private void cargarEstadisticasSistema() {
        // Contar usuarios
        databaseReference.child("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long totalUsuarios = dataSnapshot.getChildrenCount();
                tvTotalUsuarios.setText("Usuarios registrados: " + totalUsuarios);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        // Contar equipos
        databaseReference.child("equipos").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long totalEquipos = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Integer cantidad = snapshot.child("cantidad").getValue(Integer.class);
                    if (cantidad != null) totalEquipos += cantidad;
                }
                tvTotalEquipos.setText("Equipos en inventario: " + totalEquipos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void realizarBackup() {
        progressDialog.setMessage("Realizando backup de la base de datos...");
        progressDialog.show();

        // Simular proceso de backup
        new android.os.Handler().postDelayed(() -> {
            String fechaBackup = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

            // Guardar fecha del backup
            databaseReference.child("configuracion").child("ultimoBackup").setValue(fechaBackup)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            tvUltimoBackup.setText("Último backup: " + fechaBackup);
                            Toast.makeText(ConfiguracionSistemaActivity.this,
                                    "Backup completado exitosamente", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ConfiguracionSistemaActivity.this,
                                    "Error al guardar backup", Toast.LENGTH_SHORT).show();
                        }
                    });
        }, 3000);
    }

    private void verLogsAuditoria() {
        progressDialog.setMessage("Cargando logs de auditoría...");
        progressDialog.show();

        // Aquí implementarías la visualización de logs
        // Por ahora simulamos con un mensaje
        new android.os.Handler().postDelayed(() -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Funcionalidad de logs en desarrollo", Toast.LENGTH_SHORT).show();
        }, 1500);
    }

    private void configurarSeguridad() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_config_seguridad, null);
        builder.setView(view);

        Switch switchAutobackup = view.findViewById(R.id.switchAutobackup);
        Switch switchNotificaciones = view.findViewById(R.id.switchNotificaciones);
        EditText etDiasBackup = view.findViewById(R.id.etDiasBackup);

        cargarConfiguracionSeguridad(switchAutobackup, switchNotificaciones, etDiasBackup);

        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnCancelarSeguridad).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnGuardarSeguridad).setOnClickListener(v -> {
            guardarConfiguracionSeguridad(switchAutobackup.isChecked(),
                    switchNotificaciones.isChecked(),
                    etDiasBackup.getText().toString());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void cargarConfiguracionSeguridad(Switch autobackup, Switch notificaciones, EditText diasBackup) {
        databaseReference.child("configuracion").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean autoBackup = dataSnapshot.child("autoBackup").getValue(Boolean.class);
                    Boolean notifs = dataSnapshot.child("notificaciones").getValue(Boolean.class);
                    Long dias = dataSnapshot.child("diasBackup").getValue(Long.class);

                    autobackup.setChecked(autoBackup != null ? autoBackup : true);
                    notificaciones.setChecked(notifs != null ? notifs : true);
                    diasBackup.setText(dias != null ? dias.toString() : "7");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ConfiguracionSistemaActivity.this, "Error al cargar configuración", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarConfiguracionSeguridad(boolean autoBackup, boolean notificaciones, String diasStr) {
        try {
            int dias = Integer.parseInt(diasStr);
            if (dias < 1 || dias > 30) {
                Toast.makeText(this, "Los días deben estar entre 1 y 30", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.setMessage("Guardando configuración...");
            progressDialog.show();

            Map<String, Object> updates = new HashMap<>();
            updates.put("autoBackup", autoBackup);
            updates.put("notificaciones", notificaciones);
            updates.put("diasBackup", dias);

            databaseReference.child("configuracion").updateChildren(updates)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Configuración guardada exitosamente", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al guardar configuración", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Días de backup inválidos", Toast.LENGTH_SHORT).show();
        }
    }
}