package com.android.passmanager.Util;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * 自定义Toast消息
 */
public  class MyToast extends Toast {
    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */
    public MyToast(Context context) {
        super( context );
    }
    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */
    public static void showToast(Context context , String word , long time) {
        final Toast toast = Toast.makeText(context, word, Toast.LENGTH_LONG );
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed( new Runnable() {
            public void run() {
                toast.cancel();
            }
        }, time );
    }

    public static void showToast(final Activity activity, final String word , final long time) {
        activity.runOnUiThread( new Runnable() {
            public void run() {
                final Toast toast = Toast.makeText(activity, word, Toast.LENGTH_LONG );
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed( new Runnable() {
                    public void run() {
                        toast.cancel();
                    }
                }, time );
            }
        } );
    }
}