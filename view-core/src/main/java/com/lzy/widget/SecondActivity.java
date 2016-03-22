package com.lzy.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SecondActivity extends AppCompatActivity {

    @Bind(R.id.param) EditText param;
    private String activityName;
    private String listenerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        activityName = intent.getStringExtra("activityName");
        listenerName = intent.getStringExtra("listenerName");
    }

    /**
     * 反射外部类中的方法
     */
    @OnClick(R.id.outerMethod)
    public void outerMethod(View view) {
        try {
            Context c = createPackageContext(getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            Class activityClazz = c.getClassLoader().loadClass(activityName);
            Object activity = activityClazz.newInstance();
            Method method = activityClazz.getMethod("print", Context.class, String.class);
            method.invoke(activity, this, param.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 反射内部类中的方法
     */
    @OnClick(R.id.innerMethod)
    public void innerMethod(View view) {
        try {
            Context c = createPackageContext(getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            Class activityClazz = c.getClassLoader().loadClass(activityName);
            Object activity = activityClazz.newInstance();
            Class listenerClazz = Class.forName(listenerName);
            Constructor constructor = listenerClazz.getDeclaredConstructor(activityClazz);
            //这里创建内部类需要外部类的引用，如果外部类有无参构造，那么可以直接使用内部类的class创建对象
            //但是如果外部类没有午餐构造，那么必须先创建外部类实例，才能创建内部类实例
            Listener listener = (Listener) constructor.newInstance(activity);
            listener.onImageDisplay(this, param.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
