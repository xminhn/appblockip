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

public class ManualInputFragment extends Fragment {
    private EditText editMac, editIp;
    private Button btnAdd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manual_input, container, false);
        editMac = view.findViewById(R.id.edit_mac);
        editIp = view.findViewById(R.id.edit_ip);
        btnAdd = view.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(v -> addManualEntry());
        return view;
    }

    private void addManualEntry() {
        String mac = editMac.getText().toString().trim();
        String ip = editIp.getText().toString().trim();

        if (mac.isEmpty() && ip.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập MAC hoặc IP", Toast.LENGTH_SHORT).show();
            return;
        }

        String endpoint = !mac.isEmpty() ? "https://192.168.1.1:4433/add_mac" : "https://192.168.1.1:4433/add_ip";
        String data = !mac.isEmpty() ? mac : ip;

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(new SSLSocketFactoryImpl(), new X509TrustManagerImpl())
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
            RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), data);
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(requestBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                        editMac.setText("");
                        editIp.setText("");
                    });
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Thêm thất bại", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}