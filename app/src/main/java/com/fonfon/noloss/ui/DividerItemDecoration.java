package com.fonfon.noloss.ui;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable divider;
    private int leftPadding = 0;
    private int rightPadding = 0;

    public DividerItemDecoration(Drawable divider) {
        this.divider = divider;
    }

    public DividerItemDecoration(Drawable divider, int leftPadding) {
        this.divider = divider;
        this.leftPadding = leftPadding;
    }

    public DividerItemDecoration(Drawable divider, int leftPadding, int rightPadding) {
        this.divider = divider;
        this.leftPadding = leftPadding;
        this.rightPadding = rightPadding;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        drawVertical(c, parent);
    }

    private void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        for (int i = 0, childCount = parent.getChildCount(); i < childCount - 1; i++) {
            final int top = parent.getChildAt(i).getBottom();
            divider.setBounds(left + leftPadding, top, right - rightPadding, top + divider.getIntrinsicHeight());
            divider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (parent.getChildAdapterPosition(view) == 0) {
            return;
        }
        outRect.top = divider.getIntrinsicHeight();
    }

}
