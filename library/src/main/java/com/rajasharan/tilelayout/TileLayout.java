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

    private void measureAndLayoutTile(View view, Point tag) {
        view.measure(MeasureSpec.makeMeasureSpec(mTileWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mTileHeight, MeasureSpec.EXACTLY));

        int l = tag.x + mOrigin.x;
        int t = tag.y + mOrigin.y;

        view.layout(l, t, l+mTileWidth, t+mTileHeight);
    }

    private void invalidateTile(View view, Point tag) {
        int l = tag.x + mOrigin.x;
        int t = tag.y + mOrigin.y;

        invalidate(l, t, l+mTileHeight, t+mTileHeight);
    }

    private void drawTile(Canvas canvas, View view, Point tag) {
        int l = tag.x + mOrigin.x;
        int t = tag.y + mOrigin.y;
        int savepoint = canvas.save();
        boolean clipped = canvas.clipRect(l, t, l+mTileWidth, t+mTileHeight);
        view.draw(canvas);
        //Log.d(TAG, clipped + ": " + canvas.getClipBounds() + view.toString());
        canvas.restoreToCount(savepoint);
    }

    /**
     * request tiles from adapter to fill up the screen bounded by (left,top - right,bottom)
     */
    private void requestScreenTiles(int left, int top, int right, int bottom) {
        if (mScreenTilesRequested) {
            return;
        }
        left = left - mOrigin.x;
        top = left - mOrigin.y;
        right = right - mOrigin.x;
        bottom = bottom - mOrigin.y;

        int x = left;
        int y = top;
        //mTileViews.clear();
        while (y < bottom) {
            while (x < right) {
                Point p = new Point(x, y);
                View tile = mAdapter.getView(p);
                measureAndLayoutTile(tile, p);
                mTileViews.add(tile);
                x = x + mTileWidth;
            }
            x = left;
            y = y + mTileHeight;
        }
        mScreenTilesRequested = true;
        //Log.d(TAG, "requestScreenTiles: getChildCount(): " + getChildCount());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        width = Math.max(width, getSuggestedMinimumWidth());
        height = Math.max(height, getSuggestedMinimumHeight());

        setMeasuredDimension(width, height);
        requestScreenTiles(0, 0, width, height);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //Log.d(TAG, "dispatchDraw: getChildCount(): " + getChildCount());
        for (View view: mTileViews) {
            Point p = (Point) view.getTag();
            drawTile(canvas, view, p);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        /* no need to layout here, already done during tile creation */
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
        measureAndLayoutTile(child, point);
        replaceTileAtPoint(child, point);
        invalidateTile(child, point);
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

                if (Math.abs(dx) <= mTileWidth/2) dx = 0;
                else if (Math.abs(dx) > mTileWidth/2) dx = (dx > 0)? mTileWidth: -mTileWidth;

                if (Math.abs(dy) <= mTileHeight/2) dy = 0;
                else if (Math.abs(dy) > mTileHeight/2) dy = (dy > 0)? mTileHeight: -mTileHeight;

                mOrigin.x = mOriginTouchDown.x + dx;
                mOrigin.y = mOriginTouchDown.y + dy;

                if (Math.abs(dx) > 0 || Math.abs(dy) > 0) {
                    mStartTouch = new Point(x, y);
                    mOriginTouchDown = new Point(mOrigin);
                    relayoutTiles();
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                break;
        }
        Log.d(TAG, mOrigin.toString());
        return true;
    }

    private void relayoutTiles() {
        for (View view: mTileViews) {
            Point p = (Point) view.getTag();
            measureAndLayoutTile(view, p);
            invalidateTile(view, p);
        }
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
