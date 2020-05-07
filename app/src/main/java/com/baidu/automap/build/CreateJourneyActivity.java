package com.baidu.automap.build;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.automap.R;
import com.baidu.automap.entity.Journey;
import com.baidu.automap.entity.response.JourneyResponse;
import com.baidu.automap.util.HttpUtil;

import org.json.JSONObject;

import java.util.Date;

public class CreateJourneyActivity extends AppCompatActivity {

    private EditText titleEdit;
    private EditText contentEdit;
    private Button sureSendButton;
    private Button cancelSendButton;
    private LinearLayout makeSureExitLayout;
    private Button sureExitButton;
    private Button cancelExitButton;

    private int userId;
    private String uId;

    private static final String KEY = "createJourneyActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_journey);
        titleEdit = (EditText)findViewById(R.id.journey_title_edit);
        contentEdit = (EditText) findViewById(R.id.journey_content_edit);
        sureSendButton = (Button) findViewById(R.id.sure_send_journey);
        cancelSendButton = (Button) findViewById(R.id.cancel_send_journey);
        makeSureExitLayout = (LinearLayout)findViewById(R.id.make_sure_exit);
        sureExitButton = (Button) findViewById(R.id.sure_exit);
        cancelExitButton = (Button) findViewById(R.id.cancel_exit);
        makeSureExitLayout.setVisibility(View.GONE);


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        userId = bundle.getInt("userId");
        uId = bundle.getString("uId");

        //取消发送按钮点击
        cancelSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeSureExitLayout.setVisibility(View.VISIBLE);
            }
        });

        cancelExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeSureExitLayout.setVisibility(View.GONE);
            }
        });

        //确认退出按钮点击
        sureExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        //确认发送按钮点击
        sureSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleEdit.getText().toString();
                String content = contentEdit.getText().toString();

                if(title == null || title.length() == 0) {
                    Toast.makeText(CreateJourneyActivity.this, "请编辑题目", Toast.LENGTH_SHORT).show();
                } else {
                    if(content == null || content.length() == 0){
                        Toast.makeText(CreateJourneyActivity.this, "请编辑内容", Toast.LENGTH_SHORT).show();
                    } else {
                        Journey journey = new Journey();
                        journey.setUserId(userId);
                        journey.setDesId(uId);
                        journey.setCreateTime(new Date());
                        journey.setTitle(title);
                        journey.setDetail(content);

                        ThreadCreateJourney thread = new ThreadCreateJourney(journey);
                        thread.start();

                        try {
                            thread.join();

                            if(thread.getIsSuccess()) {
                                Log.d(KEY, "create journey success");
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Log.d(KEY, thread.getResponse().getMessage());
                                Toast.makeText(CreateJourneyActivity.this, thread.getResponse().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (InterruptedException e) {
                            Log.d(KEY, e.toString());
                        }
                    }
                }
            }
        });

    }

    private class ThreadCreateJourney extends Thread {
        private boolean isSuccess;
        private Journey mJourney;
        private JourneyResponse mJourneyResponse;

        public JourneyResponse getResponse() {
            return mJourneyResponse;
        }

        public boolean getIsSuccess() {
            return isSuccess;
        }

        private String url;

        public ThreadCreateJourney(Journey journey) {
            this.url = "insertJourney";
            this.mJourney = journey;
            mJourneyResponse = new JourneyResponse();
        }

        @Override
        public void run() {
            try {
                Log.d(KEY, "begin http connect " + mJourney.toString());

                byte[] data = HttpUtil.readJourneyParse(url , mJourney);
                String str = new String(data);
                JSONObject jsonObject = new JSONObject(str);

                mJourneyResponse.setMessage(jsonObject.getString("message"));
                if(mJourneyResponse != null && mJourneyResponse.getMessage().compareTo("success!") == 0) {
                    Log.d(KEY, url + " get data from server success!");
                    isSuccess = true;
                } else {
                    isSuccess = false;
                    Log.d(KEY, mJourneyResponse.getMessage());
                }
                Log.d(KEY, str);
            } catch (Exception e) {
                Log.e(KEY, e.toString());
            }
        }
    }
}
