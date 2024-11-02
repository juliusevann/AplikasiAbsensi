package com.example.utsmaplab

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var dateTime: TextView
    private lateinit var absenButton: ImageView
    private lateinit var logoutButton: Button
    private lateinit var bottomNavView: BottomNavigationView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dateTime = findViewById(R.id.dateTimeTextView)
        absenButton = findViewById(R.id.absenButton)
        logoutButton = findViewById(R.id.logoutButton)
        bottomNavView = findViewById(R.id.bottom_nav_view)

        val currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale.getDefault()).format(Date())
        dateTime.text = currentDate

        absenButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, AbsensiActivity::class.java))
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Tetap di MainActivity, tidak perlu navigasi ulang
                    true
                }
                R.id.nav_absen_history -> {
                    startActivity(Intent(this, AbsensiHistoryActivity::class.java))
                    finish() // Selesaikan MainActivity untuk menghindari kembali ke layar ini
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish() // Selesaikan MainActivity untuk menghindari kembali ke layar ini
                    true
                }
                else -> false
            }
        }
    }
}
