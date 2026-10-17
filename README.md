# کاغذ (Kaghaz)

اپ یادداشت‌برداری اندرویدی با پشتیبانی مارک‌داون — متن‌باز، سبک و بدون اینترنت.

## امکانات
- ساخت، ویرایش و حذف یادداشت (حذف با نگه‌داشتن روی کارت)
- مارک‌داون با پیش‌نمایش زنده (تیتر، لیست، بولد، ایتالیک، کد، نقل‌قول، لینک)
- ذخیره‌سازی کاملاً محلی روی دستگاه
- رابط فارسی و راست‌به‌چپ

## دانلود APK
آخرین خروجی اندروید از بخش [Releases](../../releases) قابل دانلود است.
بیلد به‌صورت خودکار با GitHub Actions انجام می‌شود.

## بیلد محلی
پیش‌نیاز: JDK 17 و Gradle 8.7 (و Android SDK با compileSdk 34)

```
gradle assembleDebug
```

## ساختار پروژه
- `app/src/main/java/com/kaghaz/app/MainActivity.kt` — لیست یادداشت‌ها
- `app/src/main/java/com/kaghaz/app/EditorActivity.kt` — ویرایشگر و پیش‌نمایش
- `app/src/main/assets/preview.html` — رندر مارک‌داون داخل WebView
- `.github/workflows/android.yml` — بیلد خودکار و ساخت Release

## مجوز
MIT — جزئیات در فایل [LICENSE](LICENSE).
