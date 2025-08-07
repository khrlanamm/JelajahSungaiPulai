package com.khrlanamm.jelajahsungaipulai.ui.demografi

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.khrlanamm.jelajahsungaipulai.data.Warga // Import data class Warga

class DemografiViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance() // Inisialisasi Firestore
    private val _wargaList = MutableLiveData<List<Warga>>()
    val wargaList: LiveData<List<Warga>> = _wargaList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Fungsi untuk memuat data penduduk dari Firestore
    fun loadPendudukData(kampungFilter: String?) {
        _isLoading.value = true // Set status loading menjadi true
        _errorMessage.value = null // Reset pesan error

        // Log filter yang diterima untuk debugging
        Log.d("DemografiViewModel", "Memuat data dengan filter kampung: '$kampungFilter'")

        val collectionRef = db.collection("penduduk")
        val query: Query = if (kampungFilter != null) {
            // Jika ada filter kampung, tambahkan klausa where
            // Pastikan nilai 'kampungFilter' sama persis (case-sensitive dan tanpa spasi ekstra)
            // dengan nilai di Firestore.
            collectionRef.whereEqualTo("kampung", kampungFilter)
        } else {
            // Jika tidak ada filter, ambil semua data
            collectionRef
        }

        // Mendengarkan perubahan data secara real-time dengan onSnapshot
        query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("DemografiViewModel", "Gagal mendengarkan data.", e)
                _errorMessage.value = "Gagal memuat data: ${e.message}"
                _isLoading.value = false // Sembunyikan progress bar jika ada error
                return@addSnapshotListener
            }

            val list = mutableListOf<Warga>()
            if (snapshots != null) {
                for (doc in snapshots) {
                    // Konversi setiap dokumen menjadi objek Warga
                    val warga = doc.toObject(Warga::class.java)
                    list.add(warga)
                }
            }
            _wargaList.value = list // Perbarui LiveData dengan daftar warga
            _isLoading.value = false // Set status loading menjadi false
            Log.d("DemografiViewModel", "Data berhasil dimuat: ${list.size} item.")
            if (list.isEmpty() && kampungFilter != null) {
                Log.d("DemografiViewModel", "Tidak ada data ditemukan untuk filter '$kampungFilter'. Periksa kembali penulisan filter dan data di Firestore (case-sensitive).")
            }
        }
    }
}

