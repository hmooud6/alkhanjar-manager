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
