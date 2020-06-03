package com.samramakrishnan.campusbustracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Utils {
    public static boolean IS_TEST_VERSION = true;
    public static final String BASE_URL = "http://transitdata.cityofmadison.com/";
    public static LatLng START_MAP_POSITION = new LatLng(43.071108, -89.399063);
    public static int START_MAP_ZOOM = 10;
    public static int MAP_REFRESH_RATE = 90; // Refresh every x seconds

    public  static void displayErrorDialog (Context ctx, String errMsg){
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(errMsg);
        builder.setTitle(ctx.getResources().getString(R.string.app_name));
        builder.setNeutralButton(ctx.getResources().getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    public static String formatTime(long time){

        // Creating date format
        DateFormat simple = new SimpleDateFormat("HH : mm a");

        // Creating date from milliseconds
        // using Date() constructor
        Date result = new Date((time*1000));

        // Formatting Date according to the
        // given format
        return simple.format(result);
    }
}
