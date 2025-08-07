package com.khrlanamm.jelajahsungaipulai.ui.penduduk

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PendudukViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // LiveData untuk menyimpan status admin
    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> get() = _isAdmin

    // LiveData untuk menandai ketika pengecekan selesai
    private val _isCheckComplete = MutableLiveData<Boolean>()
    val isCheckComplete: LiveData<Boolean> get() = _isCheckComplete

    init {
        checkUserRole()
    }

    private fun checkUserRole() {
        val currentUser = auth.currentUser
        if (currentUser?.email == null) {
            _isAdmin.postValue(false)
            _isCheckComplete.postValue(true)
            return
        }

        db.collection("admins")
            .whereEqualTo("email", currentUser.email)
            .get()
            .addOnSuccessListener { documents ->
                // Jika dokumen tidak kosong, berarti user adalah admin
                _isAdmin.postValue(!documents.isEmpty)
                _isCheckComplete.postValue(true)
            }
            .addOnFailureListener {
                // Jika gagal, anggap bukan admin
                _isAdmin.postValue(false)
                _isCheckComplete.postValue(true)
            }
    }
}
