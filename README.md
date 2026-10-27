# GeoField — مساعد المساحة الميداني / Field Survey Companion

تطبيق أندرويد أصلي يعمل بالكامل بالعربية والإنجليزية، يساعد المساح الميداني على
تسجيل نقاط المسح وحساب المسافات والاتجاهات والمساحات ودقة إغلاق المسار.

A native Android app (Kotlin + Jetpack Compose + Material 3) that helps field
surveyors record points and instantly compute distances, bearings, areas and
traverse closure precision — fully offline.

---

## ✨ المزايا / Features

| | |
|---|---|
| 🗺️ **تسجيل النقاط** | إنشاء وتعديل وحذف النقاط (إيستنق / نورثنق / منسوب) مع 5 أنواع: ركن، مسار، نقطة معيارية، معلم، ملاحظة. |
| 📐 **حسابات هندسية** | مسافة، اتجاه (DMS)، مساحة بطريقة الشو lace، خطأ إغلاق المسار، ونسبة الدقة **1 : N** مع تعديل Bowditch. |
| 📈 **القطاع الطولي** | رسم بياني للمنسوب مرسوم بالكامل عبر Canvas بدون أي مكتبة رسوم خارجية. |
| 🗺️ **مسقط المسار** | مخطط المسار مع فاصل الشمال ومقياس الرسم ومتجه خطأ الإغلاق. |
| 🔍 **بحث وفلترة** | بحث فوري بالكود والوصف، فلترة حسب النوع، ترتيب حسب التاريخ/الكود/المنسوب. |
| ↩️ **تراجع عن الحذف** | حذف بالسحب + زر تراجع داخل الـ Snackbar. |
| 📤 **تصدير** | مشاركة البيانات بصيغة CSV أو GeoJSON عبر أي تطبيق على الجهاز. |
| 🌙 **مظهر** | وضع فاتح/داكن/تلقائي + الوحدات المترية/الإمبراطورية + دقة عشرية قابلة للضبط. |
| 🌍 **ثنائي اللغة** | عربي/إنجليزي كامل مع دعم RTL تلقائي. |
| 💾 **تخزين محلي** | JSON على الجهاز + DataStore للإعدادات — لا يحتاج إنترنت. |

---

## 📱 الشاشات / Screens

1. **الرئيسية** — لوحة مؤشرات: عدد النقاط، طول المسار، دقة الإغلاق، المساحة، القطاع الطولي، إجراءات سريعة، آخر النقاط.
2. **النقاط** — قائمة قابلة للبحث والفلترة، سحب للحذف، زر عائم لإضافة نقطة.
3. **المسار** — حساب المسار المغلق/المفتوح، جدول الأضلاع، الإحداثيات المعدّلة، التصدير.
4. **تفاصيل النقطة** — الإحداثيات مع النسخ، المسافات والاتجاهات لكل نقطة قريبة.
5. **الإعدادات** — المظهر، اللغة، الوحدات، اسم المشروع، إدارة البيانات.

---

## 🛠️ البناء / Build

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

```bash
./gradlew installDebug   # install on a device or emulator
```

## 🧱 البنية / Architecture

```
app/src/main/java/com/agon/app/
├── MainActivity.kt              # Compose scaffold, navigation, theming, RTL
├── data/
│   ├── Models.kt                # SurveyPoint, TraverseResult, AppSettings
│   ├── SurveyCalculator.kt      # distances, bearings, area, Bowditch closure
│   ├── SurveyRepository.kt      # local JSON persistence
│   └── SettingsRepository.kt    # DataStore preferences
├── ui/
│   ├── Strings.kt               # bilingual copy (Arabic / English)
│   ├── Format.kt                # number + unit formatting
│   ├── theme/                   # colour scheme
│   ├── components/              # shared components + Canvas charts
│   └── screens/                 # the five screens
└── viewmodel/SurveyViewModel.kt # app state (StateFlow)
```

**Stack:** Kotlin 2.2.21 · Compose BOM 2026.01.01 · Material 3 ·
Navigation Compose · DataStore · kotlinx.serialization · ViewModel/StateFlow ·
compileSdk 36 / minSdk 24

---

## 📄 الترخيص / License

MIT — للاستخدام التعليمي والمهني الحر.
