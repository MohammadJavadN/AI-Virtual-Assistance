package com.example.ai_virtual_assistance.ui.home;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.AttributeSet;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@SuppressLint("ViewConstructor")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback,
        Camera.PreviewCallback, View.OnTouchListener {
    private static final String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    public static boolean isCameraReleased = false;
    private OverlayView mOverlayView;

    public CameraPreview(Context context) {
        super(context);
        init(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    private MainActivity mActivity;
    private void init(Context context) {
        mHolder = getHolder();
        mHolder.addCallback(this);
        setOnTouchListener(this); // Set the touch listener
        mActivity = (MainActivity) context;
//        if (objectDetector == null)
//            setupObjectDetector();
    }

    public void setOverlayView(OverlayView overlayView) {
        mOverlayView = overlayView;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setupCamera();
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
        releaseCamera();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new android.graphics.Rect(0, 0, previewSize.width, previewSize.height), 100, out);
        byte[] imageBytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        // Process the captured frame
        mActivity.processFrame(bitmap);
    }
//    @Override
//    public void onPreviewFrame(byte[] data, Camera camera) {
//        Log.d(TAG, " onPreviewFrame");
//        if (mCamera == null || isCameraReleased) {
//            return;
//        }
//        Camera.Parameters parameters = camera.getParameters();
//        Camera.Size size = parameters.getPreviewSize();
////        InputImage image = InputImage.fromByteArray(data, size.width, size.height, 90, ImageFormat.NV21);
////        processImage(image);
//        Bitmap bitmap = getBitmapFromByteArray(data, size.width, size.height);
//        mActivity.processFrame(bitmap);
//    }
    private Bitmap getBitmapFromByteArray(byte[] data, int width, int height) {
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, out);
        byte[] bytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public void setupCamera() {
        if (isCameraReleased) {
            releaseCamera();
        }
        mCamera = getCameraInstance();
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.setPreviewCallback(this);
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();
                isCameraReleased = false;
            } catch (IOException e) {
                Log.e("CameraPreview", "Error setting up camera preview", e);
            }
        }
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            isCameraReleased = true;
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            Log.e("CameraPreview", "Camera is not available", e);
        }
        return c;
    }
//    private void processImage(InputImage image) {
//        objectDetector.process(image)
//                .addOnSuccessListener(detectedObjects -> {
//                    if (mOverlayView != null) {
//                        mOverlayView.setDetectedObjects(detectedObjects);
//                    }
//                })
//                .addOnFailureListener(e -> Log.e("CameraPreview", "Object detection failed", e));
//    }
//    private void processImage2(InputImage image) {
//        Log.d(TAG, " processImage");
//
//        objectDetector.process(image)
//                .addOnSuccessListener(detectedObjects -> {
//                    Log.d(TAG, " detectedObjects");
//                    StringBuilder result = new StringBuilder();
//                    for (DetectedObject detectedObject : detectedObjects) {
//                        result.append("Object detected at ")
//                                .append(detectedObject.getBoundingBox().toString())
//                                .append("\n");
//
//                        // Get the labels (categories) for the detected object
//                        List<DetectedObject.Label> labels = detectedObject.getLabels();
//                        for (DetectedObject.Label label : labels) {
//                            result.append("Label: ")
//                                    .append(label.getText())
//                                    .append(", Confidence: ")
//                                    .append(label.getConfidence())
//                                    .append("\n");
//                        }
//                    }
////                    mTextView.setText(result.toString()); todo
//                    Log.d(TAG, "Object detection result: " + result.toString());
//                })
//                .addOnFailureListener(e -> Log.e(TAG, "Object detection failed: " + e.getMessage()));
//    }

//    private static ObjectDetector objectDetector = null;
//    private void setupObjectDetector() {
//        Log.d(TAG, " setupObjectDetector");
//
//        // Configure the object detector
//        ObjectDetectorOptions options = new ObjectDetectorOptions.Builder()
//                .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
//                .enableClassification()  // Optional: Enable classification
//                .build();
//        objectDetector = ObjectDetection.getClient(options);
//    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            ((MainActivity) getContext()).stopListening();
            ((MainActivity) getContext()).onResume(); // TODO: 31.05.24 uncomment
            return true;
        } else
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            releaseCamera();
            ((MainActivity) getContext()).listenToSpeak();
            return true;
        }
        return false;
    }

}