package com.lzy.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ScrollView;

import java.util.ArrayList;

public class StickyScrollView extends ScrollView {

    private static final String STICKY_TAG = "sticky";     //固定在顶部的Tag

    private int mShadowHeight = 10;                        //固定View下方的阴影高度，单位dp;
    private Drawable mShadowDrawable;                      //固定View下方的阴影

    private ArrayList<View> stickyViews;
    private View currentlyStickingView;
    private float stickyViewTopOffset;
    private int stickyViewLeftOffset;
    private boolean redirectTouchesToStickyView;

    public StickyScrollView(Context context) {
        this(context, null);
    }

    public StickyScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.scrollViewStyle);
    }

    public StickyScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mShadowHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mShadowHeight, context.getResources().getDisplayMetrics());
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StickyScrollView, defStyle, 0);
        mShadowHeight = a.getDimensionPixelSize(R.styleable.StickyScrollView_stuckShadowHeight, mShadowHeight);
        int shadowDrawableRes = a.getResourceId(R.styleable.StickyScrollView_stuckShadowDrawable, R.drawable.sticky_shadow_default);
        mShadowDrawable = context.getResources().getDrawable(shadowDrawableRes);
        a.recycle();
        stickyViews = new ArrayList<>();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        stickyViews.clear();
        findStickyViews(this);
        doTheStickyThing();
        invalidate();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        doTheStickyThing();
    }

    public void doTheStickyThing() {
        View viewThatShouldStick = null;
        View approachingView = null;
        for (View v : stickyViews) {
            int viewTop = getTopForViewRelativeOnlyChild(v) - getScrollY();
            System.out.println("viewTop:" + viewTop + "  getScrollY:" + getScrollY());
            if (viewTop <= 0) {
                //寻找出当前条件中，最后一个满足sticky条件的View
                if (viewThatShouldStick == null || viewTop > (getTopForViewRelativeOnlyChild(viewThatShouldStick) - getScrollY())) {
                    viewThatShouldStick = v;
                }
            } else {
                //寻找出当前条件中，最接近sticky的View
                if (approachingView == null || viewTop < (getTopForViewRelativeOnlyChild(approachingView) - getScrollY())) {
                    approachingView = v;
                }
            }
        }
        if (viewThatShouldStick != null) {
            //计算出sticky的偏移量
            if (approachingView != null) {
                stickyViewTopOffset = Math.min(0, getTopForViewRelativeOnlyChild(approachingView) - getScrollY() - viewThatShouldStick.getHeight());
            } else {
                stickyViewTopOffset = 0;
            }
            if (viewThatShouldStick != currentlyStickingView) {
                stickyViewLeftOffset = getLeftForViewRelativeOnlyChild(viewThatShouldStick);
                if (currentlyStickingView != null) {
                    showView(currentlyStickingView);
                }
                currentlyStickingView = viewThatShouldStick;
                hideView(currentlyStickingView);
            }
        } else if (currentlyStickingView != null) {
            showView(currentlyStickingView);
            currentlyStickingView = null;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (currentlyStickingView != null) {
            canvas.save();
            canvas.translate(getPaddingLeft() + stickyViewLeftOffset, getScrollY() + stickyViewTopOffset + getPaddingTop());
            canvas.clipRect(0, -stickyViewTopOffset, getWidth() - stickyViewLeftOffset, currentlyStickingView.getHeight() + mShadowHeight + 1);

            if (mShadowDrawable != null) {
                int left = 0;
                int right = currentlyStickingView.getWidth();
                int top = currentlyStickingView.getHeight();
                int bottom = currentlyStickingView.getHeight() + mShadowHeight;
                mShadowDrawable.setBounds(left, top, right, bottom);
                mShadowDrawable.draw(canvas);
            }

            canvas.clipRect(0, -stickyViewTopOffset, getWidth(), currentlyStickingView.getHeight());
            //保证透明度
            showView(currentlyStickingView);
            currentlyStickingView.draw(canvas);
            hideView(currentlyStickingView);
            canvas.restore();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            redirectTouchesToStickyView = true;
        }

        if (redirectTouchesToStickyView) {
            redirectTouchesToStickyView = currentlyStickingView != null;
            if (redirectTouchesToStickyView) {
                redirectTouchesToStickyView = ev.getY() <= (currentlyStickingView.getHeight() + stickyViewTopOffset) &&
                        ev.getX() >= getLeftForViewRelativeOnlyChild(currentlyStickingView) &&
                        ev.getX() <= getRightForViewRelativeOnlyChild(currentlyStickingView);
            }
        } else if (currentlyStickingView == null) {
            redirectTouchesToStickyView = false;
        }
        if (redirectTouchesToStickyView) {
            ev.offsetLocation(0, -1 * ((getScrollY() + stickyViewTopOffset) - getTopForViewRelativeOnlyChild(currentlyStickingView)));
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean hasNotDoneActionDown = true;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (redirectTouchesToStickyView) {
            ev.offsetLocation(0, ((getScrollY() + stickyViewTopOffset) - getTopForViewRelativeOnlyChild(currentlyStickingView)));
        }

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            hasNotDoneActionDown = false;
        }

        if (hasNotDoneActionDown) {
            MotionEvent down = MotionEvent.obtain(ev);
            down.setAction(MotionEvent.ACTION_DOWN);
            super.onTouchEvent(down);
            hasNotDoneActionDown = false;
        }

        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            hasNotDoneActionDown = true;
        }

        return super.onTouchEvent(ev);
    }

    private int getLeftForViewRelativeOnlyChild(View v) {
        int left = v.getLeft();
        while (v.getParent() != getChildAt(0)) {
            v = (View) v.getParent();
            left += v.getLeft();
        }
        return left;
    }

    private int getTopForViewRelativeOnlyChild(View v) {
        int top = v.getTop();
        while (v.getParent() != getChildAt(0)) {
            v = (View) v.getParent();
            top += v.getTop();
        }
        return top;
    }

    private int getRightForViewRelativeOnlyChild(View v) {
        int right = v.getRight();
        while (v.getParent() != getChildAt(0)) {
            v = (View) v.getParent();
            right += v.getRight();
        }
        return right;
    }

    private int getBottomForViewRelativeOnlyChild(View v) {
        int bottom = v.getBottom();
        while (v.getParent() != getChildAt(0)) {
            v = (View) v.getParent();
            bottom += v.getBottom();
        }
        return bottom;
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        findStickyViews(child);
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
        findStickyViews(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        findStickyViews(child);
    }

    @Override
    public void addView(View child, int width, int height) {
        super.addView(child, width, height);
        findStickyViews(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        findStickyViews(child);
    }

    private void findStickyViews(View v) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                String tag = getStringTagForView(vg.getChildAt(i));
                if (tag != null && tag.contains(STICKY_TAG)) {
                    stickyViews.add(vg.getChildAt(i));
                } else if (vg.getChildAt(i) instanceof ViewGroup) {
                    findStickyViews(vg.getChildAt(i));
                }
            }
        } else {
            String tag = (String) v.getTag();
            if (tag != null && tag.contains(STICKY_TAG)) {
                stickyViews.add(v);
            }
        }
    }

    private String getStringTagForView(View v) {
        Object tagObject = v.getTag();
        return String.valueOf(tagObject);
    }

    private void hideView(View v) {
        if (Build.VERSION.SDK_INT >= 11) {
            v.setAlpha(0);
        } else {
            AlphaAnimation anim = new AlphaAnimation(1, 0);
            anim.setDuration(0);
            anim.setFillAfter(true);
            v.startAnimation(anim);
        }
    }

    private void showView(View v) {
        if (Build.VERSION.SDK_INT >= 11) {
            v.setAlpha(1);
        } else {
            AlphaAnimation anim = new AlphaAnimation(0, 1);
            anim.setDuration(0);
            anim.setFillAfter(true);
            v.startAnimation(anim);
        }
    }

    public void setShadowHeight(int height) {
        mShadowHeight = height;
    }
}