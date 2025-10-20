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
        pendudukViewModel = ViewModelProvider(this)[PendudukViewModel::class.java]
        _binding = FragmentPendudukBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupAddButton()
        setupSearchView()
        observeAdminStatus()
        observePendudukData()
        observeStatusMessage()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = getString(R.string.penduduk_toolbar_title)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        pendudukAdapter = PendudukAdapter { penduduk ->
            showPendudukDetailsDialog(penduduk)
        }
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
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
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
                    binding.btnAddPenduduk.visibility = View.VISIBLE
                    binding.searchView.visibility = View.VISIBLE
                } else {
                    binding.btnAddPenduduk.visibility = View.GONE
                    binding.searchView.visibility = View.GONE
                    showAccessDeniedDialog()
                }
            }
        }
    }

    private fun observePendudukData() {
        pendudukViewModel.filteredPendudukList.observe(viewLifecycleOwner) { list ->
            pendudukAdapter.submitList(list)
            if (list.isEmpty() && pendudukViewModel.isAdmin.value == true && binding.searchView.query.isNullOrEmpty()) {
                Toast.makeText(context, getString(R.string.penduduk_no_data_found), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeStatusMessage() {
        pendudukViewModel.statusMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                pendudukViewModel.clearStatusMessage()
            }
        }
    }

    private fun showAccessDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.penduduk_access_denied_title))
            .setMessage(getString(R.string.penduduk_access_denied_message))
            .setPositiveButton(getString(R.string.dialog_button_ok)) { _, _ ->
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

        val dash = "-"
        dialogView.findViewById<TextView>(R.id.tv_detail_nama).text = getString(R.string.detail_label_nama, penduduk.nama ?: dash)
        dialogView.findViewById<TextView>(R.id.tv_detail_nik).text = getString(R.string.detail_label_nik, penduduk.nik ?: dash)
        dialogView.findViewById<TextView>(R.id.tv_detail_no_kk).text = getString(R.string.detail_label_no_kk, penduduk.no_kk ?: dash)
        dialogView.findViewById<TextView>(R.id.tv_detail_hub_keluarga).text = getString(R.string.detail_label_hub_keluarga, penduduk.hub_keluarga ?: dash)
        dialogView.findViewById<TextView>(R.id.tv_detail_jenis_kel).text = getString(R.string.detail_label_jenis_kel, penduduk.jenis_kel ?: dash)
        dialogView.findViewById<TextView>(R.id.tv_detail_tempat_lahir).text = getString(R.string.detail_label_tempat_lahir, penduduk.tempat_lahir ?: dash)
        dialogView.findViewById<TextView>(R.id.tv_detail_tgl_lahir).text = getString(R.string.detail_label_tgl_lahir, penduduk.tgl_lahir ?: dash)
        dialogView.findViewById<TextView>(R.id.tv_detail_agama).text = getString(R.string.detail_label_agama, penduduk.agama ?: dash)
        dialogView.findViewById<TextView>(R.id.tv_detail_pendidikan).text = getString(R.string.detail_label_pendidikan, penduduk.pendidikan ?: dash)
        dialogView.findViewById<TextView>(R.id.tv_detail_pekerjaan).text = getString(R.string.detail_label_pekerjaan, penduduk.pekerjaan ?: dash)
        dialogView.findViewById<TextView>(R.id.tv_detail_status).text = getString(R.string.detail_label_status, penduduk.status ?: dash)
        dialogView.findViewById<TextView>(R.id.tv_detail_rt).text = getString(R.string.detail_label_rt, penduduk.rt?.toString() ?: dash)
        dialogView.findViewById<TextView>(R.id.tv_detail_kampung).text = getString(R.string.detail_label_kampung, penduduk.kampung ?: dash)
        dialogView.findViewById<TextView>(R.id.tv_detail_kep_kel).text = getString(R.string.detail_label_kep_kel, penduduk.kep_kel ?: dash)

        val btnEdit = dialogView.findViewById<Button>(R.id.btn_edit)
        val btnDelete = dialogView.findViewById<Button>(R.id.btn_delete)

        btnEdit.setOnClickListener {
            dialog.dismiss()
            showEditPendudukDialog(penduduk)
        }

        btnDelete.setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmationDialog(penduduk)
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(penduduk: Penduduk) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_dialog_title))
            .setMessage(getString(R.string.delete_dialog_message, penduduk.nama, penduduk.nik))
            .setPositiveButton(getString(R.string.dialog_button_delete)) { _, _ ->
                penduduk.documentId?.let { docId ->
                    pendudukViewModel.deletePenduduk(docId)
                } ?: run {
                    Toast.makeText(context, getString(R.string.delete_error_no_doc_id), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.dialog_button_cancel), null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun showAddPendudukDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_penduduk, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.tv_dialog_title)?.text = getString(R.string.add_dialog_title)
        val etNik = dialogView.findViewById<TextInputEditText>(R.id.et_nik)
        etNik.isEnabled = true

        val etTglLahir = dialogView.findViewById<TextInputEditText>(R.id.et_tgl_lahir)
        etTglLahir.setOnClickListener {
            showDatePicker(etTglLahir)
        }

        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel_edit)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save_edit)
        btnSave.text = getString(R.string.dialog_button_add)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val newNama = dialogView.findViewById<TextInputEditText>(R.id.et_nama).text.toString()
            val newNik = etNik.text.toString()

            if (newNama.isBlank() || newNik.isBlank()) {
                Toast.makeText(context, getString(R.string.add_edit_validation_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPenduduk = Penduduk(
                nama = newNama,
                nik = newNik,
                no_kk = dialogView.findViewById<TextInputEditText>(R.id.et_no_kk).text.toString(),
                hub_keluarga = dialogView.findViewById<TextInputEditText>(R.id.et_hub_keluarga).text.toString(),
                jenis_kel = dialogView.findViewById<TextInputEditText>(R.id.et_jenis_kel).text.toString(),
                tempat_lahir = dialogView.findViewById<TextInputEditText>(R.id.et_tempat_lahir).text.toString(),
                tgl_lahir = etTglLahir.text.toString(),
                agama = dialogView.findViewById<TextInputEditText>(R.id.et_agama).text.toString(),
                pendidikan = dialogView.findViewById<TextInputEditText>(R.id.et_pendidikan).text.toString(),
                pekerjaan = dialogView.findViewById<TextInputEditText>(R.id.et_pekerjaan).text.toString(),
                status = dialogView.findViewById<TextInputEditText>(R.id.et_status).text.toString(),
                rt = dialogView.findViewById<TextInputEditText>(R.id.et_rt).text.toString().toLongOrNull(),
                kampung = dialogView.findViewById<TextInputEditText>(R.id.et_kampung).text.toString(),
                kep_kel = dialogView.findViewById<TextInputEditText>(R.id.et_kep_kel).text.toString()
            )

            pendudukViewModel.addPenduduk(newPenduduk)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditPendudukDialog(penduduk: Penduduk) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_penduduk, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.tv_dialog_title)?.text = getString(R.string.edit_dialog_title)

        val etNik = dialogView.findViewById<TextInputEditText>(R.id.et_nik)
        etNik.setText(penduduk.nik)
        etNik.isEnabled = false
        val etTglLahir = dialogView.findViewById<TextInputEditText>(R.id.et_tgl_lahir)
        etTglLahir.setText(penduduk.tgl_lahir)
        etTglLahir.setOnClickListener {
            showDatePicker(etTglLahir)
        }

        dialogView.findViewById<TextInputEditText>(R.id.et_nama).setText(penduduk.nama)
        dialogView.findViewById<TextInputEditText>(R.id.et_no_kk).setText(penduduk.no_kk)
        dialogView.findViewById<TextInputEditText>(R.id.et_hub_keluarga).setText(penduduk.hub_keluarga)
        dialogView.findViewById<TextInputEditText>(R.id.et_jenis_kel).setText(penduduk.jenis_kel)
        dialogView.findViewById<TextInputEditText>(R.id.et_tempat_lahir).setText(penduduk.tempat_lahir)
        dialogView.findViewById<TextInputEditText>(R.id.et_agama).setText(penduduk.agama)
        dialogView.findViewById<TextInputEditText>(R.id.et_pendidikan).setText(penduduk.pendidikan)
        dialogView.findViewById<TextInputEditText>(R.id.et_pekerjaan).setText(penduduk.pekerjaan)
        dialogView.findViewById<TextInputEditText>(R.id.et_status).setText(penduduk.status)
        dialogView.findViewById<TextInputEditText>(R.id.et_rt).setText(penduduk.rt?.toString())
        dialogView.findViewById<TextInputEditText>(R.id.et_kampung).setText(penduduk.kampung)
        dialogView.findViewById<TextInputEditText>(R.id.et_kep_kel).setText(penduduk.kep_kel)

        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel_edit)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save_edit)
        btnSave.text = getString(R.string.dialog_button_save)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val updatedPenduduk = Penduduk(
                documentId = penduduk.documentId,
                nik = etNik.text.toString(),
                nama = dialogView.findViewById<TextInputEditText>(R.id.et_nama).text.toString(),
                no_kk = dialogView.findViewById<TextInputEditText>(R.id.et_no_kk).text.toString(),
                hub_keluarga = dialogView.findViewById<TextInputEditText>(R.id.et_hub_keluarga).text.toString(),
                jenis_kel = dialogView.findViewById<TextInputEditText>(R.id.et_jenis_kel).text.toString(),
                tempat_lahir = dialogView.findViewById<TextInputEditText>(R.id.et_tempat_lahir).text.toString(),
                tgl_lahir = etTglLahir.text.toString(),
                agama = dialogView.findViewById<TextInputEditText>(R.id.et_agama).text.toString(),
                pendidikan = dialogView.findViewById<TextInputEditText>(R.id.et_pendidikan).text.toString(),
                pekerjaan = dialogView.findViewById<TextInputEditText>(R.id.et_pekerjaan).text.toString(),
                status = dialogView.findViewById<TextInputEditText>(R.id.et_status).text.toString(),
                rt = dialogView.findViewById<TextInputEditText>(R.id.et_rt).text.toString().toLongOrNull(),
                kampung = dialogView.findViewById<TextInputEditText>(R.id.et_kampung).text.toString(),
                kep_kel = dialogView.findViewById<TextInputEditText>(R.id.et_kep_kel).text.toString()
            )

            pendudukViewModel.updatePenduduk(updatedPenduduk)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDatePicker(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val existingDateString = editText.text.toString()
        if (existingDateString.isNotBlank()) {
            try {
                val format = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
                val date = format.parse(existingDateString)
                date?.let { calendar.time = it }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val selectedDateCalendar = Calendar.getInstance()
                selectedDateCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth)
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
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
