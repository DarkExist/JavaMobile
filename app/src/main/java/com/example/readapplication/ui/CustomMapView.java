package com.example.readapplication.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.yandex.mapkit.mapview.MapView;

public class CustomMapView extends MapView {
    public CustomMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.onInterceptTouchEvent(ev);
    }
}