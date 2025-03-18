package com.example.blockweb;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.blockweb.utils.SSLSocketFactoryImpl;
import com.example.blockweb.utils.X509TrustManagerImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BlocklistFragment extends Fragment {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> blockedSites;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blocklist, container, false);
        listView = view.findViewById(R.id.list_blocked_sites);
        blockedSites = new ArrayList<>();
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, blockedSites);
        listView.setAdapter(adapter);
        loadBlockedSites();

        // Thêm logic xóa khi nhấn giữ (long click)
        listView.setOnItemLongClickListener((parent, view1, position, id) -> {
            String item = blockedSites.get(position);
            String endpoint = item.contains(".") ? (item.matches("^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$") ? "/remove_ip" : "/remove_url") : "/remove_mac";

            new Thread(() -> {
                OkHttpClient client = new OkHttpClient.Builder()
                        .sslSocketFactory(new SSLSocketFactoryImpl(), new X509TrustManagerImpl())
                        .hostnameVerifier((hostname, session) -> true)
                        .build();
                RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), item);
                Request request = new Request.Builder()
                        .url("https://192.168.1.1:4433" + endpoint)
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Item removed successfully", Toast.LENGTH_SHORT).show();
                            blockedSites.remove(position);
                            adapter.notifyDataSetChanged();
                        });
                    } else {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to remove item", Toast.LENGTH_SHORT).show());
                    }
                } catch (IOException e) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();
            return true;
        });

        return view;
    }

    private void loadBlockedSites() {
        new Thread(() -> {
            blockedSites.clear();
            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(new SSLSocketFactoryImpl(), new X509TrustManagerImpl())
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
            Request request = new Request.Builder()
                    .url("https://192.168.1.1:4433/list")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("Blocklist", "Response: " + responseBody); // Debug response
                    String[] items = responseBody.split("\n");
                    for (String item : items) {
                        if (!item.isEmpty()) {
                            blockedSites.add(item);
                        }
                    }
                    getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to load blocked sites", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}