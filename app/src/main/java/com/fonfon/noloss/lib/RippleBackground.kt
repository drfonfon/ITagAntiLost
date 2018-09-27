package com.fonfon.noloss.lib

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import com.fonfon.noloss.R


class RippleBackground @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

  private var rippleColor = ContextCompat.getColor(context, R.color.mariner)
  private var rippleStrokeWidth = 6f
  private var rippleRadius = 100f
  private var rippleDurationTime = 10000
  private var rippleAmount = 2
  private var rippleDelay = rippleDurationTime / rippleAmount
  private var rippleScale = 10f
  private var rippleType = 0
  private var paint = Paint().apply {
    isAntiAlias = true
    color = rippleColor
    if (rippleType == 0) {
      rippleStrokeWidth = 0f
      style = Paint.Style.FILL
    } else {
      style = Paint.Style.STROKE
    }
  }
  private var isRippleAnimationRunning = false
  private var animatorSet = AnimatorSet().apply {
    interpolator = AccelerateDecelerateInterpolator()
  }
  private var animatorList: ArrayList<Animator> = ArrayList()
  private var rippleParams = RelativeLayout.LayoutParams((2 * (rippleRadius + rippleStrokeWidth)).toInt(), (2 * (rippleRadius + rippleStrokeWidth)).toInt()).apply {
    addRule(CENTER_IN_PARENT, RelativeLayout.TRUE)
  }
  private val rippleViewList = ArrayList<RippleView>()

  init {
    for (i in 0 until rippleAmount) {
      val rippleView = RippleView(getContext())
      addView(rippleView, rippleParams)
      rippleViewList.add(rippleView)
      animatorList.add(ObjectAnimator.ofFloat(rippleView, "ScaleX", 1.0f, rippleScale).apply {
        repeatCount = ObjectAnimator.INFINITE
        repeatMode = ObjectAnimator.RESTART
        startDelay = (i * rippleDelay).toLong()
        duration = rippleDurationTime.toLong()
      })
      animatorList.add(ObjectAnimator.ofFloat(rippleView, "ScaleY", 1.0f, rippleScale).apply {
        repeatCount = ObjectAnimator.INFINITE
        repeatMode = ObjectAnimator.RESTART
        startDelay = (i * rippleDelay).toLong()
        duration = rippleDurationTime.toLong()
      })
      animatorList.add(ObjectAnimator.ofFloat(rippleView, "Alpha", 0.05f, 0f).apply {
        repeatCount = ObjectAnimator.INFINITE
        repeatMode = ObjectAnimator.RESTART
        startDelay = (i * rippleDelay).toLong()
        duration = rippleDurationTime.toLong()
      })
    }

    animatorSet.playTogether(animatorList)
  }

  private inner class RippleView(context: Context) : View(context) {

    init {
      this.visibility = View.INVISIBLE
      this.alpha = 0.05f
    }

    override fun onDraw(canvas: Canvas) {
      val radius = Math.min(width, height) / 2
      canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius - rippleStrokeWidth, paint)
    }
  }

  fun startRippleAnimation() {
    if (!isRippleAnimationRunning) {
      rippleViewList.forEach {
        it.visibility = View.VISIBLE
      }
      animatorSet.start()
      isRippleAnimationRunning = true
    }
  }

  fun stopRippleAnimation() {
    if (isRippleAnimationRunning) {
      animatorSet.end()
      isRippleAnimationRunning = false
    }
  }
}