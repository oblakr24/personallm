# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

-keep, allowobfuscation, allowoptimization, allowaccessmodification @kotlinx.serialization.Serializable class *



 # With R8 full mode generic signatures are stripped for classes that are not
 # kept. Suspend functions are wrapped in continuations where the type argument
 # is used.
 -keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

 -keepclassmembers,allowobfuscation class * {
     @com.google.gson.annotations.SerializedName <fields>;
   }
 -keep,allowobfuscation @interface com.google.gson.annotations.SerializedName

 -dontwarn java.awt.event.ActionEvent
 -dontwarn java.lang.management.ManagementFactory
 -dontwarn java.lang.management.RuntimeMXBean

# Ktor
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.atomicfu.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**
-dontwarn org.slf4j.**
-keep class io.netty.** {*; }
-keep class kotlin.reflect.jvm.internal.** { *; }

-dontwarn kotlinx.serialization.internal.AbstractPolymorphicSerializer

-dontwarn java.awt.event.ActionListener
-dontwarn javax.swing.SwingUtilities
-dontwarn javax.swing.Timer
-dontwarn org.slf4j.impl.StaticLoggerBinder