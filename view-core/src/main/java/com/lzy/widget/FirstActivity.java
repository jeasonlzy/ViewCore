package com.lzy.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FirstActivity extends AppCompatActivity {

    private MyListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        ButterKnife.bind(this);
        listener = new MyListener();
    }

    @OnClick(R.id.open)
    public void open(View view) {
        Intent intent = new Intent(FirstActivity.this, SecondActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("activityName", FirstActivity.class.getName());
        bundle.putString("listenerName", MyListener.class.getName());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public class MyListener extends Listener {

        @Override
        public void onItemClick() {

        }

        @Override
        public void onImageDisplay(Context context, String text) {
            System.out.println("我是FirstActivity的内部类的方法:" + text);
            Toast.makeText(context, "我是FirstActivity的内部类的方法:" + text, Toast.LENGTH_SHORT).show();
        }
    }

    public void print(Context context, String text) {
        System.out.println("我是FirstActivity的方法:" + text);
        Toast.makeText(context, "我是FirstActivity的的方法:" + text, Toast.LENGTH_SHORT).show();
    }
}
