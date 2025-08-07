package com.khrlanamm.jelajahsungaipulai.ui.penduduk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.khrlanamm.jelajahsungaipulai.databinding.FragmentPendudukBinding

class PendudukFragment : Fragment() {

    private var _binding: FragmentPendudukBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val penduduklViewModel =
            ViewModelProvider(this).get(PenduduklViewModel::class.java)

        _binding = FragmentPendudukBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textPenduduk
        penduduklViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}