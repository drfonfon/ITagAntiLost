package com.fonfon.noloss.ui.main;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.fonfon.noloss.App;
import com.fonfon.noloss.R;
import com.fonfon.noloss.databinding.ActivityMainBinding;
import com.fonfon.noloss.ui.DividerItemDecoration;
import com.fonfon.noloss.ui.SwipeHelper;

public final class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainActivityViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = new MainActivityViewModel(this);
        binding.setModel(model);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        int padding = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        binding.recycler.addItemDecoration(
                new DividerItemDecoration(getDrawable(R.drawable.divider), padding, padding)
        );

        binding.recycler.setAdapter(model.getAdapter());

        SwipeHelper swipeHelper = new SwipeHelper(this, model);
        swipeHelper.attachToRecyclerView(binding.recycler);
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
}
