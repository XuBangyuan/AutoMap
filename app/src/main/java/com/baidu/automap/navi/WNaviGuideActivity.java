package com.baidu.automap.navi;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.baidu.automap.R;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;


public class WNaviGuideActivity extends AppCompatActivity {

    private final String KEY = WNaviGuideActivity.class.getName();

    /*
     * 对于导航模块有两种方式来实现发起导航。 1：使用通用接口来实现 2：使用传统接口来实现
     *
     */
    // 是否使用通用接口
    private boolean useCommonInterface = true;
    private WalkNavigateHelper mNaviHelper;
    private MapView mMapView;
    private BaiduMap mBaidumap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_walking_route);


        // 初始化地图
        mMapView = (MapView) findViewById(R.id.map);
        mBaidumap = mMapView.getMap();

        //获取WalkNavigateHelper实例
        mNaviHelper = WalkNavigateHelper.getInstance();
        //获取诱导页面地图展示View
        View view = mNaviHelper.onCreate(WNaviGuideActivity.this);
        if (view != null) {
            setContentView(view);
        }
        mNaviHelper.startWalkNavi(WNaviGuideActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNaviHelper.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNaviHelper.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNaviHelper.quit();
    }
}