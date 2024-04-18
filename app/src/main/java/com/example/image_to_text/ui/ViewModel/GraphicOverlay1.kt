package com.example.image_to_text.ui.ViewModel

import android.content.Context
import android.graphics.Canvas
import android.hardware.camera2.CameraCharacteristics
import android.util.AttributeSet
import android.view.View

class GraphicOverlay1(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    private val lock = Any()
    private var previewWidth = 0
    private var widthScaleFactor = 1.0f
    private var previewHeight = 0
    private var heightScaleFactor = 1.0f
    private var facing = CameraCharacteristics.LENS_FACING_BACK
    private val graphics: MutableSet<Graphic> = HashSet()

    abstract class Graphic(private val overlay: GraphicOverlay1) {
        abstract fun draw(canvas: Canvas?)
        fun scaleX(horizontal: Float): Float {
            return horizontal * overlay.widthScaleFactor
        }
        fun scaleY(vertical: Float): Float {
            return vertical * overlay.heightScaleFactor
        }

        val applicationContext: Context
            get() = overlay.context.applicationContext

        fun translateX(x: Float): Float {
            return if (overlay.facing == CameraCharacteristics.LENS_FACING_FRONT) {
                overlay.width - scaleX(x)
            } else {
                scaleX(x)
            }
        }

        fun translateY(y: Float): Float {
            return scaleY(y)
        }

        fun postInvalidate() {
            overlay.postInvalidate()
        }
    }
    fun getTextGraphics(): List<TextGraphic1> {
        return graphics.filterIsInstance<TextGraphic1>()
    }
    fun clear() {
        synchronized(lock) { graphics.clear() }
        postInvalidate()
    }
    fun add(graphic: Graphic) {
        synchronized(lock) { graphics.add(graphic) }
        postInvalidate()
    }
    fun remove(graphic: Graphic) {
        synchronized(lock) { graphics.remove(graphic) }
        postInvalidate()
    }
    fun setTranslatedTextToOverlay(translatedText: String) {
        // Get all text elements from the GraphicOverlay
        val textGraphics = graphics.filterIsInstance<TextGraphic1>()
        // Set translated text to each text element in the GraphicOverlay
        textGraphics.forEach { graphic ->
            graphic.text = translatedText
        }
        // Refresh the overlay to reflect the changes
        postInvalidate()
    }
    fun getTranslatedText(): String {
        val textGraphics = getTextGraphics()
        val translatedText = StringBuilder()
        for (graphic in textGraphics) {
            val translatedWord = graphic.getTranslatedText() // Assuming you have a method to get translated text in TextGraphic1
            translatedText.append(translatedWord).append(" ")
        }
        return translatedText.toString().trim()
    }

    fun addTranslatedWordToOverlay(translatedWord: String, index: Int) {
        synchronized(lock) {
            if (index < graphics.size) {
                val graphic = graphics.elementAt(index)
                if (graphic is TextGraphic1) {
                    graphic.setTranslatedText(translatedWord)
                    postInvalidate()
                } else {
                    // Handle if the graphic is not of type TextGraphic1
                }
            } else {
                // Handle if the index is out of bounds
            }
        }
    }

    fun updateTextForTextGraphics(translatedText: String) {
        // Get all text graphics from the GraphicOverlay
        val textGraphics = graphics.filterIsInstance<TextGraphic1>()
        // Set translated text to each text graphic in the GraphicOverlay
        textGraphics.forEach { graphic ->
            graphic.updateText(translatedText)
        }
        // Refresh the overlay to reflect the changes
        postInvalidate()
    }
    /**
     * Sets the camera attributes for size and facing direction, which informs how to transform image
     * coordinates later.
     */
    fun setCameraInfo(previewWidth: Int, previewHeight: Int, facing: Int) {
        synchronized(lock) {
            this.previewWidth = previewWidth
            this.previewHeight = previewHeight
            this.facing = facing
        }
        postInvalidate()
    }

    /**
     * Draws the overlay with its associated graphic objects.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(lock) {
            if (previewWidth != 0 && previewHeight != 0) {
                widthScaleFactor = width.toFloat() / previewWidth.toFloat()
                heightScaleFactor = height.toFloat() / previewHeight.toFloat()
            }
            for (graphic in graphics) {
                graphic.draw(canvas)
            }
        }
    }
}