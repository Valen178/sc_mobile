package com.example.sportconnection.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.sportconnection.R;

/**
 * Diálogo de carga reutilizable para mostrar durante operaciones asíncronas
 */
public class LoadingDialog {

    private final Dialog dialog;
    private final TextView textMessage;

    public LoadingDialog(Context context) {
        dialog = new Dialog(context);

        // Inflar el layout personalizado
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        textMessage = view.findViewById(R.id.textLoadingMessage);

        dialog.setContentView(view);
        dialog.setCancelable(false);

        // Hacer el fondo transparente
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public void show() {
        show("Cargando...");
    }

    public void show(String message) {
        if (textMessage != null) {
            textMessage.setText(message);
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void setMessage(String message) {
        if (textMessage != null) {
            textMessage.setText(message);
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}

