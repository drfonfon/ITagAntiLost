package com.fonfon.noloss.ui;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public final class DividerItemDecoration extends RecyclerView.ItemDecoration {

  private final Drawable divider;
  private final int padding;

  public DividerItemDecoration(Drawable divider, int padding) {
    this.divider = divider;
    this.padding = padding;
  }

  @Override
  public final void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
    super.onDraw(c, parent, state);
    final int left = parent.getPaddingLeft();
    final int right = parent.getWidth() - parent.getPaddingRight();
    for (int i = 0, childCount = parent.getChildCount(); i < childCount - 1; i++) {
      final int top = parent.getChildAt(i).getBottom();
      divider.setBounds(left + padding, top, right - padding, top + divider.getIntrinsicHeight());
      divider.draw(c);
    }
  }

  @Override
  public final void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
    super.getItemOffsets(outRect, view, parent, state);
    if (parent.getChildAdapterPosition(view) == 0) {
      return;
    }
    outRect.top = divider.getIntrinsicHeight();
  }
}