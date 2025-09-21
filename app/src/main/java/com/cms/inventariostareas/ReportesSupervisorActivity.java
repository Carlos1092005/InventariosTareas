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

import com.cms.inventariostareas.adapters.ReportesAdapter;
import com.cms.inventariostareas.models.Prestamo;
import com.cms.inventariostareas.models.Reporte;
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

public class ReportesSupervisorActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private FirebaseUser currentUser;
    private RecyclerView recyclerView;
    private ReportesAdapter adapter;
    private List<Reporte> listaReportes;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes_supervisor);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnGenerarReporte = findViewById(R.id.btnGenerarReporte);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView = findViewById(R.id.recyclerViewReportes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaReportes = new ArrayList<>();
        adapter = new ReportesAdapter(listaReportes);
        recyclerView.setAdapter(adapter);

        btnVolver.setOnClickListener(v -> finish());
        btnGenerarReporte.setOnClickListener(v -> generarReporteSupervisor());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando reportes...");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarReportesSupervisor();
    }

    private void cargarReportesSupervisor() {
        progressDialog.show();

        // Cargar préstamos aprobados por este supervisor
        databaseReference.child("prestamos")
                .orderByChild("supervisorId")
                .equalTo(currentUser != null ? currentUser.getUid() : "")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        listaReportes.clear();

                        int totalAprobados = 0;
                        int totalRechazados = 0;
                        int totalDevueltos = 0;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Prestamo prestamo = snapshot.getValue(Prestamo.class);
                            if (prestamo != null) {
                                switch (prestamo.getEstado()) {
                                    case "aprobado":
                                        totalAprobados++;
                                        break;
                                    case "rechazado":
                                        totalRechazados++;
                                        break;
                                    case "devuelto":
                                        totalDevueltos++;
                                        break;
                                }
                            }
                        }

                        // Agregar reportes estadísticos
                        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                        listaReportes.add(new Reporte("Estadísticas", "Préstamos Aprobados", String.valueOf(totalAprobados), fecha));
                        listaReportes.add(new Reporte("Estadísticas", "Préstamos Rechazados", String.valueOf(totalRechazados), fecha));
                        listaReportes.add(new Reporte("Estadísticas", "Préstamos Devueltos", String.valueOf(totalDevueltos), fecha));
                        listaReportes.add(new Reporte("Estadísticas", "Total Revisados", String.valueOf(totalAprobados + totalRechazados + totalDevueltos), fecha));

                        adapter.notifyDataSetChanged();
                        progressDialog.dismiss();

                        if (listaReportes.isEmpty()) {
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
                        Toast.makeText(ReportesSupervisorActivity.this,
                                "Error al cargar reportes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generarReporteSupervisor() {
        progressDialog.setMessage("Generando reporte detallado...");
        progressDialog.show();

        // Generar reporte completo con todos los préstamos revisados
        databaseReference.child("prestamos")
                .orderByChild("supervisorId")
                .equalTo(currentUser != null ? currentUser.getUid() : "")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Integer> equiposAprobados = new HashMap<>();
                        Map<String, Integer> instructoresFrecuentes = new HashMap<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Prestamo prestamo = snapshot.getValue(Prestamo.class);
                            if (prestamo != null && "aprobado".equals(prestamo.getEstado())) {
                                // Contar equipos aprobados
                                String equipo = prestamo.getEquipoNombre();
                                equiposAprobados.put(equipo, equiposAprobados.getOrDefault(equipo, 0) + prestamo.getCantidad());

                                // Contar instructores frecuentes
                                String instructor = prestamo.getInstructorNombre();
                                instructoresFrecuentes.put(instructor, instructoresFrecuentes.getOrDefault(instructor, 0) + 1);
                            }
                        }

                        // Agregar reportes detallados
                        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                        for (Map.Entry<String, Integer> entry : equiposAprobados.entrySet()) {
                            listaReportes.add(new Reporte("Equipos", entry.getKey(), entry.getValue() + " unidades aprobadas", fecha));
                        }

                        for (Map.Entry<String, Integer> entry : instructoresFrecuentes.entrySet()) {
                            listaReportes.add(new Reporte("Instructores", entry.getKey(), entry.getValue() + " préstamos aprobados", fecha));
                        }

                        adapter.notifyDataSetChanged();
                        progressDialog.dismiss();
                        Toast.makeText(ReportesSupervisorActivity.this,
                                "Reporte generado exitosamente", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(ReportesSupervisorActivity.this,
                                "Error al generar reporte", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}