package com.example.ai_virtual_assistance.ui.home;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.ai_virtual_assistance.MainActivity;

import java.io.IOException;
import java.util.Arrays;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {
    private static final String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        setOnTouchListener(this); // Set the touch listener
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
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
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            handleTouch(event);
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            ((MainActivity) getContext()).speech();
            return true;
        }
        return false;
    }

    private void handleTouch(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Log.d(TAG, "Touched at: (" + x + ", " + y + ")");
        // Implement your custom functionality here
        // For example, you can use the touch coordinates to focus the camera
        focusOnTouch((int) x, (int) y);
    }

    private void focusOnTouch(int x, int y) {
        Camera.Parameters params = mCamera.getParameters();
        if (params.getMaxNumFocusAreas() > 0) {
            Camera.Area focusArea = new Camera.Area(calculateFocusArea(x, y), 1000);
            params.setFocusAreas(Arrays.asList(focusArea));
            mCamera.setParameters(params);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    // Do something when focus is achieved or failed
                }
            });
        } else {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    // Do something when focus is achieved or failed
                }
            });
        }
    }

    private Rect calculateFocusArea(int x, int y) {
        int left = clamp(x - 100, 0, 2000) - 1000;
        int top = clamp(y - 100, 0, 2000) - 1000;
        int right = clamp(x + 100, 0, 2000) - 1000;
        int bottom = clamp(y + 100, 0, 2000) - 1000;
        return new Rect(left, top, right, bottom);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}