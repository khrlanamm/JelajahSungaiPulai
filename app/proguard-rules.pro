# ===================================================================
# Aturan Umum ProGuard
# ===================================================================
# Mempertahankan anotasi yang digunakan oleh library seperti Room dan Firebase.
-keepattributes *Annotation*

# Mempertahankan metadata penting untuk refleksi dan generik.
-keepattributes Signature

# Mempertahankan implementasi Parcelable, penting untuk mengirim objek antar komponen.
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ===================================================================
# Aturan untuk Library Firebase
# ===================================================================
# Aturan ini penting agar Firebase Authentication dan Firestore berfungsi dengan baik.
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.functions.** { *; }

# Mempertahankan nama field yang dianotasi dengan @PropertyName untuk serialisasi Firestore.
-keepclassmembers,allowshrinking class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}


# ===================================================================
# Aturan Aplikasi Jelajah Sungai Pulai
# ===================================================================
# Ganti 'com.khrlanamm.jelajahsungaipulai' jika namespace Anda berbeda.

# --- 1. Paket Data (Model) ---
# Melindungi semua kelas di dalam paket 'data' (cth: Penduduk, Warga).
# Ini penting agar nama field tidak diubah, sehingga serialisasi/deserialisasi
# dari Firestore atau Room tidak gagal.
-keep class com.khrlanamm.jelajahsungaipulai.data.** { *; }
-keepnames class com.khrlanamm.jelajahsungaipulai.data.** { *; }
-keepclassmembers class com.khrlanamm.jelajahsungaipulai.data.** { *; }

# --- 2. Paket UI ---
# Melindungi semua kelas di dalam paket 'ui' dan sub-paketnya
# (cth: Activity, Fragment, ViewModel, Adapter).
# Ini mencegah error karena Android Framework tidak dapat menemukan kelas-kelas ini.
-keep class com.khrlanamm.jelajahsungaipulai.ui.** { *; }
-keepnames class com.khrlanamm.jelajahsungaipulai.ui.** { *; }
-keepclassmembers class com.khrlanamm.jelajahsungaipulai.ui.** { *; }


# ===================================================================
# Aturan untuk Library Pihak Ketiga Lainnya
# ===================================================================

# --- Aturan untuk Room ---
# Memastikan kelas-kelas yang berhubungan dengan database Room tidak dihapus.
-keep class androidx.room.RoomDatabase** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.migration.Migration
-keep class * implements androidx.room.migration.Migration
-keepclassmembers class * {
    @androidx.room.Dao <methods>;
    @androidx.room.Entity <fields>;
    @androidx.room.PrimaryKey <fields>;
    @androidx.room.ColumnInfo <fields>;
    @androidx.room.Embedded <fields>;
    @androidx.room.Relation <fields>;
}

# --- Aturan untuk Glide ---
# Diperlukan agar Glide dapat memuat gambar dengan benar setelah obfuscation.
# Aturan spesifik untuk enum dihapus karena sering menyebabkan error pada versi baru
# dan aturan bawaan (consumer rules) dari Glide biasanya sudah cukup.
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule

# --- Aturan untuk Gson ---
# Mempertahankan konstruktor dan class yang diperlukan untuk serialisasi/deserialisasi.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.TypeAdapter

# --- Aturan untuk PhotoView ---
-keep class com.github.chrisbanes.photoview.** { *; }


# --- Aturan untuk Kotlin Coroutines (Diperbarui) ---
# Sebagian besar aturan coroutines sudah disertakan secara otomatis oleh library (consumer rules).
# Aturan spesifik untuk kelas internal yang usang telah dihapus untuk menghindari error.
# Aturan di bawah ini dipertahankan untuk meningkatkan kualitas stack trace saat debug.
-keepnames class kotlin.coroutines.jvm.internal.DebugMetadataKt

