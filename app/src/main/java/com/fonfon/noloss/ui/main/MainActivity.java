package com.fonfon.noloss.ui.main;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.fonfon.noloss.R;
import com.fonfon.noloss.databinding.ActivityMainBinding;
import com.fonfon.noloss.lib.BleService;
import com.fonfon.noloss.ui.SwipeToDismissHelper;

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

        SwipeToDismissHelper swipeToDismissHelper = new SwipeToDismissHelper(this, model);
        swipeToDismissHelper.attachToRecyclerView(binding.recycler);
        model.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        model.resume();
        startService(new Intent(MainActivity.this, BleService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        model.pause();
        stopService(new Intent(MainActivity.this, BleService.class));
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
    public void notifyAdapter() {
        binding.recycler.getAdapter().notifyDataSetChanged();
    }
}
