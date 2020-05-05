package com.baidu.automap.searchroute;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.automap.R;
import com.baidu.automap.entity.RoutePlanList;
import com.baidu.automap.entity.UserRoute;
import com.baidu.automap.entity.response.RouteResponse;
import com.baidu.automap.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class RoutePlanActivity extends AppCompatActivity {

    private Button createRouteButton;
    private TextView curRouteName;
    private TextView curRouteDetail;

    private TextView allRouteName;
    private RecyclerView allRouteDetail;

    private RouteResponse curRouteResponse;
    private int curUserId;

    private static final String KEY = "routePlanActivity";

    RoutePlanList aRouteList = new RoutePlanList();
    RoutePlanList bRouteList = new RoutePlanList();
    RoutePlanList cRouteList = new RoutePlanList();

    //唤起的方式
    private static final int CALL_FOR_ADD = 1;
    private static final int CALL_FOR_ROUTE = 2;

    private int callMethod;

    private static final int SELECT_ROUTE_PLAN = 1;

    //bundle取出key
    private static final String CALL_FOR_ROUTE_PLAN_ACTIVITY = "CALL_FOR_ROUTE_PLAN_ACTIVITY";


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_route_window);

        allRouteDetail = (RecyclerView) findViewById(R.id.all_route_recycler_list);
        allRouteDetail.setLayoutManager(new LinearLayoutManager(RoutePlanActivity.this));
        createRouteButton = (Button) findViewById(R.id.create_route_plan);

        //新增路线按钮点击
        createRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRoutePlan();
            }
        });

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        curUserId = bundle.getInt("userId");
        curRouteResponse = new RouteResponse();
        Log.d(KEY, "userId :" + curUserId);
        updateUserRoute();

        callMethod = bundle.getInt(CALL_FOR_ROUTE_PLAN_ACTIVITY);


    }

    private void createRoutePlan() {
        UserRoute userRoute = new UserRoute();
        userRoute.setUserId(curUserId);
        ThreadCreateRoute threadCreateRoute = new ThreadCreateRoute(userRoute, "createRoute");
        threadCreateRoute.start();

        try {
            threadCreateRoute.join();

            if(threadCreateRoute.isSuccess) {
                updateUserRoute();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ThreadCreateRoute extends Thread {

        private boolean isSuccess = false;

        private String message;

        private UserRoute mUserRoute;

        private String urlAdd;

        public ThreadCreateRoute(UserRoute userRoute, String urlAdd) {
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

                RouteResponse routeResponse = new RouteResponse();
                routeResponse.setMessage(jsonObject.getString("message"));
                if(routeResponse != null && routeResponse.getMessage().compareTo("success!") == 0) {
                    Log.d(KEY, urlAdd + " get data from server success!");
                    isSuccess = true;
                } else {
                    isSuccess = false;
                    message = routeResponse.getMessage();
                    Log.d(KEY, routeResponse.getMessage());
                }
                Log.d(KEY, str);
            } catch (Exception e) {
                Log.e(KEY, e.toString());
            }
        }

    }

    private void updateUserRoute() {
        UserRoute userRoute = new UserRoute();
        userRoute.setUserId(curUserId);
        ThreadUpdateRoute threadRoute = new ThreadUpdateRoute(userRoute, "queryAllRoutes");
        threadRoute.start();

        try {
            threadRoute.join();

            if(threadRoute.getIsSuccess()) {
                allRouteDetail.setAdapter(new RouteAdapter(curRouteResponse.getRouteList()));
            } else {
                Log.d(KEY, threadRoute.getMessage());
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(curRouteResponse.getRouteList() == null || curRouteResponse.getRouteList().size() == 0) {
                Toast.makeText(RoutePlanActivity.this, "路线列表为空，请添加路线", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ThreadUpdateRoute extends Thread {

        private boolean isSuccess = false;

        private String message;

        private UserRoute mUserRoute;

        private String urlAdd;

        public ThreadUpdateRoute(UserRoute userRoute, String urlAdd) {
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

                RouteResponse routeResponse = new RouteResponse();
                routeResponse.setMessage(jsonObject.getString("message"));
                if(routeResponse != null && routeResponse.getMessage().compareTo("success!") == 0) {
                    Log.d(KEY, urlAdd + " get data from server success!");
                    isSuccess = true;
                    JSONArray jsonArray =new JSONArray(jsonObject.getString("routeList"));

                    curRouteResponse.getRouteList().clear();

                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        UserRoute newRoute = new UserRoute();
                        newRoute.setDescription(object.getString("description"));
                        newRoute.setRouteId(object.getInt("routeId"));
                        newRoute.setUserId(object.getInt("userId"));
                        curRouteResponse.addRoute(newRoute);
                    }

                } else {
                    isSuccess = false;
                    message = routeResponse.getMessage();
                    Log.d(KEY, routeResponse.getMessage());
                }
                Log.d(KEY, str);
            } catch (Exception e) {
                Log.e(KEY, e.toString());
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(KEY, "request : " + requestCode + " result : " + resultCode);
        if(RESULT_OK == resultCode) {
            if(SELECT_ROUTE_PLAN == requestCode) {
                Log.d(KEY, "onActivityResult SELECT_ROUTE_PLAN");

                updateUserRoute();

                allRouteDetail.setAdapter(new RouteAdapter(curRouteResponse.getRouteList()));

            }
        }
    }

    private class RouteNodeHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private UserRoute route;

        private TextView routeDetail;

        public RouteNodeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.route_node_layout, parent, false));
            Log.d("holder", "begin build");
            itemView.setOnClickListener(this);

            routeDetail = (TextView) itemView.findViewById(R.id.route_detail);
            Log.d("holder", "end build");
        }

        public void bind(UserRoute route) {
            Log.d("holder", "begin bind");

            this.route = route;
            if(route.getDescription() != null) {
                routeDetail.setText(route.getDescription());
            } else {
                routeDetail.setText("路线暂时没有节点");
            }


            Log.d("holder", "end bind");

        }

        @Override
        public void onClick(View view) {
            Log.d("holder", "begin click");

            if(callMethod == CALL_FOR_ROUTE) {

                Intent intent = new Intent(RoutePlanActivity.this, SelectRoutePlanActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("routeId", route.getRouteId());
                intent.putExtras(bundle);

                Log.d(KEY, "routeId : " + route.getRouteId() + ", des : " + route.getDescription());
                startActivityForResult(intent, SELECT_ROUTE_PLAN);
            } else if(callMethod == CALL_FOR_ADD) {
                //返回主界面，执行路线添加
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putInt("routeId", route.getRouteId());
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }


        }

    }


    private class RouteAdapter extends RecyclerView.Adapter<RouteNodeHolder> {

        private List<UserRoute> allPlanList;

        public RouteAdapter(List<UserRoute> list) {
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

            UserRoute route = allPlanList.get(position);
            holder.bind(route);

            Log.d("adapter", "end bind");
        }

        @Override
        public int getItemCount() {
            return allPlanList.size();
        }
    }

}


