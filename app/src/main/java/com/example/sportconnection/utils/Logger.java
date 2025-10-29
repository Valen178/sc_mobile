package com.example.sportconnection.utils;

import android.util.Log;

/**
 * Clase de utilidad para logging centralizado
 * Usa un TAG común para facilitar el filtrado en Logcat
 */
public class Logger {

    private static final String BASE_TAG = "SportConnection";
    private static final boolean ENABLE_LOGS = true; // Cambiar a false para deshabilitar todos los logs

    public static void d(String tag, String message) {
        if (ENABLE_LOGS) {
            Log.d(BASE_TAG + "_" + tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (ENABLE_LOGS) {
            Log.e(BASE_TAG + "_" + tag, message);
        }
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (ENABLE_LOGS) {
            Log.e(BASE_TAG + "_" + tag, message, throwable);
        }
    }

    public static void i(String tag, String message) {
        if (ENABLE_LOGS) {
            Log.i(BASE_TAG + "_" + tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (ENABLE_LOGS) {
            Log.w(BASE_TAG + "_" + tag, message);
        }
    }

    public static void v(String tag, String message) {
        if (ENABLE_LOGS) {
            Log.v(BASE_TAG + "_" + tag, message);
        }
    }

    // Método especial para logs que SIEMPRE deben mostrarse
    public static void forceLog(String tag, String message) {
        Log.wtf(BASE_TAG + "_" + tag, message);
    }
}

