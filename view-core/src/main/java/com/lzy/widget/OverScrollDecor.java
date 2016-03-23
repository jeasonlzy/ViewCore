package com.lzy.widget;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * ================================================
 * 作    者：廖子尧
 * 版    本：1.0
 * 创建日期：2016/3/8
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class OverScrollDecor extends FrameLayout {

    private ViewDragHelper mDragHelper;

    public OverScrollDecor(Context context) {
        this(context, null);
    }

    public OverScrollDecor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverScrollDecor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDragHelper = ViewDragHelper.create(this, 1.0f, new OverScrollCallBack());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean shouldIntercept = false;
        try {
            shouldIntercept = mDragHelper.shouldInterceptTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shouldIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            mDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private class OverScrollCallBack extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            MarginLayoutParams params = (MarginLayoutParams) releasedChild.getLayoutParams();
            mDragHelper.smoothSlideViewTo(releasedChild, params.leftMargin, params.topMargin);
            ViewCompat.postInvalidateOnAnimation(OverScrollDecor.this);
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return Math.abs(child.getHeight());
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return child.getLeft();
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return child.getTop() + dy / 2;
        }
    }
}
