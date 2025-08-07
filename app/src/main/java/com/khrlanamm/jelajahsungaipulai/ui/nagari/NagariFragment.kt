package com.khrlanamm.jelajahsungaipulai.ui.nagari

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView // Keep ImageView import if still needed elsewhere, but PhotoView will be used for maps
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView // Import PhotoView for zoomable images
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
     * Sets up click listeners for various UI elements.
     * Includes listeners for the account button, account name, map cards, and demography card.
     */
    private fun setupClickListeners() {
        binding.accountButton.setOnClickListener { navigateToAccountActivity() }
        binding.accountName.setOnClickListener { navigateToAccountActivity() }
        binding.cardMap.setOnClickListener { showMapPopup(R.drawable.peta_sungpul) } // Pass drawable ID
        binding.cardDemography.setOnClickListener { navigateToDemografiActivity() }
        binding.cardPetaUmkm.setOnClickListener { showMapPopup(R.drawable.peta_umkm) }
        binding.cardPetaBencana.setOnClickListener { showMapPopup(R.drawable.peta_bencana) } // New click listener for Peta Rawan Bencana
    }

    /**
     * Sets the welcome text for the user, displaying "Halo, [First Two Words of Name]!".
     * @param userName The full name of the user.
     */
    private fun setupWelcomeText(userName: String?) {
        if (userName != null) {
            val displayName = userName.split(" ").take(2).joinToString(" ")
            binding.accountName.text = "Halo, $displayName!"
        }
    }

    /**
     * Loads and displays the user's profile picture using Glide, applying a circular crop.
     * @param photoUrl The URL of the user's profile picture.
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
     * Navigates the user to the AccountActivity.
     */
    private fun navigateToAccountActivity() {
        val intent = Intent(activity, AccountActivity::class.java)
        startActivity(intent)
    }

    /**
     * Navigates the user to DemografiActivity.
     */
    private fun navigateToDemografiActivity() {
        val intent = Intent(activity, DemografiActivity::class.java)
        startActivity(intent)
    }

    /**
     * Sets up the custom back press behavior for the fragment.
     * On first press, shows a toast. On second press within 2 seconds, exits the app.
     */
    private fun setupOnBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    backToast?.cancel()
                    requireActivity().finish()
                    return
                } else {
                    backToast = Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT)
                    backToast?.show()
                }
                backPressedTime = System.currentTimeMillis()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    /**
     * Observes the isLoading LiveData from the ViewModel and updates the ProgressBar visibility.
     */
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

        // Load the map image into the PhotoView using Glide
        Glide.with(this)
            .load(mapResId)
            .into(mapImageView)

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
