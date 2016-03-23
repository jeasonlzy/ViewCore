
package com.lzy.widget.tab;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lzy.widget.R;

/**
 * ================================================
 * 作    者：廖子尧
 * 版    本：1.0
 * 创建日期：2015/10/19
 * 描    述：强大的ViewPager的指示器，满足各种情况的需求
 * 修订历史：现存Bug：
 * 1.横竖屏旋转时的状态保存和恢复
 * 2.赋初始值时，viewPager.setCurrentItem()，如果设置过，那么第一次展示Indicator的时候，初始位置有问题，ScollerTo调用无效。。
 * ================================================
 */
public class TabTitleIndicator extends HorizontalScrollView {

    /**
     * 让Adapter实现这个接口可以实现图标的切换
     * Adapter实现该接口后就不用重写 getPageTitle 方法
     */
    public interface IconTabProvider {
        /**
         * 获取图标的资源ID
         */
        int getPageIconResId(int position);
    }

    //以下为自定义属性 （单位）大小均为dp  字体均为sp
    private int indicatorHeight = 4;   //指示线的高度
    private int indicatorColor = 0xFF0084FF; //指示线颜色
    private int underlineHeight = 2;   //底部线的高度
    private int underlineColor = 0x1A000000;  //底部线颜色
    private int dividerWidth = 1;      //分割线的宽度
    private int dividerPaddingTopBottom = 12;   //分割线的上下边距
    private int dividerColor = 0x1A000000;  //分割线颜色
    private int triangleHeight = 4;   //默认的三角形的高度
    private int tabPaddingLeftRight = 20;       //每个Tab的左右内边距
    private int tabTextSizeNormal = 16;      //文字默认大小
    private int tabTextSizeSelected = 18;    //文字选中大小
    private int tabTextColorNormal = 0xFF666666;   //文字默认颜色
    private int tabTextColorSelected = 0xFF0084FF;   //文字选中的颜色
    private int tabBackgroundResId = R.drawable.tab_title_selector;//title按下的selector
    private int scrollOffset = 100;       //指示线距离左边的偏移量，当visibleCount=0时才有效
    private int visibleCount = 4;       //当前可见的数量 0表示不扩展，从左往右依次排序

    //以下为全局变量
    private LinearLayout tabsContainer;   //存放Tab的容器
    private PageListener mPagerChangeListener;  //ViewPager滑动的监听
    private ViewPager mViewPager;  //要监听的ViewPager
    private int tabCount;          //Tab的数量
    private int tabWidth;          //如果按数量显示，每个Tab的宽度
    private int currentPosition = 0;  //当前选中的tab位置
    private float currentPositionOffset = 0f;   //当前滑动的偏移量 0.0 - 1.0
    private Paint rectPaint;     //矩形画笔
    private Paint dividerPaint;  //线画笔
    private Paint trianglePaint;//三角形的画笔
    private Path trianglePath;  //三角形的路径
    private boolean isRestoreInstanceState;   //是否被销毁过
    private boolean isDetached = false;       //是否被移除过

    public TabTitleIndicator(Context context) {
        this(context, null);
    }

    public TabTitleIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabTitleIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        System.out.println("TabTitleIndicator");
        //当子控件的宽高不足以填充父窗体的时候，调用该方法可以扩展至填充父窗体
        setFillViewport(true);
        //如果重写了onDraw() 等绘图的方法，应该重写此方法避免onDraw没有调用
        setWillNotDraw(false);

        //创建Tab容器
        tabsContainer = new LinearLayout(context);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(tabsContainer);

        //初始化默认值
        DisplayMetrics dm = getResources().getDisplayMetrics();
        scrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset, dm);
        indicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
        underlineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight, dm);
        dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth, dm);
        dividerPaddingTopBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPaddingTopBottom, dm);
        triangleHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, triangleHeight, dm);
        tabPaddingLeftRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPaddingLeftRight, dm);
        tabTextSizeNormal = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSizeNormal, dm);
        tabTextSizeSelected = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSizeSelected, dm);

        //获取系统定义的的属性
//        int[] systemAttrs = new int[]{android.R.attr.textSize, android.R.attr.textColor};
//        TypedArray a = context.obtainStyledAttributes(attrs, systemAttrs);
//        tabTextSizeNormal = a.getDimensionPixelSize(0, tabTextSizeNormal);
//        tabTextColorNormal = a.getColor(1, tabTextColorNormal);
//        a.recycle();

        //获取自定义的属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabTitleIndicator);
        indicatorHeight = a.getDimensionPixelSize(R.styleable.TabTitleIndicator_tti_indicatorHeight, indicatorHeight);
        indicatorColor = a.getColor(R.styleable.TabTitleIndicator_tti_indicatorColor, indicatorColor);
        underlineHeight = a.getDimensionPixelSize(R.styleable.TabTitleIndicator_tti_underlineHeight, underlineHeight);
        underlineColor = a.getColor(R.styleable.TabTitleIndicator_tti_underlineColor, underlineColor);
        dividerWidth = a.getDimensionPixelSize(R.styleable.TabTitleIndicator_tti_dividerWidth, dividerWidth);
        dividerPaddingTopBottom = a.getDimensionPixelSize(R.styleable.TabTitleIndicator_tti_dividerPaddingTopBottom, dividerPaddingTopBottom);
        dividerColor = a.getColor(R.styleable.TabTitleIndicator_tti_dividerColor, dividerColor);
        triangleHeight = a.getDimensionPixelSize(R.styleable.TabTitleIndicator_tti_triangleHeight, triangleHeight);
        tabTextSizeNormal = a.getDimensionPixelSize(R.styleable.TabTitleIndicator_tti_tabTextSizeNormal, tabTextSizeNormal);
        tabTextSizeSelected = a.getDimensionPixelSize(R.styleable.TabTitleIndicator_tti_tabTextSizeSelected, tabTextSizeSelected);
        tabTextColorNormal = a.getColor(R.styleable.TabTitleIndicator_tti_tabTextColorNormal, tabTextColorNormal);
        tabTextColorSelected = a.getColor(R.styleable.TabTitleIndicator_tti_tabTextColorSelected, tabTextColorSelected);
        tabPaddingLeftRight = a.getDimensionPixelSize(R.styleable.TabTitleIndicator_tti_tabPaddingLeftRight, tabPaddingLeftRight);
        tabBackgroundResId = a.getResourceId(R.styleable.TabTitleIndicator_tti_tabBackground, tabBackgroundResId);
        visibleCount = a.getInteger(R.styleable.TabTitleIndicator_tti_visibleCount, visibleCount);
        scrollOffset = a.getDimensionPixelSize(R.styleable.TabTitleIndicator_tti_scrollOffset, scrollOffset);
        a.recycle();

        initPaint();
    }

    /**
     * 当完成加载后，初始化画笔，各个方法的执行顺序如下
     * onFinishInflate
     * onRestoreInstanceState （只有当该view可能销毁的时候才调用，如：旋转屏幕，按下Home键 等）
     * onAttachedToWindow
     * onMeasure
     * onSizeChanged
     * dispatchDraw
     * onSaveInstanceState  （只有当该view可能销毁的时候才调用，如：旋转屏幕，按下Home键 等）
     * onDetachedFromWindow
     */
    private void initPaint() {
        //初始化画笔
        rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Style.FILL);
        dividerPaint = new Paint();
        dividerPaint.setAntiAlias(true);
        dividerPaint.setStrokeWidth(dividerWidth);
        trianglePaint = new Paint();
        trianglePaint.setAntiAlias(true);
        trianglePaint.setStyle(Style.FILL);
        trianglePath = new Path();
        //设置圆滑过度根据需要可以是自行设置
        //trianglePaint.setPathEffect(new CornerPathEffect(3));
    }

    /**
     * 设置ViewPager 并且自动设置标题头
     */
    public void setViewPager(ViewPager pager) {
        System.out.println("setViewPager");
        mViewPager = pager;
        if (mViewPager == null || mViewPager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager不能为空或者ViewPager没有设置Adapter！");
        }
        mPagerChangeListener = new PageListener();
        mViewPager.addOnPageChangeListener(mPagerChangeListener);
        tabCount = mViewPager.getAdapter().getCount();
        currentPosition = mViewPager.getCurrentItem();
        addTitleItems();
    }

    /**
     * 在布局完成后再添加子条目View
     */
    public void addTitleItems() {
        if (mViewPager == null || mViewPager.getAdapter() == null) {
            throw new IllegalStateException("必须调用setViewPager()方法!");
        }

        tabsContainer.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        for (int i = 0; i < tabCount; i++) {
            final int finalI = i;
            if (mViewPager.getAdapter() instanceof IconTabProvider) {
                int resId = ((IconTabProvider) mViewPager.getAdapter()).getPageIconResId(i);
                ImageButton tab = new ImageButton(getContext());
                tab.setLayoutParams(params);
                tab.setFocusable(true);
                tab.setImageResource(resId);
                //add之前设置点击事件才不会让上层的ScrollerView抢占焦点事件
                tab.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mViewPager != null) {
                            mViewPager.setCurrentItem(finalI);
                        }
                    }
                });
                tabsContainer.addView(tab);
            } else {
                TextView tab = new TextView(getContext());
                tab.setLayoutParams(params);
                tab.setText(mViewPager.getAdapter().getPageTitle(i).toString());
                tab.setFocusable(true);
                tab.setGravity(Gravity.CENTER);
                tab.setSingleLine(true);
                //add之前设置点击事件才不会让上层的ScrollerView抢占焦点事件
                tab.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mViewPager != null) {
                            mViewPager.setCurrentItem(finalI);
                        }
                    }
                });
                tabsContainer.addView(tab);
            }
        }
        updateTabStyles();
    }

    /**
     * 更新Tab样式
     */
    private void updateTabStyles() {
        for (int i = 0; i < tabCount; i++) {
            View tab = tabsContainer.getChildAt(i);
            tab.setBackgroundResource(tabBackgroundResId);
            if (visibleCount != 0) {
                tab.setPadding(0, 0, 0, 0);
            } else {
                tab.setPadding(tabPaddingLeftRight, 0, tabPaddingLeftRight, 0);
            }
            if (tab instanceof TextView) {
                TextView textView = (TextView) tab;
                //默认选中 currentPosition
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, i == currentPosition ? tabTextSizeSelected : tabTextSizeNormal);
                textView.setTextColor(i == currentPosition ? tabTextColorSelected : tabTextColorNormal);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        System.out.println("onMeasure " + getMeasuredWidth() + "  " + getMeasuredHeight() + "  " + visibleCount);
        if (visibleCount != 0 && tabCount != 0) {
            //不允许visibleCount的值比真实的title数量多
            visibleCount = Math.min(visibleCount, tabCount);
            tabWidth = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / visibleCount;
            //默认偏移量为一个Tab宽度，当可见数量为1时,偏移量为0
            scrollOffset = tabWidth;
            System.out.println("tabWidth " + tabWidth);
            if (visibleCount == 1) {
                scrollOffset = 0;
            }
            for (int i = 0; i < tabsContainer.getChildCount(); i++) {
                View tabItem = tabsContainer.getChildAt(i);
                ViewGroup.LayoutParams params = tabItem.getLayoutParams();
                params.width = tabWidth;
                tabItem.setLayoutParams(params);
                //当子View的大小改变的时候，一定要调用该方法，重新测量子View的大小
                tabItem.measure(MeasureSpec.makeMeasureSpec(tabWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        System.out.println("onLayout " + currentPosition + "  " + isRestoreInstanceState);
        //在被销毁后，重置状态，只需要执行一次
        if (isRestoreInstanceState) {
            System.out.println("---onLayout  " + currentPosition);
            scrollToChild(currentPosition, 0);
            isRestoreInstanceState = false;
        }
    }

    /**
     * 当View附着在Activity上的时候添加监听
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        System.out.println("onAttachedToWindow " + isDetached + "  " + (mViewPager != null) + "  " + (mPagerChangeListener != null));
        if (isDetached && mViewPager != null && mPagerChangeListener != null) {
            mViewPager.addOnPageChangeListener(mPagerChangeListener);
            isDetached = false;
        }
    }

    /**
     * 当View销毁的时候移除监听，每次条目不可见的时候一定会执行的方法
     * 但是onRestoreInstanceState不一定会执行
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        System.out.println("onDetachedFromWindow");
        if (mViewPager != null && mPagerChangeListener != null) {
            mViewPager.removeOnPageChangeListener(mPagerChangeListener);
        }
        isDetached = true;
    }

    /**
     * 数据保存
     */
    @Override
    public Parcelable onSaveInstanceState() {
        System.out.println("onSaveInstanceState " + currentPosition);
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putInt("currentPosition", currentPosition);
        return bundle;
    }

    /**
     * 状态恢复
     */
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        System.out.println("onRestoreInstanceState " + currentPosition);
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable("superState"));
            currentPosition = bundle.getInt("currentPosition");
        } else {
            super.onRestoreInstanceState(state);
        }
        isRestoreInstanceState = true;
    }

    /**
     * 绘制指示线和三角形
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode() || tabCount == 0) {
            return;
        }

        final int height = getHeight();
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();

        //画指示线
        rectPaint.setColor(indicatorColor);
        View currentTab = tabsContainer.getChildAt(currentPosition);
        float lineLeft = currentTab.getLeft() + paddingLeft;
        float lineRight = currentTab.getRight() + paddingLeft;
        if (currentPositionOffset > 0f && currentPosition < tabCount - 1) {
            View nextTab = tabsContainer.getChildAt(currentPosition + 1);
            final float nextTabLeft = nextTab.getLeft() + paddingLeft;
            final float nextTabRight = nextTab.getRight() + paddingLeft;
            lineLeft = (currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft);
            lineRight = (currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight);
        }
        canvas.drawRect(lineLeft, height - indicatorHeight - paddingBottom, lineRight, height - paddingBottom, rectPaint);

        //画三角形
        trianglePaint.setColor(indicatorColor);
        final float centerX = (lineLeft + lineRight) / 2;
        //默认三角形的宽度是高的两倍
        trianglePath.moveTo(centerX - (triangleHeight + indicatorHeight), height - paddingBottom);
        trianglePath.lineTo(centerX + (triangleHeight + indicatorHeight), height - paddingBottom);
        trianglePath.lineTo(centerX, height - paddingBottom - (triangleHeight + indicatorHeight));
        trianglePath.close();
        canvas.drawPath(trianglePath, trianglePaint);
        trianglePath.reset();

        //画底部线
        rectPaint.setColor(underlineColor);
        canvas.drawRect(paddingLeft + getScrollX(), height - underlineHeight - paddingBottom,
                getScrollX() + getWidth() - paddingRight, height - paddingBottom, rectPaint);

        //画分割线
        dividerPaint.setColor(dividerColor);
        for (int i = 0; i < tabCount - 1; i++) {
            View tab = tabsContainer.getChildAt(i);
            canvas.drawLine(tab.getRight() + paddingLeft, dividerPaddingTopBottom + paddingTop,
                    tab.getRight() + paddingLeft, height - dividerPaddingTopBottom - paddingBottom, dividerPaint);
        }
    }

    private class PageListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //滑动结束后才选中
            System.out.println("onPageScrolled " + position + "  " + positionOffset);
            currentPosition = position;
            currentPositionOffset = positionOffset;
            scrollToChild(position, (int) (positionOffset * tabsContainer.getChildAt(position).getWidth()));
            invalidate();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                scrollToChild(mViewPager.getCurrentItem(), 0);
            }
        }

        @Override
        public void onPageSelected(int position) {
            //使当前item高亮
            for (int i = 0; i < tabCount; i++) {
                View view = tabsContainer.getChildAt(i);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, i == position ? tabTextSizeSelected : tabTextSizeNormal);
                    textView.setTextColor(i == position ? tabTextColorSelected : tabTextColorNormal);
                }
            }
        }

    }

    /**
     * 滑动到指定位置，当滑出的值过大或过小的时候，HorizontalScroller 会自动纠正
     */
    private void scrollToChild(int position, int offset) {
        if (tabCount == 0)
            return;
        int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset;
        if (position > 0 || offset > 0) {
            newScrollX = newScrollX - scrollOffset;
        }
        System.out.println("newScrollX  " + newScrollX);
        scrollTo(newScrollX, 0);
    }

    public void setVisibleCount(int visibleCount) {
        this.visibleCount = visibleCount;
    }

    //以下为各个属性的方法设置
    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
        invalidate();
    }

    @SuppressWarnings("deprecation")
    public void setIndicatorColorResource(int resId) {
        this.indicatorColor = getResources().getColor(resId);
        invalidate();
    }

    public int getIndicatorColor() {
        return this.indicatorColor;
    }

    public void setIndicatorHeight(int indicatorLineHeightPx) {
        this.indicatorHeight = indicatorLineHeightPx;
        invalidate();
    }

    public int getIndicatorHeight() {
        return indicatorHeight;
    }

    public void setUnderlineColor(int underlineColor) {
        this.underlineColor = underlineColor;
        invalidate();
    }

    @SuppressWarnings("deprecation")
    public void setUnderlineColorResource(int resId) {
        this.underlineColor = getResources().getColor(resId);
        invalidate();
    }

    public int getUnderlineColor() {
        return underlineColor;
    }

    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
        invalidate();
    }

    @SuppressWarnings("deprecation")
    public void setDividerColorResource(int resId) {
        this.dividerColor = getResources().getColor(resId);
        invalidate();
    }

    public int getDividerColor() {
        return dividerColor;
    }

    public void setUnderlineHeight(int underlineHeightPx) {
        this.underlineHeight = underlineHeightPx;
        invalidate();
    }

    public int getUnderlineHeight() {
        return underlineHeight;
    }

    public void setDividerPaddingTopBottom(int dividerPaddingPx) {
        this.dividerPaddingTopBottom = dividerPaddingPx;
        invalidate();
    }

    public int getDividerPaddingTopBottom() {
        return dividerPaddingTopBottom;
    }

    public void setScrollOffset(int scrollOffsetPx) {
        this.scrollOffset = scrollOffsetPx;
        invalidate();
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setTextSize(int textSizePx) {
        this.tabTextSizeNormal = textSizePx;
        updateTabStyles();
    }

    public int getTextSize() {
        return tabTextSizeNormal;
    }

    public void setTextColor(int textColor) {
        this.tabTextColorNormal = textColor;
        updateTabStyles();
    }

    @SuppressWarnings("deprecation")
    public void setTextColorResource(int resId) {
        this.tabTextColorNormal = getResources().getColor(resId);
        updateTabStyles();
    }

    public int getTextColor() {
        return tabTextColorNormal;
    }

    public void setTabBackground(int resId) {
        this.tabBackgroundResId = resId;
    }

    public int getTabBackground() {
        return tabBackgroundResId;
    }

    public void setTabPaddingLeftRight(int paddingPx) {
        this.tabPaddingLeftRight = paddingPx;
        updateTabStyles();
    }

    public int getTabPaddingLeftRight() {
        return tabPaddingLeftRight;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
        mViewPager.setCurrentItem(currentPosition);
    }
}
