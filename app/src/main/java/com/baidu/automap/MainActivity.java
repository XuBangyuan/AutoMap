package com.baidu.automap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;

import com.baidu.automap.search.PoiSugSearchDemo;
import com.baidu.automap.search.ResultEntity;
import com.baidu.automap.searchroute.WalkingRouteSearchDemo;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.DistanceUtil;

import java.text.DecimalFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnGetPoiSearchResultListener {

    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;

    //是否替换back键动作
    boolean isFirstLocate = true;

    //poi检索
    private PoiSearch mPoiSearch = null;

    private SDKReceiver mReceiver;

    //地点信息
    private LinearLayout line = null;
    private Button searchButton = null;
    private Button goBackButton = null;

    private LinearLayout briedIntroductionLinear = null;
    private TextView buildNameText = null;
    private LinearLayout locInfoLinear = null;
    private TextView buildToMeDisText = null;
    private TextView locDescriptionText = null;
    private TextView openTimeText = null;
    private LinearLayout browseButtonLinear = null;
    private Button buildDetailButton = null;
    private Button buildSurroundButton = null;
    private Button buildInsertIntoRouteButton = null;
    private Button buildGuildButton = null;
    //路线规划按钮
    private Button routePlanButton = null;

    //poi_sug搜索
    private static final int POI_SUG = 1;
    //步行导航
    private static final int WALKING_ROUTE = 2;
    private static final int POI_SUG_SINGLE = 1;
    private static final int POI_SUG_LIST = 2;

    //当前定位地点
    private BDLocation curLocation = null;
    //导航起点
    private LatLng startNode = null;
    //导航终点
    private LatLng endNode = null;



    private static final String KEY = "mainActivity";
    private static boolean isBack = false;

    public class SDKReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                //key验证失败，做相应处理
            } else if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                //key验证成功，做相应处理
                Log.d(KEY, "验证成功");
            }
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "mainActivity first");
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);


        line = (LinearLayout) findViewById(R.id.line);
        searchButton = (Button) findViewById(R.id.search_button);
        goBackButton = (Button) findViewById(R.id.go_back_button);

        briedIntroductionLinear = (LinearLayout) findViewById(R.id.brief_introduction);
        buildNameText = (TextView) findViewById(R.id.build_name);
        buildToMeDisText = (TextView) findViewById(R.id.build_to_me_distance);
        locDescriptionText = (TextView) findViewById(R.id.loc_description);
        openTimeText = (TextView) findViewById(R.id.open_time);
        buildDetailButton = (Button) findViewById(R.id.build_detail_button);
        buildSurroundButton = (Button) findViewById(R.id.build_surround_button);
        buildInsertIntoRouteButton = (Button) findViewById(R.id.build_insert_into_route_button);
        buildGuildButton = (Button) findViewById(R.id.build_guild_button);
        routePlanButton = (Button) findViewById(R.id.route_plan);

        // 创建poi检索实例，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

//        briedIntroductionLinear.setZ(0.f);
        briedIntroductionLinear.setVisibility(View.GONE);
        line.setZ(4.f);
        briedIntroductionLinear.setZ(5.f);

        //注册广播
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);


        //marker点击
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            //marker被点击时回调的方法
            //若响应点击事件，返回true，否则返回false
            //默认返回false
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d(KEY, "marker onclick begin");
                ResultEntity result = (ResultEntity) marker.getExtraInfo().getSerializable("result");
//                isBack = true;
                Log.d(KEY, "marker onClick" + result.getuId());
                searchButtonProcess(result.getuId());
                return true;
            }
        });

        //搜索按钮点击
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("mainActivity", "searchButton onclick, curCity : " + curLocation.getCity());
                clearPage();
                Intent intent = new Intent(MainActivity.this, PoiSugSearchDemo.class);
                Bundle bundle = new Bundle();
                bundle.putString("city",curLocation.getCity());
                intent.putExtras(bundle);
                //Log.d("mainActivity", bundle.getString("city"));
                startActivityForResult(intent, POI_SUG);

            }
        });

        //poi导航按钮点击
        buildGuildButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(KEY, "build_guild_button onclick");
                startNode = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
                routeSearchBegin();
            }
        });

        //归位按钮点击
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curLocation != null) {
                    LatLng latLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
                    navigateTo(latLng);
                }
            }
        });

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher_background);
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, bitmapDescriptor));

        //单击事件
        BaiduMap.OnMapClickListener mapOnClickListener = new BaiduMap.OnMapClickListener() {
            /**
             * 地图单击事件回调函数
             *
             * @param point 点击的地理坐标
             */
            @Override
            public void onMapClick(LatLng point) {
                Log.d(KEY, point.latitude+ " , " +  point.longitude);
            }

            /**
             * 地图内 Poi 单击事件回调函数
             *
             * @param mapPoi 点击的 poi 信息
             */
            @Override
            public void onMapPoiClick(MapPoi mapPoi) {
                Log.d(KEY, "onMapPoiClick " + mapPoi.getName());
                Log.d(KEY, mapPoi.getPosition().latitude + " , " + mapPoi.getPosition().longitude);
                mPoiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUids(mapPoi.getUid()));
            }
        };
        //设置地图单击事件监听
        mBaiduMap.setOnMapClickListener(mapOnClickListener);

        //定位初始化
        mLocationClient = new LocationClient(this);

        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true); // 打开gps
        option.setIsNeedAddress(true);
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(0);

        //设置locationClientOption
        mLocationClient.setLocOption(option);

        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();
    }

    /**
     * 页面重置
     */
    private void clearPage() {
        briedIntroductionLinear.setVisibility(View.GONE);
    }

    /**
     * 发起详情检索
     */
    public void searchButtonProcess(String uId){
        // 该方法要在Listener之后执行，否则会在某些场景出现拿不到回调结果的情况
        mPoiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUids(uId));
    }

    /**
     * 开始导航
     * @param
     */
    public void routeSearchBegin() {

        Intent intent = new Intent(MainActivity.this, WalkingRouteSearchDemo.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("startNode", startNode);
        bundle.putParcelable("endNode", endNode);
        bundle.putString("city", curLocation.getCity());
        intent.putExtras(bundle);
        //Log.d("mainActivity", bundle.getString("city"));
        startActivity(intent);
    }


    @Override
    public void onGetPoiResult(PoiResult result) {

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {

    }

    /**
     * poi 详情查询结果回调
     * V5.2.0版本新增接口，用于返回详细检索结果
     *
     * @param poiDetailSearchResult poi详情查询结果
     */
    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
        if (poiDetailSearchResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else {
            List<PoiDetailInfo> poiDetailInfoList = poiDetailSearchResult.getPoiDetailInfoList();
            if (null == poiDetailInfoList || poiDetailInfoList.isEmpty()) {
                Toast.makeText(MainActivity.this, "抱歉，检索结果为空", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int i = 0; i < poiDetailInfoList.size(); i++) {
                // 获取详情检索结果
                PoiDetailInfo poiDetailInfo = poiDetailInfoList.get(i);
                if (null != poiDetailInfo) {
                    // 展示Detail 相关信息
                    Log.d(KEY, poiDetailInfo.getName());
                    Log.d(KEY, poiDetailInfo.getAddress());
                    Log.d(KEY, Integer.valueOf(poiDetailInfo.getDistance()).toString());
                    Log.d(KEY, poiDetailInfo.getShopHours());

                    addDetailInfor(poiDetailInfo);
                }
            }
        }
    }

    /**
     *  在地图下方介绍框中展示 Detail 相关信息
     *
     * @param poiDetailInfo poi详情信息
     */
    private void addDetailInfor(PoiDetailInfo poiDetailInfo) {
//        mBaiduMap.clear();
        Log.d(KEY, "addDetailInfor");

        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        LatLng build = new LatLng(poiDetailInfo.getLocation().latitude, poiDetailInfo.getLocation().longitude);
        LatLng myLoc = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
        double dis = DistanceUtil. getDistance(build, myLoc);

        buildNameText.setText(poiDetailInfo.getName());
        buildToMeDisText.setText(decimalFormat.format(dis) + "m");
        locDescriptionText.setText(poiDetailInfo.getAddress());
        openTimeText.setText(poiDetailInfo.getShopHours());

        endNode = build;

        isBack = true;
        briedIntroductionLinear.setVisibility(View.VISIBLE);
        routePlanButton.setVisibility(View.GONE);
    }



    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }


    //修改back键动作
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        Log.d(KEY, "back pressed!");

        if (isBack) {
//            briedIntroductionLinear.setZ(0.f);
            briedIntroductionLinear.setVisibility(View.GONE);
            routePlanButton.setVisibility(View.VISIBLE);
            isBack = false;
            return;
        }

        finish();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(KEY, "request : " + requestCode + " result : " + resultCode);
        if(RESULT_OK == resultCode) {
            if(POI_SUG == requestCode) {
                //Bundle bundle = data.getExtras();
                Log.d(KEY, "want to know data");
                ResultEntity resultEntity = (ResultEntity) data.getSerializableExtra("result");
                //ResultEntity result = (ResultEntity) bundle.getSerializable("result");
                Log.d(KEY, ((ResultEntity)data.getSerializableExtra("result")).getCity() + " result");
                LatLng latLng = new LatLng(resultEntity.getLatitude(), resultEntity.getLongitude());
                navigateTo(latLng);
                createMarker(resultEntity);
            }
        }
    }

    private void createMarker(ResultEntity resultEntity) {
        mBaiduMap.clear();
        //构建Marker图标
        LatLng point = new LatLng(resultEntity.getLatitude(), resultEntity.getLongitude());
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_marka);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        Marker marker = (Marker) mBaiduMap.addOverlay(option);

        Bundle bundle = new Bundle();
        bundle.putSerializable("result", resultEntity);
        marker.setExtraInfo(bundle);
    }

    private void navigateTo(LatLng latLng) {

        Log.d("navigateTo", "isFirstLocate : " + isFirstLocate );
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.animateMapStatus(update);
        update = MapStatusUpdateFactory.zoomTo(16f);
        mBaiduMap.animateMapStatus(update);
//        Log.d("mainActivity", "navigateTo curCity : " + location.getCity());
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d("listener", "execute listener");

            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null){
                Log.d("location", "location == null");
                return;
            }
            Log.d("location data", location.getLatitude() + ", " + location.getLongitude());
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);

            Log.d("network", location.getLocTypeDescription());
            Log.d("location", location.getLatitude() + ", " + location.getLongitude());

//            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
//            mBaiduMap.animateMapStatus(update);
//            update = MapStatusUpdateFactory.zoomTo(16f);
//            mBaiduMap.animateMapStatus(update);
            curLocation = location;

            if(isFirstLocate) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                navigateTo(latLng);
                isFirstLocate = false;
            }

        }
    }



}
