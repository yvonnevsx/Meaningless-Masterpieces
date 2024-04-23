package com.example.myapplication.ui.dashboard

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.example.myapplication.R
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.myapplication.MainActivity2

class PhotoDisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_display)
        supportActionBar?.hide()

        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(imageUriString)
        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            onBackPressed()
        }
        val photoImageView = findViewById<ImageView>(R.id.photoImageView)
        photoImageView.setImageURI(imageUri)

        val tags = listOf("People Portraits", "Trash", "Landscapes", "Design")
        val flexboxLayout: FlexboxLayout = findViewById(R.id.tagsFlexboxLayout)

        tags.forEach { tagName ->
            val tagView = LayoutInflater.from(this).inflate(R.layout.tag_item, flexboxLayout, false) as TextView
            tagView.text = tagName
            tagView.setOnClickListener { view ->
                view.isSelected = !view.isSelected
                // Handle the logic for max selection here if needed
            }
            flexboxLayout.addView(tagView)
        }


        findViewById<Button>(R.id.buttonPost).setOnClickListener {
            val backToGallery = Intent(this, MainActivity2::class.java)
            startActivity(backToGallery)
        }


        findViewById<Button>(R.id.buttonGenerateTitle).setOnClickListener {
            imageUri?.let {
                val base64Image = convertImagetoBase64(it)
                queryOpenAI("Add a generic meaningful title, description and value in dollar to this art, keep it as vague as possible", base64Image) { response ->
                    runOnUiThread {
                        val splitResponse = response.split("\n", limit = 2)
                        val title = splitResponse.getOrNull(0) ?: "No title"
                        val description = splitResponse.getOrNull(1) ?: "No description"

                        findViewById<TextView>(R.id.title).text = title
                        findViewById<TextView>(R.id.textViewArtsyDescription).text = description
                    }
                }
            }
        }
    }
    private fun convertImagetoBase64(imageUri: Uri): String {
        contentResolver.openInputStream(imageUri).use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            ByteArrayOutputStream().use { outputStream ->
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                val byteArray = outputStream.toByteArray()
                return Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
        }
        return ""
    }

    private fun queryOpenAI(prompt: String, imageBase64: String, onResponse: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = URL("https://api.openai.com/v1/chat/completions")
            val apiKey = "APIKEY"
            val messages = """
            [
                {"role": "system", "content": "You are a helpful assistant."},
                {"role": "user", "content": "$prompt"}
            ]
        """.trimIndent()

            val postData = """
            {
              "model": "gpt-3.5-turbo",
              "messages": $messages,
              "max_tokens": 150
            }
        """.trimIndent()

            with(url.openConnection() as HttpURLConnection) {
                try {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(postData) }

                    val responseContent = if (responseCode == HttpURLConnection.HTTP_OK) {
                        inputStream.bufferedReader().use(BufferedReader::readText)
                    } else {
                        errorStream.bufferedReader().use(BufferedReader::readText)
                    }

                    // Parsing the JSON response to extract only the content text
                    val jsonResponse = Gson().fromJson(responseContent, Response::class.java)
                    val textResponse = jsonResponse.choices?.firstOrNull()?.message?.content ?: "No response"

                    withContext(Dispatchers.Main) {
                        onResponse(textResponse)
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        onResponse("Error: ${e.message}")
                    }
                } finally {
                    disconnect()
                }
            }
        }
    }

    data class Response(
        val choices: List<Choice>?
    )

    data class Choice(
        val message: MessageContent?
    )

    data class MessageContent(
        val content: String?
    )}
