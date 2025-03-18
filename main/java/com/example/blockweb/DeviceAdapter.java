package com.example.blockweb;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blockweb.utils.SSLSocketFactoryImpl;
import com.example.blockweb.utils.X509TrustManagerImpl;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeviceAdapter extends ArrayAdapter<Device> {
    public DeviceAdapter(Context context, List<Device> devices) {
        super(context, 0, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Device device = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.device_info);
        Switch switchView = convertView.findViewById(R.id.block_switch);

        String deviceInfo = "MAC: " + (device.getMac() != null ? device.getMac() : "N/A") +
                "\nIP: " + device.getIp() +
                "\nHostname: " + device.getHostname();
        textView.setText(deviceInfo);
        switchView.setChecked(device.isBlocked());

        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            device.setBlocked(isChecked);
            String url = isChecked ? "https://192.168.1.1:4433/add_mac" : "https://192.168.1.1:4433/remove_mac";
            new Thread(() -> {
                OkHttpClient client = new OkHttpClient.Builder()
                        .sslSocketFactory(new SSLSocketFactoryImpl(), new X509TrustManagerImpl())
                        .hostnameVerifier((hostname, session) -> true)
                        .build();
                RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), device.getMac() != null ? device.getMac() : "");
                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        getContext().getMainExecutor().execute(() ->
                                Toast.makeText(getContext(), isChecked ? "Đã chặn thiết bị" : "Đã bỏ chặn thiết bị", Toast.LENGTH_SHORT).show());
                    } else {
                        getContext().getMainExecutor().execute(() ->
                                Toast.makeText(getContext(), "Thất bại khi " + (isChecked ? "chặn" : "bỏ chặn") + " thiết bị", Toast.LENGTH_SHORT).show());
                    }
                } catch (IOException e) {
                    getContext().getMainExecutor().execute(() ->
                            Toast.makeText(getContext(), "Lỗi mạng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        return convertView;
    }
}