package com.figengungor.androidcameraplayground;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    //https://android.jlelse.eu/androids-new-image-capture-from-a-camera-using-file-provider-dd178519a954
    //https://developer.android.com/training/camera/photobasics.html
    //https://developer.android.com/topic/performance/graphics/load-bitmap.html
    //https://github.com/udacity/AdvancedAndroid_Emojify
    //https://android.jlelse.eu/loading-large-bitmaps-efficiently-in-android-66826cd4ad53

    // TODO STEP 1 : Declare <uses-feature> for camera feature in AndroidManifest.xml
    /* If an essential function of your application is taking pictures, then restrict its visibility
       on Google Play to devices that have a camera. To advertise that your application depends on
       having a camera, put a <uses-feature> tag in your manifest
       https://developer.android.com/training/camera/photobasics.html#TaskScalePhoto */

    // TODO STEP 2 : Add required permission to save image file to external storage
            //TODO STEP 2b : Check runtime permission
    /*<uses-permission
       android:name="android.permission.WRITE_EXTERNAL_STORAGE" />*/

    /*Start camera intent with this code and
      this code will be returned in onActivityResult() when the activity exists(camera app).
      So with this code you can distinguish several requests for different actions. */
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    String tempPhotoFilePath;

    ImageView photoIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        photoIv = findViewById(R.id.photoIv);
    }

    // TODO STEP 3: Create an Intent to delegate the taking picture action to camera app
    /* MediaStore.ACTION_IMAGE_CAPTURE
       Standard Intent action that can be sent
       to have the camera application capture an image and return it.*/

    private void openCameraWithIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        /*Before calling startActivityForResult, make sure there is an activity which can handle
          this Intent. Use resolveActivity() which returns the first activity component that can
          handle the Intent*/

        // TODO STEP 3a: Create a temporary image file
       /* The Android Camera application saves a full-size photo if you give it a file to save into.
        You must provide a fully qualified file name where the camera app should save the photo.*/
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTemporaryImageFile(this);
            } catch (IOException e) {
                //Error occurred while creating the file
                e.printStackTrace();
            }
            //Continue only if the file was successfully created
            if (photoFile != null) {
                // TODO Step 3b: Get temporary file path to use image processing later
                //Get path of the temporary file
                tempPhotoFilePath = photoFile.getAbsolutePath();

                // TODO Step 3c: Configure FileProvider.
                // TODO Step 3c1: Add a provider to your application in AndroidManifest.xml
                // TODO Step 3c2: Get Uri from FileProvider
               /* Note: We are using getUriForFile(Context, String, File) which returns
                  a content:// URI. For more recent apps targeting Android 7.0 (API level 24)
                  and higher, passing a file:// URI across a package boundary causes
                  a FileUriExposedException.Therefore, we now present a more generic way
                  of storing images using a FileProvider.

                  https://developer.android.com/training/camera/photobasics.html

                  Make sure that the authorities string matches the second argument to
                  getUriForFile(Context, String, File). In the meta-data section of the
                  provider definition, you can see that the provider expects eligible paths
                  to be configured in a dedicated resource file, res/xml/file_paths.xml.
                  */
                Uri photoUri = FileProvider.getUriForFile(
                        this,
                        getString(R.string.file_provider_authority),
                        photoFile);

               /* MediaStore.EXTRA_OUTPUT
                  The name of the Intent-extra used to indicate a content resolver Uri to be used
                  to store the requested image or video.*/
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }

        } else {
            Toast.makeText(this, R.string.camera_not_found_message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Return of the Camera
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            //Picture is taken
            if (resultCode == RESULT_OK) {
                /*Photos taken using your Android devices's camera which are typically much higher
                resolution than the screen density of your device.

                Given that you are working with limited memory, ideally you only want to load
                a lower resolution version in memory. The lower resolution version should match
                the size of the UI component that displays it.

                https://developer.android.com/topic/performance/graphics/load-bitmap.html
                https://android.jlelse.eu/loading-large-bitmaps-efficiently-in-android-66826cd4ad53
                // TODO 4: Load large bitmap efficiently
                Steps
                    Get size of image without loading into memory
                    Calculate scale factor with image’s size.
                    Load bitmap into memory with calculated values.
                */
                Bitmap resultBitmap = BitmapUtils.resamplePic(this, tempPhotoFilePath);
                photoIv.setImageBitmap(resultBitmap);


            } else { //Action is cancelled, delete the temporary file
                BitmapUtils.deleteImageFile(this, tempPhotoFilePath);
            }
        }
    }

    public void onTakeAPictureBtnClicked(View view) {
        checkCameraPermission();

    }

    private void checkCameraPermission() {
        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            // Launch the camera if the permission exists
            openCameraWithIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera
                    openCameraWithIntent();
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}
