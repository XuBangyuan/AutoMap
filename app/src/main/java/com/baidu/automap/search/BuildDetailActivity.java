package com.baidu.automap.search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.automap.R;
import com.baidu.automap.entity.DesDetailIntroduction;
import com.baidu.automap.entity.response.DesDetailResponse;
import com.baidu.automap.entity.response.UserResponse;
import com.baidu.automap.util.HttpUtil;

import org.json.JSONObject;

public class BuildDetailActivity extends AppCompatActivity {

    //查看攻略按钮
    private Button strategyButton;
    //语音播报按钮
    private Button guideVideoButton;
    //更新介绍按钮
    private Button updateIntroduceButton;
    private EditText introduceText;


    //是否是管理员
    private Boolean isAdmin;
    //地点介绍信息
    private DesDetailIntroduction introduction;

    private String KEY = "buildDetailActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity_layout);

        introduction = new DesDetailIntroduction();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        isAdmin = bundle.getBoolean("isAdmin");
        introduction.setuId(bundle.getString("desId"));
        Log.d(KEY, "isAdmin : " + isAdmin + ", uId : " + introduction.getuId());

        strategyButton = (Button) findViewById(R.id.strategy_button);
        guideVideoButton = (Button) findViewById(R.id.guide_video_button);
        updateIntroduceButton = (Button) findViewById(R.id.update_introduce);
        introduceText = (EditText) findViewById(R.id.introduce_detail);

        if(!isAdmin) {
            Log.d(KEY, "not admin");
            introduceText.setEnabled(false);
            updateIntroduceButton.setVisibility(View.GONE);
        }

        getDesDetailIntroduction();

        //查看攻略按钮点击
        strategyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //查看语音播报按钮点击
        guideVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //更新景点介绍按钮点击
        updateIntroduceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDesDetailIntroduction();
            }
        });
    }

    private void updateDesDetailIntroduction() {
        introduction.setIntroduction(introduceText.getText().toString());
        ThreadDetail threadDetail = new ThreadDetail("saveDesDetailIntroduction");
        threadDetail.start();

        try {
            threadDetail.join();

            DesDetailResponse response = threadDetail.getResponse();
            Log.d(KEY, response.toString());

            if(threadDetail.isSuccess) {
                introduction.setDesDetailIntroduction(response.getIntroduction());
                introduceText.setText(introduction.getIntroduction());
                Toast.makeText(BuildDetailActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(BuildDetailActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } catch (InterruptedException e) {
            Log.d(KEY, e.toString());
        }

    }

    private void getDesDetailIntroduction() {
        ThreadDetail threadDetail = new ThreadDetail("queryDesDetailIntroduction");
        threadDetail.start();

        try {
            threadDetail.join();

            DesDetailResponse response = threadDetail.getResponse();
            Log.d(KEY, response.toString());

            if(threadDetail.isSuccess) {
                introduction.setDesDetailIntroduction(response.getIntroduction());
                introduceText.setText(introduction.getIntroduction());
            } else {
                Toast.makeText(BuildDetailActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } catch (InterruptedException e) {
            Log.d(KEY, e.toString());
        }

    }

    private class ThreadDetail extends Thread {
        private boolean isSuccess;

        private DesDetailResponse response;

        public DesDetailResponse getResponse() {
            return this.response;
        }

        private String url;

        public ThreadDetail(String url) {
            response = new DesDetailResponse();
            this.url = url;
        }

        @Override
        public void run() {
            try {

                byte[] data = HttpUtil.readDesDetailParse(url , introduction);
                String str = new String(data);
                JSONObject jsonObject = new JSONObject(str);

                response.setMessage(jsonObject.getString("message"));
                if(response != null && response.getMessage().compareTo("success!") == 0) {
                    Log.d(KEY, "get data from server success!");
                    isSuccess = true;
                    JSONObject jsonDetail = new JSONObject(jsonObject.getString("introduction"));

                    DesDetailIntroduction ddI = new DesDetailIntroduction();
                    ddI.setDesId(jsonDetail.getInt("desId"));
                    ddI.setuId(jsonDetail.getString("uId"));
                    ddI.setIntroduction(jsonDetail.getString("introduction"));

                    response.setIntroduction(ddI);

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
