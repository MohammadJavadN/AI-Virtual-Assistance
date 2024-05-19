package com.example.ai_virtual_assistance.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ai_virtual_assistance.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TextView gptTextView;
    private EditText editText;

    private String stringURLEndPoint = "https://api.openai.com/v1/chat/completions";
    private String stringAPIKey = "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";
    private String stringOutput = "";

    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private Intent intent;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);


        gptTextView = binding.gptTextView;
        editText = binding.editText;
        Button assistButton = binding.assistButton;

        assistButton.setOnClickListener(e -> buttonAssist());

        // Verify that the ID R.id.editText is correct and exists in the header layout
        textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                textToSpeech.setSpeechRate((float) 0.8);
            }
        });

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR"); // Farsi language code
//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something");

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
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
                    editText.setText(string);
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
                System.out.println("RecognitionListener->onEvent");
            }
        });
        return root;
    }

    private void sendMessageToServer(String inputMessage) {
        String url = "http://127.0.0.1:5000//get_response?input=" + Uri.encode(inputMessage);
    
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String serverResponse = response.getString("response");
                        gptTextView.setText(serverResponse);
                        textToSpeech.speak(serverResponse, TextToSpeech.QUEUE_FLUSH, null, null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        gptTextView.setText("Error parsing response.");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    gptTextView.setText("Server error: " + error.toString());
                }
            });
    
        Volley.newRequestQueue(getContext()).add(jsonObjectRequest);
    }

    public void buttonAssist() {
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
            return;
        }
        stringOutput = "";
        speechRecognizer.startListening(intent);
    }

    private void chatGPTModel(String stringInput) {
        gptTextView.setText("In Progress ...");
        textToSpeech.speak("In Progress", TextToSpeech.QUEUE_FLUSH, null, null);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("model", "gpt-3.5-turbo");

            JSONArray jsonArrayMessage = new JSONArray();
            JSONObject jsonObjectMessage = new JSONObject();
            jsonObjectMessage.put("role", "user");
            jsonObjectMessage.put("content", stringInput);
            jsonArrayMessage.put(jsonObjectMessage);

            jsonObject.put("messages", jsonArrayMessage);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                stringURLEndPoint, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String stringText = null;
                try {
                    stringText = response.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                stringOutput = stringOutput + stringText;
                gptTextView.setText(stringOutput);
                textToSpeech.speak(stringOutput, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> mapHeader = new HashMap<>();
                mapHeader.put("Authorization", "Bearer " + stringAPIKey);
                mapHeader.put("Content-Type", "application/json");

                return mapHeader;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };

        int intTimeoutPeriod = 60000; // 60 seconds timeout duration defined
        RetryPolicy retryPolicy = new DefaultRetryPolicy(intTimeoutPeriod,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        Volley.newRequestQueue(getContext()).add(jsonObjectRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
