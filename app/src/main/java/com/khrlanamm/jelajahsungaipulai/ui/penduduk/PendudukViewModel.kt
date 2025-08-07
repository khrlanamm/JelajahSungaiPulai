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

    // LiveData untuk pesan status (berhasil/gagal)
    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> get() = _statusMessage

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
            }
            .addOnFailureListener { exception ->
                // Tangani kesalahan (misalnya, log, tampilkan pesan toast)
                // Untuk saat ini, post daftar kosong jika gagal
                _pendudukList.postValue(emptyList())
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
            val dataToUpdate = hashMapOf(
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
                "tempat_lahir" to penduduk.tempat_lahir,
                // Perlu penanganan khusus untuk tgl_lahir jika ingin diupdate sebagai Timestamp
                // Untuk saat ini, asumsikan tgl_lahir tetap string atau diabaikan jika tidak ada perubahan format
                "tgl_lahir" to penduduk.tgl_lahir // Jika Anda ingin mengupdate ini sebagai Timestamp, Anda perlu mengkonversi string kembali ke Timestamp
            )

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
}
