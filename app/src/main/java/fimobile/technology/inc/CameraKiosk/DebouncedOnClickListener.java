package fimobile.technology.inc.CameraKiosk;

import android.os.SystemClock;
import android.view.View;

public abstract class DebouncedOnClickListener implements View.OnClickListener {

//    private final long minimumInterval;
//    private Map<View, Long> lastClickMap;
//
//    /**
//     * Implement this in your subclass instead of onClick
//     * @param v The view that was clicked
//     */
//    public abstract void onDebouncedClick(View v);
//
//    /**
//     * The one and only constructor
//     * @param minimumIntervalMsec The minimum allowed time between clicks - any click sooner than this after a previous click will be rejected
//     */
//    public DebouncedOnClickListener(long minimumIntervalMsec) {
//        this.minimumInterval = minimumIntervalMsec;
//        this.lastClickMap = new WeakHashMap<View, Long>();
//    }
//
//    @Override public void onClick(View clickedView) {
//        Long previousClickTimestamp = lastClickMap.get(clickedView);
//        long currentTimestamp = SystemClock.uptimeMillis();
//
//        lastClickMap.put(clickedView, currentTimestamp);
//        if(previousClickTimestamp == null || Math.abs(currentTimestamp - previousClickTimestamp.longValue()) > minimumInterval) {
//            onDebouncedClick(clickedView);
//        }
//    }

    protected int defaultInterval;//override in child class
    private long lastTimeClicked = 0;

    public DebouncedOnClickListener() {
        this(1000);//Default 1 second interval
    }

    public DebouncedOnClickListener(int minInterval) {
        this.defaultInterval = minInterval;
    }

    @Override
    public void onClick(View v) {
        if ((SystemClock.elapsedRealtime() - lastTimeClicked) < defaultInterval) {
            return;
        }
        lastTimeClicked = SystemClock.elapsedRealtime();
        performClick(v);
    }

    //This is the method to call on click, must implement at child class
    public abstract void performClick(View v);


}
