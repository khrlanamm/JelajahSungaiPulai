package com.khrlanamm.jelajahsungaipulai.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.khrlanamm.jelajahsungaipulai.R
import com.khrlanamm.jelajahsungaipulai.databinding.ActivityAccountBinding
import kotlinx.coroutines.launch

class AccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var credentialManager: CredentialManager

    companion object {
        private const val TAG = "AccountActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Akun Saya"

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        credentialManager = CredentialManager.create(this)

        populateProfileData()
        setupActionListeners()
    }

    private fun populateProfileData() {
        val user = auth.currentUser
        if (user != null) {
            binding.textName.text = user.displayName
            binding.textEmail.text = user.email

            Glide.with(this)
                .load(user.photoUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .into(binding.profileImage)

            checkUserRole(user.email)
        }
    }

    private fun checkUserRole(email: String?) {
        if (email == null) {
            setupRoleTag(isAdmin = false)
            return
        }

        db.collection("admins")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                val isAdmin = !documents.isEmpty
                setupRoleTag(isAdmin)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memeriksa status admin.", Toast.LENGTH_SHORT).show()
                setupRoleTag(isAdmin = false)
            }
    }

    private fun setupRoleTag(isAdmin: Boolean) {
        with(binding.textRoleTag) {
            visibility = View.VISIBLE
            if (isAdmin) {
                // UBAH: Menggunakan string resource
                text = getString(R.string.role_admin)
                background = ContextCompat.getDrawable(this@AccountActivity, R.drawable.tag_pink)
            } else {
                // UBAH: Menggunakan string resource
                text = getString(R.string.role_user)
                background = ContextCompat.getDrawable(this@AccountActivity, R.drawable.tag_green)
            }
        }
    }

    private fun setupActionListeners() {
        binding.buttonEditProfile.setOnClickListener {
            showConfirmationDialog(
                title = "Edit Profil",
                message = "Anda akan diarahkan ke halaman Akun Google untuk mengedit profil. Lanjutkan?",
                positiveAction = {
                    // UBAH: Menggunakan KTX extension .toUri()
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://myaccount.google.com/personal-info".toUri()
                    )
                    startActivity(intent)
                }
            )
        }

        binding.buttonLogout.setOnClickListener {
            showConfirmationDialog(
                title = "Konfirmasi Logout",
                message = "Apakah Anda yakin ingin keluar dari aplikasi?",
                positiveAction = { logout() }
            )
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            try {
                auth.signOut()
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                Log.d(TAG, "Credential state berhasil dibersihkan.")
                Toast.makeText(this@AccountActivity, "Anda telah logout", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@AccountActivity, AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "Error saat proses logout", e)
                Toast.makeText(
                    this@AccountActivity,
                    "Terjadi kesalahan saat logout.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showConfirmationDialog(title: String, message: String, positiveAction: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setIcon(R.mipmap.ic_launcher)
            .setPositiveButton("Ya") { _, _ -> positiveAction.invoke() }
            .setNegativeButton("Tidak", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
