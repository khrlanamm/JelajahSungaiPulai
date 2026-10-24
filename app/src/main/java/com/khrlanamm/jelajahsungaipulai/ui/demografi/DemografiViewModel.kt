package com.khrlanamm.jelajahsungaipulai.ui.demografi

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.khrlanamm.jelajahsungaipulai.data.StatistikPenduduk

class DemografiViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _totalPenduduk = MutableLiveData<Int>()
    val totalPenduduk: LiveData<Int> = _totalPenduduk

    private val _totalKepalaKeluarga = MutableLiveData<Int>()
    val totalKepalaKeluarga: LiveData<Int> = _totalKepalaKeluarga

    private val _totalLakiLaki = MutableLiveData<Int>()
    val totalLakiLaki: LiveData<Int> = _totalLakiLaki

    private val _totalPerempuan = MutableLiveData<Int>()
    val totalPerempuan: LiveData<Int> = _totalPerempuan

    private val _ageGroupData = MutableLiveData<Map<String, Int>>()
    val ageGroupData: LiveData<Map<String, Int>> = _ageGroupData

    private val _educationData = MutableLiveData<Map<String, Int>>()
    val educationData: LiveData<Map<String, Int>> = _educationData

    private val _occupationData = MutableLiveData<Map<String, Int>>()
    val occupationData: LiveData<Map<String, Int>> = _occupationData

    private val _maritalStatusData = MutableLiveData<Map<String, Int>>()
    val maritalStatusData: LiveData<Map<String, Int>> = _maritalStatusData

    private val _religionData = MutableLiveData<Map<String, Int>>()
    val religionData: LiveData<Map<String, Int>> = _religionData

    private val _kampungRtData = MutableLiveData<Map<String, Int>>()
    val kampungRtData: LiveData<Map<String, Int>> = _kampungRtData

    private val _currentFilter = MutableLiveData<String?>()
    val currentFilter: LiveData<String?> = _currentFilter

    // Membaca ringkasan di koleksi statistik. Data mentah penduduk hanya boleh dibaca admin.
    fun loadPendudukData(kampungFilter: String?) {
        _isLoading.value = true
        _errorMessage.value = ""
        _currentFilter.value = kampungFilter

        Log.d("DemografiViewModel", "Memuat statistik dengan filter kampung: '$kampungFilter'")

        listener?.remove()
        listener = db.collection(StatistikPenduduk.COLLECTION)
            .document(kampungFilter ?: StatistikPenduduk.SEMUA)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    Log.w("DemografiViewModel", "Gagal mendengarkan data.", e)
                    _errorMessage.value = "Gagal memuat data: ${e.message}"
                    return@addSnapshotListener
                }

                val statistik = snapshot?.let { StatistikPenduduk.fromFirestore(it) }
                if (statistik == null) {
                    _errorMessage.value = "Data demografi belum tersedia."
                    return@addSnapshotListener
                }
                tampilkan(statistik)
            }
    }

    private fun tampilkan(statistik: StatistikPenduduk) {
        _totalPenduduk.value = statistik.total
        _totalKepalaKeluarga.value = statistik.kepalaKeluarga
        _totalLakiLaki.value = statistik.lakiLaki
        _totalPerempuan.value = statistik.perempuan
        _ageGroupData.value = StatistikPenduduk.KELOMPOK_UMUR.associateWith { statistik.umur[it] ?: 0 }
        _educationData.value = statistik.pendidikan
        _occupationData.value = statistik.pekerjaan
        _maritalStatusData.value = statistik.status
        _religionData.value = statistik.agama
        _kampungRtData.value = statistik.wilayah
    }

    override fun onCleared() {
        listener?.remove()
        super.onCleared()
    }
}
