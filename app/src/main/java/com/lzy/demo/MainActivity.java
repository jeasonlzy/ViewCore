package com.lzy.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ArrayList<Project> projects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        projects = new ArrayList<>();
        projects.add(new Project("HexagonView", "六边形带圆角的自定义View，支持图文混排，点击区域，水平垂直方向切换，圆角大小等各种属性", "https://github.com/jeasonlzy0216/HexagonView"));
        projects.add(new Project("AlphaView", "仿微信底部tab标签，滑动的时候颜色渐变，使用极其简单，只需要两行代码。", "https://github.com/jeasonlzy0216/AlphaIndicatorView"));
        projects.add(new Project("OverScrollDecor", "类似IOS的over-scrolling效果，即对于滑动到顶部的View继续滑动时会超出，松手后自动还原到原始位置。支持ListView，GridView，ScrollView，WebView，RecyclerView，以及其他的任意View和ViewGroup。", "https://github.com/jeasonlzy0216/OverScrollDecor"));
        projects.add(new Project("PullZoomView", "类似QQ空间，新浪微博个人主页下拉头部放大的布局效果，支持ListView，GridView，ScrollView，WebView，RecyclerView，以及其他的任意View和ViewGroup。支持头部视差动画，阻尼下拉放大，滑动过程监听。", "https://github.com/jeasonlzy0216/PullZoomView"));
        projects.add(new Project("VerticalSlide", "类似淘宝的商品详情页，继续拖动查看详情，其中拖动增加了阻尼，并且重写了ListView，GridView，ScrollView，WebView，RecyclerView 的 dispatchTouchEvent 方法，使用的时候无须额外的代码，可以任意嵌套使用。", "https://github.com/jeasonlzy0216/VerticalSlideView"));
        projects.add(new Project("HeaderViewPager", "具有共同头部的 ViewPager，支持与ListView，GridView，ScrollView，WebView，RecyclerView 嵌套使用。具有连续的滑动事件 和 滑动监听， 支持下拉刷新。", "https://github.com/jeasonlzy0216/HeaderViewPager"));
        projects.add(new Project("CircleImageView", "(暂时没有demo)圆形的ImageView，用法同ImageView一样", ""));
        projects.add(new Project("LoopViewPager", "(暂时没有demo)可以循环滑动和自动轮播的ViewPager", ""));
        projects.add(new Project("TabTitleIndicator", "(暂时没有demo)ViewPager的指示器", ""));
        projects.add(new Project("CircleIndicator", "(暂时没有demo)ViewPager的点状指示器", ""));

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new MyAdapter());
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("url", projects.get(position).getUrl());
        startActivity(intent);
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return projects.size();
        }

        @Override
        public Project getItem(int position) {
            return projects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.item_list, null);
                convertView.setTag(new ViewHolder(convertView));
            }

            Project project = getItem(position);
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.title.setText(project.title);
            holder.desc.setText(project.desc);
            return convertView;
        }
    }

    static class ViewHolder {
        TextView title;
        TextView desc;

        public ViewHolder(View convertView) {
            title = (TextView) convertView.findViewById(R.id.title);
            desc = (TextView) convertView.findViewById(R.id.desc);
        }
    }
}
