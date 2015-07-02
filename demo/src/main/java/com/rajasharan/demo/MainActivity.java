package com.rajasharan.demo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.rajasharan.tilelayout.TileLayout;
import com.rajasharan.tilelayout.adapters.SimpleTileAdapter;
import com.rajasharan.tilelayout.adapters.api.AsyncAdapter;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = "DEMO-ACTIVITY";
    private TileLayout root;
    private AsyncAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        root = (TileLayout) findViewById(R.id.tiles);
        adapter = new SimpleTileAdapter(this);
        root.setAsyncAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
