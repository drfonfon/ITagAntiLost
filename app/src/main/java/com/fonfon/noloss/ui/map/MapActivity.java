package com.fonfon.noloss.ui.map;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.fonfon.noloss.App;
import com.fonfon.noloss.R;
import com.fonfon.noloss.databinding.ActivityMapBinding;
import com.google.android.gms.maps.SupportMapFragment;

public final class MapActivity extends AppCompatActivity {

    public static void show(Activity activity) {
        activity.startActivity(new Intent(activity, MapActivity.class));
        activity.overridePendingTransition(R.anim.slide_left, R.anim.no_change);
    }

    private ActivityMapBinding binding;
    private MapActivityViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map);
        model = new MapActivityViewModel(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(model);

        binding.toolbar.setNavigationIcon(R.drawable.ic_back);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        model.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.getInstance().setActivityVisible(true);
        model.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.getInstance().setActivityVisible(false);
        model.pause();
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
}
