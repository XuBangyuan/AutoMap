package com.baidu.automap.navi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;

public class WNaviGuideActivity extends Activity {

    private WalkNavigateHelper mNaviHelper;
    private static String KEY = "wNaviGuideActivity";


    @Override
    protected void onDestroy() {
        mNaviHelper.quit();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        mNaviHelper.resume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mNaviHelper.pause();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        Log.d(KEY, "back pressed!");
        setResult(RESULT_OK, null);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNaviHelper = WalkNavigateHelper.getInstance();

        View view = mNaviHelper.onCreate(WNaviGuideActivity.this);
        if (view != null) {
            setContentView(view);
        }

        mNaviHelper.startWalkNavi(WNaviGuideActivity.this);
    }

}
