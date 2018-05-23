package com.wolffincdevelopment.hiit_it.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by kylewolff on 9/10/2016.
 */
public class PermissionUtil {

    private String READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private String PHONE_STATE = Manifest.permission.READ_PHONE_STATE;

    public PermissionUtil() {

    }

    public interface PermissionCheckCallback {
        void onPermissionCheckGranted( int permissionID );
        void onPermissionCheckDenied( int permissionID );
    }


    public boolean isPermissionGranted(Context context, String permission) {
        return (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED);
    }

    public boolean isReadStoragePermissionGranted(Context context) {
        return isPermissionGranted(context, READ_STORAGE);
    }

    public boolean checkReadStoragePermission(@NonNull Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, READ_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            checkPermissionRationale(activity, READ_STORAGE);

            return false;
        }
    }

    public boolean checkPhoneStatePermission(@NonNull Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            checkPermissionRationale(activity, PHONE_STATE);

            return false;
        }
    }

    @TargetApi(23)
    public void checkPermissionRationale(@NonNull Activity activity, String permission) {
        ActivityCompat.requestPermissions(activity,
                new String[]{permission},
                0);
    }

}
