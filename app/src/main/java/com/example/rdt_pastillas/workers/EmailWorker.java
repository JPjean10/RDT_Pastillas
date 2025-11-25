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
    private final Context context;

    public EmailWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Iniciando tarea de envío de correo.");

        // 1. Ajusta tu cuenta y contraseña de aplicación
        final String username = "jeanpauldc90@gmail.com";
        final String appPassword = "vdxf plht jpvx qzdf"; // 16 dígitos

        // 2. Configuración SMTP
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

            // Asunto con fecha
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fechaHoy = sdf.format(new Date());
            message.setSubject("Reporte de Glucosa - " + fechaHoy);

            // Cuerpo del correo
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Hola,\n\nAdjunto el reporte de registros de glucosa.\n\nSaludos.");

            // Adjuntar archivo
            MimeBodyPart attachmentPart = new MimeBodyPart();
            File documentsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS);
            File file = new File(documentsDir, "registros_glucosa.txt");
            if (!file.exists()) {
                Log.e(TAG, "El archivo no existe: " + file.getAbsolutePath());
                return Result.failure();
            }
            DataSource source = new FileDataSource(file);
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName(file.getName());

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            // Enviar
            Transport.send(message);

            Log.d(TAG, "Correo enviado exitosamente.");
            return Result.success();

        } catch (MessagingException e) {
            Log.e(TAG, "Error al enviar correo.", e);
            return Result.failure();
        }
    }
}
