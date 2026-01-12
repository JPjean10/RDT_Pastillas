package com.example.rdt_pastillas.util;

public class DateUtil {

    public static String formatearFechaParaTxt(String fechaOriginal) {
        if (fechaOriginal == null) return "";
        return fechaOriginal.replace("-", "").replace(" ", "");
    }

    public static String restaurarFechaDesdeTxt(String fechaTxt) {
        if (fechaTxt == null || fechaTxt.length() < 14) return fechaTxt;
        try {
            // De 2026011011:55:56 a 2026-01-10 11:55:56
            return fechaTxt.substring(0, 4) + "-" +
                    fechaTxt.substring(4, 6) + "-" +
                    fechaTxt.substring(6, 8) + " " +
                    fechaTxt.substring(8);
        } catch (Exception e) {
            return fechaTxt;
        }
    }

}
