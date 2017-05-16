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

public class SwipeToDismissHelper extends ItemTouchHelper.SimpleCallback {

    private Context context;
    private Bitmap icon;
    private Paint paint;
    private ItemTouchHelper itemTouchHelper;
    private RectF background;
    private RectF iconDest;
    private DeleteListener deleteListener;

    public SwipeToDismissHelper(Context context, DeleteListener listener) {
        super(0, ItemTouchHelper.LEFT);
        this.context = context;
        this.deleteListener = listener;
        icon = getBitmapFromDrawable(context, R.drawable.ic_delete);
        itemTouchHelper = new ItemTouchHelper(this);
        paint = new Paint();
        paint.setColor(ContextCompat.getColor(context, R.color.mojo));
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
                c.drawBitmap(icon, null, iconDest, paint);
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

    public interface DeleteListener{
        void onItemDelete(int position);
    }
}
