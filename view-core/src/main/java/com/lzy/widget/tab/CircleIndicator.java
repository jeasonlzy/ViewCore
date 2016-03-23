package com.lzy.widget.tab;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.lzy.widget.R;

/**
 * ================================================
 * 作    者：廖子尧
 * 版    本：1.0
 * 创建日期：2015/10/16
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class CircleIndicator extends View {

    private float normalRadius = 3;    //普通点的半径
    private float selectedRadius = 4;  //选择点的半径
    private int normalRadiusColor = 0xDD333333;  //普通点的颜色
    private int selectedRadiusColor = 0xAAFF0000; //选中点的颜色
    private float dotPadding = 10;   //每个点之间的距离
    private boolean isStroke = true; //是否是空心圆
    private float normalStrokeWidth = 1;    //空心圆的线宽
    private boolean isBlink = false;    //是否是以闪现的方式滑动

    private int dotNum;  //要展示的点的数量,默认按照Adapter的Count，如果设置Num，以设置的为准
    private Paint mNormalPaint;
    private Paint mSelectedPaint;
    private float mCx;  //第一个圆心的X坐标
    private float mCy;  //第一个圆心的Y坐标
    private ViewPager mViewPager;
    private MyOnPageChangeListener mListener;
    private float mTranslationX;  //移动的偏移量
    private boolean isDetached;   //是否被回收过

    public CircleIndicator(Context context) {
        this(context, null);
    }

    public CircleIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        normalRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, normalRadius, getResources().getDisplayMetrics());
        selectedRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, selectedRadius, getResources().getDisplayMetrics());
        dotPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dotPadding, getResources().getDisplayMetrics());
        normalStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, normalStrokeWidth, getResources().getDisplayMetrics());

        TypedArray a = getResources().obtainAttributes(attrs, R.styleable.CircleIndicator);
        normalRadius = a.getDimension(R.styleable.CircleIndicator_ci_normalRadius, normalRadius);
        selectedRadius = a.getDimension(R.styleable.CircleIndicator_ci_selectedRadius, selectedRadius);
        normalRadiusColor = a.getColor(R.styleable.CircleIndicator_ci_normalRadiusColor, normalRadiusColor);
        selectedRadiusColor = a.getColor(R.styleable.CircleIndicator_ci_selectedRadiusColor, selectedRadiusColor);
        dotPadding = a.getDimension(R.styleable.CircleIndicator_ci_dotPadding, dotPadding);
        isStroke = a.getBoolean(R.styleable.CircleIndicator_ci_isStroke, isStroke);
        normalStrokeWidth = a.getDimension(R.styleable.CircleIndicator_ci_normalStrokeWidth, normalStrokeWidth);
        isBlink = a.getBoolean(R.styleable.CircleIndicator_ci_isBlink, isBlink);
        a.recycle();

        initPaint();
    }

    /**
     * 各个方法执行顺序
     * CircleIndicator
     * onFinishInflate
     * setXXX
     * onSizeChanged
     * onDraw
     */
    private void initPaint() {
        mNormalPaint = new Paint();
        mNormalPaint.setAntiAlias(true);
        mNormalPaint.setColor(normalRadiusColor);
        mNormalPaint.setStrokeWidth(normalStrokeWidth);
        if (isStroke)
            mNormalPaint.setStyle(Paint.Style.STROKE);
        else mNormalPaint.setStyle(Paint.Style.FILL);

        mSelectedPaint = new Paint();
        mSelectedPaint.setAntiAlias(true);
        mSelectedPaint.setColor(selectedRadiusColor);
    }

    /**
     * 计算第一个圆的圆心位置
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float maxRadius = Math.max(normalRadius, selectedRadius);
        float availableWidth = w - getPaddingLeft() - getPaddingRight();
        float availableHeight = h - getPaddingTop() - getPaddingBottom();
        float drawWidth = (dotNum - 1) * dotPadding + maxRadius * 2;
        if (dotNum == 1)
            drawWidth = maxRadius * 2;
        if (dotNum <= 0)
            drawWidth = 0;
        mCx = (availableWidth - drawWidth) / 2 + maxRadius + getPaddingLeft();
        mCy = availableHeight / 2 + getPaddingTop();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (dotNum > 0) {
            for (int i = 0; i < dotNum; i++) {
                //绘制普通的圆
                canvas.drawCircle(mCx + i * dotPadding, mCy, normalRadius, mNormalPaint);
            }
            //绘制选中的圆
            canvas.drawCircle(mCx + mTranslationX, mCy, selectedRadius, mSelectedPaint);
        }
    }

    public CircleIndicator setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        if (mViewPager == null || mViewPager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager不能为空或者ViewPager没有设置Adapter！");
        }
        dotNum = mViewPager.getAdapter().getCount();
        mListener = new MyOnPageChangeListener();
        mViewPager.addOnPageChangeListener(mListener);
        return this;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isDetached && mViewPager != null && mListener != null) {
            mViewPager.addOnPageChangeListener(mListener);
            isDetached = false;
        }
    }

    /**
     * 移除监听
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mViewPager != null && mListener != null)
            mViewPager.removeOnPageChangeListener(mListener);
        isDetached = true;
    }

    private class MyOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (dotNum > 0) {
                if (isBlink) {
                    if (positionOffset == 0)
                        mTranslationX = position * dotPadding;
                } else {
                    //对循环滑动的ViewPager做特殊的处理,滑动到边界的时候，继续滑动，小点应该返回
                    if ((position == dotNum - 1) && positionOffset > 0)
                        mTranslationX = (dotNum - 1) * dotPadding * (1 - positionOffset);
                    else
                        mTranslationX = (position + positionOffset) * dotPadding;
                }
                invalidate();
            }
        }
    }
}
