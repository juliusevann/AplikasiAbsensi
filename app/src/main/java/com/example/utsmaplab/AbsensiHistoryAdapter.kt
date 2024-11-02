package com.example.utsmaplab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

data class Absensi(
    val imageUrl: String,
    val date: String,
    val time: String
)

class AbsensiHistoryAdapter(private val absensiList: List<Absensi>) :
    RecyclerView.Adapter<AbsensiHistoryAdapter.AbsensiViewHolder>() {

    class AbsensiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val absensiImage: ImageView = itemView.findViewById(R.id.absensiImage)
        val absensiDate: TextView = itemView.findViewById(R.id.absensiDate)
        val absensiTime: TextView = itemView.findViewById(R.id.absensiTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsensiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_absensi_history, parent, false)
        return AbsensiViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsensiViewHolder, position: Int) {
        val absensi = absensiList[position]
        Glide.with(holder.itemView.context)
            .load(absensi.imageUrl)
            .into(holder.absensiImage)
        holder.absensiDate.text = absensi.date
        holder.absensiTime.text = absensi.time
    }

    override fun getItemCount(): Int {
        return absensiList.size
    }
}
