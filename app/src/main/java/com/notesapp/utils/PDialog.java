package com.notesapp.utils;

import android.app.Activity;
import android.app.ProgressDialog;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class PDialog {
    static ProgressDialog progressDialog;
    public static void method(Activity activity, String message) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
    }
    public static void show(){
        progressDialog.show();
    }

    public static void dismiss(){
        progressDialog.dismiss();
    }


    public static void me(FragmentActivity activity, String message) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
}
