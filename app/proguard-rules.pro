# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ML Kit
-keep class com.google.mlkit.** { *; }
# Keep specific methods and classes of AndroidHttpClient
# Exclude android.net.http.AndroidHttpClient completely
# Ignore the android.net.http package
-dontwarn android.net.http.**

# Keep specific methods and classes of HttpClient
-keep class org.apache.http.client.HttpClient {
    *;
}

-dontwarn com.google.mlkit.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-dontwarn org.jspecify.nullness.Nullable
-dontwarn reactor.blockhound.integration.BlockHoundIntegration
# Other libraries...
