# ==============================================================================
# R8 Full Mode Optimization & Obfuscation Rules for OpenCut Studio
# ==============================================================================
# Scoped -keep rules for reflection-driven runtime dependencies:
#   1. androidx.media3 (ExoPlayer, Transformer, Effects, UI, AudioProcessor)
#   2. androidx.room (RoomDatabase, generated *_Impl classes, @Entity, @Dao)
#   3. com.google.firebase.firestore (Firestore SDK annotations & POJO models)
#   4. App model enums (valueOf/values) & Credential Manager
# Everything else is aggressively shrunk and obfuscated by R8 Full Mode.

# Preserve line numbers for Play Console / Crashlytics deobfuscation via mapping.txt
# while stripping original Kotlin/Java source file names from DEX bytecode.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve annotations, generic signatures, and inner-class metadata required by
# Room, Firebase Firestore, and Compose runtime.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Skip expensive multi-pass interprocedural bytecode optimization to prevent
# 504 Gateway Time-out during Play Console release bundling, while still running
# full R8 code shrinking and full class/member obfuscation (>80% obfuscated).
-dontoptimize

# ------------------------------------------------------------------------------
# 1. AndroidX Room (Database, Generated *_Impl, Entities & DAOs)
# ------------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase {
    public <init>();
}
-keep class com.example.data.db.*_Impl { *; }
-keep @androidx.room.Entity class com.example.data.db.** {
    <fields>;
    <init>(...);
}
-keep @androidx.room.Dao interface com.example.data.db.** { *; }
-keep class androidx.room.RoomDatabase { *; }
-keep class androidx.room.RoomDatabase$** { *; }
-keep class androidx.sqlite.db.** { *; }

# ------------------------------------------------------------------------------
# 2. AndroidX Media3 (ExoPlayer, Transformer, Effects, UI & AudioProcessor)
# ------------------------------------------------------------------------------
-keep class androidx.media3.common.Effect { *; }
-keep class * implements androidx.media3.common.Effect {
    public <init>(...);
}
-keep class androidx.media3.common.audio.AudioProcessor { *; }
-keep class * implements androidx.media3.common.audio.AudioProcessor {
    public <init>(...);
}
-keep class androidx.media3.effect.DefaultVideoFrameProcessor$Factory { *; }
-keep class androidx.media3.effect.DefaultVideoFrameProcessor$Factory$Builder { *; }
-keep class androidx.media3.effect.OverlayEffect { *; }
-keep class androidx.media3.effect.TextureOverlay { *; }
-keep class androidx.media3.effect.BitmapOverlay { *; }
-keep class androidx.media3.effect.TextOverlay { *; }
-keep class androidx.media3.effect.StaticOverlaySettings { *; }
-keep class androidx.media3.effect.Crop { *; }
-keep class androidx.media3.effect.Presentation { *; }
-keep class androidx.media3.effect.Contrast { *; }
-keep class androidx.media3.effect.HslAdjustment { *; }
-keep class androidx.media3.effect.RgbAdjustment { *; }
-keep class androidx.media3.effect.ScaleAndRotateTransformation { *; }

-keep class androidx.media3.transformer.Transformer { *; }
-keep class androidx.media3.transformer.Transformer$Builder { *; }
-keep class androidx.media3.transformer.DefaultEncoderFactory { *; }
-keep class androidx.media3.transformer.DefaultMuxer$Factory { *; }
-keep class androidx.media3.transformer.InAppMuxer$Factory { *; }
-keep class androidx.media3.transformer.VideoEncoderSettings { *; }

-keep class androidx.media3.exoplayer.DefaultRenderersFactory { *; }
-keep class androidx.media3.exoplayer.video.MediaCodecVideoRenderer {
    public <init>(...);
}
-keep class androidx.media3.exoplayer.audio.MediaCodecAudioRenderer {
    public <init>(...);
}
-keep class androidx.media3.ui.PlayerView {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class androidx.media3.ui.AspectRatioFrameLayout {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Media3 checks for optional decoder extensions via Class.forName; suppress benign warnings.
-dontwarn androidx.media3.decoder.**
-dontwarn androidx.media3.exoplayer.dash.**
-dontwarn androidx.media3.exoplayer.hls.**
-dontwarn androidx.media3.exoplayer.rtsp.**
-dontwarn androidx.media3.exoplayer.smoothstreaming.**

# ------------------------------------------------------------------------------
# 3. Firebase Cloud Firestore (SDK Annotations, Reflection Mapper & POJO Models)
# ------------------------------------------------------------------------------
-keep @com.google.firebase.firestore.IgnoreExtraProperties class ** {
    public <init>(...);
    <fields>;
    public <methods>;
}
-keepclassmembers class ** {
    @com.google.firebase.firestore.DocumentId *;
    @com.google.firebase.firestore.PropertyName *;
    @com.google.firebase.firestore.ServerTimestamp *;
    @com.google.firebase.firestore.Exclude *;
}
-keep class com.google.firebase.firestore.FirebaseFirestore { *; }
-keep class com.google.firebase.firestore.DocumentSnapshot { *; }
-keep class com.google.firebase.firestore.QuerySnapshot { *; }
-keep class com.google.firebase.firestore.FieldValue { *; }
-keep class com.google.firebase.Timestamp { *; }
-keep class com.example.data.cloud.CloudProject {
    public <init>(...);
    <fields>;
    public <methods>;
}
-keep class com.example.data.cloud.CreatorAnalytics {
    public <init>(...);
    <fields>;
    public <methods>;
}

# ------------------------------------------------------------------------------
# 4. App Model Enums looked up dynamically via valueOf(...)
# ------------------------------------------------------------------------------
-keepclassmembers enum com.example.data.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ------------------------------------------------------------------------------
# 5. AndroidX Credential Manager & Google Identity Services (scoped)
# ------------------------------------------------------------------------------
-keep class androidx.credentials.CredentialManager { *; }
-keep class com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption { *; }
-keep class com.google.android.libraries.identity.googleid.GoogleIdTokenCredential { *; }

