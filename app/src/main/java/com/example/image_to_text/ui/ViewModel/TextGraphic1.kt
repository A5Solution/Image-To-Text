package com.example.image_to_text.ui.ViewModel


import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.google.mlkit.vision.text.Text

class TextGraphic1(overlay: GraphicOverlay1, private val element: Text.Element) :
    GraphicOverlay1.Graphic(overlay) {

    companion object {
        private const val TAG = "TextGraphic"
        private const val TEXT_COLOR = Color.BLACK
        private const val TEXT_SIZE = 40.0f
        private const val STROKE_WIDTH = 1.0f
    }

    // Properties for storing text and paint objects
    var text: String = ""
    private val rectPaint: Paint = Paint().apply {
        color = TEXT_COLOR
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
    }
    private val textPaint: Paint = Paint().apply {
        color = TEXT_COLOR
        textSize = TEXT_SIZE
    }
    fun setTranslatedText(translatedText: String) {
        this.text = translatedText
    }
    fun getTranslatedText(): String {
        return text
    }

    fun getElementText(): String {
        return element.text
    }
    // Method to update the text content
    fun updateText(newText: String) {
        text = newText
    }

    override fun draw(canvas: Canvas?) {
        if (element == null) {
            throw IllegalStateException("Attempting to draw a null text.")
        }

        // Get the bounding box of the text element
        val boundingBox = element.boundingBox

        // Adjust the bounding box to fit within the screen boundaries
        val adjustedBoundingBox = adjustBoundingBox(boundingBox!!, canvas?.width ?: 0, canvas?.height ?: 0)

        // Calculate the width of the text element
        val textWidth = textPaint.measureText(text)

        // Adjust the width of the bounding box based on the text width
        adjustedBoundingBox.right = adjustedBoundingBox.left + textWidth.toInt()

        // Draw semi-transparent background rectangle
        val backgroundPaint = Paint().apply {
            color = Color.parseColor("#80FFFFFF") // Light color with reduced opacity
            style = Paint.Style.FILL
        }
        canvas?.drawRect(adjustedBoundingBox, backgroundPaint)

        // Calculate the new left position for the text to be horizontally centered
        val newX = adjustedBoundingBox.left

        // Calculate the height of the text
        val textHeight = textPaint.textSize

        // Calculate the baseline of the text to align it vertically centered
        val baseline = adjustedBoundingBox.centerY() + (textHeight / 2)

        // Draws the adjusted bounding box around the TextBlock
        canvas?.drawRect(adjustedBoundingBox, rectPaint)

        // Renders the text at the center of the adjusted bounding box
        canvas?.drawText(text, newX.toFloat(), baseline, textPaint)
    }



    private fun adjustBoundingBox(boundingBox: Rect, maxWidth: Int, maxHeight: Int): Rect {
        var left = boundingBox.left
        var top = boundingBox.top
        var right = boundingBox.right
        var bottom = boundingBox.bottom

        // Adjust left and right coordinates if they extend beyond the screen boundaries
        if (left < 0) {
            right -= left // Subtract the overflow from the right
            left = 0 // Set left to 0
        }
        if (right > maxWidth) {
            left -= (right - maxWidth) // Shift left to fit within the screen
            right = maxWidth // Set right to the maximum width
        }

        // Adjust top and bottom coordinates if they extend beyond the screen boundaries
        if (top < 0) {
            bottom -= top // Subtract the overflow from the bottom
            top = 0 // Set top to 0
        }
        if (bottom > maxHeight) {
            top -= (bottom - maxHeight) // Shift top to fit within the screen
            bottom = maxHeight // Set bottom to the maximum height
        }

        return Rect(left, top, right, bottom)
    }
}
