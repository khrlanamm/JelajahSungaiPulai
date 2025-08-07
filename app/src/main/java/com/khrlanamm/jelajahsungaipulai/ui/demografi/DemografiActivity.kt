package com.khrlanamm.jelajahsungaipulai.ui.demografi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.khrlanamm.jelajahsungaipulai.databinding.ActivityDemografiBinding
import com.khrlanamm.jelajahsungaipulai.data.Warga // Import data class Warga

class DemografiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDemografiBinding
    private lateinit var demografiViewModel: DemografiViewModel

    // Konstanta untuk nama filter kampung
    companion object {
        const val KAMPUNG_FILTER_EXTRA = "KAMPUNG_FILTER"
        const val KAMPUNG_TANAH_NAGO = "TANAH NAGO"
        const val KAMPUNG_SILAUT_III = "SILAUT III"
        const val KAMPUNG_MEKAR_SARI = "MEKAR SARI MULYO"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityDemografiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Demografi Penduduk"

        // Inisialisasi ViewModel
        demografiViewModel = ViewModelProvider(this).get(DemografiViewModel::class.java)

        // Ambil filter mentah dari Intent
        val rawKampungFilter = intent.getStringExtra(KAMPUNG_FILTER_EXTRA)

        // Konversi filter mentah ke format yang sesuai dengan data Firestore
        val firestoreCompatibleFilter = when (rawKampungFilter) {
            "tanah_nago" -> KAMPUNG_TANAH_NAGO
            "silaut_3" -> KAMPUNG_SILAUT_III
            "mekar_sari" -> KAMPUNG_MEKAR_SARI
            else -> rawKampungFilter // Jika tidak ada dalam daftar, gunakan apa adanya (misal: null atau string lain)
        }

        // Muat data berdasarkan filter yang sudah dikonversi
        demografiViewModel.loadPendudukData(firestoreCompatibleFilter)

        // Amati LiveData untuk daftar warga
        demografiViewModel.wargaList.observe(this) { wargaList ->
            // Ketika data berubah, perbarui TextView (sementara)
            if (wargaList.isNotEmpty()) {
                binding.dataTextView.text = "Jumlah penduduk: ${wargaList.size}\n\n" +
                        "Data pertama:\n" +
                        "Nama: ${wargaList[0].nama}\n" +
                        "Kampung: ${wargaList[0].kampung}\n" +
                        "Pekerjaan: ${wargaList[0].pekerjaan}\n" +
                        "Agama: ${wargaList[0].agama}"
            } else {
                binding.dataTextView.text = "Tidak ada data penduduk ditemukan."
            }
        }

        // Amati LiveData untuk status loading
        demografiViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.dataTextView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // Amati LiveData untuk pesan error
        demografiViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Fungsi untuk navigasi ke DemografiActivity dengan filter
    // Fungsi ini tetap sama, karena ia bertanggung jawab untuk MENGIRIM filter.
    // Konversi ke format Firestore sekarang ditangani oleh DemografiActivity penerima.
    private fun navigateToDemografiActivity(filter: String? = null) {
        val intent = Intent(this, DemografiActivity::class.java).apply {
            filter?.let {
                // Mengirim filter dalam format "slug" (misal: "tanah_nago")
                // DemografiActivity akan mengonversinya ke format Firestore yang benar.
                putExtra(KAMPUNG_FILTER_EXTRA, it)
            }
        }
        startActivity(intent)
    }

    // Contoh penggunaan setupClickListeners (jika ini ada di Activity sebelumnya)
    // Anda perlu menambahkan CardView dengan ID yang sesuai di layout Activity sebelumnya
    /*
    private fun setupClickListeners() {
        // Asumsi cardTanahNago, cardSilautIii, cardMekarSari ada di layout Activity sebelumnya
        // dan binding untuk Activity tersebut sudah diatur.
        // binding.cardTanahNago.setOnClickListener {
        //     navigateToDemografiActivity("tanah_nago")
        // }
        // binding.cardSilautIii.setOnClickListener {
        //     navigateToDemografiActivity("silaut_3")
        // }
        // binding.cardMekarSari.setOnClickListener {
        //     navigateToDemografiActivity("mekar_sari")
        // }
    }
    */
}

