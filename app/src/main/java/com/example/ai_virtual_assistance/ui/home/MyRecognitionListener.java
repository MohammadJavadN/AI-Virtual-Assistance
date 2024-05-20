package com.example.ai_virtual_assistance.ui.home;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

public class MyRecognitionListener implements RecognitionListener {
    private final SpeechRecognizer speechRecognizer;

    public MyRecognitionListener(SpeechRecognizer speechRecognizer) {
        this.speechRecognizer = speechRecognizer;
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
        } else {
            System.out.println("matches is null");
        }
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
