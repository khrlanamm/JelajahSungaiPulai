package com.khrlanamm.jelajahsungaipulai.ui.nagari

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
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

    private fun setupClickListeners() {
        binding.accountButton.setOnClickListener { navigateToAccountActivity() }
        binding.accountName.setOnClickListener { navigateToAccountActivity() }
        binding.cardMap.setOnClickListener { showMapPopup() }
        binding.cardDemography.setOnClickListener { navigateToDemografiActivity() } // New click listener for demography card
    }

    private fun setupWelcomeText(userName: String?) {
        if (userName != null) {
            val displayName = userName.split(" ").take(2).joinToString(" ")
            binding.accountName.text = "Halo, $displayName!"
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
                    backToast = Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT)
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

    private fun showMapPopup() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_map_image, null)
        val mapImageView: ImageView = dialogView.findViewById(R.id.fullScreenMapImageView)

        Glide.with(this)
            .load(R.drawable.peta_sungpul)
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
