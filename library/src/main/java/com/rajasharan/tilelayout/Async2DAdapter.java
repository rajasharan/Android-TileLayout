package com.rajasharan.tilelayout;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by rajasharan on 7/2/15.
 */

/**
 * Async2DAdapter represents view data over an infinite 2D surface
 * with any arbitrary origin picked to be the absolute reference point
 */
public interface Async2DAdapter {
    /**
     * width of a single view tile
     * @return
     */
    int getWidth();

    /**
     * height of a single view tile
     * @return
     */
    int getHeight();

    /**
     * Returns the View whose left,top are dx,dy distance away from origin.
     * Can return blank view when data still not available.
     * @param dx View's left coordinate dx distance away from origin
     * @param dy View's top coordinate dy distance away from origin
     * @return immediately return default view if no view available
     */
    View getView(int dx, int dy);

    void setOnViewDataAvailableListener(OnViewDataAvailableListener listener);

    interface OnViewDataAvailableListener {
        /**
         * This method is invoked when the view is now available
         * whose left,top are at dx,dy distances from origin.
         * <br>
         * <b>Note:</b> method will/should be called on non-UI thread. Use View#post(Runnable) to do UI work
         *
         * @param dx view's left coord dx distanct away from origin
         * @param dy view's top coord dy distance away from origin
         * @param view the available view
         */
        void OnViewDataAvailable(int dx, int dy, View view);
    }
}
