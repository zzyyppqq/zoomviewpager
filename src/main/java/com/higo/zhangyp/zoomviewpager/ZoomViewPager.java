package com.higo.zhangyp.zoomviewpager;

/**
 * Created by zhangyipeng on 15/11/30.
 */

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;


/**
 * Created by zhangyipeng on 15/11/30.
 */
public class ZoomViewPager extends ViewGroup {


    private Scroller scroller;
    private VelocityTracker velocityTracker;
    private float scaleRatio = 0.8f;
    private int distance = 50;
    private int distanceRatio = 30;
    private float density = 0;
    private float distancePx;
    private int width;
    private int mdx;

    public ZoomViewPager(Context context) {
        this(context, null);
    }

    public ZoomViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e("MyViwePager", "getMeasuredWidth():" + getMeasuredWidth());
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        Log.e("MyViwePager", "measuredHeight:" + measuredHeight);

        int childCount = getChildCount();
        distancePx = distance * density;
        width = (int) (measuredWidth - distancePx * 2);
        int height = measuredHeight;
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        Log.e("@MySubViewPager", "width:" + width);

        for (int i = 0; i < childCount; i++) {
            getChildAt(i).measure(childWidthMeasureSpec, childHeightMeasureSpec);

        }
        Log.e("@MySubViewPager", "getChildAt(i).getMeasuredWidth():" + getChildAt(0).getMeasuredWidth());

        scaleRatio = (width - 2 * distanceRatio * density) / width;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            childAt.layout((int) (distancePx) + i * childAt.getMeasuredWidth(), 0, (int) (distancePx) + (i + 1) * childAt.getMeasuredWidth(), getMeasuredHeight());
            Log.e("@MySubViewPager", "childAt.getMeasuredWidth():" + childAt.getMeasuredWidth());
            Log.e("@MySubViewPager", "childAt.getWidth():" + childAt.getWidth());
            if (i != 0) {
                childAt.setScaleX(scaleRatio);
                childAt.setScaleY(scaleRatio);
            }
        }

    }

    private void init() {
        density = getResources().getDisplayMetrics().density;
        scroller = new Scroller(getContext());

    }

    private float mX = 0;
    private int position = 0;
    private boolean flag = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (velocityTracker == null) {

            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);


        float x = event.getX();


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //停止动画
                if (scroller != null) {
                    if (scroller.computeScrollOffset()) {
                        scroller.abortAnimation();
                        Log.e("lMyViwePager", "----停止动画-----");
                    }
                }

                mX = x;

                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (mX - x);

                if (dx >= 0) {
                    flag = true;
                } else if (dx < 0) {
                    flag = false;

                }

                Log.e("MyViwePager", "dx:" + dx);
                scrollBy(dx, 0);
                scale(dx);
                mX = x;

                break;
            case MotionEvent.ACTION_UP:

                final VelocityTracker mVelocityTracker = velocityTracker;
                mVelocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int) mVelocityTracker.getXVelocity();
                Log.e("lMyViwePager", "velocityX:" + velocityX);
                if (velocityX > 600) {

                    position--;
                } else if (velocityX < -600) {

                    position++;
                } else {
                    int halfPosition = (getScrollX() + width / 2) / width;
                    position = halfPosition;
                }

                up();

                mX = x;

                break;
        }


        return true;
    }

    private void scale(int dx) {
        float d = getScrollX() - position * width;
        mdx = dx;
        View nextView = null;
        View currView = null;
        //移动时的变化速率 （0-1）


        float nextScale = 0;
        float currScale = 0;

        Log.e("***", "dx=" + dx+ ",d="+d+",flag="+flag);
        if (d > 0) {//手指向左划

            float scale = (float) (getScrollX() - position * width) / width;
            if (flag) {
                nextView = getChildAt(position + 1);
                currView = getChildAt(position);

                nextScale = scaleRatio + (1.0f - scaleRatio) * scale;
                currScale = 1.0f - (1.0f - scaleRatio) * scale;

            } else {

                nextView = getChildAt(position);
                currView = getChildAt(position + 1);

                currScale = scaleRatio + (1.0f - scaleRatio) * scale;
                nextScale = 1.0f - (1.0f - scaleRatio) * scale;

            }
        } else if (d < 0) {
            float scale = (float) (position * width-getScrollX()) / width;
            if (flag) {
                nextView = getChildAt(position);
                currView = getChildAt(position - 1);

                currScale = scaleRatio + (1.0f - scaleRatio) * scale;
                nextScale = 1.0f - (1.0f - scaleRatio) * scale;
            } else {

                nextView = getChildAt(position - 1);
                currView = getChildAt(position);

                nextScale = scaleRatio + (1.0f - scaleRatio) * scale;
                currScale = 1.0f - (1.0f - scaleRatio) * scale;

            }
        }
        if (currView != null) {
            ViewCompat.setScaleX(currView, currScale);
            ViewCompat.setScaleY(currView, currScale);
            currView.invalidate();
        }
        if (nextView != null) {
            ViewCompat.setScaleX(nextView, nextScale);
            ViewCompat.setScaleY(nextView, nextScale);
            nextView.invalidate();
        }

    }

    private void up() {


        if (position >= getChildCount() - 1) {
            position = getChildCount() - 1;
        } else if (position < 0) {
            position = 0;
        }

        int d = width * position - getScrollX();
        Log.e("MyViwePager", "d:" + d);


        scroller.startScroll(getScrollX(), 0, d, 0, 600);

        invalidate();

    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            scale(mdx);
            postInvalidate();

        }
    }
}

