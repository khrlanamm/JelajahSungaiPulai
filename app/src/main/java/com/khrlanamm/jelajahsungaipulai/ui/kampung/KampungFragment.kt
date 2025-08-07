package com.khrlanamm.jelajahsungaipulai.ui.kampung

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.khrlanamm.jelajahsungaipulai.databinding.FragmentKampungBinding

class KampungFragment : Fragment() {

    private var _binding: FragmentKampungBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val kampungViewModel =
            ViewModelProvider(this).get(KampungViewModel::class.java)

        _binding = FragmentKampungBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textKampung
        kampungViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}