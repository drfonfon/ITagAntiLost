package com.fonfon.noloss.ui.detail;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.fonfon.noloss.R;
import com.fonfon.noloss.databinding.ActivityDetailBinding;
import com.fonfon.noloss.lib.Device;
import com.google.android.gms.maps.SupportMapFragment;

public final class DetailActivity extends AppCompatActivity {

    public static void show(Activity context, String deviceAddress) {
        context.startActivity(new Intent(context, DetailActivity.class)
                .putExtra(Device.ADDRESS, deviceAddress)
        );
        context.overridePendingTransition(R.anim.slide_left, R.anim.no_change);
    }

    private ActivityDetailBinding binding;
    private DetailActivityViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String address = getIntent().getStringExtra(Device.ADDRESS);
        if (address == null) {
            finish();
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);
        model = new DetailActivityViewModel(this, address);
        binding.setModel(model);
        binding.connect.setConnect(model.isConnected);

        binding.toolbar.setNavigationIcon(R.drawable.ic_back);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.toolbar.inflateMenu(R.menu.device_detail);
        binding.toolbar.setOnMenuItemClickListener(model);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(model);
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
        super.onActivityResult(requestCode, resultCode, data);
        model.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_change, R.anim.slide_right);
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

}
