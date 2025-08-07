package com.khrlanamm.jelajahsungaipulai.ui.penduduk

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.khrlanamm.jelajahsungaipulai.R
import com.khrlanamm.jelajahsungaipulai.data.Penduduk
import com.khrlanamm.jelajahsungaipulai.databinding.FragmentPendudukBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
        setupAddButton() // Setup tombol "Tambah Penduduk"
        setupSearchView() // Setup Search View
        observeAdminStatus()
        observePendudukData()
        observeStatusMessage() // Observe status messages from ViewModel
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

    private fun setupAddButton() {
        binding.btnAddPenduduk.setOnClickListener {
            showAddPendudukDialog()
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Tidak perlu melakukan apa-apa saat submit, filtering sudah di onQueryTextChange
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Panggil fungsi filter di ViewModel
                pendudukViewModel.filterPenduduk(newText.orEmpty())
                return true
            }
        })
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
                    binding.btnAddPenduduk.visibility = View.VISIBLE // Tampilkan tombol jika admin
                    binding.searchView.visibility = View.VISIBLE // Tampilkan search view jika admin
                } else {
                    binding.btnAddPenduduk.visibility = View.GONE // Sembunyikan tombol jika bukan admin
                    binding.searchView.visibility = View.GONE // Sembunyikan search view jika bukan admin
                    showAccessDeniedDialog()
                }
            }
        }
    }

    private fun observePendudukData() {
        // Amati daftar penduduk yang difilter dari ViewModel dan perbarui adapter
        pendudukViewModel.filteredPendudukList.observe(viewLifecycleOwner) { list ->
            pendudukAdapter.submitList(list)
            if (list.isEmpty() && pendudukViewModel.isAdmin.value == true && binding.searchView.query.isNullOrEmpty()) {
                Toast.makeText(context, "Tidak ada data penduduk ditemukan.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeStatusMessage() {
        pendudukViewModel.statusMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                // Clear the message after showing to prevent re-showing on configuration changes
                pendudukViewModel.clearStatusMessage()
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

    private fun showPendudukDetailsDialog(penduduk: Penduduk) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_penduduk_details, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Inisialisasi TextViews dari layout dialog
        dialogView.findViewById<TextView>(R.id.tv_detail_nama).text = "Nama: ${penduduk.nama ?: "-"}"
        dialogView.findViewById<TextView>(R.id.tv_detail_nik).text = "NIK: ${penduduk.nik ?: "-"}"
        dialogView.findViewById<TextView>(R.id.tv_detail_no_kk).text = "No. KK: ${penduduk.no_kk ?: "-"}"
        dialogView.findViewById<TextView>(R.id.tv_detail_hub_keluarga).text = "Hubungan Keluarga: ${penduduk.hub_keluarga ?: "-"}"
        dialogView.findViewById<TextView>(R.id.tv_detail_jenis_kel).text = "Jenis Kelamin: ${penduduk.jenis_kel ?: "-"}"
        dialogView.findViewById<TextView>(R.id.tv_detail_tempat_lahir).text = "Tempat Lahir: ${penduduk.tempat_lahir ?: "-"}"
        dialogView.findViewById<TextView>(R.id.tv_detail_tgl_lahir).text = "Tanggal Lahir: ${penduduk.tgl_lahir ?: "-"}"
        dialogView.findViewById<TextView>(R.id.tv_detail_agama).text = "Agama: ${penduduk.agama ?: "-"}"
        dialogView.findViewById<TextView>(R.id.tv_detail_pendidikan).text = "Pendidikan: ${penduduk.pendidikan ?: "-"}"
        dialogView.findViewById<TextView>(R.id.tv_detail_pekerjaan).text = "Pekerjaan: ${penduduk.pekerjaan ?: "-"}"
        dialogView.findViewById<TextView>(R.id.tv_detail_status).text = "Status: ${penduduk.status ?: "-"}"
        dialogView.findViewById<TextView>(R.id.tv_detail_rt).text = "RT: ${penduduk.rt ?: "-"}"
        dialogView.findViewById<TextView>(R.id.tv_detail_kampung).text = "Kampung: ${penduduk.kampung ?: "-"}"
        dialogView.findViewById<TextView>(R.id.tv_detail_kep_kel).text = "Kepala Keluarga: ${penduduk.kep_kel ?: "-"}"

        // Inisialisasi tombol edit dan hapus
        val btnEdit = dialogView.findViewById<Button>(R.id.btn_edit)
        val btnDelete = dialogView.findViewById<Button>(R.id.btn_delete)

        btnEdit.setOnClickListener {
            dialog.dismiss() // Tutup dialog detail
            showEditPendudukDialog(penduduk) // Tampilkan dialog edit
        }

        btnDelete.setOnClickListener {
            dialog.dismiss() // Tutup dialog detail
            showDeleteConfirmationDialog(penduduk) // Tampilkan konfirmasi hapus
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(penduduk: Penduduk) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Data Penduduk")
            .setMessage("Apakah Anda yakin ingin menghapus data ${penduduk.nama} (NIK: ${penduduk.nik})?")
            .setPositiveButton("Hapus") { _, _ ->
                penduduk.documentId?.let { docId ->
                    pendudukViewModel.deletePenduduk(docId)
                } ?: run {
                    Toast.makeText(context, "Gagal menghapus: ID dokumen tidak ditemukan.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun showAddPendudukDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_penduduk, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Set judul dialog
        dialogView.findViewById<TextView>(R.id.tv_dialog_title)?.text = "Tambah Data Penduduk"
        // NIK harus bisa diisi untuk data baru
        val etNik = dialogView.findViewById<TextInputEditText>(R.id.et_nik)
        etNik.isEnabled = true

        val etTglLahir = dialogView.findViewById<TextInputEditText>(R.id.et_tgl_lahir)
        etTglLahir.setOnClickListener {
            showDatePicker(etTglLahir)
        }

        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel_edit)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save_edit)
        btnSave.text = "Tambah" // Ubah teks tombol menjadi "Tambah"

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            // Ambil data dari EditTexts
            val newNama = dialogView.findViewById<TextInputEditText>(R.id.et_nama).text.toString()
            val newNik = etNik.text.toString()
            val newNoKk = dialogView.findViewById<TextInputEditText>(R.id.et_no_kk).text.toString()
            val newHubKeluarga = dialogView.findViewById<TextInputEditText>(R.id.et_hub_keluarga).text.toString()
            val newJenisKel = dialogView.findViewById<TextInputEditText>(R.id.et_jenis_kel).text.toString()
            val newTempatLahir = dialogView.findViewById<TextInputEditText>(R.id.et_tempat_lahir).text.toString()
            val newTglLahir = etTglLahir.text.toString()
            val newAgama = dialogView.findViewById<TextInputEditText>(R.id.et_agama).text.toString()
            val newPendidikan = dialogView.findViewById<TextInputEditText>(R.id.et_pendidikan).text.toString()
            val newPekerjaan = dialogView.findViewById<TextInputEditText>(R.id.et_pekerjaan).text.toString()
            val newStatus = dialogView.findViewById<TextInputEditText>(R.id.et_status).text.toString()
            val newRt = dialogView.findViewById<TextInputEditText>(R.id.et_rt).text.toString().toLongOrNull()
            val newKampung = dialogView.findViewById<TextInputEditText>(R.id.et_kampung).text.toString()
            val newKepKel = dialogView.findViewById<TextInputEditText>(R.id.et_kep_kel).text.toString()

            // Validasi sederhana
            if (newNama.isBlank() || newNik.isBlank()) {
                Toast.makeText(context, "Nama dan NIK tidak boleh kosong.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Buat objek Penduduk baru (tanpa documentId karena ini data baru)
            val newPenduduk = Penduduk(
                nama = newNama,
                nik = newNik,
                no_kk = newNoKk,
                hub_keluarga = newHubKeluarga,
                jenis_kel = newJenisKel,
                tempat_lahir = newTempatLahir,
                tgl_lahir = newTglLahir,
                agama = newAgama,
                pendidikan = newPendidikan,
                pekerjaan = newPekerjaan,
                status = newStatus,
                rt = newRt,
                kampung = newKampung,
                kep_kel = newKepKel
            )

            // Panggil ViewModel untuk menambahkan data
            pendudukViewModel.addPenduduk(newPenduduk)
            dialog.dismiss() // Tutup dialog
        }

        dialog.show()
    }

    private fun showEditPendudukDialog(penduduk: Penduduk) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_penduduk, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Set judul dialog
        dialogView.findViewById<TextView>(R.id.tv_dialog_title)?.text = "Edit Data Penduduk"

        // Inisialisasi EditTexts dari layout dialog dan isi dengan data yang ada
        dialogView.findViewById<TextInputEditText>(R.id.et_nama).setText(penduduk.nama)
        val etNik = dialogView.findViewById<TextInputEditText>(R.id.et_nik)
        etNik.setText(penduduk.nik)
        etNik.isEnabled = false // NIK tidak bisa diedit
        dialogView.findViewById<TextInputEditText>(R.id.et_no_kk).setText(penduduk.no_kk)
        dialogView.findViewById<TextInputEditText>(R.id.et_hub_keluarga).setText(penduduk.hub_keluarga)
        dialogView.findViewById<TextInputEditText>(R.id.et_jenis_kel).setText(penduduk.jenis_kel)
        dialogView.findViewById<TextInputEditText>(R.id.et_tempat_lahir).setText(penduduk.tempat_lahir)
        val etTglLahir = dialogView.findViewById<TextInputEditText>(R.id.et_tgl_lahir)
        etTglLahir.setText(penduduk.tgl_lahir)
        etTglLahir.setOnClickListener {
            showDatePicker(etTglLahir)
        }
        dialogView.findViewById<TextInputEditText>(R.id.et_agama).setText(penduduk.agama)
        dialogView.findViewById<TextInputEditText>(R.id.et_pendidikan).setText(penduduk.pendidikan)
        dialogView.findViewById<TextInputEditText>(R.id.et_pekerjaan).setText(penduduk.pekerjaan)
        dialogView.findViewById<TextInputEditText>(R.id.et_status).setText(penduduk.status)
        dialogView.findViewById<TextInputEditText>(R.id.et_rt).setText(penduduk.rt?.toString())
        dialogView.findViewById<TextInputEditText>(R.id.et_kampung).setText(penduduk.kampung)
        dialogView.findViewById<TextInputEditText>(R.id.et_kep_kel).setText(penduduk.kep_kel)

        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel_edit)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save_edit)
        btnSave.text = "Simpan" // Pastikan teks tombol adalah "Simpan"

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            // Ambil data dari EditTexts
            val updatedNama = dialogView.findViewById<TextInputEditText>(R.id.et_nama).text.toString()
            val updatedNik = etNik.text.toString() // NIK tetap diambil meski disabled
            val updatedNoKk = dialogView.findViewById<TextInputEditText>(R.id.et_no_kk).text.toString()
            val updatedHubKeluarga = dialogView.findViewById<TextInputEditText>(R.id.et_hub_keluarga).text.toString()
            val updatedJenisKel = dialogView.findViewById<TextInputEditText>(R.id.et_jenis_kel).text.toString()
            val updatedTempatLahir = dialogView.findViewById<TextInputEditText>(R.id.et_tempat_lahir).text.toString()
            val updatedTglLahir = etTglLahir.text.toString()
            val updatedAgama = dialogView.findViewById<TextInputEditText>(R.id.et_agama).text.toString()
            val updatedPendidikan = dialogView.findViewById<TextInputEditText>(R.id.et_pendidikan).text.toString()
            val updatedPekerjaan = dialogView.findViewById<TextInputEditText>(R.id.et_pekerjaan).text.toString()
            val updatedStatus = dialogView.findViewById<TextInputEditText>(R.id.et_status).text.toString()
            val updatedRt = dialogView.findViewById<TextInputEditText>(R.id.et_rt).text.toString().toLongOrNull()
            val updatedKampung = dialogView.findViewById<TextInputEditText>(R.id.et_kampung).text.toString()
            val updatedKepKel = dialogView.findViewById<TextInputEditText>(R.id.et_kep_kel).text.toString()

            // Buat objek Penduduk baru dengan data yang diperbarui (pastikan documentId tetap sama)
            val updatedPenduduk = Penduduk(
                documentId = penduduk.documentId,
                nama = updatedNama,
                nik = updatedNik,
                no_kk = updatedNoKk,
                hub_keluarga = updatedHubKeluarga,
                jenis_kel = updatedJenisKel,
                tempat_lahir = updatedTempatLahir,
                tgl_lahir = updatedTglLahir,
                agama = updatedAgama,
                pendidikan = updatedPendidikan,
                pekerjaan = updatedPekerjaan,
                status = updatedStatus,
                rt = updatedRt,
                kampung = updatedKampung,
                kep_kel = updatedKepKel
            )

            // Panggil ViewModel untuk mengupdate data
            pendudukViewModel.updatePenduduk(updatedPenduduk)
            dialog.dismiss() // Tutup dialog edit
        }

        dialog.show()
    }

    // Fungsi untuk menampilkan DatePickerDialog
    private fun showDatePicker(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()

        // Coba parsing tanggal yang sudah ada di EditText jika ada
        val existingDateString = editText.text.toString()
        if (existingDateString.isNotBlank()) {
            try {
                val format = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                val date = format.parse(existingDateString)
                date?.let { calendar.time = it }
            } catch (e: Exception) {
                // Biarkan tanggal default jika parsing gagal
                e.printStackTrace()
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                // Format tanggal yang dipilih ke "dd MMMM yyyy"
                val selectedDateCalendar = Calendar.getInstance()
                selectedDateCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth)
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                val formattedDate = dateFormat.format(selectedDateCalendar.time)
                editText.setText(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
