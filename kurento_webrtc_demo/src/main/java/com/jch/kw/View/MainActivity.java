package com.jch.kw.View;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.jch.kw.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button masterbtn;
    private Button viewerbtn;
    private ListView roomlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items.
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {

    }

    private void initialize() {

        masterbtn = (Button) findViewById(R.id.master_btn);
        viewerbtn = (Button) findViewById(R.id.viewer_btn);
        roomlist = (ListView) findViewById(R.id.roomlist);
    }
}
