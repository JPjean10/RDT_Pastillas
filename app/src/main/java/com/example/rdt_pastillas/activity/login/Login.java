package com.example.rdt_pastillas.activity.login;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
        UsuarioEntity usuarioEntity = new UsuarioEntity(usuario,contrasena);
        servicio.insertar_usuario(this,usuarioEntity);
    }
//______________________________________________________________________________________________
//______________no tocar___________________________________________________________






// ________________________________________________________________________________
//______________________no tocar___________________________________________________
}
