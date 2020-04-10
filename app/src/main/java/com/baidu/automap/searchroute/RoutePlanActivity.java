package com.baidu.automap.searchroute;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.automap.R;
import com.baidu.automap.entity.ResultEntity;
import com.baidu.automap.entity.RoutePlanList;
import com.baidu.automap.entity.RoutePlanNode;
import com.baidu.automap.search.PoiSugSearchDemo;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteNode;

import java.util.LinkedList;
import java.util.List;

public class RoutePlanActivity extends AppCompatActivity {

    private Button createRouteButton;
    private TextView curRouteName;
    private TextView curRouteDetail;

    private TextView allRouteName;
    private RecyclerView allRouteDetail;

    private static final String KEY = "routePlanActivity";

    private static List<RoutePlanList> allRouteList = new LinkedList<>();

//    static List<RoutePlanNode> curRouteList = new LinkedList<>();
//    List<RoutePlanNode> aRouteList = new LinkedList<>();
//    List<RoutePlanNode> bRouteList = new LinkedList<>();
//    List<RoutePlanNode> cRouteList = new LinkedList<>();
    //List<List<RoutePlanNode>> allRouteList = new LinkedList<>();
    RoutePlanList aRouteList = new RoutePlanList();
    RoutePlanList bRouteList = new RoutePlanList();
    RoutePlanList cRouteList = new RoutePlanList();
    RoutePlanList curRouteList = new RoutePlanList();

    //唤起的方式
    private static final int CALL_FOR_ADD = 1;
    private static final int CALL_FOR_ROUTE = 2;

    private static final int SELECT_ROUTE_PLAN = 1;

    //bundle取出key
    private static final String CALL_FOR_ROUTE_PLAN_ACTIVITY = "CALL_FOR_ROUTE_PLAN_ACTIVITY";

     {
         curRouteList = aRouteList;
         //a路线
         LatLng aLoc = new LatLng(31.716205897230694 , 113.3500127893836);
         String aName = "楚天超市";
         aRouteList.add(new RoutePlanNode(aLoc, aName));

         aLoc = new LatLng(31.71736545380307 , 113.32903735572563);
         aName = "星乐小区";
         aRouteList.add(new RoutePlanNode(aLoc, aName));

         aLoc = new LatLng(31.73153236289405 , 113.35258194314216);
         aName = "随州市植物园";
         aRouteList.add(new RoutePlanNode(aLoc, aName));

         aLoc = new LatLng(31.708933377382394 , 113.36652364465701);
         aName = "白云公园";
         aRouteList.add(new RoutePlanNode(aLoc, aName));

         //b路线
         LatLng bLoc = new LatLng(31.727478316334143 , 113.37837229433362);
         String bName = "大润发";
         bRouteList.add(new RoutePlanNode(bLoc, bName));

         bLoc = new LatLng(31.72144298345487 , 113.3835195849058);
         bName = "时代广场";
         bRouteList.add(new RoutePlanNode(bLoc, bName));

         bLoc = new LatLng(31.741359611105874 , 113.37715059884005);
         bName = "明珠广场";
         bRouteList.add(new RoutePlanNode(bLoc, bName));

         bLoc = new LatLng( 31.748030791339843 , 113.3873104341579);
         bName = "曾都二中";
         bRouteList.add(new RoutePlanNode(bLoc, bName));

         //c路线
         LatLng cLoc = new LatLng(31.732906705449654, 113.3627777106804);
         String cName = "随州博物馆";
         cRouteList.add(new RoutePlanNode(cLoc, cName));

         cLoc = new LatLng(31.742411371396905, 113.36523008472264);
         cName = "随州市一中";
         cRouteList.add(new RoutePlanNode(cLoc, cName));

         cLoc = new LatLng(31.756052453979482, 113.3792616167885);
         cName = "圆梦星光城";
         cRouteList.add(new RoutePlanNode(cLoc, cName));

         cLoc = new LatLng(31.761801520065358, 113.37615347972398);
         cName = "星光便民店";
         cRouteList.add(new RoutePlanNode(cLoc, cName));

         allRouteList.clear();
         allRouteList.add(aRouteList);
         allRouteList.add(bRouteList);
         allRouteList.add(cRouteList);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_route_window);

        curRouteDetail = (TextView) findViewById(R.id.cur_route_detail);

        curRouteDetail.setText(getRouteDetail(curRouteList));

        allRouteDetail = (RecyclerView) findViewById(R.id.all_route_recycler_list);
        allRouteDetail.setLayoutManager(new LinearLayoutManager(RoutePlanActivity.this));
        allRouteDetail.setAdapter(new RouteApapter(allRouteList));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(KEY, "request : " + requestCode + " result : " + resultCode);
        if(RESULT_OK == resultCode) {
            if(SELECT_ROUTE_PLAN == requestCode) {
                //Bundle bundle = data.getExtras();
                Log.d(KEY, "want to know data");
//                ResultEntity resultEntity = (ResultEntity) data.getSerializableExtra("result");
                RoutePlanList routePlanList = (RoutePlanList) data.getParcelableExtra("route_list");

                Log.d(KEY, "resultRouteId : " + routePlanList.getId());
                for(RoutePlanList planList : allRouteList) {
                    Log.d(KEY, "curRouteId : " + planList.getId());
                    if(planList.getId().compareTo(routePlanList.getId()) == 0) {
                        Log.d(KEY, "update routePlan");
                        if(routePlanList.getList() == null || routePlanList.getList().size() == 0) {
                            allRouteList.remove(planList);
                            Log.d(KEY, "remove size : " + allRouteList.size());
                        } else {
                            planList.setList(routePlanList.getList());
                        }
                    }
                }

                curRouteDetail.setText(getRouteDetail(routePlanList));
                curRouteList = routePlanList;
                allRouteDetail.setAdapter(new RouteApapter(allRouteList));


            }
        }
    }

    private class RouteNodeHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private RoutePlanList routeNodeList;

        private TextView routeDetail;

        public RouteNodeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.route_node_layout, parent, false));
            Log.d("holder", "begin build");
            itemView.setOnClickListener(this);

            routeDetail = (TextView) itemView.findViewById(R.id.route_detail);
            Log.d("holder", "end build");
        }

        public void bind(RoutePlanList nodeList) {
            Log.d("holder", "begin bind");

            routeNodeList = nodeList;
            String detail = getRouteDetail(routeNodeList);
            routeDetail.setText(detail);

            Log.d("holder", "end bind");

        }

        @Override
        public void onClick(View view) {
            Log.d("holder", "begin click");
            curRouteList = routeNodeList;

            for(RoutePlanNode node : routeNodeList.getList()) {
                Log.d(KEY, "nodeLatlng " + node.getLatLng().latitude + " , " + node.getLatLng().longitude);
            }

            Intent intent = new Intent(RoutePlanActivity.this, SelectRoutePlanActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("route_list", routeNodeList);
            intent.putExtras(bundle);

            RoutePlanList list1 = (RoutePlanList) bundle.getParcelable("route_list");
            List<RoutePlanNode> list2 = list1.getList();
            for(RoutePlanNode node : list2) {
                Log.d(KEY, node.getName());
            }

            Log.d(KEY, "routeId : " + list1.getId());
            startActivityForResult(intent, SELECT_ROUTE_PLAN);

        }

    }


    private class RouteApapter extends RecyclerView.Adapter<RouteNodeHolder> {

        private List<RoutePlanList> allPlanList;

        public RouteApapter(List<RoutePlanList> list) {
            allPlanList = list;
        }

        @Override
        public RouteNodeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(RoutePlanActivity.this);
            Log.d("adapter", "begin");

            return new RouteNodeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(RouteNodeHolder holder, int position) {
            Log.d("adapter", "begin bind");

            RoutePlanList list = allPlanList.get(position);
            holder.bind(list);

            Log.d("adapter", "end bind");
        }

        @Override
        public int getItemCount() {
            return allPlanList.size();
        }
    }

    private String getRouteDetail(RoutePlanList list) {
        StringBuffer detailBuffer = new StringBuffer();
        for(RoutePlanNode node : list.getList()) {
            detailBuffer.append(node.getName());
            detailBuffer.append(" -- ");
        }
        detailBuffer.delete(detailBuffer.length() - 4, detailBuffer.length() - 1);
        String routeDetail = detailBuffer.toString();
        Log.d(KEY, "routeDetail : " + routeDetail + " , id : " +  list.getId());
        return routeDetail;
    }
}


