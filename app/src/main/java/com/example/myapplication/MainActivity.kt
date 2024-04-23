package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val buttonTutorial: Button = findViewById(R.id.btnTutorial)
        val buttonHome: Button = findViewById(R.id.btnStartCreating)
        buttonHome.setOnClickListener {
            // Navigate to MainActivity2 when the button is clicked
            startCreating()
        }
        buttonTutorial.setOnClickListener {
            startTutorial()
        }
    }

    private fun startCreating() {
        val intent = Intent(this, MainActivity2::class.java)
        startActivity(intent)
    }
    private fun startTutorial() {
        val intent = Intent(this, TutorialActivity::class.java)
        startActivity(intent)
    }
}