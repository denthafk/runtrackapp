package com.project.myapplication.ui.history

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.myapplication.R
import com.project.myapplication.data.model.TrackEntity
import com.project.myapplication.databinding.ActivityHistoryBinding
import com.project.myapplication.ui.adapter.TrackHistoryAdapter
import com.project.myapplication.ui.viewmodels.MainViewModel
import com.project.myapplication.ui.viewmodels.factory.MainViewModelFactory

class HistoryActivity : AppCompatActivity() {

    private val mainViewModel by viewModels<MainViewModel> {
        MainViewModelFactory(this.application)
    }

    private lateinit var binding: ActivityHistoryBinding
    private val trackHistoryAdapter = TrackHistoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "History"

        setRecyclerView()
        observeTrackHistory()
    }

    private fun setRecyclerView() {
        binding.rvHistory.apply {
            adapter = trackHistoryAdapter
            layoutManager = LinearLayoutManager(this@HistoryActivity)
        }
    }

    private fun observeTrackHistory() {
        mainViewModel.getAllTrack().observe(this) { trackList ->
            if (trackList.isNotEmpty()) {
                showList(true)
                trackHistoryAdapter.setTrack(trackList as ArrayList<TrackEntity>)

            } else {
                showList(false)

            }
        }
    }

    private fun showList(isListNotEmpty: Boolean) {
        binding.apply {
            rvHistory.isVisible = isListNotEmpty
            layoutHeader.isVisible = isListNotEmpty
            tvNoData.isVisible = !isListNotEmpty
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.history_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn_clear -> {
                mainViewModel.deleteAllTrack()
                observeTrackHistory()
                Toast.makeText(this, "Deleted All History!", Toast.LENGTH_SHORT).show()
            }
        }

        return super.onOptionsItemSelected(item)
    }

}