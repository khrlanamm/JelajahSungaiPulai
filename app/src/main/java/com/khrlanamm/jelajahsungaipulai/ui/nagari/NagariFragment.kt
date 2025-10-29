package com.khrlanamm.jelajahsungaipulai.ui.nagari

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.khrlanamm.jelajahsungaipulai.R
import com.khrlanamm.jelajahsungaipulai.databinding.FragmentNagariBinding
import com.khrlanamm.jelajahsungaipulai.ui.auth.AccountActivity
import com.khrlanamm.jelajahsungaipulai.ui.demografi.DemografiActivity
// --- PERUBAHAN --- Import PerangkatActivity
import com.khrlanamm.jelajahsungaipulai.ui.perangkat.PerangkatActivity

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
        // --- PERUBAHAN --- Mengarahkan ke PerangkatActivity, bukan popup
        binding.cardProfilPerangkat.setOnClickListener { navigateToPerangkatActivity() }
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

    // --- FUNGSI BARU ---
    private fun navigateToPerangkatActivity() {
        val intent = Intent(activity, PerangkatActivity::class.java)
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
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}