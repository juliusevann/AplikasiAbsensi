package com.example.utsmaplab

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    private lateinit var inputNama: EditText
    private lateinit var inputNIM: EditText
    private lateinit var saveProfileButton: Button
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var progressBar: ProgressBar

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        inputNama = findViewById(R.id.inputNama)
        inputNIM = findViewById(R.id.inputNIM)
        saveProfileButton = findViewById(R.id.saveProfileButton)
        progressBar = findViewById(R.id.progressBar)
        bottomNavView = findViewById(R.id.bottom_nav_view)

        loadProfile()

        setupBottomNavigation()

        saveProfileButton.setOnClickListener {
            saveProfile()
        }
    }

    private fun setupBottomNavigation() {
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_absen_history -> {
                    startActivity(Intent(this, AbsensiHistoryActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun loadProfile() {
        progressBar.visibility = View.VISIBLE
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nama = document.getString("nama")
                        val nim = document.getString("nim")

                        inputNama.setText(nama)
                        inputNIM.setText(nim)
                    }
                    progressBar.visibility = View.GONE
                }
                .addOnFailureListener { exception ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Gagal memuat profil: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfile() {
        val nama = inputNama.text.toString().trim()
        val nim = inputNIM.text.toString().trim()

        if (nama.isEmpty() || nim.isEmpty()) {
            Toast.makeText(this, "Nama dan NIM tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        if (userId != null) {
            val userProfile = hashMapOf(
                "nama" to nama,
                "nim" to nim
            )

            db.collection("users").document(userId).set(userProfile)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Profil berhasil disimpan", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error saving profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
