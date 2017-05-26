package com.fonfon.noloss.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.fonfon.noloss.R;

final class SwipeHelper extends ItemTouchHelper.SimpleCallback {

    private final Bitmap iconDelete;
    private final Paint paint;
    private final RectF background;
    private final RectF iconDest;
    private final SwipeListener deleteListener;

    private final int mojo;

    SwipeHelper(SwipeListener listener, RecyclerView recyclerView) {
        super(0, ItemTouchHelper.LEFT);
        this.deleteListener = listener;
        iconDelete = getBitmapFromDrawable(recyclerView.getContext(), R.drawable.ic_delete);
        new ItemTouchHelper(this).attachToRecyclerView(recyclerView);

        mojo = ContextCompat.getColor(recyclerView.getContext(), R.color.mojo);

        paint = new Paint();
        paint.setColor(mojo);

        background = new RectF();
        iconDest = new RectF();
    }

    private Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;
            float height = (float) itemView.getBottom() - (float) itemView.getTop();
            float width = height / 3;

            if (dX < 0) {
                paint.setColor(mojo);
                background.set(
                        (float) itemView.getRight() + dX,
                        (float) itemView.getTop(),
                        (float) itemView.getRight(),
                        (float) itemView.getBottom()
                );
                c.drawRect(background, paint);
                iconDest.set(
                        (float) itemView.getRight() - 2 * width,
                        (float) itemView.getTop() + width,
                        (float) itemView.getRight() - width,
                        (float) itemView.getBottom() - width
                );
                c.drawBitmap(iconDelete, null, iconDest, paint);
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        deleteListener.onItemDelete(viewHolder.getAdapterPosition());
    }

    interface SwipeListener {
        void onItemDelete(int adapterPosition);
    }
}
