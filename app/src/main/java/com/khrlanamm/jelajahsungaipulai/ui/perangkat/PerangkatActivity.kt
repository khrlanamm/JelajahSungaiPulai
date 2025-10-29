package com.khrlanamm.jelajahsungaipulai.ui.perangkat

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.khrlanamm.jelajahsungaipulai.R
import com.khrlanamm.jelajahsungaipulai.data.Perangkat
import com.khrlanamm.jelajahsungaipulai.databinding.ActivityPerangkatBinding

class PerangkatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerangkatBinding
    private lateinit var perangkatAdapter: PerangkatAdapter
    private val listPerangkat = ArrayList<Perangkat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityPerangkatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(insets.left, 0, insets.right, 0)
            binding.appbar.setPadding(0, insets.top, 0, 0)
            binding.scrollView.setPadding(0, 0, 0, insets.bottom)

            WindowInsetsCompat.CONSUMED
        }

        setupToolbar()
        setupPerangkatList()
        setupRecyclerView()
        setupKepalaKampung()
        loadGroupPhoto()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.title_activity_perangkat)
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadGroupPhoto() {
        Glide.with(this)
            .load(R.drawable.perangkat_ful)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(16.dpToPx())))
            .into(binding.ivGroupPhoto)
    }

    private fun setupPerangkatList() {
        val perangkatImagesArray = resources.obtainTypedArray(R.array.perangkat_nagari_images_array)
        val perangkatJabatan = resources.getStringArray(R.array.perangkat_nagari_jabatan)
        val perangkatNama = resources.getStringArray(R.array.perangkat_nagari_nama)

        for (i in perangkatNama.indices) {
            val imageResId = perangkatImagesArray.getResourceId(i, R.drawable.ic_profile)
            val perangkat = Perangkat(
                perangkatNama[i],
                perangkatJabatan[i],
                imageResId
            )
            listPerangkat.add(perangkat)
        }
        perangkatImagesArray.recycle()
    }

    private fun setupRecyclerView() {
        perangkatAdapter = PerangkatAdapter(listPerangkat)

        val layoutManager = GridLayoutManager(this@PerangkatActivity, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (perangkatAdapter.getItemViewType(position)) {
                    PerangkatAdapter.VIEW_TYPE_FEATURED -> 2
                    PerangkatAdapter.VIEW_TYPE_GRID -> 1
                    else -> 1
                }
            }
        }

        binding.rvPerangkat.apply {
            this.layoutManager = layoutManager
            adapter = perangkatAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupKepalaKampung() {
        val kepalaKampungData = mapOf(
            "Kepala Kampung Tanah Nago" to "Sukarsin",
            "Kepala Kampung Mekar Sari Mulyo" to "Joni Riyadi",
            "Kepala Kampung Silaut III" to "Maryono"
        )

        kepalaKampungData.forEach { (jabatan, nama) ->
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 4.dpToPx(), 0, 4.dpToPx())
            }

            val namaTextView = TextView(this).apply {
                text = nama
                setTextColor(ContextCompat.getColor(this@PerangkatActivity, R.color.text_primary))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(null, Typeface.BOLD)
            }

            val jabatanTextView = TextView(this).apply {
                text = jabatan
                setTextColor(ContextCompat.getColor(this@PerangkatActivity, R.color.text_secondary))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }

            itemLayout.addView(namaTextView)
            itemLayout.addView(jabatanTextView)
            binding.kepalaKampungListContainer.addView(itemLayout)
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density + 0.5f).toInt()
    }
}