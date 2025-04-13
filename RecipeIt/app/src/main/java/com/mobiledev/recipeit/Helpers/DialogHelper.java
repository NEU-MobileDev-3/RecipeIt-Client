package com.mobiledev.recipeit.Helpers;

import android.app.AlertDialog;
import android.content.Context;

public class DialogHelper {
    public static void showErrorDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)              // e.g. "Error"
                .setMessage(message)          // e.g. "Something went wrong."
                .setCancelable(true)
                .setPositiveButton("OK", null)
                .show();
    }
}
