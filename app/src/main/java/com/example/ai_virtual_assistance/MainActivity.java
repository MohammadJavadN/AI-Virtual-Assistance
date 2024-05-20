package com.example.ai_virtual_assistance;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ai_virtual_assistance.ui.home.CameraPreview;
import com.example.ai_virtual_assistance.ui.home.MyRecognitionListener;
import com.example.ai_virtual_assistance.ui.home.ServerConnection;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ServerConnection.OnMessageReceived, TextToSpeech.OnInitListener {

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Camera mCamera;
    private CameraPreview mPreview;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private Intent intent;
    private ServerConnection serverConnection;
    private MyRecognitionListener recognitionListener;

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            Log.d("MainActivity", "Camera is not available: " + e.getMessage());
        }
        return c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();
//        speak("Hello, how are you?");
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, INTERNET) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA, RECORD_AUDIO, INTERNET},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            setupTTS();
            setupCamera();
        }
    }

    private void setupTTS() {
        // Initialize TTS with eSpeak NG
        tts = new TextToSpeech(this, this, "com.google.android.tts");
//        tts = new TextToSpeech(this, this);

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ur"); // Farsi language code
//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something");

        serverConnection = new ServerConnection("your.server.address", 12345, this);
//            serverConnection.connect();

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognitionListener = new MyRecognitionListener(speechRecognizer, serverConnection);
        speechRecognizer.setRecognitionListener(recognitionListener);

    }

    @Override
    public void onInit(int status) {
        int result;
        if (status == TextToSpeech.SUCCESS) {
//            int result = tts.setLanguage(new Locale("fa_IR"));
            result = tts.setLanguage(new Locale("ur"));
//            result = tts.setLanguage(Locale.UK);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported or missing data");
                Log.e("TTS", "Create new intent");
                // Handle the error
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
                Log.e("TTS", "activity started");
            }
            else {
                Log.d("TTS", "Initialization successful");
                speak("سلام خوش آمدید. چطور می توانم به شما کمک کنم؟");
            }
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }

    private void speak(String text) {
        if (tts != null && !tts.getEngines().isEmpty()) {
            releaseCamera();
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            Log.e("TTS", "TTS engine is not initialized or not available");
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
        serverConnection.disconnect();
    }

    @Override
    public void messageReceived(String message) {
        runOnUiThread(() -> {
            String newMessage = recognitionListener.createMessage(message);
            tts.speak(newMessage, TextToSpeech.QUEUE_FLUSH, null, null);
        });
    }

    public void listenToSpeak() {
        if (tts.isSpeaking()) {
            tts.stop();
            return;
        }
        releaseCamera();
        System.out.println("### in speech");
        speechRecognizer.startListening(intent);
    }

    public void stopListening(){
        speechRecognizer.stopListening();
    }

    private void setupCamera() {
        // Open the camera and create a CameraPreview instance
        mCamera = getCameraInstance();
        if (mCamera != null) {
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera = getCameraInstance();
            if (mCamera != null) {
                mPreview.setCamera(mCamera);
                mCamera.startPreview();
            }
        }
    }
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupTTS();
                setupCamera();
            } else {
                // Permission denied
                Log.d("MainActivity", "Camera permission denied");
                getPermission();
            }
        }
    }
}
