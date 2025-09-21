package com.cms.inventariostareas;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    TextView lblregistrar;
    EditText etcorreol, etpasswordl;
    Button btningresarl;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference; // Referencia a Realtime Database
    String correo = "", password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etcorreol = findViewById(R.id.etcorreol);
        etpasswordl = findViewById(R.id.etpasswordl);
        btningresarl = findViewById(R.id.btningresarl);
        lblregistrar = findViewById(R.id.lblregistrar);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(); // Inicializar Realtime DB

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Espere por favor");
        progressDialog.setCanceledOnTouchOutside(false);

        // Botón para ir al registro
        lblregistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegistroActivity.class));
            }
        });

        // Botón para ingresar (login)
        btningresarl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                correo = etcorreol.getText().toString().trim();
                password = etpasswordl.getText().toString().trim();

                if (TextUtils.isEmpty(correo)) {
                    Toast.makeText(MainActivity.this, "Ingrese un correo", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Ingrese una contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginUsuario();
            }
        });
    }

    private void loginUsuario() {
        progressDialog.setMessage("Iniciando sesión...");
        progressDialog.show();

        // Autenticar usuario
        firebaseAuth.signInWithEmailAndPassword(correo, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login exitoso, verificar rol del usuario
                            verificarRolUsuario();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void verificarRolUsuario() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // Consultar el usuario en Realtime Database para obtener su rol
            databaseReference.child("usuarios").child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            progressDialog.dismiss();
                            if (dataSnapshot.exists()) {
                                // Verificar si el usuario está activo
                                Boolean activo = dataSnapshot.child("activo").getValue(Boolean.class);
                                if (activo != null && !activo) {
                                    Toast.makeText(MainActivity.this, "Su cuenta está inactiva. Contacte al administrador.", Toast.LENGTH_LONG).show();
                                    firebaseAuth.signOut(); // Cerrar sesión
                                    return;
                                }

                                // Obtener el rol y redirigir al dashboard correspondiente
                                String rol = dataSnapshot.child("rol").getValue(String.class);
                                if (rol != null) {
                                    redirigirSegunRol(rol);
                                } else {
                                    Toast.makeText(MainActivity.this, "Error: rol no definido", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Error: datos de usuario no encontrados", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Error al obtener datos: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void redirigirSegunRol(String rol) {
        Intent intent;
        switch (rol) {
            case "Administrador":
                intent = new Intent(MainActivity.this, Administrador_DashboardActivity.class);
                break;
            case "Supervisor":
                intent = new Intent(MainActivity.this, Supervisor_DashboardActivity.class);
                break;
            case "Instructor":
                intent = new Intent(MainActivity.this, Instructor_DashboardActivity.class);
                break;
            default:
                Toast.makeText(this, "Rol no reconocido: " + rol, Toast.LENGTH_SHORT).show();
                return;
        }
        startActivity(intent);
        finish(); // Finalizar MainActivity para que no pueda volver atrás con el back button
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Si el usuario ya está logueado, redirigir directamente al dashboard correspondiente
        if (firebaseAuth.getCurrentUser() != null) {
            verificarRolUsuario();
        }
    }
}