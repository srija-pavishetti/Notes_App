package com.notesapp.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

public class toast {
    public static void message(Context context, String message) {
       // Toast.makeText(context, message, Toast.LENGTH_LONG).show();

            Toast mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);

            // cancel previous toast and display correct answer toast
            try {
                if (mToast.getView().isShown()) {
                    mToast.cancel();
                }
                // cancel same toast only on Android P and above, to avoid IllegalStateException on addView
                if (Build.VERSION.SDK_INT >= 28 && mToast.getView().isShown()) {
                    mToast.cancel();
                }
                mToast.show();
            } catch (Exception e) {
                e.printStackTrace();
                mToast.show();//This line will show toast if previous toast is null
            }

    }

    public static void me(FragmentActivity activity, String message) {
        //Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        activity.runOnUiThread(()->{
            Toast mToast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);

            // cancel previous toast and display correct answer toast
            try {
                if (mToast.getView().isShown()) {
                    mToast.cancel();
                }
                // cancel same toast only on Android P and above, to avoid IllegalStateException on addView
                if (Build.VERSION.SDK_INT >= 28 && mToast.getView().isShown()) {
                    mToast.cancel();
                }
                mToast.show();
            } catch (Exception e) {
                e.printStackTrace();
                mToast.show();//This line will show toast if previous toast is null
            }
        });

    }

    public static void meActivity(Activity activity, String message) {
        //Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        activity.runOnUiThread(()-> {
            Toast mToast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);

            // cancel previous toast and display correct answer toast
            try {
                if (mToast.getView().isShown()) {
                    mToast.cancel();
                }
                // cancel same toast only on Android P and above, to avoid IllegalStateException on addView
                if (Build.VERSION.SDK_INT >= 28 && mToast.getView().isShown()) {
                    mToast.cancel();
                }
                mToast.show();
            } catch (Exception e) {
                e.printStackTrace();
                mToast.show();//This line will show toast if previous toast is null
            }
        });
    }
}
