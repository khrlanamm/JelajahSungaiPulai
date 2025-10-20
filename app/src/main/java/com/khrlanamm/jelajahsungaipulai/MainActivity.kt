package com.khrlanamm.jelajahsungaipulai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.khrlanamm.jelajahsungaipulai.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // 1. Membuat layout menjadi edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Menerapkan padding secara manual untuk menghindari status bar dan navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.container) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Memberi padding atas pada container utama sebesar tinggi status bar
            view.setPadding(insets.left, insets.top, insets.right, 0)

            // Menyesuaikan margin bawah untuk BottomNavigationView agar tidak tertutup navigation bar
            val navView = binding.navView
            val layoutParams = navView.layoutParams as android.view.ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = insets.bottom
            navView.layoutParams = layoutParams

            WindowInsetsCompat.CONSUMED
        }


        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment

        val navController: NavController = navHostFragment.navController

        navView.setupWithNavController(navController)
    }
}
