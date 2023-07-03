package com.project.myapplication.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.myapplication.data.model.TrackEntity
import com.project.myapplication.databinding.ItemHistoryRowBinding

class TrackHistoryAdapter :
    RecyclerView.Adapter<TrackHistoryAdapter.ViewHolder>() {

    private val listTrack = ArrayList<TrackEntity>()

    @SuppressLint("NotifyDataSetChanged")
    fun setTrack(trackList: ArrayList<TrackEntity>) {
        this.listTrack.clear()
        this.listTrack.addAll(trackList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemHistoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listTrack[position])
    }

    override fun getItemCount() = listTrack.size

    inner class ViewHolder(private var binding: ItemHistoryRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(trackEntity: TrackEntity) {

            binding.apply {
                tvId.text = trackEntity.id.toString()
                tvNumberStep.text = trackEntity.totalSteps.toString()
                tvTotalDistance.text = trackEntity.totalDistance.toString()
                tvAveragePace.text = trackEntity.totalDuration.toString()
            }
        }
    }
}