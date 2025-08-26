package com.khrlanamm.jelajahsungaipulai.ui.demografi

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.khrlanamm.jelajahsungaipulai.data.Warga
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Date

class DemografiViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _wargaList = MutableLiveData<List<Warga>>()
    val wargaList: LiveData<List<Warga>> = _wargaList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // LiveData untuk statistik demografi
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

    fun loadPendudukData(kampungFilter: String?) {
        _isLoading.value = true
        _errorMessage.value = ""
        _currentFilter.value = kampungFilter // Simpan filter saat ini

        Log.d("DemografiViewModel", "Memuat data dengan filter kampung: '$kampungFilter'")

        val collectionRef = db.collection("penduduk")
        val query: Query = if (kampungFilter != null) {
            collectionRef.whereEqualTo("kampung", kampungFilter)
        } else {
            collectionRef
        }

        query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("DemografiViewModel", "Gagal mendengarkan data.", e)
                _errorMessage.value = "Gagal memuat data: ${e.message}"
                _isLoading.value = false
                return@addSnapshotListener
            }

            val list = mutableListOf<Warga>()
            if (snapshots != null) {
                for (doc in snapshots) {
                    val warga = doc.toObject(Warga::class.java)
                    list.add(warga)
                }
            }
            _wargaList.value = list
            _isLoading.value = false
            Log.d("DemografiViewModel", "Data berhasil dimuat: ${list.size} item.")

            if (list.isEmpty() && kampungFilter != null) {
                Log.d("DemografiViewModel", "Tidak ada data ditemukan untuk filter '$kampungFilter'. Periksa kembali penulisan filter dan data di Firestore (case-sensitive).")
            }

            // Setelah data dimuat, hitung statistik demografi
            calculateDemographics(list)
        }
    }

    private fun calculateDemographics(wargaList: List<Warga>) {
        // 1. Jumlah Penduduk Total
        _totalPenduduk.value = wargaList.size

        // 2. Jumlah Kepala Keluarga
        _totalKepalaKeluarga.value = wargaList.count { it.hub_keluarga == "Kepala Keluarga" }

        // 3. Jumlah Laki-Laki dan Perempuan
        _totalLakiLaki.value = wargaList.count { it.jenis_kel == "L" }
        _totalPerempuan.value = wargaList.count { it.jenis_kel == "P" }

        // 4. Berdasarkan Kelompok Umur
        val ageGroups = mutableMapOf(
            "0-5 Tahun" to 0,
            "6-12 Tahun" to 0,
            "13-18 Tahun" to 0,
            "19-30 Tahun" to 0,
            "31-50 Tahun" to 0,
            "51+ Tahun" to 0
        )
        val currentDate = LocalDate.now()
        for (warga in wargaList) {
            warga.tgl_lahir?.toDate()?.let { birthDate ->
                val localBirthDate = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                val age = Period.between(localBirthDate, currentDate).years
                when (age) {
                    in 0..5 -> ageGroups["0-5 Tahun"] = ageGroups.getValue("0-5 Tahun") + 1
                    in 6..12 -> ageGroups["6-12 Tahun"] = ageGroups.getValue("6-12 Tahun") + 1
                    in 13..18 -> ageGroups["13-18 Tahun"] = ageGroups.getValue("13-18 Tahun") + 1
                    in 19..30 -> ageGroups["19-30 Tahun"] = ageGroups.getValue("19-30 Tahun") + 1
                    in 31..50 -> ageGroups["31-50 Tahun"] = ageGroups.getValue("31-50 Tahun") + 1
                    else -> ageGroups["51+ Tahun"] = ageGroups.getValue("51+ Tahun") + 1
                }
            }
        }
        _ageGroupData.value = ageGroups

        // 5. Berdasarkan Pendidikan
        val educationCounts = mutableMapOf<String, Int>()
        for (warga in wargaList) {
            warga.pendidikan?.let { pendidikan ->
                educationCounts[pendidikan] = educationCounts.getOrDefault(pendidikan, 0) + 1
            }
        }
        _educationData.value = educationCounts.toSortedMap() // Urutkan berdasarkan nama pendidikan

        // 6. Berdasarkan Pekerjaan
        val occupationCounts = mutableMapOf<String, Int>()
        for (warga in wargaList) {
            warga.pekerjaan?.let { pekerjaan ->
                occupationCounts[pekerjaan] = occupationCounts.getOrDefault(pekerjaan, 0) + 1
            }
        }
        _occupationData.value = occupationCounts.toSortedMap() // Urutkan berdasarkan nama pekerjaan

        // 7. Berdasarkan Perkawinan
        val maritalStatusCounts = mutableMapOf(
            "Belum Kawin" to 0,
            "Kawin" to 0,
            "Cerai Hidup" to 0,
            "Cerai Mati" to 0,
            "Lainnya" to 0
        )
        for (warga in wargaList) {
            when (warga.status) {
                "Belum Kawin" -> maritalStatusCounts["Belum Kawin"] = maritalStatusCounts.getValue("Belum Kawin") + 1
                "Kawin" -> maritalStatusCounts["Kawin"] = maritalStatusCounts.getValue("Kawin") + 1
                "Cerai Hidup" -> maritalStatusCounts["Cerai Hidup"] = maritalStatusCounts.getValue("Cerai Hidup") + 1
                "Cerai Mati" -> maritalStatusCounts["Cerai Mati"] = maritalStatusCounts.getValue("Cerai Mati") + 1
                else -> maritalStatusCounts["Lainnya"] = maritalStatusCounts.getValue("Lainnya") + 1
            }
        }
        _maritalStatusData.value = maritalStatusCounts

        // 8. Berdasarkan Agama
        val religionCounts = mutableMapOf(
            "Islam" to 0,
            "Kristen" to 0,
            "Katholik" to 0,
            "Hindu" to 0,
            "Buddha" to 0,
            "Konghucu" to 0,
            "Lainnya" to 0
        )
        for (warga in wargaList) {
            when (warga.agama) {
                "Islam" -> religionCounts["Islam"] = religionCounts.getValue("Islam") + 1
                "Kristen" -> religionCounts["Kristen"] = religionCounts.getValue("Kristen") + 1
                "Katholik" -> religionCounts["Katholik"] = religionCounts.getValue("Katholik") + 1
                "Hindu" -> religionCounts["Hindu"] = religionCounts.getValue("Hindu") + 1
                "Buddha" -> religionCounts["Buddha"] = religionCounts.getValue("Buddha") + 1
                "Konghucu" -> religionCounts["Konghucu"] = religionCounts.getValue("Konghucu") + 1
                else -> religionCounts["Lainnya"] = religionCounts.getValue("Lainnya") + 1
            }
        }
        _religionData.value = religionCounts

        // 9. Berdasarkan Kampung (jika tidak ada filter) atau RT (jika ada filter)
        val kampungRtCounts = mutableMapOf<String, Int>()
        if (currentFilter.value == null) { // Jika tidak ada filter kampung
            for (warga in wargaList) {
                warga.kampung?.let { kampung ->
                    kampungRtCounts[kampung] = kampungRtCounts.getOrDefault(kampung, 0) + 1
                }
            }
        } else { // Jika ada filter kampung, hitung berdasarkan RT
            for (warga in wargaList) {
                warga.rt?.let { rt ->
                    kampungRtCounts["RT $rt"] = kampungRtCounts.getOrDefault("RT $rt", 0) + 1
                }
            }
        }
        _kampungRtData.value = kampungRtCounts.toSortedMap()
    }
}

