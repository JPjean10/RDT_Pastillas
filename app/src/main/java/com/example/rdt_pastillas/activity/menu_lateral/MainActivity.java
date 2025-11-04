package com.example.rdt_pastillas.activity.menu_lateral;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.activity.menu_lateral.ui.GalleryFragment;
import com.example.rdt_pastillas.activity.menu_lateral.ui.HomeFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

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
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
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

        askNotificationPermission();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_pastilla) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        } else if (itemId == R.id.nav_gallery) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new GalleryFragment()).commit();
        } else if (itemId == R.id.nav_slideshow) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SlideshowFragment()).commit();
        }

        getSupportActionBar().setTitle(item.getTitle());
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void askNotificationPermission() {
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

    // --- NUEVO MÉTODO PARA PEDIR PERMISO DE ALARMAS EXACTAS ---
    private void askForExactAlarmPermission() {
        // El permiso es necesario a partir de Android 12 (API 31)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            // Comprobamos si la app NO puede programar alarmas exactas
            if (!alarmManager.canScheduleExactAlarms()) {
                // Mostramos un diálogo explicando por qué necesitamos el permiso.
                new AlertDialog.Builder(this)
                        .setTitle("Permiso Necesario")
                        .setMessage("Para asegurar que los recordatorios de tus medicamentos suenen a la hora exacta, la aplicación necesita un permiso especial. Por favor, actívalo en la siguiente pantalla.")
                        .setPositiveButton("Ir a Ajustes", (dialog, which) -> {
                            // Creamos un Intent para llevar al usuario a la pantalla de configuración de alarmas y recordatorios.
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            }
        }
    }

    public static class SlideshowFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_slideshow, container, false);
        }
    }
}
