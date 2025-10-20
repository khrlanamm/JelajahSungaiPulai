package com.khrlanamm.jelajahsungaipulai.ui.nagari

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.github.chrisbanes.photoview.PhotoView
import com.khrlanamm.jelajahsungaipulai.R
import com.khrlanamm.jelajahsungaipulai.databinding.FragmentNagariBinding
import com.khrlanamm.jelajahsungaipulai.ui.auth.AccountActivity
import com.khrlanamm.jelajahsungaipulai.ui.demografi.DemografiActivity

class NagariFragment : Fragment() {
    private var _binding: FragmentNagariBinding? = null
    private val binding get() = _binding!!

    private val nagariViewModel: NagariViewModel by viewModels()

    private var backPressedTime: Long = 0
    private var backToast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNagariBinding.inflate(inflater, container, false)
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

    private fun setupClickListeners() {
        binding.accountButton.setOnClickListener { navigateToAccountActivity() }
        binding.accountName.setOnClickListener { navigateToAccountActivity() }
        binding.cardProfilPerangkat.setOnClickListener { showPerangkatNagariPopup() }
        binding.cardMap.setOnClickListener { showMapPopup(R.drawable.peta_sungpul) }
        binding.cardDemography.setOnClickListener { navigateToDemografiActivity() }
        binding.cardPetaUmkm.setOnClickListener { showMapPopup(R.drawable.peta_umkm) }
        binding.cardPetaBencana.setOnClickListener { showMapPopup(R.drawable.peta_bencana) }
    }

    private fun setupWelcomeText(userName: String?) {
        if (userName != null) {
            val displayName = userName.split(" ").take(2).joinToString(" ")
            binding.accountName.text = getString(R.string.welcome_message, displayName)
        }
    }

    private fun setupProfilePicture(photoUrl: String?) {
        if (photoUrl != null) {
            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .into(binding.accountButton)
        }
    }

    private fun navigateToAccountActivity() {
        val intent = Intent(activity, AccountActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToDemografiActivity() {
        val intent = Intent(activity, DemografiActivity::class.java)
        startActivity(intent)
    }

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

    private fun observeViewModel() {
        nagariViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun showMapPopup(mapResId: Int) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_map_image, null)
        val mapImageView: PhotoView = dialogView.findViewById(R.id.fullScreenMapImageView)

        Glide.with(this)
            .load(mapResId)
            .into(mapImageView)

        builder.setView(dialogView)
        val dialog = builder.create()
        // --- PERBAIKAN --- Menggunakan KTX extension function 'toDrawable()'
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.show()
    }

    private fun showPerangkatNagariPopup() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_perangkat_nagari, null)

        val perangkatNagariFullImageView: ImageView =
            dialogView.findViewById(R.id.perangkatNagariFullImageView)
        val perangkatListContainer: LinearLayout =
            dialogView.findViewById(R.id.perangkat_list_container)
        val kepalaKampungListContainer: LinearLayout =
            dialogView.findViewById(R.id.kepala_kampung_list_container)

        perangkatNagariFullImageView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }
        Glide.with(this)
            .load(R.drawable.perangkat_ful)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(12.dpToPx())))
            .into(perangkatNagariFullImageView)

        val perangkatImagesArray = resources.obtainTypedArray(R.array.perangkat_nagari_images_array)
        val perangkatJabatan = resources.getStringArray(R.array.perangkat_nagari_jabatan)
        val perangkatNama = resources.getStringArray(R.array.perangkat_nagari_nama)

        for (i in perangkatNama.indices) {
            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8.dpToPx(), 0, 8.dpToPx())
            }

            val imageView = ImageView(requireContext()).apply {
                val imageWidth = 80.dpToPx()
                val imageHeight = (imageWidth * 4) / 3
                layoutParams = LinearLayout.LayoutParams(
                    imageWidth,
                    imageHeight
                ).apply {
                    marginEnd = 16.dpToPx()
                }
                scaleType = ImageView.ScaleType.CENTER_CROP

                val imageResId = perangkatImagesArray.getResourceId(i, 0)

                if (imageResId != 0) {
                    Glide.with(this@NagariFragment)
                        .load(imageResId)
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(12.dpToPx())))
                        .into(this)
                } else {
                    setImageResource(R.drawable.ic_profile)
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

        perangkatImagesArray.recycle()

        val kepalaKampungData = mapOf(
            "Kepala Kampung Tanah Nago" to "Sukarsin",
            "Kepala Kampung Mekar Sari Mulyo" to "Joni Riyadi",
            "Kepala Kampung Silaut III" to "Maryono"
        )

        kepalaKampungData.forEach { (jabatan, nama) ->
            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 4.dpToPx(), 0, 4.dpToPx())
            }

            val jabatanTextView = TextView(requireContext()).apply {
                text = getString(R.string.kepala_kampung_format, jabatan, nama)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            }
            itemLayout.addView(jabatanTextView)
            kepalaKampungListContainer.addView(itemLayout)
        }

        builder.setView(dialogView)
        val dialog = builder.create()
        // --- PERBAIKAN --- Menggunakan KTX extension function 'toDrawable()'
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.show()
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density + 0.5f).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
