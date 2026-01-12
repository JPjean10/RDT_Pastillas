package com.example.rdt_pastillas.util;

public class DateUtil {

    public static String formatearFechaParaTxt(String fechaOriginal) {
        if (fechaOriginal == null || fechaOriginal.length() < 16) return "";
        String limpia = fechaOriginal.replace("-", "").replace(" ", "");
        try {
            return limpia.substring(0, 8) + limpia.substring(8, 13);
        } catch (Exception e) {
            return limpia;
        }
    }

    public static String restaurarFechaDesdeTxt(String fechaTxt) {
        if (fechaTxt == null || fechaTxt.length() < 12) return fechaTxt;
        try {
            return fechaTxt.substring(0, 4) + "-" +
                    fechaTxt.substring(4, 6) + "-" +
                    fechaTxt.substring(6, 8) + " " +
                    fechaTxt.substring(8,13) + ":00";
        } catch (Exception e) {
            return fechaTxt;
        }
    }

}
