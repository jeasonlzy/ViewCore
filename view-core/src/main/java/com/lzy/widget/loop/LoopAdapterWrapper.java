package com.lzy.widget.loop;

import android.os.Parcelable;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * ================================================
 * 作    者：廖子尧
 * 版    本：1.0
 * 创建日期：2015/10/15
 * 描    述：用该类包装一个需要实现循环滚动的Adapter
 * 修订历史：
 * ================================================
 */
public class LoopAdapterWrapper extends PagerAdapter {

    private PagerAdapter mAdapter;
    private int realFirst;
    private int realLast;

    public LoopAdapterWrapper(PagerAdapter adapter) {
        this.mAdapter = adapter;
        realFirst = 1;
        realLast = realFirst + getRealCount() - 1;
    }

    @Override
    public int getCount() {
        return mAdapter.getCount() + 2;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int realPosition = toRealPosition(position);
        return mAdapter.instantiateItem(container, realPosition);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        boolean flag = mAdapter instanceof FragmentPagerAdapter || mAdapter instanceof FragmentStatePagerAdapter;
        int realPosition = toRealPosition(position);

        //头尾的两个一直不销毁
        if (flag && (position <= realFirst || position >= realLast)) {
            return;
        }
        mAdapter.destroyItem(container, realPosition, object);
    }

    /**
     * original adapter position    [0,1,2,3]
     * modified adapter position  [0,1,2,3,4,5]
     * modified     realPosition  [3,0,1,2,3,0]
     * modified     InnerPosition [4,1,2,3,4,1]
     */
    protected int toRealPosition(int position) {
        int realCount = getRealCount();
        if (realCount == 0)
            return 0;
        int realPosition = (position - 1) % realCount;
        if (realPosition < 0)
            realPosition = realPosition + realCount;
        return realPosition;
    }

    /**
     * 根据传进来的真实位子，得到该 loopAdapter 的真实条目位置
     */
    public int getInnerPosition(int realPosition) {
        return realPosition + 1;
    }

    public int getRealCount() {
        return mAdapter.getCount();
    }

    //重写对Adapter的操作
    @Override
    public void finishUpdate(ViewGroup container) {
        mAdapter.finishUpdate(container);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return mAdapter.isViewFromObject(view, object);
    }

    @Override
    public void restoreState(Parcelable bundle, ClassLoader classLoader) {
        mAdapter.restoreState(bundle, classLoader);
    }

    @Override
    public Parcelable saveState() {
        return mAdapter.saveState();
    }

    @Override
    public void startUpdate(ViewGroup container) {
        mAdapter.startUpdate(container);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        mAdapter.setPrimaryItem(container, position, object);
    }
}