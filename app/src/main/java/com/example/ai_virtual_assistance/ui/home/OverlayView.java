package com.example.ai_virtual_assistance.ui.home;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.objects.DetectedObject;

import java.util.ArrayList;
import java.util.List;


public class OverlayView extends View {
    private final Paint paint;
    private List<ObjectDetectionHelper.Detection> detections;
    float w, h;
    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4.0f);
        detections = new ArrayList<>();
        w = getWidth();
        h = getHeight();
    }

    public void setDetectedObjects(List<ObjectDetectionHelper.Detection> detections) {
        this.detections = detections;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (ObjectDetectionHelper.Detection detection : detections) {
            w = getWidth();
            h = getHeight();
            RectF b = detection.getBoundingBox();
            @SuppressLint("DrawAllocation") RectF boundingBox = new RectF(b.left * w, b.top * h, b.right * w, b.bottom * h);

            canvas.drawRect(boundingBox, paint);
            paint.setTextSize(50.0f);
            canvas.drawText("Class: " + detection.getDetectedClass() + ", Score: " + detection.getScore(), boundingBox.left, boundingBox.top, paint);
        }
    }
}