package com.khrlanamm.jelajahsungaipulai.ui.kampung

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.khrlanamm.jelajahsungaipulai.databinding.FragmentKampungBinding
import com.khrlanamm.jelajahsungaipulai.ui.demografi.DemografiActivity // Import DemografiActivity

class KampungFragment : Fragment() {
    private var _binding: FragmentKampungBinding? = null
    private val binding get() = _binding!!

    private lateinit var kampungViewModel: KampungViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        kampungViewModel = ViewModelProvider(this)[KampungViewModel::class.java]
        _binding = FragmentKampungBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupClickListeners() // Call the new click listener setup
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Profil Kampung" // Set the toolbar title
            setDisplayHomeAsUpEnabled(true) // Enable the back button
            setDisplayShowHomeEnabled(true)
        }
        // Set the navigation icon click listener to handle back press
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    /**
     * Sets up click listeners for the kampung cards.
     * Each card navigates to DemografiActivity with a specific filter.
     */
    private fun setupClickListeners() {
        binding.cardTanahNago.setOnClickListener {
            navigateToDemografiActivity("tanah_nago")
        }
        binding.cardSilautIii.setOnClickListener {
            navigateToDemografiActivity("silaut_3")
        }
        binding.cardMekarSari.setOnClickListener {
            navigateToDemografiActivity("mekar_sari")
        }
    }

    private fun navigateToDemografiActivity(filter: String? = null) {
        val intent = Intent(activity, DemografiActivity::class.java).apply {
            filter?.let { putExtra("KAMPUNG_FILTER", it) } // Pass the filter if it's not null
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding reference to prevent memory leaks
    }
}
