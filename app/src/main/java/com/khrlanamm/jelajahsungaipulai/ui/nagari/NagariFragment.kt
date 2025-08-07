package com.khrlanamm.jelajahsungaipulai.ui.nagari

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners // Import for rounded corners
import com.bumptech.glide.request.RequestOptions // Import for request options
import com.github.chrisbanes.photoview.PhotoView
import com.khrlanamm.jelajahsungaipulai.R
import com.khrlanamm.jelajahsungaipulai.databinding.FragmentNagariBinding
import com.khrlanamm.jelajahsungaipulai.ui.auth.AccountActivity
import com.khrlanamm.jelajahsungaipulai.ui.demografi.DemografiActivity

class NagariFragment : Fragment() {
    private var _binding: FragmentNagariBinding? = null
    private val binding get() = _binding!!

    private lateinit var nagariViewModel: NagariViewModel

    private var backPressedTime: Long = 0
    private var backToast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNagariBinding.inflate(inflater, container, false)
        nagariViewModel = ViewModelProvider(this).get(NagariViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityIntent = requireActivity().intent
        val userName = activityIntent.getStringExtra("USER_NAME")
        val userPhotoUrl = activityIntent.getStringExtra("USER_PHOTO_URL")

        setupWelcomeText(userName)
        setupProfilePicture(userPhotoUrl)
        setupOnBackPressed()
        setupClickListeners()
        observeViewModel()
    }

    /**
     * Mengatur pendengar klik untuk berbagai elemen UI.
     * Termasuk pendengar untuk tombol akun, nama akun, kartu profil perangkat,
     * kartu peta, kartu demografi, kartu peta UMKM, dan kartu peta bencana.
     */
    private fun setupClickListeners() {
        binding.accountButton.setOnClickListener { navigateToAccountActivity() }
        binding.accountName.setOnClickListener { navigateToAccountActivity() }
        binding.cardProfilPerangkat.setOnClickListener { showPerangkatNagariPopup() } // Click listener for Perangkat Nagari
        binding.cardMap.setOnClickListener { showMapPopup(R.drawable.peta_sungpul) }
        binding.cardDemography.setOnClickListener { navigateToDemografiActivity() }
        binding.cardPetaUmkm.setOnClickListener { showMapPopup(R.drawable.peta_umkm) }
        binding.cardPetaBencana.setOnClickListener { showMapPopup(R.drawable.peta_sungpul) }
    }

    /**
     * Mengatur teks sambutan untuk pengguna, menampilkan "Halo, [Dua Kata Pertama Nama]!".
     * @param userName Nama lengkap pengguna.
     */
    private fun setupWelcomeText(userName: String?) {
        if (userName != null) {
            val displayName = userName.split(" ").take(2).joinToString(" ")
            binding.accountName.text = "Halo, $displayName!"
        }
    }

    /**
     * Memuat dan menampilkan gambar profil pengguna menggunakan Glide, menerapkan pemotongan melingkar.
     * @param photoUrl URL gambar profil pengguna.
     */
    private fun setupProfilePicture(photoUrl: String?) {
        if (photoUrl != null) {
            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .into(binding.accountButton)
        }
    }

    /**
     * Menavigasi pengguna ke AccountActivity.
     */
    private fun navigateToAccountActivity() {
        val intent = Intent(activity, AccountActivity::class.java)
        startActivity(intent)
    }

    /**
     * Menavigasi pengguna ke DemografiActivity.
     */
    private fun navigateToDemografiActivity() {
        val intent = Intent(activity, DemografiActivity::class.java)
        startActivity(intent)
    }

    /**
     * Mengatur perilaku tombol kembali kustom untuk fragmen.
     * Pada penekanan pertama, menampilkan toast. Pada penekanan kedua dalam 2 detik, keluar dari aplikasi.
     */
    private fun setupOnBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    backToast?.cancel()
                    requireActivity().finish()
                    return
                } else {
                    backToast = Toast.makeText(
                        context,
                        "Tekan sekali lagi untuk keluar",
                        Toast.LENGTH_SHORT
                    )
                    backToast?.show()
                }
                backPressedTime = System.currentTimeMillis()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    /**
     * Mengamati LiveData isLoading dari ViewModel dan memperbarui visibilitas ProgressBar.
     */
    private fun observeViewModel() {
        nagariViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    /**
     * Menampilkan dialog pop-up yang berisi gambar peta lengkap dengan fungsionalitas zoom.
     * Dialog ini menggunakan tata letak kustom `dialog_map_image.xml` dan PhotoView.
     * @param mapResId ID sumber daya drawable dari gambar peta yang akan ditampilkan.
     */
    private fun showMapPopup(mapResId: Int) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_map_image, null)
        val mapImageView: PhotoView = dialogView.findViewById(R.id.fullScreenMapImageView)

        // Muat gambar peta ke PhotoView menggunakan Glide
        Glide.with(this)
            .load(mapResId)
            .into(mapImageView)

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Make dialog background transparent for custom shape
        dialog.show()
    }

    /**
     * Menampilkan dialog pop-up kustom yang menampilkan profil perangkat Nagari.
     * Memuat data dari string arrays dan menampilkan gambar, nama, dan jabatan.
     */
    private fun showPerangkatNagariPopup() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_perangkat_nagari, null)

        val perangkatNagariFullImageView: ImageView = dialogView.findViewById(R.id.perangkatNagariFullImageView)
        val perangkatListContainer: LinearLayout = dialogView.findViewById(R.id.perangkat_list_container)
        val kepalaKampungListContainer: LinearLayout = dialogView.findViewById(R.id.kepala_kampung_list_container)

        // Muat gambar utama perangkat Nagari dengan lebar penuh dan tinggi menyesuaikan, serta sudut melengkung
        perangkatNagariFullImageView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // Lebar penuh
            LinearLayout.LayoutParams.WRAP_CONTENT  // Tinggi menyesuaikan
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL // Pusatkan gambar
        }
        Glide.with(this)
            .load(R.drawable.perangkat_ful)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(12.dpToPx()))) // Terapkan sudut melengkung
            .into(perangkatNagariFullImageView)

        // Ambil data dari string arrays
        val perangkatImageNames = resources.getStringArray(R.array.perangkat_nagari_images)
        val perangkatJabatan = resources.getStringArray(R.array.perangkat_nagari_jabatan)
        val perangkatNama = resources.getStringArray(R.array.perangkat_nagari_nama)

        // Tambahkan item Perangkat Nagari secara dinamis
        for (i in perangkatNama.indices) {
            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8.dpToPx(), 0, 8.dpToPx()) // Tambahkan padding vertikal
            }

            val imageView = ImageView(requireContext()).apply {
                val imageWidth = 80.dpToPx() // Lebar gambar
                val imageHeight = (imageWidth * 4) / 3 // Tinggi untuk rasio 4:3
                layoutParams = LinearLayout.LayoutParams(
                    imageWidth,
                    imageHeight
                ).apply {
                    marginEnd = 16.dpToPx()
                }
                scaleType = ImageView.ScaleType.CENTER_CROP // Tetap CENTER_CROP untuk mengisi ImageView
                // Dapatkan ID sumber daya drawable dari nama string
                val imageResId = resources.getIdentifier(perangkatImageNames[i], "drawable", requireContext().packageName)
                if (imageResId != 0) {
                    Glide.with(this@NagariFragment)
                        .load(imageResId)
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(12.dpToPx()))) // Terapkan sudut melengkung
                        .into(this)
                } else {
                    // Fallback jika gambar tidak ditemukan (misalnya, gunakan ikon profil default)
                    setImageResource(R.drawable.ic_profile) // Pastikan Anda memiliki ic_profile di drawable
                }
            }

            val textLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 1f
                }
            }

            val namaTextView = TextView(requireContext()).apply {
                text = perangkatNama[i]
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(null, Typeface.BOLD)
            }

            val jabatanTextView = TextView(requireContext()).apply {
                text = perangkatJabatan[i]
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }

            textLayout.addView(namaTextView)
            textLayout.addView(jabatanTextView)
            itemLayout.addView(imageView)
            itemLayout.addView(textLayout)
            perangkatListContainer.addView(itemLayout)
        }

        // Tambahkan item Kepala Kampung secara dinamis
        val kepalaKampungData = mapOf(
            "Kepala Kampung Tanah Nago" to "Sukarsin",
            "Kepala Kampung Mekar Sari Mulyo" to "Joni Riyadi",
            "Kepala Kampung Silaut III" to "Maryono"
        )

        kepalaKampungData.forEach { (jabatan, nama) ->
            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 4.dpToPx(), 0, 4.dpToPx()) // Tambahkan padding vertikal
            }

            val jabatanTextView = TextView(requireContext()).apply {
                text = "$jabatan: $nama"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            }
            itemLayout.addView(jabatanTextView)
            kepalaKampungListContainer.addView(itemLayout)
        }

        builder.setView(dialogView)
        val dialog = builder.create()
        // Atur latar belakang dialog menjadi transparan agar sudut membulat dari dialog_background terlihat
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    /**
     * Fungsi ekstensi untuk mengkonversi nilai dp ke piksel.
     */
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density + 0.5f).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
