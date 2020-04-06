package com.baidu.automap.navi;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.automap.R;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.bikenavi.BikeNavigateHelper;
import com.baidu.mapapi.bikenavi.adapter.IBEngineInitListener;
import com.baidu.mapapi.bikenavi.adapter.IBRoutePlanListener;
import com.baidu.mapapi.bikenavi.model.BikeRoutePlanError;
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.util.ArrayList;

public class GuideActivity extends AppCompatActivity implements OnGetGeoCoderResultListener {
    private BikeNavigateHelper mNaviHelper;
    BikeNaviLaunchParam param;
    private static boolean isPermissionRequested = false;
    private GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    private BaiduMap mBaiduMap = null;
    private MapView mMapView = null;
    private LatLng startPt;
    private LatLng endPt;
    private LatLng tmpPt;
    boolean isFirstLoc = true; // 是否首次定位
    LocationClient mLocClient;
    public MyLocationListenner myListener;
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    public static final String MESSAGE_MAP="0x123";

    private EditText stCity;
    private EditText edCity;
    private EditText stPoi;
    private EditText edPoi;
    private String tmpCity="成都";
    private int btnText=0;
    public String[] map =new String[4];
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_guide);


        Intent intent = getIntent();
        startPt = (LatLng) intent.getParcelableExtra("startNode");
        endPt = (LatLng) intent.getParcelableExtra("endNode");

        myListener = new MyLocationListenner();

        mNaviHelper = BikeNavigateHelper.getInstance();

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();


        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);

//        stCity = (EditText) findViewById(R.id.city1);
//        stPoi = (EditText) findViewById(R.id.poi1);
//        stCity.setText(tmpCity);
//        stPoi.setText("我的位置");

//        edCity = (EditText) findViewById(R.id.city2);
//        edPoi = (EditText) findViewById(R.id.poi2);
//        edCity.setText(tmpCity);

//        edPoi.setText("");

        startBikeNavi();
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * 发起搜索
     *
     * @param v
     */
    public void searchButtonProcess(View v) {
        if(v.getId()==R.id.button){
            startBikeNavi();
        }

        else if (v.getId() == R.id.search_start) {
            if (stPoi.getText().toString().equals("我的位置")){
                startPt = tmpPt;//起点是我的位置
                mBaiduMap.addOverlay(new MarkerOptions().position(tmpPt)
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.icon_st)));
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(tmpPt));


            }else{
                btnText=1;

                mSearch.geocode(new GeoCodeOption().city(
                        stCity.getText().toString()).address(stPoi.getText().toString()));
            }

        } else if (v.getId() == R.id.search_end) {
            //根据地理位置
            btnText=2;
            // 地理位置搜索
            //city(edCity.getText().toString())可以不写，GeoCodeOption共有两个方法，一个是查询城市，一个是查询地址；
            //但是address（）方法必须写
           // mBotton=""
            mSearch.geocode(new GeoCodeOption().city(
                    edCity.getText().toString()).address(edPoi.getText().toString()));
        }
    }

    //初始化引擎
    private void startBikeNavi() {
        Log.d("View", "startBikeNavi");
        mNaviHelper.initNaviEngine(this, new IBEngineInitListener() {
            @Override
            public void engineInitSuccess() {
                Log.d("View", "engineInitSuccess");
                if(startPt.toString()!=null&&endPt.toString()!=null)
                    param = new BikeNaviLaunchParam().stPt(startPt).endPt(endPt).vehicle(1);
                routePlanWithParam();
            }

            @Override
            public void engineInitFail() {
                Log.d("View", "engineInitFail");
            }
        });
    }

    //开始算路
    private void routePlanWithParam() {


        mNaviHelper.routePlanWithParams(param, new IBRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                Log.d("View", "onRoutePlanStart");
            }

            @Override
            public void onRoutePlanSuccess() {
                Log.d("View", "onRoutePlanSuccess");
                Intent intent = new Intent();
                intent.setClass(GuideActivity.this, BNaviGuideActivity.class);
                startActivity(intent);
            }

            @Override
            public void onRoutePlanFail(BikeRoutePlanError error) {
                Log.d("View", "onRoutePlanFail");
            }

        });
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionRequested) {

            isPermissionRequested = true;

            ArrayList<String> permissions = new ArrayList<>();
//            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
//            }

//            if (permissions.size() == 0) {
//                return;
//            } else {
//                requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
//            }
        }
    }
    /**
     * 地理位置查询回调方法
     * @param result 返回的经纬度
     */
    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(GuideActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        //mBaiduMap.clear();
        //获取起点地址并且在地图上标注
        if(btnText==1){
            mBaiduMap.clear();
            mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_st)));
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
                    .getLocation()));
            //终点位置是geocode编码查询的结果
            startPt=result.getLocation();
            btnText=0;
        }
        //获取终点地址并且在地图上标注
        if(btnText==2){
            mBaiduMap.clear();
            mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_en)));
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
                    .getLocation()));
            //终点位置是geocode编码查询的结果
            endPt=result.getLocation();
            btnText=0;
        }

    }

    /**
     * 返回地理位置查询回调方法
     * @param result 返回的地理位置
     */

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(GuideActivity.this, "抱歉，未能找到地址", Toast.LENGTH_LONG)
                    .show();
            return;
        }
//        mBaiduMap.clear();
        //设置起点

        //设置终点获取地址并且标注

//            mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
//                    .icon(BitmapDescriptorFactory
//                            .fromResource(R.drawable.icon_en)));
//            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
//                    .getLocation()));
//



    }
    public class MyLocationListenner  implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                tmpPt = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(tmpPt).zoom(13.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                tmpCity = location.getCity();    //获取定位城市
                String tmpPoi = location.getAddrStr(); //详细地址信息


            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

//    private final Handler mHandler = new Handler() {
//
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what){
//                case MESSAGE_MAP:
//                    String[] map = (String[])msg.obj;
//                    startCity = map[0];
//                    startLocation = map[1];
//                    endCity = map[2];
//                    endLocation = map[3];
//                    break;
//
//
//            }
//
//        }
//    };
    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        mSearch.destroy();
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView = null;
        super.onDestroy();
    }



}