package com.baidu.automap.searchroute;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.automap.MainActivity;
import com.baidu.automap.R;
import com.baidu.automap.entity.ResultEntity;
import com.baidu.automap.entity.RoutePlanList;
import com.baidu.automap.entity.RoutePlanNode;
import com.baidu.automap.navi.BNaviGuideActivity;
import com.baidu.automap.search.PoiSugSearchDemo;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.bikenavi.BikeNavigateHelper;
import com.baidu.mapapi.bikenavi.adapter.IBEngineInitListener;
import com.baidu.mapapi.bikenavi.adapter.IBRoutePlanListener;
import com.baidu.mapapi.bikenavi.model.BikeRoutePlanError;
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam;
import com.baidu.mapapi.bikenavi.params.BikeRouteNodeInfo;
import com.baidu.mapapi.model.LatLng;

import java.util.LinkedList;
import java.util.List;

public class SelectRoutePlanActivity extends AppCompatActivity {
    private List<RoutePlanNode> list;

    private RecyclerView node_list_view;
    private Button addNode;
    private Button beginGuide;

    private BikeNavigateHelper mNaviHelper;
    BikeNaviLaunchParam param;
    private boolean isInit;
    private boolean isInitSuccess;

    private static String configCity;
    private static int POI_SUG = 1;
    private static final int BIKE_GUIDE_ACTIVITY = 2;

    private LatLng startLoc;
    private LatLng endLoc;
    private LatLng curLoc;
    private static final String KEY = "selectRoutePlanActivity";

    private RoutePlanList curRouteList;

    {
        configCity = "随州";

        list = new LinkedList<>();
        //a路线
        LatLng aLoc = new LatLng(31.716205897230694 , 113.3500127893836);
        String aName = "楚天超市";
        list.add(new RoutePlanNode(aLoc, aName));

        aLoc = new LatLng(31.71736545380307 , 113.32903735572563);
        aName = "星乐小区";
        list.add(new RoutePlanNode(aLoc, aName));

        aLoc = new LatLng(31.73153236289405 , 113.35258194314216);
        aName = "随州市植物园";
        list.add(new RoutePlanNode(aLoc, aName));

        aLoc = new LatLng(31.708933377382394 , 113.36652364465701);
        aName = "白云公园";
        list.add(new RoutePlanNode(aLoc, aName));

        curLoc = new LatLng(31.732906705449654, 113.3627777106804);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.select_route_show);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        curRouteList = (RoutePlanList) bundle.getParcelable("route_list");
        list = curRouteList.getList();

        addNode = (Button) findViewById(R.id.add_node);
        beginGuide = (Button) findViewById(R.id.begin_guide);

        node_list_view = (RecyclerView) findViewById(R.id.select_all_node);
        node_list_view.setLayoutManager(new LinearLayoutManager(this));
        refresh();

        mNaviHelper = BikeNavigateHelper.getInstance();

        isInit = false;
        isInitSuccess = false;
        initBikeNavi();

        //新增节点
        addNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectRoutePlanActivity.this, PoiSugSearchDemo.class);
                Bundle bundle = new Bundle();
                bundle.putString("city", configCity);
                intent.putExtras(bundle);
                //Log.d("mainActivity", bundle.getString("city"));
                startActivityForResult(intent, POI_SUG);
            }
        });

        //开始导航
        beginGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startLoc == null) {
                    startLoc = curLoc;
                }

                if(endLoc == null) {
                    Toast.makeText(SelectRoutePlanActivity.this, "请设置终点", Toast.LENGTH_LONG).show();
                } else if(startLoc.latitude == endLoc.latitude && startLoc.longitude == endLoc.longitude) {
                    Toast.makeText(SelectRoutePlanActivity.this, "起点和终点是同一个位置，请重新设定", Toast.LENGTH_LONG).show();
                } else {
                    if(!isInit) {
                        initBikeNavi();
                    }
                    Log.d(KEY, "startNode : " + startLoc.latitude + " , " + endLoc.longitude);
                    Log.d(KEY, "endNode : " + endLoc.latitude + " , " + endLoc.longitude);

                    if(isInitSuccess) {
                        startBikeNavi();
                        Log.d(KEY, "bikeNavi finished");
                        finish();
                    } else {
                        Toast.makeText(SelectRoutePlanActivity.this, "导航模块初始化失败", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        Log.d(KEY, "back pressed!");
        if(list != null) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putParcelable("route_list", curRouteList);
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    private void refresh() {
        RoutePlanAdapter routeAdapter = new RoutePlanAdapter(this, list);
        node_list_view.setAdapter(routeAdapter);
        routeAdapter.setOnItemClickListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onClick(View view, ViewName viewName, int position) {
                //在此处理点击事件即可，viewName可以区分是item还是内部控件
                switch (viewName) {
                    case DELETE_NODE:
                        refresh();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void initBikeNavi() {
        Log.d(KEY, "initBikeNavi");
        isInit = true;
        mNaviHelper.initNaviEngine(this, new IBEngineInitListener() {
            @Override
            public void engineInitSuccess() {
                Log.d(KEY, "engineInitSuccess");
                isInitSuccess = true;
            }

            @Override
            public void engineInitFail() {
                Log.d("View", "engineInitFail");
            }
        });
    }

    //初始化引擎
    private void startBikeNavi() {
        Log.d(KEY, "startBikeNavi");
        if(startLoc.toString() != null && endLoc.toString() != null) {
            BikeRouteNodeInfo startNode = new BikeRouteNodeInfo();
            startNode.setLocation(startLoc);
            BikeRouteNodeInfo endNode = new BikeRouteNodeInfo();
            endNode.setLocation(endLoc);
            param = new BikeNaviLaunchParam().startNodeInfo(startNode).endNodeInfo(endNode).vehicle(1);

        }



        routePlanWithParam();

//        mNaviHelper.initNaviEngine(this, new IBEngineInitListener() {
//            @Override
//            public void engineInitSuccess() {
//                Log.d("View", "engineInitSuccess");
//                if(startLoc.toString()!=null&&endLoc.toString()!=null)
//                    param = new BikeNaviLaunchParam().stPt(startLoc).endPt(endLoc).vehicle(1);
//                routePlanWithParam();
//            }
//
//            @Override
//            public void engineInitFail() {
//                Log.d("View", "engineInitFail");
//            }
//        });
    }

    //开始算路
    private void routePlanWithParam() {

        Log.d(KEY, "begin routePlan");

        mNaviHelper.routePlanWithRouteNode(param, new IBRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                Log.d(KEY, "onRoutePlanStart");
            }

            @Override
            public void onRoutePlanSuccess() {
                Log.d(KEY, "onRoutePlanSuccess");
                Intent intent = new Intent();
                intent.setClass(SelectRoutePlanActivity.this, BNaviGuideActivity.class);
                startActivityForResult(intent, BIKE_GUIDE_ACTIVITY);
            }

            @Override
            public void onRoutePlanFail(BikeRoutePlanError error) {
                Log.d(KEY, "onRoutePlanFail");
            }


        });
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
                RoutePlanNode newNode = new RoutePlanNode(latLng, resultEntity.getKey());
                list.add(newNode);
                refresh();
            } else if(BIKE_GUIDE_ACTIVITY == requestCode) {
                Log.d(KEY, "finish bikeNavi");
                finish();
            }
        }
    }

    private class RoutePlanAdapter extends RecyclerView.Adapter implements View.OnClickListener {

        private List<RoutePlanNode> list;
        private Context context;

        public RoutePlanAdapter(Context context, List<RoutePlanNode> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.select_route_node, parent, false);
            return new ItemHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ItemHolder itemHolder = (ItemHolder) holder;
            itemHolder.itemView.setTag(position);
            itemHolder.setStart.setTag(position);
            itemHolder.setEnd.setTag(position);
            itemHolder.deleteNode.setTag(position);

            ((ItemHolder) holder).bind(list.get(position));
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ItemHolder extends RecyclerView.ViewHolder {

            private RoutePlanNode routeNode;

            private TextView nodeName;
            private Button setStart;
            private Button setEnd;
            private Button deleteNode;

            public ItemHolder(View itemView) {
                super(itemView);

                nodeName = (TextView) itemView.findViewById(R.id.select_node_name);
                setStart = (Button) itemView.findViewById(R.id.set_start);
                setEnd = (Button) itemView.findViewById(R.id.set_end);
                deleteNode = (Button) itemView.findViewById(R.id.delete_node);

                //将创建的View注册点击事件
                itemView.setOnClickListener(RoutePlanAdapter.this);
                setStart.setOnClickListener(RoutePlanAdapter.this);
                setEnd.setOnClickListener(RoutePlanAdapter.this);
                deleteNode.setOnClickListener(RoutePlanAdapter.this);
            }

            public void bind(RoutePlanNode node) {
                routeNode = node;
                nodeName.setText(node.getName());
            }
        }


        ////////////////////////////以下为item点击处理///////////////////////////////

        private OnRecyclerViewItemClickListener mOnItemClickListener = null;

        public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
            this.mOnItemClickListener = listener;
        }


        @Override
        public void onClick(View v) {
            //注意这里使用getTag方法获取数据
            int position = (int) v.getTag();
            if (mOnItemClickListener != null) {
                switch (v.getId()){
                    case R.id.set_start:
//                        mOnItemClickListener.onClick(v, ViewName.SET_START, position);
                        startLoc = list.get(position).getLatLng();
                        Log.d(KEY, "setStart : " + startLoc.latitude + " , " + startLoc.longitude);
                        break;
                    case R.id.set_end:
                        endLoc = list.get(position).getLatLng();
                        Log.d(KEY, "setEnd : " + endLoc.latitude + " , " + endLoc.longitude);
//                        mOnItemClickListener.onClick(v, ViewName.SET_END, position);
                        break;
                    case R.id.delete_node:
                        list.remove(position);
                        mOnItemClickListener.onClick(v, ViewName.DELETE_NODE, position);
                        break;
                    default:
                        mOnItemClickListener.onClick(v, ViewName.ELSE, position);
                        break;
                }

                if(startLoc != null) {
                    Log.d(KEY, startLoc.latitude + ", " + startLoc.longitude);
                }
                if(endLoc != null ) {
                    Log.d(KEY, endLoc.latitude + ", " + endLoc.longitude);
                }
            }
        }
    }

    /** item里面有多个控件可以点击 */
    public enum ViewName {
        SET_START,
        SET_END,
        DELETE_NODE,
        ELSE
    }

    public interface OnRecyclerViewItemClickListener {
        void onClick(View view, ViewName viewName, int position);
    }



//    private class RouteNodeHolder extends RecyclerView.ViewHolder
//            implements View.OnClickListener {
//
//        private RoutePlanNode routeNode;
//
//        private TextView nodeName;
//        private Button setStart;
//        private Button setEnd;
//        private Button deleteNode;
//
//        public RouteNodeHolder(LayoutInflater inflater, ViewGroup parent) {
//            super(inflater.inflate(R.layout.route_node_layout, parent, false));
//            Log.d("holder", "begin build");
//            itemView.setOnClickListener(this);
//
//            nodeName = (TextView) itemView.findViewById(R.id.select_node_name);
//            setStart = (Button) findViewById(R.id.set_start);
//            setEnd = (Button) findViewById(R.id.set_end);
//            deleteNode = (Button) findViewById(R.id.delete_node);
//
//
//            Log.d("holder", "end build");
//        }
//
//        public void bind(RoutePlanNode node) {
//            Log.d("holder", "begin bind");
//
//            routeNode = node;
//            nodeName.setText(routeNode.getName());
//
//            Log.d("holder", "end bind");
//
//        }
//
//        @Override
//        public void onClick(View view) {
//            Log.d("holder", "begin click");
////            for(RoutePlanNode node : routeNodeList) {
////                Log.d(KEY, "nodeLatlng " + node.getLatLng().latitude + " , " + node.getLatLng().longitude);
////            }
//        }
//
//    }
//
//
//    public interface OnRecyclerViewItemClickListener {
//        void onClick(View view, ViewName viewName, int position);
//    }
//
//    public enum ViewName {
//        SET_START,
//        SET_END,
//        DEL_NODE
//    }
//
//    private class RouteAdapter extends RecyclerView.Adapter<RouteNodeHolder> {
//
//        private List<RoutePlanNode> routeList;
//        private OnRecyclerViewItemClickListener mOnItemClickListener = null;
//
//
//        public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
//            this.mOnItemClickListener = listener;
//        }
//
//        public RouteAdapter(List<RoutePlanNode> list) {
//
//            routeList = list;
//        }
//
//        @Override
//        public RouteNodeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            LayoutInflater layoutInflater = LayoutInflater.from(RoutePlanActivity.this);
//            Log.d("adapter", "begin");
//
//            return new RoutePlanActivity.RouteNodeHolder(layoutInflater, parent);
//        }
//
//
//
//        @Override
//        public void onBindViewHolder(RoutePlanActivity.RouteNodeHolder holder, int position) {
//            Log.d("adapter", "begin bind");
//
//            List<RoutePlanNode> list = routePlanList.get(position);
//            holder.bind(list);
//
//            Log.d("adapter", "end bind");
//        }
//
//        @Override
//        public int getItemCount() {
//            return routePlanList.size();
//        }
//    }

    private String getRouteDetail(List<RoutePlanNode> list) {
        StringBuffer detailBuffer = new StringBuffer();
        for(RoutePlanNode node : list) {
            detailBuffer.append(node.getName());
            detailBuffer.append(" -- ");
        }
        detailBuffer.delete(detailBuffer.length() - 4, detailBuffer.length() - 1);
        String routeDetail = detailBuffer.toString();
        Log.d(KEY, "routeDetail : " + routeDetail);
        return routeDetail;
    }
}
