package com.example.ai_virtual_assistance.ui.home;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.ai_virtual_assistance.MainActivity;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.PredefinedCategory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SuppressLint("ViewConstructor")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback,
        Camera.PreviewCallback, View.OnTouchListener {
    private static final String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    public static boolean isCameraReleased = false;
    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        setOnTouchListener(this); // Set the touch listener
        if (objectDetector == null)
            setupObjectDetector();
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            mCamera.setPreviewCallback(this); // TODO: 31.05.24
            mCamera.setDisplayOrientation(90);
            isCameraReleased = false;
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // Ignore: tried to stop a non-existent preview
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d(TAG, " onPreviewFrame");
        if (mCamera == null || isCameraReleased) {
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        InputImage image = InputImage.fromByteArray(data, size.width, size.height, 90, ImageFormat.NV21);
        processImage(image);
    }

    private void processImage(InputImage image) {
        Log.d(TAG, " processImage");

        objectDetector.process(image)
                .addOnSuccessListener(detectedObjects -> {
                    Log.d(TAG, " detectedObjects");
                    StringBuilder result = new StringBuilder();
                    for (DetectedObject detectedObject : detectedObjects) {
                        result.append("Object detected at ")
                                .append(detectedObject.getBoundingBox().toString())
                                .append("\n");
                        
                        // Get the labels (categories) for the detected object
                        List<DetectedObject.Label> labels = detectedObject.getLabels();
                        for (DetectedObject.Label label : labels) {
                            result.append("Label: ")
                                    .append(label.getText())
                                    .append(", Confidence: ")
                                    .append(label.getConfidence())
                                    .append("\n");
                        }
                    }
//                    mTextView.setText(result.toString()); todo
                    Log.d(TAG, "Object detection result: " + result.toString());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Object detection failed: " + e.getMessage()));
    }

    private static ObjectDetector objectDetector = null;
    private void setupObjectDetector() {
        Log.d(TAG, " setupObjectDetector");

        // Configure the object detector
        ObjectDetectorOptions options = new ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                .enableClassification()  // Optional: Enable classification
                .build();
        objectDetector = ObjectDetection.getClient(options);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            ((MainActivity) getContext()).stopListening();
            ((MainActivity) getContext()).onResume(); // TODO: 31.05.24 uncomment
            return true;
        } else
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            ((MainActivity) getContext()).releaseCamera();
            ((MainActivity) getContext()).listenToSpeak();
            return true;
        }
        return false;
    }

}