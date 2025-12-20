package com.example.rdt_pastillas.util.sesion;
import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    // Nombre del archivo de preferencias
    private static final String PREF_NAME = "SesionUsuario";

    // Claves
    private static final String IS_LOGGED_IN = "IsLoggedIn";
    public static final String KEY_USER_ID = "UserId";
    public static final int NO_USER = -1;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Guarda la sesión del usuario.
     * @param id ID del usuario.
     * @param recordar Si es true, activa el auto-login. Si es false, guarda la ID pero NO activa auto-login.
     */
    public void saveUserSession(int id, boolean recordar) {
        // 1. Siempre guardamos el ID para usarlo en la app
        editor.putInt(KEY_USER_ID, id);

        // 2. La bandera de "Logueado" depende del CheckBox
        editor.putBoolean(IS_LOGGED_IN, recordar);

        editor.commit(); // o editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, NO_USER);
    }

    public void logoutUser() {
        // Limpiamos todo al cerrar sesión
        editor.clear();
        editor.commit();

        // Redirigir al login si fuera necesario (opcional hacerlo aquí o en la Activity)
        /*
        Intent i = new Intent(context, Login.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        */
    }
}