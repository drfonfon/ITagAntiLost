package com.fonfon.noloss.ui;

import android.content.Context;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.util.AttributeSet;

@BindingMethods(@BindingMethod(type = CircleView.class, attribute = "progressStepValue", method = "setProgressValue"))
public final class CircleView extends rjsv.circularview.CircleView {

    public CircleView(
            Context context,
            AttributeSet attrs
    ) {
        super(context, attrs);
    }

    @Override
    public final void setProgressValue(
            float progressValue
    ) {
        super.setProgressValue(progressValue);
    }
}
