package com.rajasharan.tilelayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.rajasharan.tilelayout.adapters.api.AsyncAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajasharan on 7/2/15.
 */
public class TileLayout extends ViewGroup {
    private static final String TAG = "TileLayout";

    private int mTileWidth;
    private int mTileHeight;
    private AsyncAdapter<Point> mAdapter;
    private List<View> mTileViews;
    private List<View> mOffscreenViews;
    private Point mOrigin;
    private Point mOriginTouchDown;
    private Point mStartTouch;
    private boolean mScreenTilesRequested;

    public TileLayout(Context context) {
        this(context, null);
    }

    public TileLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TileLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mOrigin = new Point(0, 0);
        mStartTouch = null;
        mScreenTilesRequested = false;
        mTileViews = new ArrayList<>();
        mOffscreenViews = new ArrayList<>();
    }

    /**
     * request tiles from adapter to fill up the screen bounded by (left,top - right,bottom)
     */
    private void requestScreenTiles(int left, int top, int right, int bottom) {
        if (mScreenTilesRequested) {
            return;
        }
        int x = left;
        int y = top;
        //mTileViews.clear();
        int i=0;
        while (y < bottom) {
            while (x < right) {
                Point p = new Point(x, y);
                View tile = mAdapter.getView(p);
                mTileViews.add(tile);
                x = x + mTileWidth;
            }
            x = left;
            y = y + mTileHeight;
        }
        mScreenTilesRequested = true;
        Log.d(TAG, "requestScreenTiles: getChildCount(): " + getChildCount());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        width = Math.max(width, getSuggestedMinimumWidth());
        height = Math.max(height, getSuggestedMinimumHeight());

        setMeasuredDimension(width, height);
        requestScreenTiles(0, 0, width, height);

        //Log.d(TAG, "onMeasure: getChildCount(): " + getChildCount());

        for(View view: mTileViews) {
            view.measure(MeasureSpec.makeMeasureSpec(mTileWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mTileHeight, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!changed) {
            return;
        }
        layoutTiles();
        //Log.d(TAG, String.format("onLayout(%s): %s", changed, this.toString()));
    }

    private void layoutTiles() {
        //Log.d(TAG, "layoutTiles: getChildCount(): " + getChildCount());
        for (View view: mTileViews) {
            Point p = (Point) view.getTag();
            view.layout(p.x, p.y, p.x+mTileWidth, p.y+mTileHeight);
            //Log.d(TAG, view.toString());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //Log.d(TAG, "dispatchDraw: getChildCount(): " + getChildCount());
        for (View view: mTileViews) {
            Point p = (Point) view.getTag();
            int savepoint = canvas.save();
            boolean clipped = canvas.clipRect(p.x, p.y, p.x+mTileWidth, p.y+mTileHeight);
            view.draw(canvas);
            //Log.d(TAG, clipped + ": " + canvas.getClipBounds() + view.toString());
            canvas.restoreToCount(savepoint);
        }
    }

    /**
     * Implement and add a custom AsyncAdapter to this layout. {@link Point} is used as tag.
     * The async adapter provides the required views to this layout.
     *
     * @param adapter the custom implementation of AsyncAdapter
     */
    public void setAsyncAdapter(AsyncAdapter<Point> adapter) {
        mAdapter = adapter;
        mTileWidth = mAdapter.getWidth();
        mTileHeight = mAdapter.getHeight();

        mAdapter.setOnViewAvailableListener(new AsyncAdapter.OnViewAvailableListener<Point>() {
            @Override
            public void OnViewAvailable(final Point tag, final View view) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        insertTileAtPoint(view, tag);
                    }
                });
            }
        });
    }

    /**
     * insert tiles at given point (left, top) and kick off layout/invalidation cycle only for the given tile
     * @param child the tile to be added to the layout hierarchy
     */
    private void insertTileAtPoint(View child, Point point) {
        child.measure(MeasureSpec.makeMeasureSpec(mTileWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mTileHeight, MeasureSpec.EXACTLY));
        child.layout(point.x, point.y, point.x+mTileWidth, point.y+mTileHeight);
        replaceTileAtPoint(child, point);
        invalidate(point.x, point.y, point.x + mTileWidth, point.y + mTileHeight);
    }

    private void replaceTileAtPoint(View tile, Point point) {
        for (int i=0; i<mTileViews.size(); i++) {
            View view = mTileViews.get(i);
            Point p = (Point) view.getTag();
            if (p.equals(point)) {
                mTileViews.remove(view);
                break;
            }
        }
        mTileViews.add(tile);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mStartTouch = new Point(x, y);
                mOriginTouchDown = new Point(mOrigin);
                break;

            case MotionEvent.ACTION_MOVE:
                int dx = x - mStartTouch.x;
                int dy = y - mStartTouch.y;

                if (dx <= mTileWidth/2) dx = 0;
                else if (dx > mTileWidth/2) dx = mTileWidth;

                if (dy <= mTileHeight/2) dy = 0;
                else if (dy > mTileHeight/2) dy = mTileHeight;

                mOrigin.x = mOriginTouchDown.x + dx;
                mOrigin.y = mOriginTouchDown.y + dy;

                relayoutTiles();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                int dx1 = x - mStartTouch.x;
                int dy1 = y - mStartTouch.y;
                addNewTiles(dx1, dy1);
                removeOldTiles(dx1, dy1);
                break;
        }
        Log.d(TAG, mOrigin.toString());
        return false;
    }

    private void relayoutTiles() {
        requestScreenTiles(-mTileWidth, -mTileHeight, getWidth()-mTileWidth, getHeight()-mTileHeight);
        layoutTiles();
        invalidate();
    }

    private void addNewTiles(int dx, int dy) {
        if (dx > 0) {

        } else if (dx < 0) {

        }

        if (dy > 0) {

        } else if (dy < 0) {

        }
    }

    private void removeOldTiles(int dx, int dy) {

    }
}
