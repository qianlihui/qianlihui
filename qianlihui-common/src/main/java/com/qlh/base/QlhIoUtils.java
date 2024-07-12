package com.qlh.base;

import java.io.*;

public class QlhIoUtils {

    public static void copy(InputStream ins, OutputStream out) {
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = ins.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
            ins.close();
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static byte[] readAsBytes(InputStream inputStream) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            copy(inputStream, out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String readAsString(InputStream inputStream) {
        return QlhException.runtime(() -> {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                return new String(out.toByteArray());
            }
        });
    }

    public static void writeToFile(byte[] bytes, File file) {
        QlhException.runtime(() -> {
            if (!file.exists()) {
                file.createNewFile();
            }
            try (OutputStream out = new FileOutputStream(file)) {
                out.write(bytes);
            }
        });
    }

}
