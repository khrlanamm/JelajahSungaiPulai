package com.khrlanamm.jelajahsungaipulai.logger

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object ActivityLogger {

    fun recordAppOpen() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            val email = currentUser.email

            val activityRecord = hashMapOf(
                "email" to email,
                "last_opened" to FieldValue.serverTimestamp()
            )

            db.collection("activity")
                .document(uid)
                .set(activityRecord, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("ActivityLogger", "Berhasil mencatat aktivitas untuk user: $uid")
                }
                .addOnFailureListener { e ->
                    Log.w("ActivityLogger", "Gagal mencatat aktivitas", e)
                }

        } else {
            Log.d("ActivityLogger", "Tidak ada pengguna yang login, aktivitas tidak dicatat.")
        }
    }
}

