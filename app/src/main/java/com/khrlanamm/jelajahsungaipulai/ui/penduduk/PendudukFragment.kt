package com.khrlanamm.jelajahsungaipulai.ui.penduduk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.khrlanamm.jelajahsungaipulai.R
import com.khrlanamm.jelajahsungaipulai.databinding.FragmentPendudukBinding

class PendudukFragment : Fragment() {
    private var _binding: FragmentPendudukBinding? = null
    private val binding get() = _binding!!

    private lateinit var pendudukViewModel: PendudukViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inisialisasi ViewModel
        pendudukViewModel = ViewModelProvider(this)[PendudukViewModel::class.java]
        _binding = FragmentPendudukBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        observeAdminStatus()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Profil Penduduk"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun observeAdminStatus() {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentLayout.visibility = View.GONE

        pendudukViewModel.isCheckComplete.observe(viewLifecycleOwner) { isComplete ->
            if (isComplete) {
                binding.progressBar.visibility = View.GONE

                val isAdmin = pendudukViewModel.isAdmin.value ?: false
                if (isAdmin) {
                    binding.contentLayout.visibility = View.VISIBLE
                } else {
                    showAccessDeniedDialog()
                }
            }
        }
    }

    private fun showAccessDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Akses Ditolak")
            .setMessage("Halaman data penduduk hanya bisa dilihat dan dimodifikasi oleh Admin.")
            .setPositiveButton("OK") { _, _ ->
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_lock)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
