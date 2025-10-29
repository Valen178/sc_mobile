package com.example.sportconnection.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gestor de hilos para ejecutar operaciones en segundo plano
 * y actualizar la UI en el hilo principal
 */
public class ThreadManager {

    private static ThreadManager instance;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    private ThreadManager() {
        // Pool de hilos para operaciones en segundo plano
        executorService = Executors.newFixedThreadPool(4);
        // Handler para ejecutar en el hilo principal
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized ThreadManager getInstance() {
        if (instance == null) {
            instance = new ThreadManager();
        }
        return instance;
    }

    /**
     * Ejecuta una tarea en segundo plano
     */
    public void executeInBackground(Runnable task) {
        executorService.execute(task);
    }

    /**
     * Ejecuta una tarea en el hilo principal (UI)
     */
    public void executeOnMainThread(Runnable task) {
        mainHandler.post(task);
    }

    /**
     * Ejecuta una tarea en segundo plano y luego ejecuta el resultado en el hilo principal
     */
    public void executeAsync(BackgroundTask backgroundTask, UITask uiTask) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Ejecutar tarea en segundo plano
                    Object result = backgroundTask.doInBackground();

                    // Ejecutar resultado en el hilo principal
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            uiTask.onComplete(result);
                        }
                    });
                } catch (Exception e) {
                    // Manejar error en el hilo principal
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            uiTask.onError(e);
                        }
                    });
                }
            }
        });
    }

    /**
     * Ejecuta una tarea con delay en el hilo principal
     */
    public void executeWithDelay(Runnable task, long delayMillis) {
        mainHandler.postDelayed(task, delayMillis);
    }

    /**
     * Cierra el pool de hilos (llamar cuando la app se cierre)
     */
    public void shutdown() {
        executorService.shutdown();
    }

    // Interfaces para tareas asíncronas
    public interface BackgroundTask {
        Object doInBackground() throws Exception;
    }

    public interface UITask {
        void onComplete(Object result);
        void onError(Exception e);
    }
}

