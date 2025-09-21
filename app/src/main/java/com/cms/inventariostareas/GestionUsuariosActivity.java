package com.cms.inventariostareas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.cms.inventariostareas.adapters.UsuariosAdapter;
import com.cms.inventariostareas.models.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestionUsuariosActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private UsuariosAdapter adapter;
    private List<Usuario> listaUsuarios;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios);

        // Inicializar Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("usuarios");
        listaUsuarios = new ArrayList<>();

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewUsuarios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configurar adapter con listeners
        adapter = new UsuariosAdapter(listaUsuarios, new UsuariosAdapter.OnUsuarioClickListener() {
            @Override
            public void onUsuarioClick(Usuario usuario) {
                mostrarDialogoEditarUsuario(usuario);
            }

            @Override
            public void onEliminarUsuario(Usuario usuario) {
                mostrarDialogoConfirmarEliminacion(usuario);
            }
        });

        recyclerView.setAdapter(adapter);

        // Botones
        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnAgregarUsuario = findViewById(R.id.btnAgregarUsuario);

        btnVolver.setOnClickListener(v -> finish());
        btnAgregarUsuario.setOnClickListener(v -> mostrarDialogoAgregarUsuario());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando");
        progressDialog.setMessage("Espere por favor...");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        progressDialog.show();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaUsuarios.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    if (usuario != null) {
                        usuario.setUid(snapshot.getKey());
                        listaUsuarios.add(usuario);
                    }
                }
                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(GestionUsuariosActivity.this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoAgregarUsuario() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_usuario, null);
        builder.setView(view);

        EditText etNombre = view.findViewById(R.id.etNombre);
        EditText etApellido = view.findViewById(R.id.etApellido);
        EditText etCorreo = view.findViewById(R.id.etCorreo);
        EditText etPassword = view.findViewById(R.id.etPassword);
        Spinner spinnerRol = view.findViewById(R.id.spinnerRol);

        // Configurar spinner de roles
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnCancelar).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnCrear).setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String apellido = etApellido.getText().toString().trim();
            String correo = etCorreo.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String rol = spinnerRol.getSelectedItem().toString();

            if (validarDatos(nombre, apellido, correo, password)) {
                crearUsuario(nombre, apellido, correo, password, rol);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean validarDatos(String nombre, String apellido, String correo, String password) {
        if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void crearUsuario(String nombre, String apellido, String correo, String password, String rol) {
        progressDialog.setMessage("Creando usuario...");
        progressDialog.show();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            Usuario usuario = new Usuario(
                                    firebaseUser.getUid(),
                                    nombre,
                                    apellido,
                                    correo,
                                    rol,
                                    true
                            );

                            databaseReference.child(firebaseUser.getUid()).setValue(usuario)
                                    .addOnCompleteListener(taskDb -> {
                                        progressDialog.dismiss();
                                        if (taskDb.isSuccessful()) {
                                            Toast.makeText(this, "Usuario creado exitosamente", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(this, "Error al guardar datos", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarDialogoEditarUsuario(Usuario usuario) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_editar_usuario, null);
        builder.setView(view);

        EditText etNombre = view.findViewById(R.id.etNombre);
        EditText etApellido = view.findViewById(R.id.etApellido);
        Spinner spinnerRol = view.findViewById(R.id.spinnerRol);

        etNombre.setText(usuario.getNombre());
        etApellido.setText(usuario.getApellido());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(adapter);

        // Seleccionar el rol actual
        int position = adapter.getPosition(usuario.getRol());
        if (position >= 0) {
            spinnerRol.setSelection(position);
        }

        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnCancelar).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnGuardar).setOnClickListener(v -> {
            String nuevoNombre = etNombre.getText().toString().trim();
            String nuevoApellido = etApellido.getText().toString().trim();
            String nuevoRol = spinnerRol.getSelectedItem().toString();

            if (!nuevoNombre.isEmpty() && !nuevoApellido.isEmpty()) {
                actualizarUsuario(usuario, nuevoNombre, nuevoApellido, nuevoRol);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void actualizarUsuario(Usuario usuario, String nombre, String apellido, String rol) {
        progressDialog.setMessage("Actualizando usuario...");
        progressDialog.show();

        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("apellido", apellido);
        updates.put("rol", rol);

        databaseReference.child(usuario.getUid()).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarDialogoConfirmarEliminacion(Usuario usuario) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de eliminar al usuario " + usuario.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarUsuario(usuario);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarUsuario(Usuario usuario) {
        progressDialog.setMessage("Eliminando usuario...");
        progressDialog.show();

        databaseReference.child(usuario.getUid()).removeValue()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}