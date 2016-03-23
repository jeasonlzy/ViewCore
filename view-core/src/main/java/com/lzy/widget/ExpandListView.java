package com.lzy.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * ================================================
 * 作    者：廖子尧
 * 版    本：1.0
 * 创建日期：2016/3/13
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ExpandListView extends ListView {
    public ExpandListView(Context context) {
        super(context);
    }

    public ExpandListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
