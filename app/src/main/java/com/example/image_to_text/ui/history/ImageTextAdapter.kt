package com.example.image_to_text.ui.history

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.image_to_text.R
import com.example.image_to_text.ui.database.DatabaseHelper

class ImageTextAdapter(private val items: MutableList<HistoryActivity.ImageTextItem>, private val databaseHelper: DatabaseHelper) : RecyclerView.Adapter<ImageTextAdapter.ViewHolder>() {

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
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewItem)
        private val textView: TextView = itemView.findViewById(R.id.textViewItem)

        fun bind(item: HistoryActivity.ImageTextItem, position: Int, isSelected: Boolean) {
            imageView.setImageBitmap(item.bitmap)
            textView.text = item.text

            // Set background color based on item selection
            if (isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.selectedItemBackground))
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }

            // Toggle item selection when clicked
            itemView.setOnClickListener {
                if (selectedItems.contains(position)) {
                    selectedItems.remove(position)
                } else {
                    selectedItems.add(position)
                }
                notifyItemChanged(position)
            }
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
