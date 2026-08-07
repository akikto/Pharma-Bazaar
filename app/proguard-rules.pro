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

# Keep line numbers for readable crash stack traces, but hide the real
# source file name in them.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room entities/DAOs are accessed by generated code at compile time, but
# keep them defensively in case of any reflection-based access.
-keep class com.example.data.db.entities.** { *; }
-keep interface com.example.data.db.dao.** { *; }

# Firebase Auth / Firestore / Credential Manager / Google Identity classes
# used for sign-in; their own consumer-rules.pro is merged automatically,
# this is a defensive backstop.
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }
