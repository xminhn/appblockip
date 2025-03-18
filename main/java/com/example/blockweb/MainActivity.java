package com.example.blockweb;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Cập nhật button để chuyển giữa các Fragment mới
        findViewById(R.id.btn_device_list).setOnClickListener(v -> replaceFragment(new DeviceListFragment()));
        findViewById(R.id.btn_block_list).setOnClickListener(v -> replaceFragment(new BlocklistFragment()));
        findViewById(R.id.btn_manual_input).setOnClickListener(v -> replaceFragment(new ManualInputFragment()));
        findViewById(R.id.btn_url_input).setOnClickListener(v -> replaceFragment(new UrlInputFragment()));

        // Mặc định mở DeviceListFragment
        replaceFragment(new DeviceListFragment());
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}