package com.rajasharan.tilelayout.adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.View;

import com.rajasharan.tilelayout.adapters.api.AsyncAdapter;

/**
 * Created by rajasharan on 7/2/15.
 */

/**
 * Reference implementation of {@link AsyncAdapter}.
 * Generates SKY_BLUE colored tiles after a SIMULATION_DELAY time period.
 */
public class SimpleTileAdapter extends AsyncAdapter<Point> {
    private static final int SIZE = 250;
    private static final int SIMULATION_DELAY_MILLIS = 2000;

    private Context mContext;

    public SimpleTileAdapter(Context context) {
        this(context, null, SIMULATION_DELAY_MILLIS);
    }

    public SimpleTileAdapter(Context context, OnViewAvailableListener listener, int delay) {
        super(listener, delay);
        mContext = context;
    }

    @Override
    public int getWidth() {
        return SIZE;
    }

    @Override
    public int getHeight() {
        return SIZE;
    }

    @Override
    protected View getDefaultView(Point tag) {
        return new TileView(mContext, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Override
    public View getViewInBackground(Point tag) {
        return new TileView(mContext, tag.x, tag.y);
    }

    /**
     * dummy Tile representation for SimpleTileAdapter. Displays tile with X or SKY_BLUE color.
     */
    private static class TileView extends View {
        private static final int SKY_BLUE = Color.rgb(135, 206, 250);
        private Paint mBorderPaint;
        private Paint mLoadingPaint;
        private Integer mHascode;
        private int mScore;

        public TileView(Context context, int x, int y) {
            super(context);
            init(x, y);
        }

        private void init(int x, int y) {
            mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBorderPaint.setStyle(Paint.Style.STROKE);
            mBorderPaint.setColor(Color.LTGRAY);

            mLoadingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLoadingPaint.setStyle(Paint.Style.STROKE);

            if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE) {
                mHascode = null;
            }
            else {
                mHascode = 31 * x + y;
                mScore = (Math.abs(mHascode)/3) % 255;
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int l = getLeft();
            int t = getTop();
            int r = getRight();
            int b = getBottom();
            int w = getWidth();
            int h = getHeight();
            canvas.drawRect(l, t, r, b, mBorderPaint);
            if (mHascode == null) {
                canvas.drawLine(l+w/4, t+h/4, r-w/4, b-h/4, mLoadingPaint);
                canvas.drawLine(r-w/4, t+h/4, l+w/4, b-h/4, mLoadingPaint);
                //canvas.drawCircle(l + w/2, t + h/2, w/4, mLoadingPaint);
            } else {
                canvas.drawColor(Color.argb(mScore, mScore, mScore, mScore));
            }
        }
    }
}


