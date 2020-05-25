package com.baidu.automap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;

import com.baidu.automap.build.BuildDetailActivity;
import com.baidu.automap.entity.ResultEntity;
import com.baidu.automap.entity.RouteNode;
import com.baidu.automap.entity.response.RouteNodeResponse;
import com.baidu.automap.search.PoiOverlay;
import com.baidu.automap.search.PoiSugSearch;
import com.baidu.automap.searchroute.BikingRouteSearch;
import com.baidu.automap.searchroute.RoutePlanActivity;
import com.baidu.automap.searchroute.WalkingRouteSearch;
import com.baidu.automap.util.HttpUtil;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.mapapi.utils.DistanceUtil;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnGetPoiSearchResultListener, OnGetSuggestionResultListener {

    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;

    //是否替换back键动作
    boolean isFirstLocate = true;

    //poi检索
    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;
    private RouteNode curPoiRouteNode;

    private SDKReceiver mReceiver;

    //地点信息
    private LinearLayout poiSearchLayout = null;
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
    private LinearLayout guideChoice;
    private Button walkingGuide;
    private Button bikingGuide;
    //周边搜索
    private LinearLayout nearbySearchLayout;
    private AutoCompleteTextView nearbySearchText;
    private Button nearbySearchButton;
    private ArrayAdapter<String> sugAdapter = null;

    //周边搜索半径
    private int radius = 1000;

    //requestCode
    //poi_sug搜索
    private static final int POI_SUG = 1;
    //RoutePlanActivity
    private static final int ROUTE_PLAN = 2;
    //BuildDetailActivity
    private static final int BUILD_DETAIL = 3;

    //当前用户userId
    private Integer userId;
    private Boolean isAdministrator;

    private static final int BIKING_ROUTE = 4;
    private static final int WALKING_ROUTE = 3;
    private static final int POI_SUG_SINGLE = 1;
    private static final int POI_SUG_LIST = 2;

    //RoutePlanActivity识别码
    private static final int CALL_FOR_ADD = 1;
    private static final int CALL_FOR_ROUTE = 2;

    //RoutePlanActivity,bundle取出key
    private static final String CALL_FOR_ROUTE_PLAN_ACTIVITY = "CALL_FOR_ROUTE_PLAN_ACTIVITY";

    //当前定位地点
    private BDLocation curLocation = null;
    private LatLng center;
    //导航起点
    private LatLng startNode = null;
    //导航终点
    private LatLng endNode = null;



    private static final String KEY = "mainActivity";
    private static pageState curState;
    private static boolean isBack = false;
    private static boolean isOverlay = false;

    /**
     * 获取在线建议搜索结果，得到requestSuggestion返回的搜索结果
     *
     * @param res    Sug检索结果
     */
    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }

        List<String> suggest = new ArrayList<>();
        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
            if (info.key != null) {
                suggest.add(info.key);
            }
        }

        sugAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line,
                suggest);
        nearbySearchText.setAdapter(sugAdapter);
        sugAdapter.notifyDataSetChanged();
    }

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

        poiSearchLayout = (LinearLayout) findViewById(R.id.search_layout);
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
        nearbySearchLayout = (LinearLayout) findViewById(R.id.nearby_search_layout);
        nearbySearchText = (AutoCompleteTextView) findViewById(R.id.nearby_search_text);
        nearbySearchButton = (Button) findViewById(R.id.nearby_search_button);
        nearbySearchLayout.setVisibility(View.GONE);
        guideChoice = (LinearLayout) findViewById(R.id.guide_choice);
        walkingGuide = (Button) findViewById(R.id.walking_guide);
        bikingGuide = (Button) findViewById(R.id.biking_guide);
        guideChoice.setVisibility(View.GONE);


        curPoiRouteNode = new RouteNode();

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher_background);
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, bitmapDescriptor));


        final Intent intent = getIntent();
        final Bundle bundle = intent.getExtras();
        userId = bundle.getInt("userId");
        isAdministrator = bundle.getBoolean("isAdministrator");
        if(userId != null) {
            Log.d(KEY, "get userId : " + userId);
        }
        if(isAdministrator) {
            buildDetailButton.setText("编辑详情");
        }

        // 创建poi检索实例，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        // 初始化建议搜索模块，注册建议搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);

        sugAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        nearbySearchText.setAdapter(sugAdapter);
        nearbySearchText.setThreshold(1);
        //mBaiduMap = ((SupportMapFragment) (getSupportFragmentManager().findFragmentById(R.id.map))).getBaiduMap();

        /* 当输入关键字变化时，动态更新建议列表 */
        nearbySearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                if (cs.length() <= 0) {
                    return;
                }

                try {
                    /* 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新 */
                    mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                            .keyword(cs.toString())
                            .city(curLocation.getCity()));
                } catch (Exception e) {
                    Log.e(KEY, e.toString());
                }

            }
        });

//        briedIntroductionLinear.setZ(0.f);
        briedIntroductionLinear.setVisibility(View.GONE);
        poiSearchLayout.setZ(4.f);
        briedIntroductionLinear.setZ(5.f);
        guideChoice.setZ(5.f);

        //注册广播
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);

        //周边搜索按钮
        nearbySearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyWord = nearbySearchText.getText().toString();
                if(keyWord == null || keyWord.length() == 0) {
                    Toast.makeText(MainActivity.this, "请输入搜索内容",
                            Toast.LENGTH_SHORT).show();
                } else {
                    curState = pageState.NEARBY;
                    briedIntroductionLinear.setVisibility(View.GONE);
                    nearbySearchLayout.setVisibility(View.GONE);
                    searchNearbyProcess();
                }
            }
        });

        buildSurroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                poiSearchLayout.setVisibility(View.GONE);
                nearbySearchLayout.setVisibility(View.VISIBLE);
            }
        });

        //路线规划按钮点击
        routePlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RoutePlanActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(CALL_FOR_ROUTE_PLAN_ACTIVITY, CALL_FOR_ROUTE);
                bundle.putInt("userId", userId);
                bundle.putDouble("latitude", curLocation.getLatitude());
                bundle.putDouble("longitude", curLocation.getLongitude());
                intent.putExtras(bundle);
                startActivityForResult(intent, ROUTE_PLAN);
            }
        });

        //详情按钮点击
        buildDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(MainActivity.this, BuildDetailActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putString("desId", curPoiRouteNode.getDesId());
                bundle1.putInt("userId", userId);
                bundle1.putBoolean("isAdmin", isAdministrator);
                intent1.putExtras(bundle1);
                startActivityForResult(intent1, BUILD_DETAIL);
            }
        });

        //加入路线按钮点击
        buildInsertIntoRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RoutePlanActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(CALL_FOR_ROUTE_PLAN_ACTIVITY, CALL_FOR_ADD);
                bundle.putDouble("latitude", curLocation.getLatitude());
                bundle.putDouble("longitude", curLocation.getLongitude());
                bundle.putInt("userId", userId);
                intent.putExtras(bundle);
                startActivityForResult(intent, ROUTE_PLAN);
            }
        });

        //搜索按钮点击
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d("mainActivity", "searchButton onclick, curCity : " + curLocation.getCity());
                clearPage();
                Intent intent = new Intent(MainActivity.this, PoiSugSearch.class);
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
                guideChoice.setVisibility(View.VISIBLE);
                startNode = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
//                routeSearchBegin();
            }
        });

        bikingGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeSearchBegin(BIKING_ROUTE);
                guideChoice.setVisibility(View.GONE);
            }
        });

        walkingGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeSearchBegin(WALKING_ROUTE);
                guideChoice.setVisibility(View.GONE);
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

        //单击事件
        BaiduMap.OnMapClickListener mapOnClickListener =
                new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                Log.d(KEY, point.latitude+ " , " +  point.longitude);
            }

            @Override
            public void onMapPoiClick(MapPoi mapPoi) {
                Log.d(KEY, "onMapPoiClick " + mapPoi.getName());
                Log.d(KEY, mapPoi.getPosition().latitude + " , "
                        + mapPoi.getPosition().longitude);
                mPoiSearch.searchPoiDetail(new PoiDetailSearchOption()
                        .poiUids(mapPoi.getUid()));
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

    private void changePage() {
        switch (curState) {
            case START:
                mBaiduMap.clear();
                poiSearchLayout.setVisibility(View.VISIBLE);
                nearbySearchLayout.setVisibility(View.GONE);
                briedIntroductionLinear.setVisibility(View.GONE);
                routePlanButton.setVisibility(View.VISIBLE);
                isBack = false;
                break;
            case BRIEF:
                mBaiduMap.clear();
                poiSearchLayout.setVisibility(View.VISIBLE);
                nearbySearchLayout.setVisibility(View.GONE);
                briedIntroductionLinear.setVisibility(View.VISIBLE);
                break;
            case NEARBY:
                mBaiduMap.clear();
                poiSearchLayout.setVisibility(View.GONE);
                nearbySearchLayout.setVisibility(View.VISIBLE);
                briedIntroductionLinear.setVisibility(View.GONE);
                break;
        }
    }

    private enum pageState {
        START,
        BRIEF,
        NEARBY
    }

    /**
     * 响应周边搜索按钮点击事件
     */
    public void  searchNearbyProcess() {
        Log.d(KEY, "searchNearbyProcess center : " + curPoiRouteNode.getDesName());
        LatLng latLng = new LatLng(curPoiRouteNode.getLatitude(),
                curPoiRouteNode.getlongitude());
        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption()
                .keyword(nearbySearchText.getText().toString())
                .sortType(PoiSortType.distance_from_near_to_far)
                .location(latLng)
                .radius(radius)
                .pageNum(0)
                .scope(1);

        mPoiSearch.searchNearby(nearbySearchOption);
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
    public void routeSearchBegin(int choice) {
        Intent intent;
        if(choice == WALKING_ROUTE) {
            intent = new Intent(MainActivity.this, WalkingRouteSearch.class);
        } else {
            intent = new Intent(MainActivity.this, BikingRouteSearch.class);
        }
//        intent = new Intent(MainActivity.this, WalkingRouteSearch.class);

        if(DistanceUtil.getDistance(startNode, endNode) >= 50000) {
            Toast.makeText(MainActivity.this, "距离太远，暂不支持导航", Toast.LENGTH_SHORT).show();
        } else {
            Bundle bundle = new Bundle();
            bundle.putParcelable("startNode", startNode);
            bundle.putParcelable("endNode", endNode);
            bundle.putString("city", curLocation.getCity());
            intent.putExtras(bundle);
            //Log.d("mainActivity", bundle.getString("city"));
            startActivity(intent);
        }

    }


    /**
     * 获取POI搜索结果
     * @param result    Poi检索结果
     */
    public void onGetPoiResult(PoiResult result) {
        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(MainActivity.this, "未找到结果",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            mBaiduMap.clear();
            PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result);
            overlay.addToMap();
            overlay.zoomToSpan();

            LatLng latLng = new LatLng(curPoiRouteNode.getLatitude(),
                    curPoiRouteNode.getlongitude());
            showNearbyArea(latLng, radius);

            return;
        }

        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";

            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }

            strInfo += "找到结果";
            Toast.makeText(MainActivity.this, strInfo, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 对周边检索的范围进行绘制
     *
     * @param center    周边检索中心点坐标
     * @param radius    周边检索半径，单位米
     */
    public void showNearbyArea( LatLng center, int radius) {
        BitmapDescriptor centerBitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
        MarkerOptions ooMarker = new MarkerOptions().position(center).icon(centerBitmap);
        mBaiduMap.addOverlay(ooMarker);

        OverlayOptions ooCircle = new CircleOptions().fillColor( 0xE4EBEC )
                .center(center)
                .stroke(new Stroke(5, 0xFFFF00FF ))
                .radius(radius);

        mBaiduMap.addOverlay(ooCircle);
        curState = pageState.START;
        isBack = true;
    }

    private class MyPoiOverlay extends PoiOverlay {
        MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            mPoiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUids(poi.getUid()));
            Log.d(KEY, "nearby poi click, uId : " + poi.uid);
            return true;
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {
        Log.d(KEY, "onGetPoiDetailResult depressed");

        Log.d(KEY, "onGetPoiDetailResult");

        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else {

        }
    }

    /**
     * poi 详情查询结果回调
     * V5.2.0版本新增接口，用于返回详细检索结果
     *
     * @param poiDetailSearchResult poi详情查询结果
     */
    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
        Log.d(KEY, "onGetPoiDetailResult");
        if (poiDetailSearchResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "抱歉，未找到结果",
                    Toast.LENGTH_SHORT).show();
        } else {
            List<PoiDetailInfo> poiDetailInfoList = poiDetailSearchResult
                    .getPoiDetailInfoList();
            if (null == poiDetailInfoList || poiDetailInfoList.isEmpty()) {
                Toast.makeText(MainActivity.this, "抱歉，检索结果为空",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            for (int i = 0; i < poiDetailInfoList.size(); i++) {
                // 获取详情检索结果
                PoiDetailInfo poiDetailInfo = poiDetailInfoList.get(i);
                if (null != poiDetailInfo) {
                    // 展示Detail 相关信息
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
        Log.d(KEY, "addDetailInfor");
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        LatLng build = new LatLng(poiDetailInfo.getLocation().latitude,
                poiDetailInfo.getLocation().longitude);
        LatLng myLoc = new LatLng(curLocation.getLatitude(),
                curLocation.getLongitude());
        double dis = DistanceUtil. getDistance(build, myLoc);

        buildNameText.setText(poiDetailInfo.getName());
        buildToMeDisText.setText(decimalFormat.format(dis) + "m");
        locDescriptionText.setText(poiDetailInfo.getAddress());
        openTimeText.setText(poiDetailInfo.getShopHours());

        endNode = build;
        curPoiRouteNode.setDesId(poiDetailInfo.getUid());
        curPoiRouteNode.setDesName(poiDetailInfo.getName());
        curPoiRouteNode.setLatitude(poiDetailInfo.getLocation().latitude);
        curPoiRouteNode.setlongitude(poiDetailInfo.getLocation().longitude);

        isBack = true;
        curState = pageState.START;
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

        if(isBack) {
            changePage();
        } else {
            finish();
        }
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
        mPoiSearch.destroy();
        mSuggestionSearch.destroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(KEY, "request : " + requestCode + " result : " + resultCode);
        if(RESULT_OK == resultCode) {
            if(POI_SUG == requestCode) {
                Log.d(KEY, "poi_sug onActivityResult");
                ResultEntity resultEntity = (ResultEntity) data.getSerializableExtra("result");
                LatLng latLng = new LatLng(resultEntity.getLatitude(), resultEntity.getLongitude());

                mPoiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUids(resultEntity.getuId()));
                navigateTo(latLng);
                createMarker(resultEntity);
            } else if(ROUTE_PLAN == requestCode){
                Log.e(KEY, "route_plan onActivityResult");
                curPoiRouteNode.setRouteId(data.getIntExtra("routeId", -1));

                if(curPoiRouteNode.getRouteId() != -1) {
                    Log.d(KEY, "insert into routeId : " + curPoiRouteNode.getRouteId());
                    addRouteNode(curPoiRouteNode);
                }
            }
        }
    }

    //添加路线节点
    private void addRouteNode(RouteNode routeNode) {
        ThreadAddRouteNode thread = new ThreadAddRouteNode(routeNode);
        thread.start();

        try {
            thread.join();

            if(thread.getIsSuccess()) {
                Log.d(KEY, "insert into routeId : " + curPoiRouteNode.getRouteId() + " success!");
            }
        } catch (InterruptedException e) {
            Log.d(KEY, e.toString());
        }
    }

    private class ThreadAddRouteNode extends Thread {

        private boolean isSuccess = false;

        private String message;

        private RouteNode mRouteNode;

        public ThreadAddRouteNode(RouteNode routeNode) {
            this.mRouteNode = routeNode;
        }

        public boolean getIsSuccess() {
            return isSuccess;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public void run() {
            try {

                byte[] data = HttpUtil.readRouteParse("addRouteNode", null, mRouteNode);
                String str = new String(data);
                JSONObject jsonObject = new JSONObject(str);

                RouteNodeResponse routeNodeResponse = new RouteNodeResponse();
                routeNodeResponse.setMessage(jsonObject.getString("message"));
                if(routeNodeResponse != null && routeNodeResponse.getMessage().compareTo("success!") == 0) {
                    Log.d(KEY, "addRouteNode" + " get data from server success!");
                    isSuccess = true;
                } else {
                    isSuccess = false;
                    message = routeNodeResponse.getMessage();
                    Log.d(KEY, routeNodeResponse.getMessage());
                }
                Log.d(KEY, str);
            } catch (Exception e) {
                Log.e(KEY, e.toString());
            }
        }

    }

    private void createMarker(ResultEntity resultEntity) {
        mBaiduMap.clear();
//        isOverlay = true;
        isBack = true;
        curState = pageState.START;
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
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d(KEY, "listener " + "execute listener");

            //mapView 销毁后不再处理新接收的位置
            if (location == null || mMapView == null){
                Log.d(KEY, "location " + "location == null");
                return;
            }
            Log.d(KEY, "location data " + location.getLatitude() + ", "
                    + location.getLongitude());
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            Log.d(KEY, location.getLocTypeDescription());

            curLocation = location;
            center = new LatLng(location.getLatitude(), location.getLongitude());
            Log.d(KEY, curLocation.getCity() + ", " + curLocation.getLatitude()
                    + ", " + curLocation.getLongitude());

            if(isFirstLocate) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                navigateTo(latLng);
                isFirstLocate = false;
            }

        }
    }



}
