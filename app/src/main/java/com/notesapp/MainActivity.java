package com.notesapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;


import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.notesapp.prefs.PreffConst;
import com.notesapp.prefs.Preffy;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static int SLEEP_TIME = 2*1000;
    Preffy preffy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preffy = Preffy.getInstance(MainActivity.this);
        getSupportActionBar().hide();
        permissionHandler();
    }

    private void permissionHandler() {
        /****** Create Thread that will sleep for 5 seconds****/
        Thread background = new Thread() {
            public void run() {
                try {
                    // Thread will sleep for 5 seconds
                    sleep(SLEEP_TIME);
                    requestPermissions();
                } catch (Exception e) {
                }
            }
        };
        // start thread
        background.start();
    }

    private void requestPermissions() {
        // below line is use to request permission in the current activity.
        // this method is use to handle error in runtime permissions
        if (Build.VERSION.SDK_INT >= 33) {
            Dexter.withContext(this)
                    // below line is use to request the number of permissions which are required in our app.
                    .withPermissions(Manifest.permission.CAMERA,
                            // below is the list of permissions
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.RECORD_AUDIO)
                    // after adding permissions we are calling an with listener method.
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                            // this method is called when all permissions are granted
                            if (multiplePermissionsReport.areAllPermissionsGranted()) {
                                // do you work now
                                preffy.putBoolean(PreffConst.isPermissions,true);
                                Toast.makeText(MainActivity.this, "All the permissions are granted..", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),LoginRegisterActivity.class));
                                finish();
                            }
                            // check for permanent denial of any permission
                            if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                                // permission is denied permanently, we will show user a dialog message.
                                showSettingsDialog();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                            // this method is called when user grants some permission and denies some of them.
                            permissionToken.continuePermissionRequest();
                        }
                    }).withErrorListener(error -> {
                        preffy.putBoolean(PreffConst.isPermissions,false);
                        // we are displaying a toast message for error message.
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    })
                    // below line is use to run the permissions on same thread and to check the permissions
                    .onSameThread().check();
        }else {
            Dexter.withContext(this)
                    // below line is use to request the number of permissions which are required in our app.
                    .withPermissions(Manifest.permission.CAMERA,
                            // below is the list of permissions
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO)
                    // after adding permissions we are calling an with listener method.
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                            // this method is called when all permissions are granted
                            if (multiplePermissionsReport.areAllPermissionsGranted()) {
                                // do you work now
                                preffy.putBoolean(PreffConst.isPermissions,true);
                                Toast.makeText(MainActivity.this, "All the permissions are granted..", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),LoginRegisterActivity.class));
                                finish();
                            }
                            // check for permanent denial of any permission
                            if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                                // permission is denied permanently, we will show user a dialog message.
                                showSettingsDialog();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                            // this method is called when user grants some permission and denies some of them.
                            permissionToken.continuePermissionRequest();
                        }
                    }).withErrorListener(error -> {
                        preffy.putBoolean(PreffConst.isPermissions,false);
                        // we are displaying a toast message for error message.
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    })
                    // below line is use to run the permissions on same thread and to check the permissions
                    .onSameThread().check();
        }
    }

    // below is the shoe setting dialog method which is use to display a dialogue message.
    private void showSettingsDialog() {
        // we are displaying an alert dialog for permissions
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        // below line is the title for our alert dialog.
        builder.setTitle("Need Permissions");

        // below line is our message for our dialog
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
            // this method is called on click on positive button and on clicking shit button
            // we are redirecting our user from our app to the settings page of our app.
            dialog.cancel();
            // below is the intent from which we are redirecting our user.
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 101);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // this method is called when user click on negative button.
            dialog.cancel();
        });
        // below line is used to display our dialog
        builder.show();
    }
}