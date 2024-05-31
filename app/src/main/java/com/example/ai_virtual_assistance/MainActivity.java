package com.example.ai_virtual_assistance;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ai_virtual_assistance.ui.home.CameraPreview;
import com.example.ai_virtual_assistance.ui.home.MyRecognitionListener;
import com.example.ai_virtual_assistance.ui.home.OverlayView;
import com.example.ai_virtual_assistance.ui.home.ServerConnection;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
public class MainActivity extends AppCompatActivity implements ServerConnection.OnMessageReceived, TextToSpeech.OnInitListener {

//    String serverAddress = ""
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private Intent intent;
    private ServerConnection serverConnection;
    private MyRecognitionListener recognitionListener;

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
            } else {
                Log.d("TTS", "Initialization successful");
                speak("سلام خوش آمدید. چطور می توانم به شما کمک کنم؟");
            }
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }

    private void speak(String text) {
        if (tts != null && !tts.getEngines().isEmpty()) {
            System.out.println("### speak");
//            releaseCamera();
            mCameraPreview.releaseCamera();
            System.out.println("### speak releaseCamera");
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            System.out.println("### speak speak");
//            onResume();
            System.out.println("### speak onResume");
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
        startRecording();
    }

    public void stopListening() {
        stopRecording();
    }

    private CameraPreview mCameraPreview;
    private OverlayView mOverlayView;

    private void setupCamera() {
        mCameraPreview = findViewById(R.id.camera_preview);
        mOverlayView = findViewById(R.id.overlay_view);

        mCameraPreview.setOverlayView(mOverlayView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraPreview != null)
            mCameraPreview.releaseCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCameraPreview != null)
            mCameraPreview.setupCamera();
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
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE,
            CHANNEL_CONFIG, AUDIO_FORMAT);

    private AudioRecord recorder;
    private boolean isRecording = false;

    @SuppressLint("MissingPermission")
    private void startRecording() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);

        recorder.startRecording();
        isRecording = true;

        new Thread(new AudioStreamer()).start();
    }

    private void stopRecording() {
        if (recorder != null) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    private class AudioStreamer implements Runnable {
        @Override
        public void run() {
            try {
                Socket socket = new Socket("192.168.43.226", 12345);
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[BUFFER_SIZE];

                while (isRecording) {
                    int read = recorder.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        outputStream.write(buffer, 0, read);
                    }
                }

                // Signal the server that the audio data has been completely sent
                socket.shutdownOutput();

                // Read response from the server
                ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
                byte[] responseChunk = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(responseChunk)) != -1) {
                    responseBuffer.write(responseChunk, 0, bytesRead);
                }

                String response = responseBuffer.toString("UTF-8");

                // Handle the server's response
                System.out.println(response);

                inputStream.close();
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
