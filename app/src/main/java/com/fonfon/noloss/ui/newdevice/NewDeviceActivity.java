package com.fonfon.noloss.ui.newdevice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.fonfon.noloss.ActivityEvent;
import com.fonfon.noloss.R;
import com.fonfon.noloss.ui.DividerItemDecoration;
import com.hannesdorfmann.mosby3.mvi.MviActivity;
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public final class NewDeviceActivity extends MviActivity<NewDeviceView, NewDevicePresenter> implements NewDeviceView {

  public static Intent getIntent(Activity activity) {
    return new Intent(activity, NewDeviceActivity.class);
  }

  @BindView(R.id.recycler)
  RecyclerView recycler;
  @BindView(R.id.refresh)
  SwipeRefreshLayout refresh;
  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.empty_text)
  TextView empty;

  private Unbinder unbinder;
  private final PublishSubject<ActivityEvent> lifecycleSubject = PublishSubject.create();
  private final PublishSubject<Pair<String, String>> newDeviceSubject = PublishSubject.create();
  private NewDevicesAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_device);
    unbinder = ButterKnife.bind(this);

    adapter = new NewDevicesAdapter((address, name) -> newDeviceSubject.onNext(new Pair<>(address, name)));
    recycler.setLayoutManager(new LinearLayoutManager(this));
    recycler.setAdapter(adapter);
    int padding = getResources().getDimensionPixelSize(R.dimen.spacing_normal);
    recycler.addItemDecoration(new DividerItemDecoration(getDrawable(R.drawable.divider), padding));

    toolbar.setNavigationIcon(R.drawable.ic_back);
    toolbar.setNavigationOnClickListener(v -> finish());
  }

  @NonNull
  @Override
  public NewDevicePresenter createPresenter() {
    return new NewDevicePresenter(this);
  }

  @Override
  protected void onDestroy() {
    unbinder.unbind();
    super.onDestroy();
  }

  @Override
  protected void onResume() {
    super.onResume();
    lifecycleSubject.onNext(ActivityEvent.RESUME);
  }

  @Override
  protected void onPause() {
    lifecycleSubject.onNext(ActivityEvent.PAUSE);
    super.onPause();
  }

  @Override
  public Observable<ActivityEvent> onLifecycleIntent() {
    return lifecycleSubject.hide();
  }

  @Override
  public Observable<Object> onRefreshIntent() {
    return RxSwipeRefreshLayout.refreshes(refresh);
  }

  @Override
  public Observable<Pair<String, String>> onDeviceIntent() {
    return newDeviceSubject.hide();
  }

  @Override
  public void render(NewDevicesViewState state) {
    if (state instanceof NewDevicesViewState.LoadingState) {
      renderLoadingState((NewDevicesViewState.LoadingState) state);
    } else if (state instanceof NewDevicesViewState.NewDeviceState) {
      renderDataState((NewDevicesViewState.NewDeviceState) state);
    } else {
      throw new IllegalArgumentException("Don't know how to render viewState " + state);
    }
  }

  private void renderLoadingState(NewDevicesViewState.LoadingState state) {
    if (state.isLoading())
      adapter.clear();
    refresh.setRefreshing(state.isLoading());
    empty.setVisibility(state.isLoading() || recycler.getAdapter().getItemCount() > 0 ? View.GONE : View.VISIBLE);
  }

  private void renderDataState(NewDevicesViewState.NewDeviceState state) {
    adapter.add(state.getAddress(), state.getName());
  }

}
