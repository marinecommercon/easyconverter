package com.example.marinecommercon.easyconverter;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    /**
     * Removes the given file. If the file represents a directory, first removes its content and then
     * removes the directory itself (recursively).
     *
     * @param file
     */
    public static void delete(File file) {
        if (file.isDirectory()) {
            for (File child :
                    file.listFiles()) {
                delete(child);
            }
        }

        file.delete();
    }

    /**
     * Get an asset file by its filename and return the String content
     *
     * @param context
     * @param filename
     */
    public static String loadFromAsset(Context context, String filename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
}

