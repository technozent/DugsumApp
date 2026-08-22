package com.dug.sun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MeterAdapter(
    private val meters: List<MeterItem>,
    private val onItemClick: (MeterItem) -> Unit
) : RecyclerView.Adapter<MeterAdapter.MeterViewHolder>() {

    data class MeterItem(val name: String, val resId: Int)

    class MeterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvMeterName)
        val ivPreview: ImageView = view.findViewById(R.id.ivMeterPreview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meter, parent, false)
        return MeterViewHolder(view)
    }

    override fun onBindViewHolder(holder: MeterViewHolder, position: Int) {
        val item = meters[position]
        holder.tvName.text = item.name
        holder.ivPreview.setImageResource(item.resId)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = meters.size
}