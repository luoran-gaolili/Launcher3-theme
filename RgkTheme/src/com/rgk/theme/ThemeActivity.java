package com.rgk.theme;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import com.rgk.theme.R;

public class ThemeActivity extends Activity{
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         //TODO Auto-generated method stub
        switch (item.getItemId()) {
        case android.R.id.home:
            // app icon in action bar clicked; go home
            Log.d(TAG, "r.id.home click!");
            onBackPressed();
        default:
            return super.onOptionsItemSelected(item);
    }
    }

    /** Called when the activity is first created. */
    private static final String TAG = "ThemeActivity";
    private ActionBar mActionBar;
    private FragmentManager mFragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.theme);
        initActionBar();
        mFragmentManager = getFragmentManager();
        PreviewFragment fragment = new PreviewFragment();
        FragmentTransaction tran = mFragmentManager.beginTransaction();
        tran.add(R.id.main,fragment,"pre");
        tran.commit();
    }

    void initActionBar(){
        mActionBar = getActionBar();
        mActionBar.setHomeButtonEnabled(true);
    }

}