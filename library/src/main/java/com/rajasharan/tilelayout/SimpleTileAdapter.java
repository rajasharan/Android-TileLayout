package com.rajasharan.tilelayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created by rajasharan on 7/2/15.
 */

/**
 * Reference implementation of an Async Tile Adapter which generates random colors
 */
public class SimpleTileAdapter implements Async2DAdapter, Handler.Callback {
    private static final String THREAD = "TileAdapter-Thread";
    private static final int WHAT_GET_VIEW = 0;
    private static final int[] COLORS = {Color.BLUE, Color.GREEN, Color.RED, Color.MAGENTA, Color.CYAN};
    private static int CYCLE = 0;
    private static final int SIMULATION_DELAY_MILLIS = 2000;

    private static final int SIZE = 250;
    private Context mContext;
    private OnViewDataAvailableListener mListener;
    private Handler mHandler;

    public SimpleTileAdapter(Context context) {
        mContext = context;

        HandlerThread thread = new HandlerThread(THREAD);
        thread.start();
        mHandler = new Handler(thread.getLooper(), this);
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
    public View getView(int dx, int dy) {
        Point point = new Point(dx, dy);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(WHAT_GET_VIEW, point),
                SIMULATION_DELAY_MILLIS + (long)(SIMULATION_DELAY_MILLIS * Math.random()));
        return new ColorTileView(mContext, Color.LTGRAY);
    }

    @Override
    public void setOnViewDataAvailableListener(OnViewDataAvailableListener listener) {
        mListener = listener;
    }

    @Override
    public boolean handleMessage(Message msg) {
        Log.d(Thread.currentThread().getName(), msg.toString());
        switch (msg.what) {
            case WHAT_GET_VIEW:
                Point p = (Point) msg.obj;
                CYCLE = CYCLE % COLORS.length;
                View view = new ColorTileView(mContext, COLORS[CYCLE++]);
                mListener.OnViewDataAvailable(p.x, p.y, view);
                break;
        }
        return true;
    }
}

class ColorTileView extends View {
    private Paint mPaint;

    public ColorTileView(Context context, int color) {
        super(context);
        init(color);
    }

    private void init(int color) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(color);
        mPaint.setAlpha(255);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), mPaint);
    }
}
