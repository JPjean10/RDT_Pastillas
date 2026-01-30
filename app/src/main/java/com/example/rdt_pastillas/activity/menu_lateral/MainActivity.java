package com.example.rdt_pastillas.activity.menu_lateral;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.rdt_pastillas.R;
import com.example.rdt_pastillas.activity.menu_lateral.ui.Presion_fragment.PresionFragment;
import com.example.rdt_pastillas.activity.menu_lateral.ui.glucosa_fragment.GlucosaFragment;
import com.example.rdt_pastillas.activity.menu_lateral.ui.pastillas_fragment.PastillasFragment;
import com.example.rdt_pastillas.activity.menu_lateral.ui.reporte_fragment.ui.reporte_glucosa.ReporteGlucosaFragment;
import com.example.rdt_pastillas.activity.menu_lateral.ui.reporte_fragment.ui.reporte_presion.ReportePresionFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
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
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_pastilla) {
            replaceFragment(new PastillasFragment());
        }
        else if (id == R.id.nav_glucosa) {
            replaceFragment(new GlucosaFragment());
        }
        else if (id == R.id.nav_presion) {
            replaceFragment(new PresionFragment());
        }
        else if (id == R.id.nav_reporte_glucosa) {
            replaceFragment(new ReporteGlucosaFragment());
        }
        else if (id == R.id.nav_reporte_presion) {
            replaceFragment(new ReportePresionFragment());
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        getSupportActionBar().setTitle(item.getTitle());
        return true;
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}
