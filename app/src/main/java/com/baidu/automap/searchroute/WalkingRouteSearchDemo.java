/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.automap.searchroute;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.baidu.automap.MainActivity;
import com.baidu.automap.R;
import com.baidu.automap.navi.BNaviGuideActivity;
import com.baidu.automap.navi.DemoGuideActivity;
import com.baidu.automap.navi.DemoNaviActivity;
import com.baidu.automap.navi.GuideActivity;
import com.baidu.automap.navi.WNaviGuideActivity;
import com.baidu.automap.overlayUtil.WalkingRouteOverlay;
import com.baidu.automap.search.OverlayManager;
import com.baidu.mapapi.bikenavi.BikeNavigateHelper;
import com.baidu.mapapi.bikenavi.adapter.IBEngineInitListener;
import com.baidu.mapapi.bikenavi.adapter.IBRoutePlanListener;
import com.baidu.mapapi.bikenavi.model.BikeRoutePlanError;
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;

/**
 * 此demo用来展示如何进行步行路线规划并在地图使用RouteOverlay绘制
 * 同时展示如何进行节点浏览并弹出泡泡
 */
public class WalkingRouteSearchDemo extends AppCompatActivity implements BaiduMap.OnMapClickListener,
        OnGetRoutePlanResultListener, OnGetGeoCoderResultListener {

    // 浏览路线节点相关
    private Button mBtnPre = null; // 上一个节点
    private Button mBtnNext = null; // 下一个节点
    private Button startGuide = null;
    private RouteLine mRouteLine = null;
    private OverlayManager mRouteOverlay = null;
    private boolean mUseDefaultIcon = false;

    // 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
    // 如果不处理touch事件，则无需继承，直接使用MapView即可
    private MapView mMapView = null;    // 地图View
    private BaiduMap mBaidumap = null;
    // 搜索模块，也可去掉地图模块独立使用
    private RoutePlanSearch mSearch = null;
    private WalkingRouteResult mWalkingRouteResult = null;
    private boolean hasShowDialog = false;
    private NodeUtils mNodeUtils;

    BikeNaviLaunchParam param;
    private BikeNavigateHelper mNaviHelper;


    //导航起点
    private LatLng startLoc = null;
    //导航终点
    private LatLng endLoc= null;
    private String curCity = null;

    private static final String KEY = "WalkingRouteSearch";
    private WalkNaviLaunchParam mParam;
//    private WalkNavigateHelper mNaviHelper;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking_route);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        startLoc = (LatLng) bundle.getParcelable("startNode");
        endLoc = (LatLng) bundle.getParcelable("endNode");
        curCity = bundle.getString("city");

        Log.d(KEY, "startLoc : " + startLoc.latitude + " , " + startLoc.longitude);
        Log.d(KEY, "endLoc : " + endLoc.latitude + " , " + endLoc.longitude);

        // 初始化地图
        mMapView = (MapView) findViewById(R.id.map);
        mBaidumap = mMapView.getMap();

        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);

        searchButtonProcess();

        // 地图点击事件处理
        mBaidumap.setOnMapClickListener(this);

        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
        mNodeUtils = new NodeUtils(this,  mBaidumap);

        startGuide = (Button) findViewById(R.id.start_guide);
        startGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(WalkingRouteSearchDemo.this, GuideActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putParcelable("startNode", startLoc);
//                bundle.putParcelable("endNode", endLoc);
//                bundle.putString("city", curCity);
//                intent.putExtras(bundle);
//                //Log.d("mainActivity", bundle.getString("city"));
//                startActivity(intent);
                startBikeNavi();
            }
        });

//        //获取WalkNavigateHelper实例
//        mNaviHelper = WalkNavigateHelper.getInstance();
//        //获取诱导页面地图展示View
//        View view = mNaviHelper.onCreate(WalkingRouteSearchDemo.this);
//        if (view != null) {
//            setContentView(view);
//        }

//        // 获取导航控制类
//        // 引擎初始化
//        WalkNavigateHelper.getInstance().initNaviEngine(this, new IWEngineInitListener() {
//
//            @Override
//            public void engineInitSuccess() {
//                //引擎初始化成功的回调
//                Log.d(KEY, "engineInitSuccess");
//                routeWalkPlanWithParam();
//            }
//
//            @Override
//            public void engineInitFail() {
//                //引擎初始化失败的回调
//                Log.d(KEY, "engineInitFail");
//            }
//        });

        mNaviHelper = BikeNavigateHelper.getInstance();

        PlanNode startNode = PlanNode.withLocation(startLoc);
        PlanNode endNode = PlanNode.withLocation(endLoc);
        WalkingRoutePlanOption walkingRoutePlanOption = new WalkingRoutePlanOption();
        walkingRoutePlanOption.from(startNode);
        walkingRoutePlanOption.to(endNode);

        mSearch.walkingSearch(walkingRoutePlanOption); // 终点

        //searchButtonProcess();
    }

//    /**
//     * 开始路线导航
//     */
//    private void routeWalkPlanWithParam() {
//        //构造WalkNaviLaunchParam
//        mParam = new WalkNaviLaunchParam().stPt(startLoc).endPt(endLoc);
//
//        //发起算路
//        BikeNavigateHelper.getInstance().routePlanWithParams(mParam, new IWRoutePlanListener() {
//            @Override
//            public void onRoutePlanStart() {
//                //开始算路的回调
//            }
//
//            @Override
//            public void onRoutePlanSuccess() {
//                //算路成功
//                //跳转至诱导页面
//                Log.d(KEY, "onRoutePlanSuccess");
//                Intent intent = new Intent(WalkingRouteSearchDemo.this, BNaviGuideActivity.class);
//                startActivity(intent);
//            }
//
//            @Override
//            public void onRoutePlanFail(WalkRoutePlanError walkRoutePlanError) {
//                //算路失败的回调
//            }
//        });
//
//
//    }

    //初始化引擎
    private void startBikeNavi() {
        Log.d("View", "startBikeNavi");
        mNaviHelper.initNaviEngine(this, new IBEngineInitListener() {
            @Override
            public void engineInitSuccess() {
                Log.d("View", "engineInitSuccess");
                if(startLoc.toString()!=null&&endLoc.toString()!=null)
                    param = new BikeNaviLaunchParam().stPt(startLoc).endPt(endLoc).vehicle(1);
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
                intent.setClass(WalkingRouteSearchDemo.this, BNaviGuideActivity.class);
                startActivity(intent);
            }

            @Override
            public void onRoutePlanFail(BikeRoutePlanError error) {
                Log.d("View", "onRoutePlanFail");
            }

        });
    }



    /**
     * 发起路线规划搜索示例
     */
    public void searchButtonProcess() {
        // 重置浏览节点的路线数据
        mRouteLine = null;
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        // 清除之前的覆盖物
        mBaidumap.clear();
        // 设置起终点信息 起点参数
//        PlanNode startNode = PlanNode.withCityNameAndPlaceName(mEditStartCity.getText().toString().trim(),
//                mStrartNodeView.getText().toString().trim());

        // 终点参数
//        PlanNode endNode = PlanNode.withCityNameAndPlaceName(mEditEndCity.getText().toString().trim(),
//                mEndNodeView.getText().toString().trim());




//        Log.d(KEY, walkingRoutePlanOption.toString());

        // 实际使用中请对起点终点城市进行正确的设定
//        mSearch.walkingSearch((new WalkingRoutePlanOption())
//                .from(startNode) // 起点
//                .to(endNode)); // 终点

    }

    /**
     * 节点浏览示例
     */
    public void nodeClick(View view) {
        if (null != mRouteLine) {
            mNodeUtils.browseRoutNode(view,mRouteLine);
        }
    }

    /**
     * 切换路线图标，刷新地图使其生效
     * 注意： 起终点图标使用中心对齐.
     */
    public void changeRouteIcon(View v) {
        if (mRouteOverlay == null) {
            return;
        }
        if (mUseDefaultIcon) {
            ((Button) v).setText("自定义起终点图标");
            Toast.makeText(this, "将使用系统起终点图标", Toast.LENGTH_SHORT).show();
        } else {
            ((Button) v).setText("系统起终点图标");
            Toast.makeText(this, "将使用自定义起终点图标", Toast.LENGTH_SHORT).show();
        }
        mUseDefaultIcon = !mUseDefaultIcon;
        mRouteOverlay.removeFromMap();
        mRouteOverlay.addToMap();
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * 步行路线结果回调
     *
     * @param result  步行路线结果
     */
    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (null == result) {
            return;
        }

        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            AlertDialog.Builder builder = new AlertDialog.Builder(WalkingRouteSearchDemo.this);
            builder.setTitle("提示");
            builder.setMessage("检索地址有歧义，请重新设置。\n可通过getSuggestAddrInfo()接口获得建议查询信息");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            return;
        }

        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(WalkingRouteSearchDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else {
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);

            if (result.getRouteLines().size() > 1) {
                mWalkingRouteResult = result;
                if (!hasShowDialog) {
                    SelectRouteDialog selectRouteDialog = new SelectRouteDialog(WalkingRouteSearchDemo.this,
                            result.getRouteLines(), RouteLineAdapter.Type.WALKING_ROUTE);
                    selectRouteDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hasShowDialog = false;
                        }
                    });
                    selectRouteDialog.setOnItemInDlgClickLinster(new SelectRouteDialog.OnItemInDlgClickListener() {
                        public void onItemClick(int position) {
                            mRouteLine = mWalkingRouteResult.getRouteLines().get(position);
                            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaidumap);
                            mBaidumap.setOnMarkerClickListener(overlay);
                            mRouteOverlay = overlay;
                            overlay.setData(mWalkingRouteResult.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();
                        }

                    });
                    selectRouteDialog.show();
                    hasShowDialog = true;
                }
            } else if (result.getRouteLines().size() == 1) {
                // 直接显示
                mRouteLine = result.getRouteLines().get(0);
                WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaidumap);
                mBaidumap.setOnMarkerClickListener(overlay);
                mRouteOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();

            } else {
                Log.d("route result", "结果数<0");
            }
        }
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {
    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult result) {
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {

    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        private MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (mUseDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (mUseDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    @Override
    public void onMapClick(LatLng point) {
        mBaidumap.hideInfoWindow();
    }

    @Override
    public void onMapPoiClick(MapPoi poi) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放检索对象
        if (mSearch != null) {
            mSearch.destroy();
        }
        mBaidumap.clear();
        mMapView.onDestroy();
    }
}
