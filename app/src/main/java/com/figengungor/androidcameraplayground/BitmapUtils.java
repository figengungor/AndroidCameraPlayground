package com.figengungor.androidcameraplayground;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by figengungor on 3/29/2018.
 */

public class BitmapUtils {

    /*Files you save in the directories provided by getExternalFilesDir() or getFilesDir() are
    deleted when the user uninstalls your app.

    Once you decide the directory for the file, you need to create a collision-resistant file name.
    You may wish also to save the path in a member variable for later use. */

    public static File createTemporaryImageFile(Context contex) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = contex.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return imageFile;
    }

    /**
     * Resamples the captured photo to fit the screen for better memory usage.
     *
     * @param context   The application context.
     * @param imagePath The path of the photo to be resampled.
     * @return The resampled bitmap
     */
    static Bitmap resamplePic(Context context, String imagePath) {

        // Get device screen size information
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);

        int targetH = metrics.heightPixels;
        int targetW = metrics.widthPixels;

       /* "BitmapFactory.Options

           This class is a metadata provider for us. We can use this class to get image size.

           We pass BitmapFactory.Options instance to BitmapFactory.decodeFile() method.
           You can see that we configured our “options” by setting inJustDecodeBounds true.
           What is the meaning of inJustDecodeBounds? It means that we don’t want to load bitmap
           into memory. We just want to get information(width, height, etc.) about image. So we can
           calculate scale factor with that information."

           https://android.jlelse.eu/loading-large-bitmaps-efficiently-in-android-66826cd4ad53
        */

        // Get the dimensions of the original bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        /* "Now it is time to calculate inSampleSize. Wait. What is inSampleSize? inSampleSize is
           a scale factor that belongs to BitmapFactory.Options class.
           If we have an image 1000x1000 and we set inSampleSize 2 before decoding.
           We will have 500x500 image after decoding operation. If we have 200x400 image
           and we set inSampleSize 5, we will have 40x80 image after decoding.

           Can we use it just like this? No. Because we don’t know what image size is.
           If it is small image and we make it more smaller, our user can see some pixels
           instead of image. Some images have to be scaled down 5 times. Some images have
           to be scaled down 2 times. We can not set scale factor as a constant.
           So we have to do a calculation according to image size.

           Calculating inSampleSize is up to you.
           I mean, you can write your algorithm according to your needs."

           https://android.jlelse.eu/loading-large-bitmaps-efficiently-in-android-66826cd4ad53
         */

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(imagePath, bmOptions);
    }

    /**
     * Deletes image file for a given path.
     *
     * @param context   The application context.
     * @param imagePath The path of the photo to be deleted.
     */
    static boolean deleteImageFile(Context context, String imagePath) {
        // Get the file
        File imageFile = new File(imagePath);

        // Delete the image
        boolean deleted = imageFile.delete();

        // If there is an error deleting the file, show a Toast
        if (!deleted) {
            String errorMessage = context.getString(R.string.file_deletion_error);
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
        }

        return deleted;
    }
}
