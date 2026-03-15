# Keep Room entities
-keep class com.fourthshelfmedia.sleepanchor.data.local.** { *; }

# Keep serialization
-keepattributes *Annotation*
-keep class kotlinx.serialization.** { *; }
