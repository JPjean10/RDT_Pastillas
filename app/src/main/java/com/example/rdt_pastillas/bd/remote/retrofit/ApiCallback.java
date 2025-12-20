package com.example.rdt_pastillas.bd.remote.retrofit;

public interface ApiCallback<T> {
    void onSuccess(T response); // Se llama cuando la respuesta es exitosa (código 2xx)
    void onError(String errorMessage); // Se llama cuando hay un error de API (código 4xx, 5xx)
    void onFailure(String failureMessage); // Se llama cuando hay un fallo de red
}
