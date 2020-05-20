package com.baidu.automap.build;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.automap.R;
import com.baidu.automap.entity.Mp3Entity;
import com.baidu.automap.entity.response.MediumTran;
import com.baidu.automap.entity.response.Mp3Response;
import com.baidu.automap.util.HttpUtil;
import com.baidu.mapframework.commonlib.utils.IOUitls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MediumActivity extends AppCompatActivity {

    private String uId;

    private RecyclerView mediumList;
    private String path;

    private Mp3Response response;

    private MediaPlayer mediaPlayer;

    private int prePosition;
    private Mp3Entity curMp3;
    private boolean change;

    private static final String KEY = "mediumActivity";

    protected void onDestroy() {
        super.onDestroy();
        //  释放MediaPlayer 对象
        if(mediaPlayer != null) {
            mediaPlayer.release();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medium_list_activity);

        mediumList = (RecyclerView) findViewById(R.id.medium_list);
        mediumList.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        uId = bundle.getString("uId");
        response = new Mp3Response();
        curMp3 = new Mp3Entity();
        change = true;
        prePosition = -1;


        initDir();

        updateMediumList();
    }

    public void initDir() {
        path = Environment.getExternalStorageDirectory().getPath() + "/autoMap";
        Log.d(KEY, path);
        File file = new File(path + "/detail.mp3");

        try {

            if(!file.exists()) {
                file.getParentFile().mkdir();
//                file.createNewFile();
            }

            Log.d(KEY, file.getAbsolutePath());

        } catch (Exception e) {
            Log.d(KEY, e.toString());
        }
    }

    public void updateMediumList() {
        Mp3Entity entity = new Mp3Entity();
        entity.setDesId(uId);

        ThreadUpdateMedium thread = new ThreadUpdateMedium( entity,"findAllMediumById");
        thread.start();

        try {
            thread.join();

            if(thread.isSuccess) {
                Log.d(KEY, "get data from server success");
                refresh();
            } else {
                Log.d(KEY, response.getMessage());
                Toast.makeText(MediumActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException e) {
            Log.d(KEY, e.toString());
        }
    }

    private class ThreadUpdateMedium extends Thread {

        private boolean isSuccess = false;

        private Mp3Entity mMp3Entity;

        private String url;

        public ThreadUpdateMedium(Mp3Entity entity, String url) {
            this.mMp3Entity = entity;
            this.url = url;
        }

        public boolean getIsSuccess() {
            return isSuccess;
        }

        @Override
        public void run() {
            try {

                byte[] data = HttpUtil.readMP3Parse(url, mMp3Entity);
                String str = new String(data);
                JSONObject jsonObject = new JSONObject(str);
                Log.d(KEY, jsonObject.toString());

                response.setMessage(jsonObject.getString("message"));
                //response != null && response.getMessage().compareTo("success!") == 0
                if(response != null && response.getMessage().compareTo("success!") == 0) {
                    Log.d(KEY, url + " get data from server success!");
                    isSuccess = true;
                    JSONArray jsonArray =new JSONArray(jsonObject.getString("list"));

                    response.getList().clear();

                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        MediumTran entity = new MediumTran();
                        entity.setDesId(object.getString("desId"));
//                        entity.setFile(object.getString("file"));
                        entity.setId(object.getInt("id"));
                        entity.setName(object.getString("name"));

                        response.addEntity(new Mp3Entity(entity));
                    }

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

    private void refresh() {
        MediumAdapter routeAdapter = new MediumAdapter(this, response.getList());
        mediumList.setAdapter(routeAdapter);
        routeAdapter.setOnItemClickListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onClick(View view, ViewName viewName, int position) {
                //在此处理点击事件即可，viewName可以区分是item还是内部控件
//                switch (viewName) {
//                    case DELETE_NODE:
//                        refresh();
//                        break;
//                    default:
//                        break;
//                }
            }
        });
    }

    private class MediumAdapter extends RecyclerView.Adapter implements View.OnClickListener {

        private List<Mp3Entity> list;
        private Context context;

        public MediumAdapter(Context context, List<Mp3Entity> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.media_player_layout, parent, false);
            return new ItemHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ItemHolder itemHolder = (ItemHolder) holder;
            itemHolder.itemView.setTag(position);
            itemHolder.start.setTag(position);
            itemHolder.pause.setTag(position);

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

            private Mp3Entity mMp3Entity;

            private String realName;
            private TextView name;
            private Button start;
            private Button pause;

            public ItemHolder(View itemView) {
                super(itemView);

                name = (TextView) itemView.findViewById(R.id.medium_name);
                start = (Button) itemView.findViewById(R.id.media_start);
                pause = (Button) itemView.findViewById(R.id.media_pause);

                //将创建的View注册点击事件
                itemView.setOnClickListener(MediumAdapter.this);
                start.setOnClickListener(MediumAdapter.this);
                pause.setOnClickListener(MediumAdapter.this);
            }

            public void bind(Mp3Entity node) {
                realName = node.getName();
                mMp3Entity = node;
                name.setText(node.getName().substring(0, node.getName().length() - 13));
            }
        }

        private OnRecyclerViewItemClickListener mOnItemClickListener = null;

        public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
            this.mOnItemClickListener = listener;
        }


        @Override
        public void onClick(View v) {
            //注意这里使用getTag方法获取数据
            int position = (int) v.getTag();

            if(position != prePosition) {
                curMp3.setMp3(list.get(position));
                change = true;
                Log.d(KEY, "change position : " + position);
            } else {
                change = false;
            }

            prePosition = position;

            Mp3Entity mp3Entity = list.get(position);

            if (mOnItemClickListener != null) {
                switch (v.getId()){
                    case R.id.media_start:
                        startMedium();
                        break;
                    case R.id.media_pause:
                        pauseMedium();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /** item里面有多个控件可以点击 */
    public enum ViewName {
        START_PLAY,
        PAUSE_PLAY,
        ELSE
    }

    public interface OnRecyclerViewItemClickListener {
        void onClick(View view, ViewName viewName, int position);
    }

    public void startMedium() {
        if(change) {

            updateMedium();

            File file = new File(path + "/" + curMp3.getName() + ".mp3");
            if(!file.exists()) {
                try {
                    file.createNewFile();
                    InputStream in = new ByteArrayInputStream(curMp3.getFile());
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        IOUitls.copy(in, out);
                    } catch (Exception e) {
                        Log.d(KEY, e.toString());
                    }
                } catch (IOException e) {
                    Log.d(KEY, e.toString());
                }
            }

            if(mediaPlayer != null) {
                mediaPlayer.pause();
            }

            /* 获得MediaPlayer对象 */
            mediaPlayer = new MediaPlayer();

            /* 得到文件路径 *//* 注：文件存放在SD卡的根目录，一定要进行prepare()方法，使硬件进行准备 */

            try{
                /* 为MediaPlayer 设置数据源 */
                mediaPlayer.setDataSource(file.getAbsolutePath());

                /* 准备 */
                mediaPlayer.prepare();

            }catch(Exception ex){
                Log.d(KEY, ex.toString());
            }

            Toast.makeText(MediumActivity.this, "开始播放", Toast.LENGTH_SHORT).show();
        }

        mediaPlayer.start();
    }

    public void pauseMedium() {
        if(change) {
            updateMedium();

            File file = new File(path + "/" + curMp3.getName() + ".mp3");
            if(!file.exists()) {
                try {
                    file.createNewFile();

                    Log.d(KEY + "path", path);
                    Log.d(KEY, curMp3.getName());
                    Log.d(KEY + "file path", file.getAbsolutePath());

                    InputStream in = new ByteArrayInputStream(curMp3.getFile());
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        IOUitls.copy(in, out);
                    } catch (Exception e) {
                        Log.d(KEY, e.toString());
                    }
                } catch (IOException e) {
                    Log.d(KEY, e.toString());
                }
            }

            if(mediaPlayer != null) {
                mediaPlayer.pause();
            }

            /* 获得MediaPlayer对象 */
            mediaPlayer = new MediaPlayer();

            /* 得到文件路径 *//* 注：文件存放在SD卡的根目录，一定要进行prepare()方法，使硬件进行准备 */

            try{
                /* 为MediaPlayer 设置数据源 */
                mediaPlayer.setDataSource(file.getAbsolutePath());

                /* 准备 */
                mediaPlayer.prepare();

            }catch(Exception ex){
                Log.d(KEY, ex.toString());
            }
        } else {
            Toast.makeText(MediumActivity.this, "暂停播放", Toast.LENGTH_SHORT).show();
        }

        mediaPlayer.pause();
    }

    public void updateMedium() {

        File file = new File(path + "/" + curMp3.getName() + ".mp3");
        if (!file.exists()) {
            Mp3Entity entity = new Mp3Entity();
            entity.setId(curMp3.getId());
            ThreadGetMedium thread = new ThreadGetMedium(entity, "findMediumById");
            thread.start();

            try {
                thread.join();

                if(!thread.isSuccess) {
                    Log.e(KEY, "get medium failed");
                    Toast.makeText(MediumActivity.this,
                            "获取失败，请稍后再试", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                Log.d(KEY, e.toString());
            }
        }
    }

    private class ThreadGetMedium extends Thread {

        private boolean isSuccess = false;

        private Mp3Entity mMp3Entity;

        private String url;

        public ThreadGetMedium(Mp3Entity entity, String url) {
            this.mMp3Entity = entity;
            this.url = url;
        }

        public boolean getIsSuccess() {
            return isSuccess;
        }

        @Override
        public void run() {
            try {

                byte[] data = HttpUtil.readMP3Parse(url, mMp3Entity);
                String str = new String(data);
                JSONObject jsonObject = new JSONObject(str);
                Log.d(KEY, jsonObject.toString());

                response.setMessage(jsonObject.getString("message"));
                if(response != null && response.getMessage().compareTo("success!") == 0) {
                    Log.d(KEY, url + " get data from server success!");
                    isSuccess = true;

                    JSONArray jsonArray =new JSONArray(jsonObject.getString("list"));

                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        MediumTran entity = new MediumTran();
                        entity.setDesId(object.getString("desId"));
                        entity.setFile(object.getString("file"));
                        entity.setId(object.getInt("id"));
                        entity.setName(object.getString("name"));

                        curMp3 = new Mp3Entity(entity);
                    }

                    if(curMp3.getFile() != null) {
                        Log.d(KEY, "curMp3.getFile.length : " + curMp3.getFile().length);
                    } else {
                        Log.d(KEY, "curMp3.getFile failed");
                    }

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
