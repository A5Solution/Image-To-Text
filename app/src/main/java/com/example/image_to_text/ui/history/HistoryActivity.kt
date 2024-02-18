package com.example.image_to_text.ui.history

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.image_to_text.R
import com.example.image_to_text.ui.database.DatabaseHelper
import java.io.ByteArrayInputStream

class HistoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var back: ImageView
    private lateinit var delete: ImageView
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.recyclerView)
        back = findViewById(R.id.back)
        delete = findViewById(R.id.delete)

        back.setOnClickListener {
            finish()
        }

        delete.setOnClickListener {
            val adapter = recyclerView.adapter as? ImageTextAdapter
            val selectedItems = adapter?.getSelectedItems()?.toList() // Convert to List
            selectedItems?.forEach { position ->
                adapter.removeItemAt(position)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        databaseHelper = DatabaseHelper(this)
        val items = loadSavedItems()
        recyclerView.adapter = ImageTextAdapter(items.toMutableList(), databaseHelper)

    }

    private fun loadSavedItems(): List<ImageTextItem> {
        val items = mutableListOf<ImageTextItem>()
        val data = databaseHelper.getAllData()
        for (row in data) {
            try {
                val imageByteArray = row.get("image_data") as ByteArray
                val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(imageByteArray))
                val text = row.get("text") as String
                items.add(ImageTextItem(bitmap, text))
            } catch (e: Exception) {
                Log.e("HistoryActivity", "Error loading data", e)
            }
        }
        return items
    }

    data class ImageTextItem(val bitmap: Bitmap, val text: String)
}
