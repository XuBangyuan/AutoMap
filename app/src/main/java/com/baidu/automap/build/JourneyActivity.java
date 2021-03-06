package com.baidu.automap.build;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.automap.R;
import com.baidu.automap.entity.Journey;
import com.baidu.automap.entity.response.JourneyResponse;
import com.baidu.automap.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class JourneyActivity extends AppCompatActivity {

    private int userId;
    private String uId;
    //管理员标识
    private boolean isAdmin;
    private JourneyResponse response;
    private Journey curJourney;

    //创建攻略
    private Button createJourney;
    //攻略列表
    private RecyclerView journeyList;
    private LinearLayout makeSureDelete;
    private Button sureDeleteButton;
    private Button cancelDeleteButton;
    private Button checkJourneyButton;

    private boolean isShow;


    private static final String KEY = "journeyActivity";

    private static final int JOURNEY_DETAIL = 1;
    private static final int CREATE_JOURNEY = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journey_activity);

        createJourney = (Button) findViewById(R.id.create_journey);
        journeyList = (RecyclerView) findViewById(R.id.journey_name_list);
        journeyList.setLayoutManager(new LinearLayoutManager(JourneyActivity.this));
        makeSureDelete = (LinearLayout) findViewById(R.id.make_sure_delete_journey);
        sureDeleteButton = (Button) findViewById(R.id.sure_delete_journey);
        cancelDeleteButton = (Button) findViewById(R.id.cancel_delete_journey);
        makeSureDelete.setVisibility(View.GONE);
        checkJourneyButton = (Button) findViewById(R.id.check_journey);
        isShow = false;

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        userId = bundle.getInt("userId");
        uId = bundle.getString("uId");
        isAdmin = bundle.getBoolean("isAdmin");
        Log.d(KEY, "userId : " + userId + ", uId : " + uId + ", isAdmin : " + isAdmin);
        response = new JourneyResponse();

        //创建攻略按钮点击
        createJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cIntent = new Intent(JourneyActivity.this, CreateJourneyActivity.class);
                Bundle cBundle = new Bundle();
                cBundle.putInt("userId", userId);
                cBundle.putString("uId", uId);
                cIntent.putExtras(cBundle);
                startActivityForResult(cIntent, CREATE_JOURNEY);
            }
        });

        //确认删除攻略
        sureDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteJourney();
            }
        });

        //取消删除攻略
        cancelDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeSureDelete.setVisibility(View.GONE);
                curJourney = null;
            }
        });

        //管理员查看攻略
        checkJourneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JourneyActivity.this, JourneyDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("journey", curJourney);
                bundle.putLong("createTime", curJourney.getCreateTime().getTime());
                bundle.putInt("userId", userId);
                bundle.putBoolean("isAdmin", isAdmin);
                intent.putExtras(bundle);

                startActivityForResult(intent, JOURNEY_DETAIL);
            }
        });

        updateJourneyList();

    }

    @Override
    public void onBackPressed() {
        if (isShow) {
            makeSureDelete.setVisibility(View.GONE);
            isShow = false;
        } else {
            finish();
        }

    }

    //删除攻略
    private void deleteJourney() {
        ThreadJourney thread = new ThreadJourney("deleteJourneyByCondition",
                curJourney);
        thread.start();
        try {
            thread.join();
            if(thread.getIsSuccess()) {
                Log.d(KEY, "删除攻略成功，更新攻略列表");
                updateJourneyList();
                makeSureDelete.setVisibility(View.GONE);
                isShow = false;
            } else {
                Log.d(KEY, response.getMessage());
                Toast.makeText(JourneyActivity.this, response.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.d(KEY , e.toString());
        }
    }

    //更新journeyList
    private void updateJourneyList() {
        ThreadJourney thread = new ThreadJourney("findJourneyByCondition", null);
        thread.start();

        try {
            thread.join();

            if(thread.getIsSuccess()) {
                Log.d(KEY, "更新列表成功，下一步展示");
                refreshJourneyList();
            } else {
                Log.d(KEY, response.getMessage());
                Toast.makeText(JourneyActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } catch (InterruptedException e) {
            Log.d(KEY, e.toString());
        }
    }

    private void refreshJourneyList() {
        JourneyAdapter journeyAdapter = new JourneyAdapter(response.getJourneyList());
        journeyList.setAdapter(journeyAdapter);
    }

    private class JourneyHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Journey journey;

        private TextView title;
        private TextView author;
        private TextView createTime;
        private TextView agree;

        public JourneyHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.journey_item, parent, false));
            Log.d("holder", "begin build");
            itemView.setOnClickListener(this);

            title = (TextView) itemView.findViewById(R.id.journey_detail_title);
            author = (TextView) itemView.findViewById(R.id.journey_author);
            createTime = (TextView) itemView.findViewById(R.id.journey_create_time);
            agree = (TextView) itemView.findViewById(R.id.journey_agree_num);

            Log.d("holder", "end build");
        }

        public void bind(Journey journey) {
            Log.d("holder", "begin bind " + journey.toString());

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.journey = journey;
            title.setText(journey.getTitle());
            author.setText(journey.getUserId() + "");
            createTime.setText(format.format(journey.getCreateTime()));
            agree.setText("赞 ：" + journey.getAgree() + "");

            Log.d("holder", "end bind");

        }

        @Override
        public void onClick(View view) {
            Log.d("holder", "begin click");

            if(!isAdmin) {
                Intent intent = new Intent(JourneyActivity.this, JourneyDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("journey", journey);
                bundle.putLong("createTime", journey.getCreateTime().getTime());
                bundle.putInt("userId", userId);
                intent.putExtras(bundle);

                startActivityForResult(intent, JOURNEY_DETAIL);
            } else {
                curJourney = journey;
                makeSureDelete.setVisibility(View.VISIBLE);
                isShow = true;
            }

        }

    }


    private class JourneyAdapter extends RecyclerView.Adapter<JourneyHolder> {

        private List<Journey> list;

        public JourneyAdapter(List<Journey> list) {
            this.list = list;
        }

        @Override
        public JourneyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(JourneyActivity.this);
            Log.d("adapter", "begin");

            return new JourneyHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(JourneyHolder holder, int position) {
            Log.d("adapter", "begin bind");

            Journey journey = list.get(position);
            holder.bind(journey);

            Log.d("adapter", "end bind");
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(KEY, "request : " + requestCode + " result : " + resultCode);
        makeSureDelete.setVisibility(View.GONE);
        isShow = false;
        if(RESULT_OK == resultCode) {
            if(CREATE_JOURNEY == requestCode || JOURNEY_DETAIL == requestCode) {
                Log.d(KEY, "journey onActivityResult");
                updateJourneyList();
            }
        }
    }

    private class ThreadJourney extends Thread {
        private boolean isSuccess;

        public boolean getIsSuccess() {
            return isSuccess;
        }

        private String url;

        private Journey mJourney;

        public ThreadJourney(String url, Journey journey) {
            this.url = url;
            this.mJourney = journey;
        }

        @Override
        public void run() {
            try {

                if(mJourney == null) {
                    mJourney = new Journey();
                    mJourney.setUserId(userId);
                    mJourney.setDesId(uId);
                }


                Log.d(KEY, "begin http connect " + mJourney.toString());

                byte[] data = HttpUtil.readJourneyParse(url , mJourney);
                String str = new String(data);
                JSONObject jsonObject = new JSONObject(str);

                response.setMessage(jsonObject.getString("message"));
                if(response != null && response.getMessage().compareTo("success!") == 0) {
                    Log.d(KEY, url + " get data from server success!");
                    isSuccess = true;

                    if(jsonObject.getString("journeyList") != null) {
                        JSONArray jsonArray =new JSONArray(jsonObject.getString("journeyList"));

                        response.getJourneyList().clear();

                        for(int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            Journey jNode = new Journey();
                            jNode.setDesId(object.getString("desId"));
                            jNode.setUserId(object.getInt("userId"));
                            jNode.setAgree(object.getInt("agree"));
                            jNode.setCreateTime(new Date(object.getLong("createTime")));
                            jNode.setDetail(object.getString("detail"));
                            jNode.setTitle(object.getString("title"));
                            jNode.setId(object.getInt("id"));

                            response.addJourney(jNode);
                        }
                    }

                    Log.d(KEY, response.toString());

                } else {
                    isSuccess = false;
                    Log.d(KEY, response.getMessage());
                }
                Log.d(KEY, str);
            } catch (Exception e) {
                Log.e(KEY, e.toString());
            }
        }
    }

}
