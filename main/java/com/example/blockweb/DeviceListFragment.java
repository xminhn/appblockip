package com.example.blockweb;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.blockweb.utils.SSLSocketFactoryImpl;
import com.example.blockweb.utils.X509TrustManagerImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeviceListFragment extends Fragment {
    private static final String TAG = "DeviceListFragment";
    private ListView listView;
    private DeviceAdapter adapter;
    private List<Device> devices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);
        listView = view.findViewById(R.id.list_devices);

        devices = new ArrayList<>();
        adapter = new DeviceAdapter(getActivity(), devices);
        listView.setAdapter(adapter);

        loadDevices();
        return view;
    }

    private void loadDevices() {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(new SSLSocketFactoryImpl(), new X509TrustManagerImpl())
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
            Request request = new Request.Builder()
                    .url("https://192.168.1.1:4433/leases")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Dữ liệu từ server: " + responseBody);
                    parseDevices(responseBody);
                    getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Tải danh sách thiết bị thất bại: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Lỗi mạng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void parseDevices(String responseBody) {
        devices.clear();
        String[] lines = responseBody.split("\n");

        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                String[] parts = line.trim().split(",");
                if (parts.length == 4) {
                    String mac = parts[0]; // Địa chỉ MAC
                    String ip = parts[1]; // Địa chỉ IP
                    String hostname = parts[2]; // Hostname
                    boolean blocked = parts[3].equals("1"); // Trạng thái chặn
                    devices.add(new Device(mac, ip, hostname, blocked));
                }
            }
        }
    }
}