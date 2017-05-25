package com.fonfon.noloss.ui.newdevice;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.fonfon.noloss.R;
import com.fonfon.noloss.databinding.ActivityNewDeviceBinding;
import com.fonfon.noloss.ui.DividerItemDecoration;

public final class NewDeviceActivity extends AppCompatActivity implements NewDeviceViewModel.DataListener {

    public static void show(Activity activity) {
        activity.startActivity(new Intent(activity, NewDeviceActivity.class));
        activity.overridePendingTransition(R.anim.slide_left, R.anim.no_change);
    }

    private ActivityNewDeviceBinding binding;
    private NewDeviceViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_device);
        model = new NewDeviceViewModel(this, this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));

        binding.recycler.setAdapter(model.getAdapter());
        int padding = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        binding.recycler.addItemDecoration(new DividerItemDecoration(getDrawable(R.drawable.divider), padding));
        binding.refresh.setOnRefreshListener(model);

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
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_change, R.anim.slide_right);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        model.init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.unbind();
        model.onDestroy();
    }

    @Override
    public void setRefresh(boolean isRefreshing) {
        binding.refresh.setRefreshing(isRefreshing);
        if (isRefreshing || binding.recycler.getAdapter().getItemCount() > 0) {
            binding.emptyText.setVisibility(View.GONE);
        } else {
            binding.emptyText.setVisibility(View.VISIBLE);
        }
    }
}
