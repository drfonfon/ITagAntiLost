package com.fonfon.itagantilost;

import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.fonfon.itagantilost.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements MainActivityViewModel.DataListener {

    private ActivityMainBinding binding;
    private MainActivityViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = new MainActivityViewModel(this, this);
        binding.setModel(model);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(new DevicesAdapter(model));
        model.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        model.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        model.pause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        model.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.unbind();
        model.onDestroy();
    }

    @Override
    public void onDevice(ScanResult scanResult) {
        ((DevicesAdapter) binding.recycler.getAdapter()).add(scanResult);
    }

    @Override
    public void clear() {
        ((DevicesAdapter)binding.recycler.getAdapter()).clear();
    }
}
