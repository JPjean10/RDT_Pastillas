package com.example.rdt_pastillas.workers;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailWorker extends Worker {

    private static final String TAG = "EmailWorker";
    private static final String FOLDER_NAME = "MiSalud";

    public EmailWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Iniciando tarea de envío de correo de reportes médicos.");

        final String username = "jeanpauldc90@gmail.com";
        final String appPassword = "vdxf plht jpvx qzdf";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, appPassword);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse("jeanpauldc80@gmail.com")
            );

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fechaHoy = sdf.format(new Date());
            message.setSubject("Reporte Médico (Glucosa y Presión) - " + fechaHoy);

            Multipart multipart = new MimeMultipart();

            // 1. Cuerpo del correo
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Hola,\n\nSe adjuntan los reportes de registros de glucosa y presión arterial encontrados en la carpeta MiSalud.\n\nSaludos.");
            multipart.addBodyPart(textPart);

            // 2. Localizar carpeta MiSalud en Documentos
            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File miSaludDir = new File(documentsDir, FOLDER_NAME);

            if (!miSaludDir.exists() || !miSaludDir.isDirectory()) {
                Log.e(TAG, "La carpeta MiSalud no existe.");
                return Result.failure();
            }

            // 3. Obtener todos los archivos de glucosa y presión (ej: registros_glucosa_1, registros_glucosa_2, etc.)
            File[] files = miSaludDir.listFiles((dir, name) ->
                    (name.startsWith("registros_glucosa_") || name.startsWith("registros_presion_"))
                            && name.endsWith(".txt")
            );

            if (files != null && files.length > 0) {
                // Recorremos todos los archivos encontrados y los adjuntamos uno por uno
                for (File file : files) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(file);
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(file.getName());
                    multipart.addBodyPart(attachmentPart);
                    Log.d(TAG, "Archivo adjuntado con éxito: " + file.getName());
                }
            } else {
                Log.w(TAG, "No se encontraron archivos para adjuntar.");
                return Result.failure();
            }

            message.setContent(multipart);

            // 4. Enviar
            Transport.send(message);

            Log.d(TAG, "Correo enviado exitosamente con todos los adjuntos.");
            return Result.success();

        } catch (MessagingException e) {
            Log.e(TAG, "Error al enviar correo.", e);
            return Result.failure();
        }
    }
}