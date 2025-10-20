package com.khrlanamm.jelajahsungaipulai.ui.demografi

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

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // UBAH: Inisialisasi ViewModel lebih ringkas dengan operator []
        demografiViewModel = ViewModelProvider(this)[DemografiViewModel::class.java]

        val rawKampungFilter = intent.getStringExtra(KAMPUNG_FILTER_EXTRA)
        val firestoreCompatibleFilter = when (rawKampungFilter) {
            "tanah_nago" -> KAMPUNG_TANAH_NAGO
            "silaut_3" -> KAMPUNG_SILAUT_III
            "mekar_sari" -> KAMPUNG_MEKAR_SARI
            else -> rawKampungFilter
        }
        demografiViewModel.loadPendudukData(firestoreCompatibleFilter)

        observeViewModel()
    }

    private fun observeViewModel() {
        demografiViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.scrollView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        demografiViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.takeIf { it.isNotBlank() }?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        demografiViewModel.currentFilter.observe(this) { filter ->
            binding.demografiTitle.text = if (filter.isNullOrEmpty()) {
                "Data Demografi Penduduk\nNagari Sungai Pulai"
            } else {
                "Data Demografi Penduduk\nKampung $filter"
            }
        }

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
            binding.tvStatusBelumKawin.text = (data["Belum Kawin"] ?: 0).toString()
            binding.tvStatusKawin.text = (data["Kawin"] ?: 0).toString()
            binding.tvStatusCeraiHidup.text = (data["Cerai Hidup"] ?: 0).toString()
            binding.tvStatusCeraiMati.text = (data["Cerai Mati"] ?: 0).toString()
        }

        demografiViewModel.religionData.observe(this) { data ->
            updateDynamicSection(binding.llReligionContainer, data)
        }

        demografiViewModel.kampungRtData.observe(this) { data ->
            val filter = demografiViewModel.currentFilter.value
            binding.tvKampungRtTitle.text = if (filter.isNullOrEmpty()) {
                "Berdasarkan Kampung"
            } else {
                "Berdasarkan RT"
            }
            updateDynamicSection(binding.llKampungRtContainer, data)
        }
    }

    private fun updateDynamicSection(container: LinearLayout, data: Map<String, Int>) {
        container.removeAllViews()

        if (data.isEmpty()) {
            val tv = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = getString(R.string.no_data_available)
                setTextColor(ContextCompat.getColor(this@DemografiActivity, R.color.demografi_text_medium))
                gravity = Gravity.CENTER
                setPadding(0, 16, 0, 0)
            }
            container.addView(tv)
            return
        }

        data.forEach { (label, count) ->
            if (label.isEmpty()) return@forEach // Skip jika label kosong

            val rowLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 8)
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvLabel = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
                text = label
                setTextColor(ContextCompat.getColor(this@DemografiActivity, R.color.demografi_text_dark))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(null, Typeface.BOLD)
            }
            rowLayout.addView(tvLabel)

            val tvCount = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = count.toString()
                setTextColor(ContextCompat.getColor(this@DemografiActivity, R.color.demografi_text_dark))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(null, Typeface.BOLD)
            }
            rowLayout.addView(tvCount)
            container.addView(rowLayout)

            if (container.indexOfChild(rowLayout) < (data.size * 2) - 2) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                    setBackgroundColor(ContextCompat.getColor(this@DemografiActivity, R.color.demografi_text_light))
                }
                container.addView(divider)
            }
        }
    }
}
