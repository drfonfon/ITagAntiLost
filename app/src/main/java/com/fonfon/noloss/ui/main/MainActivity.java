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
                new DividerItemDecoration(getDrawable(R.drawable.divider), padding)
        );

        binding.recycler.setAdapter(model.getAdapter());

        binding.toolbar.inflateMenu(R.menu.main);
        binding.toolbar.setOnMenuItemClickListener(model);

        new SwipeHelper(this, model, binding.recycler);
        model.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.getInstance().setVisibleAddress(App.ALL_ADDRESSES);
        model.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.getInstance().setVisibleAddress(null);
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
