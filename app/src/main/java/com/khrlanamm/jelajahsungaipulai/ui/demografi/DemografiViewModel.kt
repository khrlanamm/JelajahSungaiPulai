package com.khrlanamm.jelajahsungaipulai.ui.demografi

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.khrlanamm.jelajahsungaipulai.data.Warga
import java.util.Calendar
import java.util.Date

class DemografiViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

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

    fun loadPendudukData(kampungFilter: String?) {
        _isLoading.value = true
        _errorMessage.value = ""
        _currentFilter.value = kampungFilter

        Log.d("DemografiViewModel", "Memuat data dengan filter kampung: '$kampungFilter'")

        val query: Query = if (kampungFilter != null) {
            db.collection("penduduk").whereEqualTo("kampung", kampungFilter)
        } else {
            db.collection("penduduk")
        }

        query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("DemografiViewModel", "Gagal mendengarkan data.", e)
                _errorMessage.value = "Gagal memuat data: ${e.message}"
                _isLoading.value = false
                return@addSnapshotListener
            }

            val list = snapshots?.toObjects(Warga::class.java) ?: emptyList()
            // _wargaList.value = list // Tidak perlu lagi
            _isLoading.value = false
            Log.d("DemografiViewModel", "Data berhasil dimuat: ${list.size} item.")

            calculateDemographics(list)
        }
    }

    private fun getAge(birthDate: Date): Int {
        val today = Calendar.getInstance()
        val birth = Calendar.getInstance()
        birth.time = birthDate

        var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    private fun calculateDemographics(wargaList: List<Warga>) {
        _totalPenduduk.value = wargaList.size

        _totalKepalaKeluarga.value = wargaList.count { w ->
            w.hubKeluarga?.trim()?.equals("Kepala Keluarga", ignoreCase = true) == true
        }

        _totalLakiLaki.value = wargaList.count { w ->
            w.jenisKel?.trim()?.uppercase() == "L"
        }

        _totalPerempuan.value = wargaList.count { w ->
            w.jenisKel?.trim()?.uppercase() == "P"
        }

        val ageGroups = mutableMapOf(
            "0-5 Tahun" to 0, "6-12 Tahun" to 0, "13-18 Tahun" to 0,
            "19-30 Tahun" to 0, "31-50 Tahun" to 0, "51+ Tahun" to 0
        )

        for (w in wargaList) {
            val ts = w.tglLahir
            val birthDate = ts?.toDate() ?: continue
            val age = getAge(birthDate)

            when {
                age in 0..5   -> ageGroups["0-5 Tahun"] = ageGroups.getValue("0-5 Tahun") + 1
                age in 6..12  -> ageGroups["6-12 Tahun"] = ageGroups.getValue("6-12 Tahun") + 1
                age in 13..18 -> ageGroups["13-18 Tahun"] = ageGroups.getValue("13-18 Tahun") + 1
                age in 19..30 -> ageGroups["19-30 Tahun"] = ageGroups.getValue("19-30 Tahun") + 1
                age in 31..50 -> ageGroups["31-50 Tahun"] = ageGroups.getValue("31-50 Tahun") + 1
                age > 50      -> ageGroups["51+ Tahun"] = ageGroups.getValue("51+ Tahun") + 1
            }
        }
        _ageGroupData.value = ageGroups

        _educationData.value = wargaList.groupingBy { it.pendidikan.orEmpty().trim() }.eachCount().toSortedMap()
        _occupationData.value = wargaList.groupingBy { it.pekerjaan.orEmpty().trim() }.eachCount().toSortedMap()
        _maritalStatusData.value = wargaList.groupingBy { it.status.orEmpty().trim() }.eachCount()
        _religionData.value = wargaList.groupingBy { it.agama.orEmpty().trim() }.eachCount()

        val kampungRtCounts = mutableMapOf<String, Int>()
        if (currentFilter.value == null) {
            for (w in wargaList) {
                w.kampung?.trim()?.takeIf { it.isNotEmpty() }?.let { kampung ->
                    kampungRtCounts[kampung] = kampungRtCounts.getOrDefault(kampung, 0) + 1
                }
            }
        } else {
            for (w in wargaList) {
                w.rt?.let { rt ->
                    val rtLabel = "RT $rt"
                    kampungRtCounts[rtLabel] = kampungRtCounts.getOrDefault(rtLabel, 0) + 1
                }
            }
        }
        _kampungRtData.value = kampungRtCounts.toSortedMap()
    }

}
