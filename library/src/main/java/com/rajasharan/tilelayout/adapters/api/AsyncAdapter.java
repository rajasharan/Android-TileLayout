package com.rajasharan.tilelayout.adapters.api;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.View;

/**
 * Created by rajasharan on 7/2/15.
 */

/**
 * AsyncAdapter provides API to retrieve Views in background represented by tags.
 * For eg: to retrieve views in a 2D surface you can tag the properties of the View in your data-model.
 * For reference implementation see {@link com.rajasharan.tilelayout.adapters.SimpleTileAdapter}.
 */
public abstract class AsyncAdapter<T> implements Handler.Callback {
    private static final String THREAD = "Adapter-Thread";
    private static final int WHAT_GET_VIEW = 0;
    private static int SIMULATION_DELAY_MILLIS;

    private Handler mHandler;
    private OnViewAvailableListener mListener;

    /**
     * Default constructor.
     * But remember to call <b>setOnViewAvailableListener(...)</b>
     */
    public AsyncAdapter() {
        this(null, 0);
    }

    /**
     *
     * @param listener callback listener to be called when new View is available
     */
    public AsyncAdapter(OnViewAvailableListener<T> listener) {
        this(listener, 0);
    }

    protected AsyncAdapter(OnViewAvailableListener<T> listener, int delay) {
        HandlerThread thread = new HandlerThread(THREAD);
        thread.start();
        mHandler = new Handler(thread.getLooper(), this);
        SIMULATION_DELAY_MILLIS = delay;
        mListener = listener;
    }

    /**
     *
     * @return width of the view
     */
    public abstract int getWidth();

    /**
     *
     * @return height of the view
     */
    public abstract int getHeight();

    /**
     * Returns the View represented by tag in your data-model.
     *
     * @param tag view's identifier tag from your data-model.
     * @return the default view is returned and a message sent to Adapter-Thread to work on real view
     */
    public final View getView(T tag) {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(WHAT_GET_VIEW, tag),
                SIMULATION_DELAY_MILLIS + (long)(SIMULATION_DELAY_MILLIS * Math.random()));

        return getDefaultView(tag);
    }

    /**
     * Return the default view while real view is being created in background.
     *
     * @param tag view's identifier in your data-model.
     * @return must immediately return default view
     */
    protected abstract View getDefaultView(T tag);

    /**
     * This method is called on non-UI background thread.
     * The real view can be created here from your data-model represented by tag.
     *
     * @param tag the view's identifier in your data-model.
     * @return the real view represented by tag in your data-model.
     */
    public abstract View getViewInBackground(T tag);

    @Override
    public boolean handleMessage(Message msg) {
        //Log.d(Thread.currentThread().getName(), msg.toString());
        switch (msg.what) {
            case WHAT_GET_VIEW:
                T tag = (T) msg.obj;
                View view = getViewInBackground(tag);
                view.setTag(tag);
                mListener.OnViewAvailable(tag, view);
                break;
        }
        return true;
    }

    /**
     * Intended to be called by the Root Layout that acts as an AdapterView and
     * needs its views to be available on an async basis.
     * <br>
     * Unless you are implementing your own ViewGroup don't call this method directly.
     */
    public void setOnViewAvailableListener(OnViewAvailableListener<T> listener) {
        mListener = listener;
    }

    /**
     * Listener callback interface when View is available.
     */
    public interface OnViewAvailableListener<T> {
        /**
         * This method is invoked when the newly created view is available.
         * <br>
         * <b>Note: </b> This method is inoked on non-UI background thread.
         * Use post(Runnable) to interact with UI thread.
         *
         * @param tag view's identifier in your data-model
         * @param view the newly created/available view.
         */
        void OnViewAvailable(final T tag, final View view);
    }
}
