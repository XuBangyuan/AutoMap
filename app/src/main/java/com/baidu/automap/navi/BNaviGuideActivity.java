/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.automap.navi;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.baidu.mapapi.bikenavi.BikeNavigateHelper;
import com.baidu.mapapi.bikenavi.adapter.IBRouteGuidanceListener;
import com.baidu.mapapi.bikenavi.adapter.IBTTSPlayer;
import com.baidu.mapapi.bikenavi.model.BikeRouteDetailInfo;
import com.baidu.mapapi.bikenavi.model.RouteGuideKind;
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam;
import com.baidu.navisdk.adapter.IBNRouteGuideManager;

public class BNaviGuideActivity extends Activity {

    private BikeNavigateHelper mNaviHelper;

    BikeNaviLaunchParam param;

    private static String KEY = "bNaviGuideActivity";


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
    public void onBackPressed() {
        Log.d(KEY, "back pressed!");
        setResult(RESULT_OK, null);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNaviHelper = BikeNavigateHelper.getInstance();

        View view = mNaviHelper.onCreate(BNaviGuideActivity.this);
        if (view != null) {
            setContentView(view);
        }

        mNaviHelper.startBikeNavi(BNaviGuideActivity.this);
        // 设置诱导信息回调监听



        mNaviHelper.setTTsPlayer(new IBTTSPlayer() {
            /**
             *
             * @param s 诱导文本,获取语音播报文本
             * @param b 是否抢先播报
             *
            此组件只提供导航过程中的文本输出，不包含语音播报功能，需要自行传入对应的语音回调，形成播报功能。建议使用百度语音识别服务SDK。
             * @return
             */
            @Override
            public int playTTSText(String s, boolean b) {
                Log.d("tts", s);
                return 0;
            }
        });
        //主要包括导航开始、结束，导航过程中偏航、偏航结束、诱导信息（包含诱导默认图标、诱导类型、诱导信息、剩余距离、时间、振动回调等）
        mNaviHelper.setRouteGuidanceListener(this, new IBRouteGuidanceListener() {
            @Override
            public void onRouteGuideIconUpdate(Drawable icon) {

            }

            @Override
            public void onRouteGuideKind(com.baidu.mapapi.walknavi.model.RouteGuideKind routeGuideKind) {

            }

            @Override
            public void onRoadGuideTextUpdate(CharSequence charSequence, CharSequence charSequence1) {

            }

            @Override
            public void onRemainDistanceUpdate(CharSequence charSequence) {

            }

            @Override
            public void onRemainTimeUpdate(CharSequence charSequence) {

            }

            @Override
            public void onGpsStatusChange(CharSequence charSequence, Drawable drawable) {

            }

            @Override
            public void onRouteFarAway(CharSequence charSequence, Drawable drawable) {

            }

            @Override
            public void onRoutePlanYawing(CharSequence charSequence, Drawable drawable) {

            }

            @Override
            public void onReRouteComplete() {

            }

            @Override
            public void onArriveDest() {

            }

            @Override
            public void onVibrate() {

            }

            @Override
            public void onGetRouteDetailInfo(BikeRouteDetailInfo bikeRouteDetailInfo) {

            }
        });
    }

}
