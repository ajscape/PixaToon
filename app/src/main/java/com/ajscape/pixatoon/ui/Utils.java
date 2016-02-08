package com.ajscape.pixatoon.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * UI Utility functions
 */
public class Utils {
    public static final String TAG = "Utils:";

    private static final String SAVE_FOLDER = "PixaToon";
    private static final String SAVE_FILENAME_PREFIX = "IMG";

    private static int lastSaveFileIndex = 0;

    /**
     * Reize bitmap with dimensions equal to or less than given params, without changing the aspect ratio
     * @param bitmap    Input bitmap
     * @param maxWidth  Max allowed width of resized Bitmap
     * @param maxHeight Max allowed height of resized Bitmap
     * @return Resized bitmap
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
            return scaledBitmap;
        } else {
            return bitmap;
        }
    }

    /**
     * Save bitmap as JPEG with a incremental filename, in the SAVE_FOLDER directory
     * @param activity
     * @param bitmap
     * @return
     * @throws IOException
     */
    public static String saveBitmap(Activity activity, Bitmap bitmap) throws IOException {
        File file;
        try {
            // Create a new save file
            file = createSaveFile();
            OutputStream fOut = new FileOutputStream(file);
            // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.close();

            // Add saved file to gallery
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            activity.sendBroadcast(mediaScanIntent);

        } catch(IOException e) {
            String errorMsg = "Unable to save image";
            Log.e(TAG, errorMsg + ":" + e.getMessage());
            throw new IOException(errorMsg);
        }
        return file.getPath();
    }

    /**
     * Rotate bitmap by given angle in degrees
     * @param bitmap
     * @param angle
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Create a new save file in the SAVE_FOLDER directory with name as '<SAVE_FILENAME_PREFIX>_<3 digit serial number>'
     * ( example: IMG_001)
     * @return
     * @throws IOException
     */
    private static File createSaveFile() throws IOException {
        File file;
        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOut = null;
        String saveFolderPath = path + '/' + SAVE_FOLDER;
        int saveFileIndex = lastSaveFileIndex;
        do {
            String saveFileName = SAVE_FILENAME_PREFIX + String.format("%03d", ++saveFileIndex) + ".jpg";
            String saveFilePath = saveFolderPath + '/' + saveFileName;
            Log.d(TAG,"Saving image to path - "+saveFilePath);
            file = new File(saveFilePath); // the File to save to
            file.getParentFile().mkdirs();
        } while(file.exists());
        file.createNewFile();
        return file;
    }
}
