package com.fonfon.itagantilost.lib;

import android.content.Context;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.fonfon.itagantilost.R;

@BindingMethods(@BindingMethod(type = BatteryView.class, attribute = "level", method = "setLevel"))
public class BatteryView extends View {

    private byte level = 100;
    private static final int warningLevel = 35;
    private Paint mTextValuePaint;
    private Paint mMainRectStrokePaint;
    private Paint mMainRectFillPaint;
    private int fillColor;
    private int warningColor;
    private int levelColor;
    private Rect mainRect = new Rect();
    private Rect fillRect = new Rect();
    private Rect smallRect = new Rect();

    public BatteryView(Context context) {
        super(context);
        init();
    }

    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BatteryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
        fillColor = ContextCompat.getColor(getContext(), R.color.mariner);
        warningColor = ContextCompat.getColor(getContext(), R.color.mojo);
        levelColor = ContextCompat.getColor(getContext(), R.color.fern);

        mMainRectStrokePaint = new Paint();
        mMainRectStrokePaint.setStyle(Paint.Style.STROKE);
        mMainRectStrokePaint.setStrokeWidth(5.f);
        mMainRectStrokePaint.setColor(fillColor);

        mMainRectFillPaint = new Paint();
        mMainRectFillPaint.setStyle(Paint.Style.FILL);
        mMainRectFillPaint.setColor(fillColor);

        mTextValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextValuePaint.setTextAlign(Paint.Align.CENTER);
        mTextValuePaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        mTextValuePaint.setTextSize(contentWidth * 0.2f);

        int smallRectWidth = (int) (1.f / 10.f * contentWidth);
        int smallRectHeight = 2 * smallRectWidth;
        int offset = 8;

        mainRect.set(
                paddingLeft,
                paddingTop,
                paddingLeft + contentWidth - smallRectWidth,
                paddingTop + contentHeight
        );
        fillRect.set(
                mainRect.left + offset,
                mainRect.top + offset,
                (mainRect.right - offset) * this.getmLevel() / 100,
                mainRect.bottom - offset
        );
        smallRect.set(
                paddingLeft + contentWidth - smallRectWidth,
                paddingTop + (contentHeight - smallRectHeight) / 2,
                paddingLeft + contentWidth,
                paddingTop + (contentHeight + smallRectHeight) / 2
        );

        mMainRectStrokePaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(mainRect, mMainRectStrokePaint);
        mMainRectStrokePaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(smallRect, mMainRectStrokePaint);

        fillColor = (level <= warningLevel) ? warningColor : levelColor;

        mMainRectFillPaint.setColor(fillColor);
        canvas.drawRect(fillRect, mMainRectFillPaint);

        canvas.drawText(String.valueOf(level), contentWidth * 3 / 7, contentHeight * 2 / 3, mTextValuePaint);
    }

    public int getmLevel() {
        return level;
    }

    public void setLevel(byte mLevel) {
        if (mLevel > 100) mLevel = 100;
        if (mLevel < 0) mLevel = 0;
        this.level = mLevel;
        invalidate();
    }
}
