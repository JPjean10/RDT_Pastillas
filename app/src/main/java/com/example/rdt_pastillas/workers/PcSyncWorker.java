package com.example.rdt_pastillas.workers;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Properties;

import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbFile;

public class PcSyncWorker extends Worker {
    private static final String TAG = "PcSyncWorker";

    public PcSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Iniciando sincronización con Nombre de PC...");

        // --- CONFIGURACIÓN DE TU PC ---
        String pcNombre = "JEAN-PC";       // <--- USAMOS EL NOMBRE DE TU COMPUTADORA
        String recursoCompartido = "repositorio/app/mi-salud/data-backup";
        String usuarioPc = "jean";
        String passwordPc = "admin10";
        // ------------------------------

        // La URL ahora usa el nombre en lugar de la IP
        String smbUrl = "smb://" + pcNombre + "/" + recursoCompartido + "/";

        try {
            // 1. Configurar JCIFS para resolución de nombres y SMB2
            Properties prop = new Properties();
            prop.setProperty("jcifs.smb.client.enableSMB2", "true");

            // Forzar resolución de nombres (Broadcast) para encontrar JEAN-PC sin IP
            prop.setProperty("jcifs.resolveOrder", "BCAST,DNS");

            // 2. Autenticación (Es recomendable usar el nombre de la PC como dominio)
            NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(pcNombre, usuarioPc, passwordPc);
            CIFSContext cifsContext = SingletonContext.getInstance().withCredentials(auth);

            // 3. Directorio local en el celular
            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File miSaludDir = new File(documentsDir, "MiSalud");

            File[] files = miSaludDir.listFiles((dir, name) -> name.endsWith(".txt"));

            if (files == null || files.length == 0) {
                Log.d(TAG, "No hay archivos .txt para enviar.");
                return Result.success();
            }

            for (File localFile : files) {
                SmbFile remoteFile = new SmbFile(smbUrl + localFile.getName(), cifsContext);

                try (FileInputStream in = new FileInputStream(localFile);
                     OutputStream out = remoteFile.getOutputStream()) {

                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                    out.flush();
                }
                Log.d(TAG, "Copiado a " + pcNombre + " exitosamente: " + localFile.getName());
            }

            return Result.success();

        } catch (Exception e) {
            // Si el nombre JEAN-PC no se encuentra, saltará este error
            Log.e(TAG, "No se pudo conectar con " + pcNombre + ": " + e.getMessage());
            return Result.retry();
        }
    }
}
