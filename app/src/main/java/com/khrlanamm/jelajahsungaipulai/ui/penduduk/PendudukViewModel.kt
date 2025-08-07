package com.khrlanamm.jelajahsungaipulai.ui.penduduk

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.khrlanamm.jelajahsungaipulai.data.Penduduk // Import kelas Penduduk dari folder data

class PendudukViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // LiveData untuk menyimpan status admin
    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> get() = _isAdmin

    // LiveData untuk menandai ketika pengecekan selesai
    private val _isCheckComplete = MutableLiveData<Boolean>()
    val isCheckComplete: LiveData<Boolean> get() = _isCheckComplete

    // LiveData untuk menyimpan daftar penduduk
    private val _pendudukList = MutableLiveData<List<Penduduk>>()
    val pendudukList: LiveData<List<Penduduk>> get() = _pendudukList

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
                // Jika user adalah admin, ambil data penduduk
                if (!documents.isEmpty) {
                    fetchPendudukData()
                }
            }
            .addOnFailureListener {
                // Jika gagal, anggap bukan admin
                _isAdmin.postValue(false)
                _isCheckComplete.postValue(true)
            }
    }

    // Fungsi untuk mengambil data penduduk dari koleksi "penduduk"
    private fun fetchPendudukData() {
        db.collection("penduduk")
            .get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<Penduduk>()
                for (document in result) {
                    list.add(Penduduk.fromFirestore(document))
                }
                _pendudukList.postValue(list)
            }
            .addOnFailureListener { exception ->
                // Tangani kesalahan (misalnya, log, tampilkan pesan toast)
                // Untuk saat ini, post daftar kosong jika gagal
                _pendudukList.postValue(emptyList())
                // Anda mungkin ingin menambahkan LiveData lain untuk pesan kesalahan yang akan diamati di Fragment
            }
    }
}
