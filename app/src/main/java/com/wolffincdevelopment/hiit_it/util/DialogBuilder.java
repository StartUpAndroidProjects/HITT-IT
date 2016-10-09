package com.wolffincdevelopment.hiit_it.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by kylewolff on 9/13/2016.
 */
public class DialogBuilder
{
    private AlertDialog.Builder builder;

    public DialogBuilder(String message, Context context)
    {
        builder = new AlertDialog.Builder(context);

        builder.setMessage(message);
    }

    public void setButtons(String posText, String negText, DialogInterface.OnClickListener listener)
    {
        builder.setPositiveButton(posText, listener);
        builder.setNegativeButton(negText, listener);
    }

    public void create()
    {
        builder.create();
    }

    public void show()
    {
        builder.show();
    }
}
