package com.yahya.shadow.services;

import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class DoubleClickListener implements View.OnTouchListener {

    private static final int DOUBLE_CLICK_TIME_DELTA = 300; // Time in milliseconds
    private boolean firstClick = false;
    private final Handler handler = new Handler();
    private final GestureDetector gestureDetector;
    private final OnDoubleClickListener listener;

    public interface OnDoubleClickListener {
        void onDoubleClick(View view, int position);
    }

    public DoubleClickListener(Context context, OnDoubleClickListener listener) {
        this.listener = listener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                listener.onDoubleClick(null, -1); // Replace null with actual view if needed
                return true;
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        // Handle single click here
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!firstClick) {
                    firstClick = true;
                    handler.postDelayed(() -> {
                        firstClick = false;
                    }, DOUBLE_CLICK_TIME_DELTA);
                } else {
                    firstClick = false;
                    int position = (int) view.getTag(); // Assuming you set position as a tag
                    listener.onDoubleClick(view, position);
                }
                break;
            default:
                firstClick = false;
        }
        return true;
    }
}