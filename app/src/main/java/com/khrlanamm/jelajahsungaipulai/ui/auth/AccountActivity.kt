package com.khrlanamm.jelajahsungaipulai.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.khrlanamm.jelajahsungaipulai.R
import com.khrlanamm.jelajahsungaipulai.databinding.ActivityAccountBinding

class AccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Akun Saya"

        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        db = Firebase.firestore

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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

            // Check user role from Firestore
            checkUserRole(user.email)
        }
    }

    private fun checkUserRole(email: String?) {
        if (email == null) {
            // Default to user if email is not available
            setupRoleTag(isAdmin = false)
            return
        }

        db.collection("admins")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                // If documents are not empty, user is an admin
                val isAdmin = !documents.isEmpty
                setupRoleTag(isAdmin)
            }
            .addOnFailureListener { exception ->
                // Handle failure, e.g., log error and default to user
                Toast.makeText(this, "Gagal memeriksa status admin.", Toast.LENGTH_SHORT).show()
                setupRoleTag(isAdmin = false)
            }
    }

    private fun setupRoleTag(isAdmin: Boolean) {
        with(binding.textRoleTag) {
            visibility = View.VISIBLE
            if (isAdmin) {
                text = "Admin"
                background = ContextCompat.getDrawable(this@AccountActivity, R.drawable.tag_pink)
            } else {
                text = "Pengguna"
                background = ContextCompat.getDrawable(this@AccountActivity, R.drawable.tag_green)
            }
        }
    }

    private fun setupActionListeners() {
        // Listener untuk tombol Edit Profil
        binding.buttonEditProfile.setOnClickListener {
            showConfirmationDialog(
                title = "Edit Profil",
                message = "Anda akan diarahkan ke halaman Akun Google untuk mengedit profil. Lanjutkan?",
                positiveAction = {
                    Toast.makeText(this, "Mengarahkan ke Akun Google...", Toast.LENGTH_SHORT).show()
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://myaccount.google.com/personal-info")
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
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener(this) {
            Toast.makeText(this, "Anda telah logout", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showConfirmationDialog(title: String, message: String, positiveAction: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setIcon(R.mipmap.ic_launcher)
            .setPositiveButton("Ya") { _, _ ->
                positiveAction.invoke()
            }
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
