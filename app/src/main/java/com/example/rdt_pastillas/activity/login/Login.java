package com.example.rdt_pastillas.activity.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.activity.login.componentes.UsuarioInsertDailog;
import com.example.rdt_pastillas.retrofit.entity.UsuarioEntity;
import com.example.rdt_pastillas.retrofit.servicio.UsuarioServicio;

public class Login extends AppCompatActivity implements
        UsuarioInsertDailog.insertOnClickedDailog {
    TextView btn_registrar;

    private UsuarioServicio servicio;

    // --- CÓDIGO DE PERMISOS AQUÍ ---

    // 1. Launcher para el permiso de almacenamiento
    private final ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if (Environment.isExternalStorageManager()) {
                                // El permiso fue concedido.
                                Toast.makeText(this, "Permiso de almacenamiento concedido", Toast.LENGTH_SHORT).show();
                            } else {
                                // El usuario no concedió el permiso.
                                Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    // 2. Launcher para el permiso de notificaciones
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show();
                }
            });
// ________________________________________________________________________________

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        servicio = new UsuarioServicio();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- LLAMADAS A LOS MÉTODOS DE PERMISOS ---
        checkAndRequestStoragePermission();
        permiso_notificacione();

        btn_registrar = findViewById(R.id.btn_registrar);

        btn_registrar.setOnClickListener(view -> {
            UsuarioInsertDailog dialog = new UsuarioInsertDailog(Login.this, Login.this);
            dialog.show();
        });
//______________no tocar___________________________________________________________
    }

    //_______________________________________________________________________________________________
// dailog________________________________________________________________________________________
    @Override
    public void insertOnClickedDailog(String usuario, String contrasena) {
        UsuarioEntity usuarioEntity = new UsuarioEntity(usuario, contrasena);
        servicio.insertar_usuario(this, usuarioEntity);
    }
//______________________________________________________________________________________________
// MÉTODOS DE PERMISOS AQUÍ_____________________________________________________________________

    private void checkAndRequestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    storageActivityResultLauncher.launch(intent);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    storageActivityResultLauncher.launch(intent);
                }
            }
        }
    }

    private void permiso_notificacione() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                // Si no se ha concedido, se solicita directamente.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
// ________________________________________________________________________________
//______________no tocar___________________________________________________________
}
