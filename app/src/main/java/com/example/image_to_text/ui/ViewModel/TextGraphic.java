
package com.example.image_to_text.ui.ViewModel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.google.mlkit.vision.text.Text;
public class TextGraphic extends GraphicOverlay.Graphic {

    private static final String TAG = "TextGraphic";
    private static final int TEXT_COLOR = Color.RED;
    private static final float TEXT_SIZE = 30.0f;
    private static final float STROKE_WIDTH = 1.0f;

    private final Paint rectPaint;
    private final Paint textPaint;
    private final Text.Element element;


    public TextGraphic(GraphicOverlay overlay, Text.Element element) {
        super(overlay);

        this.element = element;

        rectPaint = new Paint();
        rectPaint.setColor(TEXT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);
        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Log.d(TAG, "on draw text graphic");
        if (element == null) {
            throw new IllegalStateException("Attempting to draw a null text.");
        }

        // Get the bounding box of the text element
        Rect boundingBox = element.getBoundingBox();

        // Adjust the bounding box to fit within the screen boundaries
        Rect adjustedBoundingBox = adjustBoundingBox(boundingBox, canvas.getWidth(), canvas.getHeight());

        // Calculate the width of the text element
        float textWidth = textPaint.measureText(element.getText());

        // Calculate the difference between the text width and the bounding box width
        float widthDifference = adjustedBoundingBox.width() - textWidth;

        // Calculate the new left position for the text to be horizontally centered
        float newX = adjustedBoundingBox.left + (widthDifference / 2);

        // Calculate the height of the text
        float textHeight = textPaint.getTextSize();

        // Calculate the baseline of the text to align it vertically centered
        float baseline = adjustedBoundingBox.centerY() + (textHeight / 2);

        // Draws the adjusted bounding box around the TextBlock
        canvas.drawRect(adjustedBoundingBox, rectPaint);

        // Renders the text at the center of the adjusted bounding box
        canvas.drawText(element.getText(), newX, baseline, textPaint);
    }

    private Rect adjustBoundingBox(Rect boundingBox, int maxWidth, int maxHeight) {
        int left = boundingBox.left;
        int top = boundingBox.top;
        int right = boundingBox.right;
        int bottom = boundingBox.bottom;

        // Adjust left and right coordinates if they extend beyond the screen boundaries
        if (left < 0) {
            right -= left; // Subtract the overflow from the right
            left = 0; // Set left to 0
        }
        if (right > maxWidth) {
            left -= (right - maxWidth); // Shift left to fit within the screen
            right = maxWidth; // Set right to the maximum width
        }

        // Adjust top and bottom coordinates if they extend beyond the screen boundaries
        if (top < 0) {
            bottom -= top; // Subtract the overflow from the bottom
            top = 0; // Set top to 0
        }
        if (bottom > maxHeight) {
            top -= (bottom - maxHeight); // Shift top to fit within the screen
            bottom = maxHeight; // Set bottom to the maximum height
        }

        return new Rect(left, top, right, bottom);
    }


}
