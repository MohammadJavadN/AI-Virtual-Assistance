package com.example.ai_virtual_assistance.ui.home;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TranslateTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {
        String textToTranslate = params[0];
        String targetLang = params[1];
        String response = "";
        HttpURLConnection urlConnection = null;

        try {
            // URL of the Flask server
            URL url = new URL("http://<server-ip>:5000/translate");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoOutput(true);

            // JSON payload
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("text", textToTranslate);
            jsonParam.put("target_lang", targetLang);

            // Send the JSON request
            try (OutputStream os = urlConnection.getOutputStream()) {
                byte[] input = jsonParam.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read the response
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream(), "utf-8"))) {
                StringBuilder responseBuilder = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    responseBuilder.append(responseLine.trim());
                }
                response = responseBuilder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        // Handle the server response here

    }
}
