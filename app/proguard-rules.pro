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
# Retrofit# Keep all Retrofit service interfaces
-keep interface retrofit2.** { *; }
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Keep all @Body, @POST, etc. annotated method metadata
-keep class * {    @retrofit2.http.* <methods>;}

# OkHttp (if used)
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# Keep Retrofit annotations and generic signature info
-keepattributes Signature, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, MethodParameters, Exceptions

# Keep model classes and their fields if serialized via Gson reflection
-keep class com.yourpkg.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Only if you see related warnings and don’t use the Hilt plugin correctly
-dontwarn dagger.hilt.internal.**
-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }

# Keep Kotlin metadata so Moshi can read Kotlin property names
-keep class kotlin.Metadata { *; }

# Keep your JSON model classes’ names and properties so reflection can bind correctly.
# Narrow the package to where your DTOs/models live.
-keep class your.model.package.** { *; }

# If you have custom @JsonQualifier annotations, keep them:
-keep @com.squareup.moshi.JsonQualifier interface * { *; }

-keep class cyn.mobile.app.data.model.* { *; }
-keep class cyn.mobile.app.data.repositories.auth.request.* { *; }
-keep class cyn.mobile.app.data.repositories.auth.response.* { *; }
-keep class cyn.mobile.app.data.repositories.base.response.* { *; }


