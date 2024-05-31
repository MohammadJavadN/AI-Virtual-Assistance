package com.example.ai_virtual_assistance.ui.home;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.objects.DetectedObject;

import java.util.ArrayList;
import java.util.List;


public class OverlayView extends View {
    private List<DetectedObject> detectedObjects;
    private Paint paint;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8.0f);
    }

    public void setDetectedObjects(List<DetectedObject> detectedObjects) {
        this.detectedObjects = detectedObjects;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (detectedObjects != null) {
            for (DetectedObject detectedObject : detectedObjects) {
                for (DetectedObject.Label label : detectedObject.getLabels()) {
                    canvas.drawRect(detectedObject.getBoundingBox(), paint);
                    canvas.drawText(label.getText(), detectedObject.getBoundingBox().left, detectedObject.getBoundingBox().top, paint);
                }
            }
        }
    }
}