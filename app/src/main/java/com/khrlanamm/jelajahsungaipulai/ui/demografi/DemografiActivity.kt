package com.khrlanamm.jelajahsungaipulai.ui.demografi

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.khrlanamm.jelajahsungaipulai.R
import com.khrlanamm.jelajahsungaipulai.databinding.ActivityDemografiBinding

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
        supportActionBar?.title = "Demografi Penduduk" // Judul default

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

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

        // Amati LiveData untuk status loading
        demografiViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.scrollView.visibility =
                if (isLoading) View.GONE else View.VISIBLE // Tampilkan ScrollView saat tidak loading
        }

        // Amati LiveData untuk pesan error
        demografiViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        // Amati LiveData untuk filter saat ini dan perbarui judul
        demografiViewModel.currentFilter.observe(this) { filter ->
            val titleText = if (filter.isNullOrEmpty()) {
                "Data Demografi Penduduk\nNagari Sungai Pulai"
            } else {
                "Data Demografi Penduduk\nKampung $filter"
            }
            binding.demografiTitle.text = titleText
        }

        // Amati LiveData untuk statistik demografi dan perbarui UI
        demografiViewModel.totalPenduduk.observe(this) { count ->
            binding.tvTotalPenduduk.text = count.toString()
        }
        demografiViewModel.totalKepalaKeluarga.observe(this) { count ->
            binding.tvTotalKepalaKeluarga.text = count.toString()
        }
        demografiViewModel.totalLakiLaki.observe(this) { count ->
            binding.tvTotalLakiLaki.text = count.toString()
        }
        demografiViewModel.totalPerempuan.observe(this) { count ->
            binding.tvTotalPerempuan.text = count.toString()
        }

        demografiViewModel.ageGroupData.observe(this) { data ->
            updateDynamicSection(binding.llAgeGroupsContainer, data)
        }
        demografiViewModel.educationData.observe(this) { data ->
            updateDynamicSection(binding.llEducationContainer, data)
        }
        demografiViewModel.occupationData.observe(this) { data ->
            updateDynamicSection(binding.llOccupationContainer, data)
        }

        demografiViewModel.maritalStatusData.observe(this) { data ->
            binding.tvStatusBelumKawin.text = data["Belum Kawin"].toString()
            binding.tvStatusKawin.text = data["Kawin"].toString()
            binding.tvStatusCeraiHidup.text = data["Cerai Hidup"].toString()
            binding.tvStatusCeraiMati.text = data["Cerai Mati"].toString()
        }

        demografiViewModel.religionData.observe(this) { data ->
            updateDynamicSection(binding.llReligionContainer, data)
        }

        demografiViewModel.kampungRtData.observe(this) { data ->
            // Perbarui judul bagian Kampung/RT
            val filter = demografiViewModel.currentFilter.value
            binding.tvKampungRtTitle.text = if (filter.isNullOrEmpty()) {
                "Berdasarkan Kampung"
            } else {
                "Berdasarkan RT"
            }
            updateDynamicSection(binding.llKampungRtContainer, data)
        }
    }

    // Fungsi untuk memperbarui bagian dinamis (kelompok umur, pendidikan, pekerjaan, agama, kampung/RT)
    private fun updateDynamicSection(container: LinearLayout, data: Map<String, Int>) {
        container.removeAllViews() // Hapus tampilan sebelumnya

        if (data.isEmpty()) {
            val tv = TextView(this)
            tv.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            tv.text = "Tidak ada data tersedia."
            tv.setTextColor(ContextCompat.getColor(this, R.color.demografi_text_medium))
            tv.gravity = Gravity.CENTER
            tv.setPadding(0, 16, 0, 0)
            container.addView(tv)
            return
        }

        data.forEach { (label, count) ->
            val linearLayout = LinearLayout(this)
            linearLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.setPadding(0, 8, 0, 8)
            linearLayout.gravity = Gravity.CENTER_VERTICAL

            // Label (e.g., "0-5 Tahun")
            val tvLabel = TextView(this)
            tvLabel.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f // Ambil sebagian besar lebar
            )
            tvLabel.text = label
            tvLabel.setTextColor(ContextCompat.getColor(this, R.color.demografi_text_dark))
            // Menggunakan setTextSize dengan unit SP
            tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            tvLabel.setTypeface(null, Typeface.BOLD)
            linearLayout.addView(tvLabel)

            // Count (e.g., "123")
            val tvCount = TextView(this)
            tvCount.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            tvCount.text = count.toString()
            tvCount.setTextColor(ContextCompat.getColor(this, R.color.demografi_text_dark))
            // Menggunakan setTextSize dengan unit SP
            tvCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            tvCount.setTypeface(null, Typeface.BOLD)
            linearLayout.addView(tvCount)

            container.addView(linearLayout)

            // Tambahkan pemisah (opsional, untuk estetika)
            if (container.indexOfChild(linearLayout) < data.size - 1) {
                val divider = View(this)
                divider.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1 // Tinggi 1dp
                )
                divider.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.demografi_text_light
                    )
                )
                container.addView(divider)
            }
        }
    }

    // Fungsi untuk navigasi ke DemografiActivity dengan filter
    private fun navigateToDemografiActivity(filter: String? = null) {
        val intent = Intent(this, DemografiActivity::class.java).apply {
            filter?.let {
                putExtra(KAMPUNG_FILTER_EXTRA, it)
            }
        }
        startActivity(intent)
    }

    // Contoh penggunaan setupClickListeners (jika ini ada di Activity sebelumnya)
    /*
    private fun setupClickListeners() {
        // Asumsi cardTanahNago, cardSilautIii, cardMekarSari ada di layout Activity sebelumnya
        // dan binding untuk Activity tersebut sudah diatur.
        // binding.cardTanahNencara("tanah_nago")
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

