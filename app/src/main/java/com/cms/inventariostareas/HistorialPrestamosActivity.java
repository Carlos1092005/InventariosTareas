package com.cms.inventariostareas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
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

public class HistorialPrestamosActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private FirebaseUser currentUser;
    private RecyclerView recyclerView;
    private PrestamosAdapter adapter;
    private List<Prestamo> listaPrestamosRevisados;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_prestamos);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Button btnVolver = findViewById(R.id.btnVolver);
        Spinner spinnerFiltro = findViewById(R.id.spinnerFiltroEstado);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView = findViewById(R.id.recyclerViewPrestamos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaPrestamosRevisados = new ArrayList<>();
        adapter = new PrestamosAdapter(listaPrestamosRevisados, null);
        recyclerView.setAdapter(adapter);

        btnVolver.setOnClickListener(v -> finish());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando historial...");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarHistorialPrestamos();
    }

    private void cargarHistorialPrestamos() {
        progressDialog.show();

        databaseReference.child("prestamos")
                .orderByChild("supervisorId")
                .equalTo(currentUser != null ? currentUser.getUid() : "")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        listaPrestamosRevisados.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Prestamo prestamo = snapshot.getValue(Prestamo.class);
                            if (prestamo != null && !"pendiente".equals(prestamo.getEstado())) {
                                prestamo.setId(snapshot.getKey());
                                listaPrestamosRevisados.add(prestamo);
                            }
                        }

                        adapter.notifyDataSetChanged();
                        progressDialog.dismiss();

                        if (listaPrestamosRevisados.isEmpty()) {
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
                        Toast.makeText(HistorialPrestamosActivity.this,
                                "Error al cargar historial", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}