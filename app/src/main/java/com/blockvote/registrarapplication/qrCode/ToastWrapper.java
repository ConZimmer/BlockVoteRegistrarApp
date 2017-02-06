package com.blockvote.registrarapplication.qrCode;

import android.content.Context;
import android.widget.Toast;

public class ToastWrapper {

    public static void initiateToast(Context context, String msg){
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, msg, duration).show();

    }
}
