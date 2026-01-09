package com.example.rdt_pastillas.bd.remote.retrofit;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiHelper {

    /**
     * Método genérico para ejecutar cualquier llamada de Retrofit mostrando un ProgressDialog.
     * @param context El contexto para mostrar el ProgressDialog.
     * @param call La llamada de Retrofit a ejecutar (ej: apiService.InsertarUsuario(usuario)).
     * @param callback El callback para manejar el resultado (éxito, error, fallo).
     * @param <T> El tipo de la respuesta esperada (ej: ServerResponse).
     */
    public static <T> void execute(Context context, Call<T> call, final ApiCallback<T> callback) {


        // 2. Encolar la llamada genérica
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Éxito: Llamar al callback onSuccess
                    callback.onSuccess(response.body());
                } else {
                    // Error de API: Intentar parsear el mensaje de error
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        String mensajeError = jsonObject.optString("userMssg", "Error desconocido del servidor.");
                        // Llamar al callback onError
                        callback.onError(mensajeError);
                    } catch (Exception e) {
                        Log.e("ApiHelper", "Excepción al leer el error: " + e.getMessage());
                        callback.onError("Error al procesar la solicitud.");
                    }
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                Log.e("ApiHelper", "Fallo en la llamada a la API: " + t.getMessage());
                // Fallo de red: Llamar al callback onFailure
                callback.onFailure("No hay conexión a internet.");
            }
        });
    }
}
