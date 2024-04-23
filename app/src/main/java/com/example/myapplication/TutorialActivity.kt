package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView


class TutorialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tutorial_layout)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val images = listOf(R.drawable.tutorial_6, R.drawable.tutorial_7, R.drawable.tutorial_8, R.drawable.tutorial_9, R.drawable.tutorial_10)
        val adapter = TutorialAdapter(images)
        recyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        val offsetPosition = 0
        recyclerView.scrollToPosition(offsetPosition)

        val offset = resources.getDimensionPixelOffset(R.dimen.recycler_view_offset) // Adjust this value as needed
        val itemDecoration = CenterItemDecoration(offset)
        recyclerView.addItemDecoration(itemDecoration)

        val button: Button = findViewById(R.id.btnStartCreating)
        button.setOnClickListener {
            // Navigate to MainActivity2 when the button is clicked
            startCreating()
        }
    }

    private fun startCreating() {
        // Create an Intent to start MainActivity2
        val intent = Intent(this, MainActivity2::class.java)
        startActivity(intent)
    }
}