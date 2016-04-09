# saxrssreader library
-keep class nl.matshofman.saxrssreader.** { *; }

# retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-dontwarn okio.**
-dontwarn org.simpleframework.xml.stream.**

-keep public class org.simpleframework.** { *; }
-keep class org.simpleframework.xml.** { *; }
-keep class org.simpleframework.xml.core.** { *; }
-keep class org.simpleframework.xml.util.** { *; }

-keep class org.indywidualni.centrumfm.rest.model.** { *; }

# prettytime
-keep class org.ocpsoft.prettytime.i18n.**

# butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# fragment classes names
-keepnames class org.indywidualni.centrumfm.fragment.*

# remove logs from releases
-assumenosideeffects class android.util.Log {
    public * ;
}
