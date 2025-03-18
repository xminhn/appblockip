package com.example.blockweb;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.blockweb.utils.SSLSocketFactoryImpl;
import com.example.blockweb.utils.X509TrustManagerImpl;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UrlInputFragment extends Fragment {
    private EditText editUrl, editDuration;
    private Button btnAddUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_url_input, container, false);
        editUrl = view.findViewById(R.id.edit_url);
        editDuration = view.findViewById(R.id.edit_duration);
        btnAddUrl = view.findViewById(R.id.btn_add_url);
        btnAddUrl.setOnClickListener(v -> addUrl());
        return view;
    }

    private void addUrl() {
        String url = editUrl.getText().toString().trim();
        String durationStr = editDuration.getText().toString().trim();

        if (url.isEmpty() || durationStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr);
            long endTime = System.currentTimeMillis() / 1000 + duration;
            String body = url + "," + endTime;

            new Thread(() -> {
                OkHttpClient client = new OkHttpClient.Builder()
                        .sslSocketFactory(new SSLSocketFactoryImpl(), new X509TrustManagerImpl())
                        .hostnameVerifier((hostname, session) -> true)
                        .build();
                RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), body);
                Request request = new Request.Builder()
                        .url("https://192.168.1.1:4433/add_url")
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "URL added successfully", Toast.LENGTH_SHORT).show();
                            editUrl.setText("");
                            editDuration.setText("");
                        });
                    } else {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to add URL", Toast.LENGTH_SHORT).show());
                    }
                } catch (IOException e) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show());
                }
            }).start();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Thời gian phải là số", Toast.LENGTH_SHORT).show();
        }
    }
}