package com.example.utsmaplab

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class AbsensiHistoryActivity : AppCompatActivity() {
    private lateinit var recyclerViewHistory: RecyclerView
    private lateinit var absensiAdapter: AbsensiHistoryAdapter
    private val absensiList = mutableListOf<Absensi>()
    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_absen)

        recyclerViewHistory = findViewById(R.id.recyclerViewHistory)
        recyclerViewHistory.layoutManager = LinearLayoutManager(this)

        loadAbsensiHistory()

        bottomNavView = findViewById(R.id.bottom_nav_view)
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_absen_history -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadAbsensiHistory() {
        val db = FirebaseFirestore.getInstance()
        db.collection("absensi")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                absensiList.clear()
                val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                for (document in documents) {
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val timestamp = document.getDate("timestamp")

                    val date = if (timestamp != null) dateFormatter.format(timestamp) else "Tanggal tidak tersedia"
                    val time = if (timestamp != null) SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(timestamp) else "Waktu tidak tersedia"

                    val absensi = Absensi(imageUrl = imageUrl, date = date, time = time)
                    absensiList.add(absensi)
                }
                absensiAdapter = AbsensiHistoryAdapter(absensiList)
                recyclerViewHistory.adapter = absensiAdapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching history: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
