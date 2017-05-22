package com.fonfon.noloss.lib;

import android.content.Context;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.util.AttributeSet;

@BindingMethods(@BindingMethod(type = CircleView.class, attribute = "progressStepValue", method = "setProgressValue"))
public class CircleView extends rjsv.circularview.CircleView {

    public CircleView(Context context) {
        super(context);
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setProgressValue(float progressValue) {
        super.setProgressValue(progressValue);
    }
}
