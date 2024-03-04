package com.example.image_to_text.ui.history
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.image_to_text.R
import com.example.image_to_text.ui.database.DatabaseHelper
import com.example.image_to_text.ui.ViewTranslationActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageTextAdapter(
    private val context: Context,
    private val items: MutableList<HistoryActivity.ImageTextItem>,
    private val databaseHelper: DatabaseHelper
) : RecyclerView.Adapter<ImageTextAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position, selectedItems.contains(position))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //private val imageView: ImageView = itemView.findViewById(R.id.imageViewItem)
        private val textView: TextView = itemView.findViewById(R.id.textViewItem)

        fun bind(item: HistoryActivity.ImageTextItem, position: Int, isSelected: Boolean) {
            //imageView.setImageBitmap(item.bitmap)
            textView.text = item.text

            // Set background color based on item selection
            if (isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.selectedItemBackground))
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }

            // Toggle item selection on long press
            itemView.setOnLongClickListener {
                toggleSelection(position)
                true
            }

            // Open ViewTranslationActivity when item clicked
            itemView.setOnClickListener {
                val intent = Intent(context, ViewTranslationActivity::class.java).apply {
                   // putExtra("imageFilePath", getImageFilePath(item.bitmap))
                    putExtra("yourString", item.text)
                }
                context.startActivity(intent)
            }
        }

        private fun toggleSelection(position: Int) {
            if (selectedItems.contains(position)) {
                selectedItems.remove(position)
            } else {
                selectedItems.add(position)
            }
            notifyItemChanged(position)
        }
    }
    private fun getImageFilePath(bitmap: Bitmap): String {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File? = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

        return try {
            val stream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
            imageFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    fun getSelectedItems(): Set<Int> {
        return selectedItems
    }

    fun removeItemAt(position: Int) {
        val item = items[position]
        items.removeAt(position)
        notifyItemRemoved(position)
        // Delete the corresponding data from the database
        databaseHelper.deleteData(item.text)
    }
}
