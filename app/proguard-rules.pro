# قواعد ProGuard خاصة بالتطبيق.
# التصغير معطل في build.gradle.kts للإصدار الحالي.

-keepattributes *Annotation*

-keepclassmembers class ** {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}
