package com.example.image_to_text.ui.adapter
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.image_to_text.R
import com.example.image_to_text.ui.activities.HistoryActivity
import com.example.image_to_text.ui.activities.ViewTranslationActivity
import com.example.image_to_text.ui.database.DatabaseHelper
import com.example.image_to_text.ui.utils.Utils

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
        private val textView: TextView = itemView.findViewById(R.id.textViewItem)
        private val copyIcon: ImageView = itemView.findViewById(R.id.copy)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.delete)
        private val shareIcon: ImageView = itemView.findViewById(R.id.share)
        fun bind(item: HistoryActivity.ImageTextItem, position: Int, isSelected: Boolean) {
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
                    putExtra("yourString", item.text)
                }
                context.startActivity(intent)
            }

            // Copy text to clipboard
            copyIcon.setOnClickListener {
                Utils.logAnalytic("ImageText copy button clicked")
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("text", item.text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()

            }

            // Show delete confirmation dialog
            deleteIcon.setOnClickListener {
                Utils.logAnalytic("ImageText delete button clicked")
                showDeleteConfirmationDialog(position)
            }

            // Share text
            shareIcon.setOnClickListener {
                Utils.logAnalytic("ImageText share button clicked")
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, item.text)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(intent, "Share via"))
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

        private fun showDeleteConfirmationDialog(position: Int) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes") { _, _ ->
                    removeItemAt(position)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun removeItemAt(position: Int) {
        val item = items[position]
        items.removeAt(position)
        notifyItemRemoved(position)
        // Delete the corresponding data from the database
        databaseHelper.deleteData(item.text)
    }
}
