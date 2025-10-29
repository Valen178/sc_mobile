package com.example.sportconnection.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "SportConnectionSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_TYPE = "profile_type";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        Logger.d(TAG, "SessionManager inicializado");
    }

    // Guardar sesión
    public void saveSession(String token, int userId, String email, String profileType) {
        Logger.forceLog(TAG, "========== GUARDANDO SESIÓN ==========");
        Logger.forceLog(TAG, "Token: " + token);
        Logger.forceLog(TAG, "UserId: " + userId);
        Logger.forceLog(TAG, "Email: " + email);
        Logger.forceLog(TAG, "ProfileType: " + profileType);

        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PROFILE_TYPE, profileType);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        boolean saved = editor.commit(); // Usar commit() en lugar de apply() para guardar sincrónicamente

        Logger.forceLog(TAG, "Sesión guardada exitosamente: " + saved);

        // Verificar inmediatamente después de guardar
        boolean check = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        Logger.forceLog(TAG, "Verificación inmediata: isLoggedIn = " + check);
    }

    // Obtener token
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // Obtener ID de usuario
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    // Obtener email
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    // Obtener tipo de perfil
    public String getProfileType() {
        return prefs.getString(KEY_PROFILE_TYPE, null);
    }

    // Verificar si está logueado
    public boolean isLoggedIn() {
        boolean loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        Logger.forceLog(TAG, "isLoggedIn() llamado, resultado: " + loggedIn);
        return loggedIn;
    }

    // Cerrar sesión
    public void logout() {
        Logger.forceLog(TAG, "========== CERRANDO SESIÓN ==========");
        editor.clear();
        editor.commit();
        Logger.forceLog(TAG, "Sesión cerrada exitosamente");
    }
}

