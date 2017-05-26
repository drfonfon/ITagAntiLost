package com.fonfon.noloss.ui;

import android.databinding.BindingAdapter;
import android.support.v7.widget.AppCompatImageView;

import com.fonfon.noloss.lib.BitmapUtils;

public class BindingAdapters {

    @BindingAdapter({"stringImage"})
    public static void setSrcCompat(final AppCompatImageView view, final String image) {
        if(image != null) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    view.setImageBitmap(BitmapUtils.stringToBitMap(image));
                }
            });
        }
    }
}
