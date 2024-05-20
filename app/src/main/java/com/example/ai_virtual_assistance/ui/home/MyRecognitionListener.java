package com.example.ai_virtual_assistance.ui.home;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

public class MyRecognitionListener implements RecognitionListener {
    private final SpeechRecognizer speechRecognizer;
    private final ServerConnection serverConnection;

    public MyRecognitionListener(SpeechRecognizer speechRecognizer, ServerConnection serverConnection) {
        this.speechRecognizer = speechRecognizer;
        this.serverConnection = serverConnection;
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        System.out.println("RecognitionListener->onReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {
        System.out.println("RecognitionListener->onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float v) {
        System.out.println("RecognitionListener->onRmsChanged");
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        System.out.println("RecognitionListener->onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        System.out.println("RecognitionListener->onEndOfSpeech");
    }

    @Override
    public void onError(int i) {
        System.out.println("RecognitionListener->onError");
    }

    @Override
    public void onResults(Bundle bundle) {
        System.out.println("RecognitionListener->onResults");
        ArrayList<String> matches = bundle.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
        if (matches != null) {
            System.out.println(matches);
            String string = matches.get(0);
            System.out.println(string);
//                    editText.setText(string);
//                    chatGPTModel(string);
            String message = createMessage(string);
            // TODO: 20.05.24:
//            serverConnection.sendMessage(message);
        } else {
            System.out.println("matches is null");
        }
    }

    public String createMessage(String string) {
        // TODO: 20.05.24
        return "";
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        System.out.println("RecognitionListener->onPartialResults");
    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        System.out.println("RecognitionListener->onEvent, i= " + i);
    }
}
