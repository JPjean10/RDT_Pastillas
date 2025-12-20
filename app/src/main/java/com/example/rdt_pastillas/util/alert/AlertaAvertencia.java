package com.example.rdt_pastillas.util.alert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rdt_pastillas.R;

public class AlertaAvertencia {

    public static void show(Context context, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.alert_advertencia, null);

        TextView text = layout.findViewById(R.id.text_toast);
        text.setText(message); // Personaliza el mensaje dinámicamente

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

}