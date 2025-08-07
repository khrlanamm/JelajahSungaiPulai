package com.khrlanamm.jelajahsungaipulai.ui.penduduk

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.khrlanamm.jelajahsungaipulai.data.Penduduk // Import kelas Penduduk dari folder data
import java.text.SimpleDateFormat
import java.util.Locale

class PendudukViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // LiveData untuk menyimpan status admin
    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> get() = _isAdmin

    // LiveData untuk menandai ketika pengecekan selesai
    private val _isCheckComplete = MutableLiveData<Boolean>()
    val isCheckComplete: LiveData<Boolean> get() = _isCheckComplete

    // LiveData untuk menyimpan daftar penduduk asli (tanpa filter)
    private val _pendudukList = MutableLiveData<List<Penduduk>>()

    // LiveData untuk menyimpan daftar penduduk yang difilter
    private val _filteredPendudukList = MutableLiveData<List<Penduduk>>()
    val filteredPendudukList: LiveData<List<Penduduk>> get() = _filteredPendudukList

    // LiveData untuk pesan status (berhasil/gagal) - sekarang bisa null
    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> get() = _statusMessage

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
                _statusMessage.postValue("Gagal memeriksa peran admin: ${it.message}")
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
                _filteredPendudukList.postValue(list) // Awalnya, daftar yang difilter sama dengan daftar asli
            }
            .addOnFailureListener { exception ->
                // Tangani kesalahan (misalnya, log, tampilkan pesan toast)
                // Untuk saat ini, post daftar kosong jika gagal
                _pendudukList.postValue(emptyList())
                _filteredPendudukList.postValue(emptyList())
                _statusMessage.postValue("Gagal mengambil data penduduk: ${exception.message}")
            }
    }

    // Fungsi untuk menghapus data penduduk dari Firestore
    fun deletePenduduk(documentId: String) {
        db.collection("penduduk").document(documentId)
            .delete()
            .addOnSuccessListener {
                _statusMessage.postValue("Data penduduk berhasil dihapus.")
                fetchPendudukData() // Refresh data setelah penghapusan
            }
            .addOnFailureListener { e ->
                _statusMessage.postValue("Gagal menghapus data penduduk: ${e.message}")
            }
    }

    // Fungsi untuk mengupdate data penduduk di Firestore
    fun updatePenduduk(penduduk: Penduduk) {
        penduduk.documentId?.let { docId ->
            val dataToUpdate = hashMapOf<String, Any?>(
                "agama" to penduduk.agama,
                "hub_keluarga" to penduduk.hub_keluarga,
                "jenis_kel" to penduduk.jenis_kel,
                "kampung" to penduduk.kampung,
                "kep_kel" to penduduk.kep_kel,
                "nama" to penduduk.nama,
                "nik" to penduduk.nik,
                "no_kk" to penduduk.no_kk,
                "pekerjaan" to penduduk.pekerjaan,
                "pendidikan" to penduduk.pendidikan,
                "rt" to penduduk.rt,
                "status" to penduduk.status,
                "tempat_lahir" to penduduk.tempat_lahir
            )

            // Konversi tgl_lahir dari String ke Timestamp jika ada
            penduduk.tgl_lahir?.let {
                try {
                    val date = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).parse(it)
                    dataToUpdate["tgl_lahir"] = Timestamp(date!!) // Gunakan Timestamp yang diimpor
                } catch (e: Exception) {
                    _statusMessage.postValue("Format tanggal lahir tidak valid. Gunakan DD MMMM YYYY.")
                    return // Hentikan proses update jika format tanggal salah
                }
            } ?: run {
                dataToUpdate["tgl_lahir"] = null // Set null jika tanggal lahir kosong
            }


            db.collection("penduduk").document(docId)
                .update(dataToUpdate as Map<String, Any>) // Cast to Map<String, Any>
                .addOnSuccessListener {
                    _statusMessage.postValue("Data penduduk berhasil diperbarui.")
                    fetchPendudukData() // Refresh data setelah pembaruan
                }
                .addOnFailureListener { e ->
                    _statusMessage.postValue("Gagal memperbarui data penduduk: ${e.message}")
                }
        } ?: run {
            _statusMessage.postValue("Gagal memperbarui: ID dokumen tidak ditemukan.")
        }
    }

    // Fungsi untuk menambahkan data penduduk baru ke Firestore
    fun addPenduduk(penduduk: Penduduk) {
        val dataToAdd = hashMapOf<String, Any?>(
            "agama" to penduduk.agama,
            "hub_keluarga" to penduduk.hub_keluarga,
            "jenis_kel" to penduduk.jenis_kel,
            "kampung" to penduduk.kampung,
            "kep_kel" to penduduk.kep_kel,
            "nama" to penduduk.nama,
            "nik" to penduduk.nik,
            "no_kk" to penduduk.no_kk,
            "pekerjaan" to penduduk.pekerjaan,
            "pendidikan" to penduduk.pendidikan,
            "rt" to penduduk.rt,
            "status" to penduduk.status,
            "tempat_lahir" to penduduk.tempat_lahir
        )

        // Konversi tgl_lahir dari String ke Timestamp jika ada
        penduduk.tgl_lahir?.let {
            try {
                val date = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).parse(it)
                dataToAdd["tgl_lahir"] = Timestamp(date!!) // Gunakan Timestamp yang diimpor
            } catch (e: Exception) {
                _statusMessage.postValue("Format tanggal lahir tidak valid. Gunakan DD MMMM YYYY.")
                return // Hentikan proses add jika format tanggal salah
            }
        } ?: run {
            dataToAdd["tgl_lahir"] = null // Set null jika tanggal lahir kosong
        }

        db.collection("penduduk")
            .add(dataToAdd as Map<String, Any>) // Cast to Map<String, Any>
            .addOnSuccessListener {
                _statusMessage.postValue("Data penduduk berhasil ditambahkan.")
                fetchPendudukData() // Refresh data setelah penambahan
            }
            .addOnFailureListener { e ->
                _statusMessage.postValue("Gagal menambahkan data penduduk: ${e.message}")
            }
    }

    // Fungsi untuk memfilter daftar penduduk berdasarkan nama
    fun filterPenduduk(query: String) {
        val currentList = _pendudukList.value.orEmpty()
        if (query.isBlank()) {
            _filteredPendudukList.postValue(currentList) // Jika query kosong, tampilkan semua data
        } else {
            val filteredList = currentList.filter {
                it.nama?.contains(query, ignoreCase = true) == true
            }
            _filteredPendudukList.postValue(filteredList)
        }
    }

    // Fungsi untuk membersihkan pesan status setelah ditampilkan
    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
