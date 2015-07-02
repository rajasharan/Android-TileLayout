package com.rajasharan.tilelayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rajasharan on 7/2/15.
 */
public class TileLayout extends ViewGroup {
    private static final String TAG = "TileLayout";

    private int mTileWidth;
    private int mTileHeight;
    private Async2DAdapter mAdapter;
    private Map<Point, View> mTileViews;
    private Point mOrigin;

    public TileLayout(Context context) {
        this(context, null);
    }

    public TileLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TileLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mAdapter = new SimpleTileAdapter(context);
        mTileWidth = mAdapter.getWidth();
        mTileHeight = mAdapter.getHeight();
        mOrigin = new Point(0, 0);
        mTileViews = new HashMap<>();

        mAdapter.setOnViewDataAvailableListener(new Async2DAdapter.OnViewDataAvailableListener() {
            @Override
            public void OnViewDataAvailable(final int dx, final int dy, final View view) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mTileViews.put(new Point(dx, dy), view);
                        view.layout(mOrigin.x+dx, mOrigin.y+dy, mOrigin.x + dx + mTileWidth, mOrigin.y + dy + mTileHeight);
                        invalidate(mOrigin.x+dx, mOrigin.y+dy, mOrigin.x+dx+mTileWidth, mOrigin.y+dy+mTileHeight);
                    }
                });
            }
        });
    }

    private void requestScreenTiles(int width, int height) {
        int x = mOrigin.x;
        int y = mOrigin.y;

        while (y < height) {
            while (x < width) {
                Point p = new Point(x, y);
                if (mTileViews.get(p) == null) {
                    mTileViews.put(new Point(x, y), mAdapter.getView(x, y));
                }
                x = x + mTileWidth;
            }
            x = mOrigin.x;
            y = y + mTileHeight;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        width = Math.max(width, getSuggestedMinimumWidth());
        height = Math.max(height, getSuggestedMinimumHeight());

        setMeasuredDimension(width, height);
        requestScreenTiles(width, height);

        for(View view: mTileViews.values()) {
            view.measure(MeasureSpec.makeMeasureSpec(mTileWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mTileHeight, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        requestScreenTiles(getMeasuredWidth(), getMeasuredHeight());
        for (Point p: mTileViews.keySet()) {
            View view = mTileViews.get(p);
            view.layout(p.x, p.y, p.x+mTileWidth, p.y+mTileHeight);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        for (View view: mTileViews.values()) {
            view.draw(canvas);
        }
    }
}
