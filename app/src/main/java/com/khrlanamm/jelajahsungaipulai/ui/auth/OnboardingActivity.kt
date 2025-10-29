package com.khrlanamm.jelajahsungaipulai.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.khrlanamm.jelajahsungaipulai.R

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        hideSystemUI()
        playVideo()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun playVideo() {
        val videoView = findViewById<VideoView>(R.id.splash_video_view)
        val videoPath = "android.resource://" + packageName + "/" + R.raw.splash_video
        val uri = Uri.parse(videoPath)

        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.start()
        }

        videoView.setOnCompletionListener {
            navigateToAuth()
        }

        videoView.setOnErrorListener { _, _, _ ->
            navigateToAuth()
            true
        }
    }

    private fun navigateToAuth() {
        if (isFinishing) {
            return
        }
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}