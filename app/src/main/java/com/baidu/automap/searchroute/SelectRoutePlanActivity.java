package com.baidu.automap.searchroute;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.automap.R;
import com.baidu.automap.entity.ResultEntity;
import com.baidu.automap.entity.RouteNode;
import com.baidu.automap.entity.RoutePlanList;
import com.baidu.automap.entity.RoutePlanNode;
import com.baidu.automap.entity.UserRoute;
import com.baidu.automap.entity.response.RouteNodeResponse;
import com.baidu.automap.navi.BNaviGuideActivity;
import com.baidu.automap.search.PoiSugSearchDemo;
import com.baidu.automap.util.HttpUtil;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.bikenavi.BikeNavigateHelper;
import com.baidu.mapapi.bikenavi.adapter.IBEngineInitListener;
import com.baidu.mapapi.bikenavi.adapter.IBRoutePlanListener;
import com.baidu.mapapi.bikenavi.model.BikeRoutePlanError;
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam;
import com.baidu.mapapi.bikenavi.params.BikeRouteNodeInfo;
import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class SelectRoutePlanActivity extends AppCompatActivity {
    private List<RoutePlanNode> list;

    private RecyclerView node_list_view;
    private Button addNode;
    private Button beginGuide;
    private LinearLayout guideChoice;
    private Button walkGuide;
    private Button bikeGuide;

    private BikeNavigateHelper mNaviHelper;
    BikeNaviLaunchParam param;
    private boolean isInit;
    private boolean isInitSuccess;

    private static String configCity;
    private static int POI_SUG = 1;
    private static final int WALKING_ROUTE = 2;
    private static final int BIKING_ROUTE = 3;


    private LatLng startLoc;
    private LatLng endLoc;
    private LatLng curLoc;
    private static final String KEY = "selectRoutePlanActivity";


    private int curRouteId;
    private RouteNodeResponse curNodeResponse;

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

        guideChoice = (LinearLayout) findViewById(R.id.guide_choice_select);
        walkGuide = (Button) findViewById(R.id.walking_guide_select);
        bikeGuide = (Button) findViewById(R.id.biking_guide_select);
        guideChoice.setVisibility(View.GONE);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        curRouteId = bundle.getInt("routeId");
        curLoc = new LatLng(bundle.getDouble("latitude"), bundle.getDouble("longitude"));
        Log.d(KEY, "routeId : " + curRouteId);

        curNodeResponse = new RouteNodeResponse();

        addNode = (Button) findViewById(R.id.add_node);
        beginGuide = (Button) findViewById(R.id.begin_guide);

        mNaviHelper = BikeNavigateHelper.getInstance();

        isInit = false;
        isInitSuccess = false;
        initBikeNavi();

        node_list_view = (RecyclerView) findViewById(R.id.select_all_node);
        node_list_view.setLayoutManager(new LinearLayoutManager(this));
        updateRouteNode();

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

                    guideChoice.setVisibility(View.VISIBLE);
//                    if(!isInit) {
//                        initBikeNavi();
//                    }
//                    Log.d(KEY, "startNode : " + startLoc.latitude + " , " + endLoc.longitude);
//                    Log.d(KEY, "endNode : " + endLoc.latitude + " , " + endLoc.longitude);
//
//                    if(isInitSuccess) {
//                        startBikeNavi();
//                        Log.d(KEY, "bikeNavi finished");
//                        finish();
//                    } else {
//                        Toast.makeText(SelectRoutePlanActivity.this, "导航模块初始化失败", Toast.LENGTH_LONG).show();
//                    }
                }
            }
        });

        walkGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeSearchBegin(WALKING_ROUTE);
                guideChoice.setVisibility(View.GONE);
            }
        });

        bikeGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeSearchBegin(BIKING_ROUTE);
                guideChoice.setVisibility(View.GONE);
            }
        });

    }

    /**
     * 开始导航
     * @param
     */
    public void routeSearchBegin(int choice) {
        Intent intent;
        if(choice == WALKING_ROUTE) {
            intent = new Intent(SelectRoutePlanActivity.this, WalkingRouteSearch.class);
        } else {
            intent = new Intent(SelectRoutePlanActivity.this, BikingRouteSearch.class);
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable("startNode", startLoc);
        bundle.putParcelable("endNode", endLoc);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void addRouteNode(RouteNode routeNode) {
        ThreadAddRouteNode thread = new ThreadAddRouteNode(routeNode);
        thread.start();

        try {
            thread.join();

            if(thread.getIsSuccess()) {
                updateRouteNode();
            } else {
                Toast.makeText(SelectRoutePlanActivity.this, thread.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void deleteNode(RouteNode routeNode) {
        ThreadDeleteRouteNode thread = new ThreadDeleteRouteNode(routeNode);
        thread.start();

        try {
            thread.join();

            if(thread.getIsSuccess()) {
                updateRouteNode();
            }
        } catch (InterruptedException e) {
            Log.d(KEY, e.toString());
        }
    }

    private class ThreadDeleteRouteNode extends Thread {

        private boolean isSuccess = false;

        private String message;

        private RouteNode mRouteNode;

        public ThreadDeleteRouteNode(RouteNode routeNode) {
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

                byte[] data = HttpUtil.readRouteParse("deleteRouteNode", null, mRouteNode);
                String str = new String(data);
                JSONObject jsonObject = new JSONObject(str);

                RouteNodeResponse routeNodeResponse = new RouteNodeResponse();
                routeNodeResponse.setMessage(jsonObject.getString("message"));
                if(routeNodeResponse != null && routeNodeResponse.getMessage().compareTo("success!") == 0) {
                    Log.d(KEY, "deleteRouteNode" + " get data from server success!");
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

    private void updateRouteNode() {
        UserRoute userRoute = new UserRoute();
        userRoute.setRouteId(curRouteId);
        ThreadUpdateRouteNode thread = new ThreadUpdateRouteNode(userRoute, "queryRouteNodeByRouteId");
        thread.start();

        try {
            thread.join();

            if(thread.getIsSuccess()) {
                Log.e(KEY, "thread.success!");
                refresh();
            } else {
                Log.e(KEY, "thread.failed");
                Toast.makeText(SelectRoutePlanActivity.this, thread.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ThreadUpdateRouteNode extends Thread {

        private boolean isSuccess = false;

        private String message;

        private UserRoute mUserRoute;

        private String urlAdd;

        public ThreadUpdateRouteNode(UserRoute userRoute, String urlAdd) {
            this.mUserRoute = userRoute;
            this.urlAdd = urlAdd;
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

                byte[] data = HttpUtil.readRouteParse(urlAdd, mUserRoute, null);
                String str = new String(data);
                JSONObject jsonObject = new JSONObject(str);

                RouteNodeResponse routeNodeResponse = new RouteNodeResponse();
                routeNodeResponse.setMessage(jsonObject.getString("message"));
                if(routeNodeResponse != null && routeNodeResponse.getMessage().compareTo("success!") == 0) {
                    Log.d(KEY, urlAdd + " get data from server success!");
                    isSuccess = true;
                    JSONArray jsonArray =new JSONArray(jsonObject.getString("nodeList"));

                    curNodeResponse.getNodeList().clear();

                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        RouteNode newNode = new RouteNode();
                        newNode.setDesId(object.getString("desId"));
                        newNode.setDesName(object.getString("desName"));
                        newNode.setLatitude(object.getDouble("latitude"));
                        newNode.setlongitude(object.getDouble("longitude"));
                        newNode.setNodeId(object.getInt("nodeId"));
                        newNode.setRouteId(object.getInt("routeId"));

                        curNodeResponse.addRouteNode(newNode);
                    }

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

    @Override
    public void onBackPressed() {
        Log.d(KEY, "back pressed!");
        setResult(RESULT_OK);
        finish();
    }

    private void refresh() {
        RoutePlanAdapter routeAdapter = new RoutePlanAdapter(this, curNodeResponse.getNodeList());
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

    private void initWalkNavi() {
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

                RouteNode routeNode = new RouteNode();
                routeNode.setRouteId(curRouteId);
                routeNode.setlongitude(latLng.longitude);
                routeNode.setLatitude(latLng.latitude);
                routeNode.setDesName(resultEntity.getKey());
                routeNode.setDesId(resultEntity.getuId());

                addRouteNode(routeNode);
            } else if(BIKING_ROUTE == requestCode) {
                Log.d(KEY, "finish bikeNavi");
                finish();
            } else {
                finish();
            }
        }
    }

    private class RoutePlanAdapter extends RecyclerView.Adapter implements View.OnClickListener {

        private List<RouteNode> list;
        private Context context;

        public RoutePlanAdapter(Context context, List<RouteNode> list) {
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

            private RouteNode routeNode;

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

            public void bind(RouteNode node) {
                routeNode = node;
                nodeName.setText(node.getDesName());
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
                        startLoc = new LatLng(list.get(position).getLatitude(), list.get(position).getlongitude());
                        Log.d(KEY, "setStart : " + startLoc.latitude + " , " + startLoc.longitude);
                        break;
                    case R.id.set_end:
                        endLoc = new LatLng(list.get(position).getLatitude(), list.get(position).getlongitude());
                        Log.d(KEY, "setEnd : " + endLoc.latitude + " , " + endLoc.longitude);
//                        mOnItemClickListener.onClick(v, ViewName.SET_END, position);
                        break;
                    case R.id.delete_node:
//                        list.remove(position);
                        deleteNode(list.get(position));
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
