package com.example.rdt_pastillas.util.sesion;
import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "LoginSession";
    private static final String KEY_USER_ID = "userId";
    private static final int NO_USER = -1; // Valor por defecto si no hay usuario logueado

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Guarda el ID del usuario en SharedPreferences.
     */
    public void saveUserSession(int userId) {
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
    }

    /**
     * Obtiene el ID del usuario guardado.
     * @return El ID del usuario, o -1 si no hay ninguna sesión guardada.
     */
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, NO_USER);
    }

    /**
     * Comprueba si hay una sesión activa.
     * @return true si hay un ID de usuario guardado, false en caso contrario.
     */
    public boolean isLoggedIn() {
        return getUserId() != NO_USER;
    }

    /**
     * Borra la sesión del usuario.
     */
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}
