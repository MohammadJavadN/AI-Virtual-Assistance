package com.example.ai_virtual_assistance.ui.home;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LabelUtils {
    public static List<String> loadLabels(Context context, String fileName) throws IOException {
        List<String> labels = new ArrayList<>();
        InputStream is = context.getAssets().open(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        reader.close();
        return labels;
    }
}