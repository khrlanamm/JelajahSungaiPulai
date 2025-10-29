package com.khrlanamm.jelajahsungaipulai.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.khrlanamm.jelajahsungaipulai.MainActivity
import com.khrlanamm.jelajahsungaipulai.R
import com.khrlanamm.jelajahsungaipulai.databinding.ActivityAuthBinding
import kotlinx.coroutines.launch
import java.security.SecureRandom

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    companion object {
        private const val TAG = "AuthActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        if (auth.currentUser != null) {
            navigateToMain(auth.currentUser)
            return
        }

        binding.buttonGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        updateUI(isLoading = true)
        val nonce = generateNonce()

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setNonce(nonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@AuthActivity, request)
                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        Log.d(TAG, "Google ID Token berhasil didapatkan.")
                        firebaseAuthWithGoogle(idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Gagal mem-parsing token Google.", e)
                        Toast.makeText(this@AuthActivity, "Gagal mem-parsing token Google.", Toast.LENGTH_SHORT).show()
                        updateUI(false)
                    }
                } else {
                    Log.w(TAG, "Tipe kredensial tidak dikenali: ${credential.type}")
                    Toast.makeText(this@AuthActivity, "Tipe kredensial tidak dikenali.", Toast.LENGTH_SHORT).show()
                    updateUI(false)
                }

            } catch (e: GetCredentialException) {
                Log.w(TAG, "Gagal mendapatkan kredensial.", e)
                val userMessage = when (e) {
                    is NoCredentialException -> "Tidak ada akun Google yang ditemukan untuk login."
                    is GetCredentialCancellationException -> "Proses login dibatalkan."
                    else -> "Gagal login: ${e.message}"
                }
                Toast.makeText(this@AuthActivity, userMessage, Toast.LENGTH_SHORT).show()
                updateUI(false)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential_firebase:success")
                    val user = task.result.user
                    Toast.makeText(this, "Berhasil login sebagai ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    navigateToMain(user)
                } else {
                    Log.w(TAG, "signInWithCredential_firebase:failure", task.exception)
                    Toast.makeText(this, "Autentikasi Firebase gagal.", Toast.LENGTH_SHORT).show()
                    updateUI(false)
                }
            }
    }

    private fun navigateToMain(user: FirebaseUser?) {
        if (user == null) return
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_NAME", user.displayName)
            putExtra("USER_PHOTO_URL", user.photoUrl?.toString())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun updateUI(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonGoogleSignIn.isEnabled = !isLoading
    }

    private fun generateNonce(length: Int = 16): String {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}