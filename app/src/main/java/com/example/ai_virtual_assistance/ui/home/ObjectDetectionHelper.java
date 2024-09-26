package com.example.ai_virtual_assistance.ui.home;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import com.example.ai_virtual_assistance.ml.SsdMobilenetV1;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ObjectDetectionHelper {
    private static final String TAG = "ObjectDetectionHelper";
    private final SsdMobilenetV1 model;
    private final int inputImageWidth = 300;
    private final int inputImageHeight = 300;

    private List<String> labels;
    public ObjectDetectionHelper(Context context) throws IOException {
        model = SsdMobilenetV1.newInstance(context);
        try {
            // Load labels from assets
            labels = LabelUtils.loadLabels(context, "labels.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static ByteBuffer bitmapToByteBuffer(Bitmap bitmap, int modelInputSize) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * modelInputSize * modelInputSize * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        // Resize the bitmap to the expected input size
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, modelInputSize, modelInputSize, true);

        // Preprocess the bitmap and put the data into the ByteBuffer
        int[] intValues = new int[modelInputSize * modelInputSize];
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());

        // Normalize and convert pixel values to float
        int pixel = 0;
        for (int i = 0; i < modelInputSize; ++i) {
            for (int j = 0; j < modelInputSize; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f)); // Red
                byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));  // Green
                byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));         // Blue
            }
        }

        return byteBuffer;
    }
    public List<Detection> detectObjects(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true);
        TensorImage tensorImage = TensorImage.fromBitmap(resizedBitmap);

        // Ensure the byte buffer size matches the model's expected input tensor size
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, inputImageWidth, inputImageHeight, 3}, DataType.FLOAT32);
//        ByteBuffer byteBuffer = tensorImage.getBuffer();
        ByteBuffer byteBuffer = bitmapToByteBuffer(bitmap, inputImageWidth);
        inputFeature0.loadBuffer(byteBuffer);

        // Run model inference
        SsdMobilenetV1.Outputs outputs = null;
        try {
            outputs = model.process(inputFeature0);
        } catch (Exception e) {
            Log.e(TAG, "Error running model inference", e);
            return new ArrayList<>();
        }

        TensorBuffer outBBoxes = outputs.getOutputFeature0AsTensorBuffer();
        TensorBuffer outputFeature1 = outputs.getOutputFeature1AsTensorBuffer();
        TensorBuffer outputFeature2 = outputs.getOutputFeature2AsTensorBuffer();
        TensorBuffer outputFeature3 = outputs.getOutputFeature3AsTensorBuffer();
        System.out.println("00000000000000000000000000000000000000000000");
        System.out.println("+++ getDataType: " + outBBoxes.getDataType());
        System.out.println("+++ shape: " + Arrays.toString(outBBoxes.getShape()));
        System.out.println("+++ getFloatArray: " + Arrays.toString(outBBoxes.getFloatArray()));

        System.out.println("1111111111111111111111111111111111111111111");
        System.out.println("+++ getDataType: " + outputFeature1.getDataType());
        System.out.println("+++ shape: " + Arrays.toString(outputFeature1.getShape()));
        System.out.println("+++ getFloatArray: " + Arrays.toString(outputFeature1.getFloatArray()));

        System.out.println("22222222222222222222222222222222222222222222222");
        System.out.println("+++ getDataType: " + outputFeature2.getDataType());
        System.out.println("+++ shape: " + Arrays.toString(outputFeature2.getShape()));
        System.out.println("+++ getFloatArray: " + Arrays.toString(outputFeature2.getFloatArray()));

        System.out.println("3333333333333333333333333333333333333333333333333");
        System.out.println("+++ getDataType: " + outputFeature3.getDataType());
        System.out.println("+++ shape: " + Arrays.toString(outputFeature3.getShape()));
        System.out.println("+++ getFloatArray: " + Arrays.toString(outputFeature3.getFloatArray()));
        return parseDetectionResults(outBBoxes, outputFeature1, outputFeature2, outputFeature3, bitmap.getWidth(), bitmap.getHeight());
    }

    private List<Detection> parseDetectionResults(TensorBuffer outBBoxes, TensorBuffer outputFeature1, TensorBuffer outputFeature2, TensorBuffer outputFeature3, int originalWidth, int originalHeight) {
        List<Detection> detections = new ArrayList<>();

        // Parse the outBBoxes to get bounding boxes, classes, and scores.
        // This is an example and might need adjustment based on the actual model output format.
        float[] boxes = outBBoxes.getFloatArray();
        float[] classes = outputFeature1.getFloatArray();
        float[] scores = outputFeature2.getFloatArray();
        float[] numDetections = outputFeature3.getFloatArray();

        int numDetectedObjects = (int) numDetections[0];
        for (int i = 0; i < numDetectedObjects; i++) {
            int offset = i * 4;
//            float left = boxes[offset + 1] * originalWidth;
//            float top = boxes[offset] * originalHeight;
//            float right = boxes[offset + 3] * originalWidth;
//            float bottom = boxes[offset + 2] * originalHeight;
            float left = boxes[offset + 1];
            float top = boxes[offset];
            float right = boxes[offset + 3];
            float bottom = boxes[offset + 2];
            RectF boundingBox = new RectF(left, top, right, bottom);

            int detectedClass = (int) classes[i];
            float score = scores[i];
            String detectedLabel = labels.get(detectedClass);
            detections.add(new Detection(boundingBox, detectedLabel, score));
        }

        return detections;
    }

    public static class Detection {
        private final RectF boundingBox;
        private final String detectedLabel;
        private final float score;

        public Detection(RectF boundingBox, String detectedLabel, float score) {
            this.boundingBox = boundingBox;
            this.detectedLabel = detectedLabel;
            this.score = score;
        }

        public RectF getBoundingBox() {
            return boundingBox;
        }

        public String getDetectedClass() {
            return detectedLabel;
        }

        public float getScore() {
            return score;
        }
    }

    public void close() {
        model.close();
    }
}