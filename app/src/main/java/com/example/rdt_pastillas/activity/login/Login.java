package com.example.rdt_pastillas.activity.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.rdt_pastillas.MainApplication;
import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.activity.login.componentes.UsuarioInsertDailog;
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.usuario_entity.UsuarioEntity;
import com.example.rdt_pastillas.activity.menu_lateral.MainActivity;
import com.example.rdt_pastillas.bd.SyncService.SyncManager;
import com.example.rdt_pastillas.bd.repository.UsuarioRepository;
import com.example.rdt_pastillas.util.alert.AlertaAvertencia;
import com.example.rdt_pastillas.util.alert.AlertaError;
import com.example.rdt_pastillas.util.sesion.SessionManager;
import com.example.rdt_pastillas.workers.EmailWorker;
import com.example.rdt_pastillas.workers.PcSyncWorker;

import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity implements
        UsuarioInsertDailog.insertOnClickedDailog {
    TextView btnRegistrar;
    private Button btnIniciarSesion;
    private CheckBox cbRecordarSesion;

    private EditText txtUsuario, txtContrasena;

    private UsuarioRepository servicio;
    private SessionManager sessionManager;
    private static final String PREF_NAME = "ocultar_boton";
    private static final String KEY_OCULTAR = "btnRegistrar";
    private boolean ocultarRegistro = false;
    // --- CÓDIGO DE PERMISOS AQUÍ ---

    // 1. Launcher para el permiso de almacenamiento
    private final ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if (Environment.isExternalStorageManager()) {
                                Toast.makeText(this, "Permiso de almacenamiento concedido.", Toast.LENGTH_SHORT).show();
                                Log.i("login", "Permiso de almacenamiento concedido.");
                                // ESTO ES LO QUE "RETORNA" LA LÓGICA A LA APLICACIÓN
                                MainApplication app = (MainApplication) getApplication();

                                // 1. Inicializa servicios base (Correo, Room Sync, Despertador PC 1 AM)
                                app.intentarInicializacionGlobal();

                                // 2. EJECUCIÓN INMEDIATA: Solo esta vez al recibir el permiso
                                app.ejecutarSincronizacionAhora();
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

        servicio = new UsuarioRepository(this.getApplication());
        sessionManager = new SessionManager(this);


        // --- LÓGICA DE AUTO-LOGIN ---
        // Comprobar si ya hay una sesión guardada ANTES de mostrar nada.
        if (sessionManager.isLoggedIn()) {
            iniciarMainActivity();
            return; // Detiene la ejecución de onCreate para no mostrar la pantalla de login
        }

        // Si no hay sesión, configurar la UI
        setupLoginUI();
    }

    private void setupLoginUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // ... (tu código de insets)
            return insets;
        });

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- LLAMADAS A LOS MÉTODOS DE PERMISOS ---
        permiso_notificacione();
        checkAndRequestStoragePermission();

        txtUsuario = findViewById(R.id.txt_usuario);
        txtContrasena = findViewById(R.id.txt_contrasena);
        cbRecordarSesion = findViewById(R.id.cb_recordar_sesion);
        btnIniciarSesion = findViewById(R.id.btn_iniciar_sesion);
        btnRegistrar = findViewById(R.id.btn_registrar);

        txtUsuario.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(20)});
        txtContrasena.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(20)});


        // 🔹 LEER SharedPreferences AL INICIAR
        ocultarRegistro = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getBoolean(KEY_OCULTAR, false);

        ocultarBotonRegistroSiEsNecesario();

        btnRegistrar.setOnClickListener(view -> {
            UsuarioInsertDailog dialog = new UsuarioInsertDailog(Login.this, Login.this);
            dialog.show();
        });


        btnIniciarSesion.setOnClickListener(view -> {
            String usuario = txtUsuario.getText().toString().trim();
            String contrasena = txtContrasena.getText().toString().trim();
            boolean recordar = cbRecordarSesion.isChecked();

            if (usuario.isEmpty() || contrasena.isEmpty()) {
                AlertaAvertencia.show(this, "Por favor, complete todos los campos");
                return;
            }

            servicio.login(this,usuario, contrasena, recordar, new UsuarioRepository.LoginCallback() {
                @Override
                public void onLoginSuccess(int userId) {
                    // 🔹 Guardar bandera
                    getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                            .edit()
                            .putBoolean(KEY_OCULTAR, true)
                            .apply();

                    ocultarRegistro = true;
                    iniciarMainActivity();
                }

                @Override
                public void onLoginFailed(String error) {
                    AlertaError.show(Login.this,error);
                }
            });
        });

        ocultarBotonRegistroSiEsNecesario();
//______________no tocar___________________________________________________________
    }

    //_______________________________________________________________________________________________
// dailog________________________________________________________________________________________
    @Override
    public void insertOnClickedDailog(String usuario, String contrasena, String nombre) {
        UsuarioEntity usuarioEntity = new UsuarioEntity(usuario, contrasena, nombre);
        servicio.insertar_usuario(this, usuarioEntity, () -> {
            // 🔹 Guardar bandera
            getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                    .edit()
                    .putBoolean(KEY_OCULTAR, true)
                    .apply();

            ocultarRegistro = true;
            ocultarBotonRegistroSiEsNecesario();
        });
    }
//______________________________________________________________________________________________
// MÉTODOS______________________________________________________________________________________

    private void iniciarMainActivity() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void ocultarBotonRegistroSiEsNecesario() {
        btnRegistrar.setVisibility(ocultarRegistro ? View.GONE : View.VISIBLE);
    }
// _____________________________________________________________________________________________
// MÉTODOS DE PERMISOS AQUÍ_____________________________________________________________________
    private void checkAndRequestStoragePermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (Environment.isExternalStorageManager()) {
        } else {
            // Solicitar permiso
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
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
