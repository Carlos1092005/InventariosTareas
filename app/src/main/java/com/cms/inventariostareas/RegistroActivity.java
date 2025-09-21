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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {
    TextView lblvolverlogin;
    EditText etnombres, etapellidos, etcorreo, etpassword, etconfirpassword;
    Button btnregistrar;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference; // Referencia a Realtime Database
    ProgressDialog progressDialog;

    String nombre = "", apellido = "", correo = "", password = "", confirmarpassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etnombres = findViewById(R.id.etnombres);
        etapellidos = findViewById(R.id.etapellidos);
        etcorreo = findViewById(R.id.etcorreo);
        etpassword = findViewById(R.id.etpassword);
        etconfirpassword = findViewById(R.id.etconfirpassword);
        btnregistrar = findViewById(R.id.btnregistrar);
        lblvolverlogin = findViewById(R.id.lblvolverlogin);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(); // Inicializar Realtime DB

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Espere por favor");
        progressDialog.setCanceledOnTouchOutside(false);

        // Botón para volver al login
        lblvolverlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegistroActivity.this, MainActivity.class));
                finish();
            }
        });

        // Botón para registrar
        btnregistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtener datos
                nombre = etnombres.getText().toString().trim();
                apellido = etapellidos.getText().toString().trim();
                correo = etcorreo.getText().toString().trim();
                password = etpassword.getText().toString().trim();
                confirmarpassword = etconfirpassword.getText().toString().trim();

                if (TextUtils.isEmpty(nombre)) {
                    Toast.makeText(RegistroActivity.this, "Ingrese su nombre", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(apellido)) {
                    Toast.makeText(RegistroActivity.this, "Ingrese su apellido", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(correo)) {
                    Toast.makeText(RegistroActivity.this, "Ingrese un correo", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(RegistroActivity.this, "Ingrese una contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(confirmarpassword)) {
                    Toast.makeText(RegistroActivity.this, "Confirme su contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmarpassword)) {
                    Toast.makeText(RegistroActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Crear usuario con rol de Instructor
                crearUsuarioInstructor();
            }
        });
    }

    private void crearUsuarioInstructor() {
        progressDialog.setMessage("Creando su cuenta...");
        progressDialog.show();

        // Crear usuario en Firebase Authentication
        firebaseAuth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Usuario creado en Auth, ahora guardar información en Realtime DB
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            guardarUsuarioEnRealtimeDB(user);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(RegistroActivity.this, "Error al crear cuenta: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void guardarUsuarioEnRealtimeDB(FirebaseUser user) {
        if (user != null) {
            // Crear un mapa con los datos del usuario
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("uid", user.getUid());
            usuario.put("nombre", nombre);
            usuario.put("apellido", apellido);
            usuario.put("correo", user.getEmail());
            usuario.put("rol", "Instructor"); // Siempre será Instructor
            usuario.put("activo", false); // Inactivo hasta que un admin lo active

            // Guardar en la ruta "usuarios/{uid}"
            databaseReference.child("usuarios").child(user.getUid())
                    .setValue(usuario)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(RegistroActivity.this, "Cuenta de Instructor creada con éxito. Espere a que un administrador la active.", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(RegistroActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(RegistroActivity.this, "Error al guardar datos: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }
}