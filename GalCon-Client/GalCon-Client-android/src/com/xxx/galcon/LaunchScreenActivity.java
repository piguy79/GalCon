package com.xxx.galcon;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class LaunchScreenActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_launch_screen, menu);
        return true;
    }
}
