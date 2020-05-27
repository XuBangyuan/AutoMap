package com.baidu.automap.build;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.automap.R;
import com.baidu.automap.entity.ImgEntity;
import com.baidu.automap.entity.Journey;
import com.baidu.automap.entity.response.ImgResponse;
import com.baidu.automap.entity.response.JourneyResponse;
import com.baidu.automap.util.FolderActivity;
import com.baidu.automap.util.HttpUtil;
import com.baidu.mapframework.commonlib.utils.IOUitls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class CreateJourneyActivity extends AppCompatActivity {

    private EditText titleEdit;
    private EditText contentEdit;
    private Button sureSendButton;
    private Button cancelSendButton;
    private LinearLayout makeSureExitLayout;
    private Button sureExitButton;
    private Button cancelExitButton;
    private RecyclerView imgList;
    private Button addImg;

    private int userId;
    private String uId;
    private List<ImgEntity> mList;
    private FileAdapter mAdapter;

    private static final String KEY = "createJourneyActivity";
    private static final int FLODER_ACTIVITY = 1;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message){

            if(message.what >= 0) {
                Toast.makeText(CreateJourneyActivity.this,
                        "正在上传图片 : " + mList.get(message.what).getName(), Toast.LENGTH_SHORT).show();
            } else if (message.what == -1) {
                Toast.makeText(CreateJourneyActivity.this,
                        "上传图片失败，请稍后再试", Toast.LENGTH_SHORT).show();
            } else {
                setResult(RESULT_OK);
                finish();
            }
        }
    };

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
        imgList = (RecyclerView) findViewById(R.id.img_upload_list);
        imgList.setLayoutManager(new LinearLayoutManager(CreateJourneyActivity.this));
        addImg = (Button) findViewById(R.id.add_img);


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        userId = bundle.getInt("userId");
        uId = bundle.getString("uId");

        mList = new LinkedList<>();
        mAdapter = new FileAdapter(mList);
        imgList.setAdapter(mAdapter);

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

        //添加图片按钮点击
        addImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imgIntent = new Intent(CreateJourneyActivity.this, FolderActivity.class);
                startActivityForResult(imgIntent, FLODER_ACTIVITY);
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

                                int journeyId = thread.getResponse().getJourneyList().get(0).getId();

                                for(ImgEntity entity : mList) {
                                    entity.setJourneyId(journeyId);
                                }

                                if(mList != null && mList.size() != 0) {
                                    uploadImg();
                                } else {
                                    setResult(RESULT_OK);
                                    finish();
                                }

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


    public void uploadImg() {
        ThreadUploadImg thread = new ThreadUploadImg(mList);
        thread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(KEY, "request : " + requestCode + " result : " + resultCode);
        if(RESULT_OK == resultCode) {
            if(FLODER_ACTIVITY == requestCode) {
                Log.d(KEY, "folderActivity");
//                ImgEntity resultEntity = (ImgEntity) data.getSerializableExtra("result");

                ImgEntity entity = new ImgEntity();
                String path = data.getStringExtra("path");
                String name = data.getStringExtra("name");

                entity.setName(name);
                entity.setData(IOUitls.readFile(path));

                mList.add(entity);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    /*
     * 计算采样率
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public Bitmap getBitmap(byte[] data) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//如此，无法decode bitmap
//        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 40, 40);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;//如此，方可decode bitmap

        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    private class FileHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private ImgEntity mEntity;

        private ImageView img;
        private TextView name;

        public FileHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.listitem_folder, parent, false));
            Log.d("holder", "begin build");
            itemView.setOnClickListener(this);

            img = (ImageView) itemView.findViewById(R.id.folder_img);
            name = (TextView) itemView.findViewById(R.id.folder_name);
        }

        public void bind(ImgEntity entity) {
            Log.d(KEY, "bind begin");

            this.mEntity = entity;
            Bitmap bitmap = getBitmap(mEntity.getData());
            img.setImageBitmap(bitmap);

            name.setText(mEntity.getName());

            Log.d(KEY, "bind finish");

        }

        @Override
        public void onClick(View view) {
            Log.d("holder", "begin click");



        }

    }


    private class FileAdapter extends RecyclerView.Adapter<FileHolder> {

        private List<ImgEntity> list;

        public FileAdapter(List<ImgEntity> list) {
            this.list = list;
        }

        @Override
        public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(CreateJourneyActivity.this);
            Log.d("adapter", "begin");

            return new FileHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(FileHolder holder, int position) {
            Log.d("adapter", "begin bind");

            ImgEntity entity = list.get(position);
            holder.bind(entity);

            Log.d("adapter", "end bind");
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private class ThreadUploadImg extends Thread {
        private boolean isSuccess;

        private List<ImgEntity> mList;

        private ImgResponse response;

        public ImgResponse getResponse() {
            return response;
        }

        public boolean getIsSuccess() {
            return isSuccess;
        }

        private String url;

        public ThreadUploadImg(List<ImgEntity> list) {
            this.url = "saveImg";
            mList = new LinkedList<>();
            if(list != null && list.size() != 0) {
                for(ImgEntity entity : list) {
                    mList.add(entity);
                }
            }
            response = new ImgResponse();
        }

        @Override
        public void run() {
            try {


                Log.d(KEY, "total upload size : " + mList.size());
                for(int i = 0; i < mList.size(); i++) {
                    Message message = new Message();
                    Log.d(KEY, "upload img " + i);
                    message.what = i;
                    mHandler.sendMessage(message);

                    byte[] data = HttpUtil.readImgParse(url , mList.get(i));
                    String str = new String(data);
                    JSONObject jsonObject = new JSONObject(str);

                    response.setMessage(jsonObject.getString("message"));
                    if(response != null && response.getMessage().compareTo("success!") == 0) {
                        Log.d(KEY, url + " get data from server success!");
                        isSuccess = true;
                    } else {
                        isSuccess = false;
                        Log.d(KEY, response.getMessage());
                        message.what = -1;
                        mHandler.sendMessage(message);
                    }
                }
            } catch (Exception e) {
                Log.e(KEY, e.toString());
            } finally {
                Message message = new Message();
                message.what = -2;
                mHandler.sendMessage(message);
            }
        }
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
                    JSONArray jsonArray = new JSONArray(jsonObject.getString("journeyList"));

                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        Journey jNode = new Journey();
                        jNode.setDesId(object.getString("desId"));
//                        jNode.setUserId(object.getInt("userId"));
//                        jNode.setAgree(object.getInt("agree"));
//                        jNode.setCreateTime(new Date(object.getLong("createTime")));
//                        jNode.setDetail(object.getString("detail"));
//                        jNode.setTitle(object.getString("title"));
                        jNode.setId(object.getInt("id"));

                        mJourneyResponse.addJourney(jNode);
                    }
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
