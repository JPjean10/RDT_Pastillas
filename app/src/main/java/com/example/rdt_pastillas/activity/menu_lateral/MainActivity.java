package com.example.rdt_pastillas.activity.menu_lateral;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.activity.menu_lateral.ui.Presion_fragment.PresionFragment;
import com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.GlucosaFragment;
import com.example.rdt_pastillas.activity.menu_lateral.ui.pastillas_fragment.PastillasFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    private final ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Este bloque se ejecuta cuando el usuario vuelve de la pantalla de configuración.
                        // Aquí puedes añadir lógica si es necesario, como reiniciar una sincronización.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if (Environment.isExternalStorageManager()) {
                                // El permiso fue concedido. ¡Genial!
                                // Podrías reiniciar la sincronización si es necesario, pero
                                // el SyncManager ya lo intentó al inicio de la app.                      com.google.ai.edge.litert.Environment.isExternalStorageManager()
                                // La próxima vez que se inicie la app, funcionará.
                            } else {
                                // El usuario no concedió el permiso.
                                // Puedes mostrar un mensaje o un Toast.
                            }
                        }
                    });
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.
        } else {
            // Explain to the user that the feature is unavailable because the
            // feature requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PastillasFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_pastilla);
            getSupportActionBar().setTitle(navigationView.getMenu().findItem(R.id.nav_pastilla).getTitle());
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                callback.setEnabled(true);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                callback.setEnabled(false);
            }

            @Override
            public void onDrawerStateChanged(int newState) {}
        });

        permiso_notificacione();
        checkAndRequestStoragePermission();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_pastilla) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PastillasFragment()).commit();
        } else if (itemId == R.id.nav_glucosa) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new GlucosaFragment()).commit();
        } else if (itemId == R.id.nav_slideshow) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PresionFragment()).commit();
        }

        getSupportActionBar().setTitle(item.getTitle());
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // --- ¡AÑADE ESTE MÉTODO COMPLETO DENTRO DE TU CLASE! ---
    private void checkAndRequestStoragePermission() {
        // Esta lógica es solo para Android 11 (API 30) y versiones superiores.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Comprueba si la app NO tiene el permiso.
            if (!Environment.isExternalStorageManager()) {
                try {
                    // Crea un intent para abrir la pantalla de configuración específica de tu app.
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    // Lanza la pantalla para que el usuario conceda el permiso.
                    storageActivityResultLauncher.launch(intent);
                } catch (Exception e) {
                    // En algunos dispositivos, el intent puede fallar. Intentamos con la acción genérica.
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    storageActivityResultLauncher.launch(intent);
                }
            }
        }
    }
    private void permiso_notificacione() {
        //Esto solo es necesario para el nivel de API 33 o superior.+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // El SDK de FCM (y tu aplicación) puede publicar notificaciones+.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: mostrar una interfaz de usuario educativa que explique al usuario las funciones que estarán habilitadas
                // Al otorgarles el permiso POST_NOTIFICATION. Esta interfaz de usuario debería proporcionar al usuario
                // con dos opciones: "Aceptar" y "No, gracias". Si el usuario selecciona "Aceptar", solicite directamente el permiso..
                // Si el usuario selecciona "No, gracias", permítale continuar sin notificaciones.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
