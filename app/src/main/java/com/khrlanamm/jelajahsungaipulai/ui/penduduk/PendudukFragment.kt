package com.khrlanamm.jelajahsungaipulai.ui.penduduk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.khrlanamm.jelajahsungaipulai.R
import com.khrlanamm.jelajahsungaipulai.data.Penduduk // Import kelas Penduduk dari folder data
import com.khrlanamm.jelajahsungaipulai.databinding.FragmentPendudukBinding

class PendudukFragment : Fragment() {
    private var _binding: FragmentPendudukBinding? = null
    private val binding get() = _binding!!

    private lateinit var pendudukViewModel: PendudukViewModel
    private lateinit var pendudukAdapter: PendudukAdapter

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
        setupRecyclerView()
        observeAdminStatus()
        observePendudukData()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Data Penduduk" // Mengubah judul untuk mencerminkan konten
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        // Inisialisasi adapter dengan pendengar klik
        pendudukAdapter = PendudukAdapter { penduduk ->
            showPendudukDetailsDialog(penduduk)
        }
        // Atur RecyclerView dengan LinearLayoutManager dan adapter
        binding.rvPenduduk.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pendudukAdapter
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
                    // Pengambilan data sekarang dipicu di dalam ViewModel jika isAdmin adalah true
                } else {
                    showAccessDeniedDialog()
                }
            }
        }
    }

    private fun observePendudukData() {
        // Amati daftar penduduk dari ViewModel dan perbarui adapter
        pendudukViewModel.pendudukList.observe(viewLifecycleOwner) { list ->
            pendudukAdapter.submitList(list)
            // Secara opsional, Anda dapat menambahkan logika di sini untuk menampilkan pesan "Tidak ada data ditemukan"
            // jika daftar kosong dan pengguna adalah admin.
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

    private fun showPendudukDetailsDialog(penduduk: Penduduk) {
        // Format semua detail penduduk ke dalam string untuk dialog
        val details = """
            Nama: ${penduduk.nama ?: "-"}
            NIK: ${penduduk.nik ?: "-"}
            No. KK: ${penduduk.no_kk ?: "-"}
            Hubungan Keluarga: ${penduduk.hub_keluarga ?: "-"}
            Jenis Kelamin: ${penduduk.jenis_kel ?: "-"}
            Tempat Lahir: ${penduduk.tempat_lahir ?: "-"}
            Tanggal Lahir: ${penduduk.tgl_lahir ?: "-"}
            Agama: ${penduduk.agama ?: "-"}
            Pendidikan: ${penduduk.pendidikan ?: "-"}
            Pekerjaan: ${penduduk.pekerjaan ?: "-"}
            Status: ${penduduk.status ?: "-"}
            RT: ${penduduk.rt ?: "-"}
            Kampung: ${penduduk.kampung ?: "-"}
            Kepala Keluarga: ${penduduk.kep_kel ?: "-"}
        """.trimIndent()

        // Tampilkan AlertDialog dengan semua detail
        AlertDialog.Builder(requireContext())
            .setTitle("Detail Penduduk")
            .setMessage(details)
            .setPositiveButton("OK", null) // Tidak ada tindakan yang diperlukan pada OK, cukup tutup
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
