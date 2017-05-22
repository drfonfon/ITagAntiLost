package com.fonfon.noloss.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.fonfon.noloss.R;

public final class SwipeHelper extends ItemTouchHelper.SimpleCallback {

    private Bitmap iconDelete;
    private Bitmap iconAlertOn;
    private Bitmap iconAlertOff;
    private Paint paint;
    private ItemTouchHelper itemTouchHelper;
    private RectF background;
    private RectF iconDest;
    private DeleteListener deleteListener;

    private final int mojo;
    private final int fern;
    private final int sun;

    public SwipeHelper(Context context, DeleteListener listener) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.deleteListener = listener;
        iconDelete = getBitmapFromDrawable(context, R.drawable.ic_delete);
        iconAlertOn = getBitmapFromDrawable(context, R.drawable.ic_volume_up);
        iconAlertOff = getBitmapFromDrawable(context, R.drawable.ic_volume_off);
        itemTouchHelper = new ItemTouchHelper(this);
        paint = new Paint();
        mojo = ContextCompat.getColor(context, R.color.mojo);
        fern = ContextCompat.getColor(context, R.color.fern);
        sun = ContextCompat.getColor(context, R.color.sun);
        paint.setColor(mojo);
        background = new RectF();
        iconDest = new RectF();
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private static Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable || drawable instanceof VectorDrawableCompat) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

            View itemView = viewHolder.itemView;
            float height = (float) itemView.getBottom() - (float) itemView.getTop();
            float width = height / 3;

            if (dX < 0) {
                boolean isAlarmed = deleteListener.onMove(viewHolder.getAdapterPosition());
                paint.setColor(isAlarmed ? sun : fern);
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
                c.drawBitmap(isAlarmed ? iconAlertOff : iconAlertOn, null, iconDest, paint);
            } else {
                paint.setColor(mojo);
                background.set(
                        (float) itemView.getLeft(),
                        (float) itemView.getTop(),
                        dX,
                        (float) itemView.getBottom()
                );
                c.drawRect(background, paint);
                iconDest.set(
                        (float) itemView.getLeft() + width,
                        (float) itemView.getTop() + width,
                        (float) itemView.getLeft() + 2 * width,
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
        if (direction == ItemTouchHelper.RIGHT) {
            deleteListener.onItemDelete(viewHolder.itemView.getTag(), viewHolder.getAdapterPosition());
        } else {
            deleteListener.onItemAlert(viewHolder.itemView.getTag(), viewHolder.getAdapterPosition());
        }

    }

    public interface DeleteListener {
        void onItemDelete(Object tag, int adapterPosition);
        void onItemAlert(Object tag, int adapterPosition);
        boolean onMove(int adapterPosition);
    }
}
