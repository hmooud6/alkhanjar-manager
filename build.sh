#!/bin/bash

echo "🔱 AlKhanjar Manager - Build Script"
echo "=================================="
echo ""

cd android-app

echo "📦 تنظيف المشروع..."
./gradlew clean

echo "🔨 بناء APK..."
./gradlew assembleDebug --no-daemon

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ تم البناء بنجاح!"
    echo "📍 الملف: android-app/app/build/outputs/apk/debug/app-debug.apk"
else
    echo ""
    echo "❌ فشل البناء!"
    exit 1
fi
