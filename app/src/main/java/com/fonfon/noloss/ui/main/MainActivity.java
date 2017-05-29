package com.fonfon.noloss.ui.main;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.fonfon.noloss.R;
import com.fonfon.noloss.databinding.ActivityMainBinding;

public final class MainActivity extends AppCompatActivity implements MainActivityViewModel.DataListener {

    private ActivityMainBinding binding;
    private MainActivityViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = new MainActivityViewModel(this, this);
        binding.setModel(model);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));

        binding.recycler.setAdapter(model.getAdapter());
        new SwipeHelper(model, binding.recycler);
        binding.refresh.setOnRefreshListener(model);
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
    protected void onDestroy() {
        super.onDestroy();
        binding.unbind();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void setRefresh(boolean isRefreshing) {
        binding.refresh.setRefreshing(isRefreshing);
    }
}
