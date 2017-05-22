package com.fonfon.noloss.ui.main;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.fonfon.noloss.R;
import com.fonfon.noloss.databinding.ActivityMainBinding;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.ui.DividerItemDecoration;
import com.fonfon.noloss.ui.SwipeHelper;

import io.realm.RealmResults;

public final class MainActivity extends AppCompatActivity implements MainActivityViewModel.DataListener {

    private ActivityMainBinding binding;
    private MainActivityViewModel model;
    private DevicesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = new MainActivityViewModel(this, this);
        binding.setModel(model);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        int padding = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        binding.recycler.addItemDecoration(
                new DividerItemDecoration(getDrawable(R.drawable.divider), padding, padding)
        );
        adapter = new DevicesAdapter(model);
        binding.recycler.setAdapter(adapter);

        SwipeHelper swipeHelper = new SwipeHelper(this, model);
        swipeHelper.attachToRecyclerView(binding.recycler);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        model.init();
    }

    @Override
    public void onDevices(RealmResults<Device> devices) {
        adapter.addDevices(devices);
    }

    @Override
    public void deviceConnected(String address) {
        adapter.deviceConnected(address);
    }

    @Override
    public void deviceDisconnected(String address) {
        adapter.deviceDisconnected(address);
    }

    @Override
    public void deviceDeleted(int index) {
        adapter.deviceDeleted(index);
    }

    @Override
    public void deviceAlert(int index) {
        adapter.deviceAlerted(index);
    }

    @Override
    public boolean getDeviceAlertStatus(int index) {
        return adapter.getDeviceAlertStatus(index);
    }
}
