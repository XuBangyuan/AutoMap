package com.baidu.automap;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.mapframework.commonlib.utils.IOUitls;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import map.baidu.ar.utils.IOUtils;

public class MediaTrialActivity extends AppCompatActivity {

    private Button mediaStart,mediaPause,mediaStop;
    private MediaPlayer mediaPlayer;

    private static final String KEY = "mediaTrialActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_player_layout);

        /* 初始化 */
        init();
        mediaPlayer_init();



        String path = Environment.getExternalStorageDirectory().getPath();
        Log.d(KEY, path);

        File file = new File(path + "/trial/trial.txt");

        String str = "abc";

        try {

            if(!file.exists()) {
                file.getParentFile().mkdir();
                file.createNewFile();
            }

            FileOutputStream out = new FileOutputStream(file);



            out.write(str.getBytes());
            out.flush();
            out.close();

            Log.d(KEY, file.getAbsolutePath());

        } catch (Exception e) {
            Log.d(KEY, e.toString());
        }


//        /* 事件：控制音频文件的状态 */
//        View.OnClickListener listener = new View.OnClickListener() {
//
//            public void onClick(View v) {
//                Button btn = (Button)v;
//                int id = btn.getId();
//                switch (id) {
//                    case R.id.media_start:   //开始
//                        mediaPlayer.start();
//                        Toast.makeText(MediaTrialActivity.this, "开始播放", Toast.LENGTH_SHORT).show();
//                        break;
//                    case R.id.media_pause:   //暂停
//                        if(mediaPlayer.isPlaying()){
//                            mediaPlayer.pause();
//                            Toast.makeText(MediaTrialActivity.this, "暂停播放", Toast.LENGTH_SHORT).show();
//                        }
//                        break;
//
//                    default:
//                        break;
//                }
//            }
//        };
//
//        /* 设置事件监听 */
//        mediaPause.setOnClickListener(listener);
//        mediaStart.setOnClickListener(listener);
//        mediaStop.setOnClickListener(listener);
    }

    /* MediaPlayer 初始化工作 *//* 音频文件在SD卡上 */
    public void mediaPlayer_init(){
//        newDirectory("trial", "sky1.mp3");

        String path = Environment.getExternalStorageDirectory().getPath();
        File file = new File(path + "/sky.mp3");
        try {
            byte[] bytes= IOUtils.getBytesInputSteam(new FileInputStream("sky.mp3"));
            FileOutputStream outputStream = new FileOutputStream(file);
            InputStream in= new ByteArrayInputStream(bytes);//读

            IOUitls.copy(in, outputStream);
        } catch (Exception e) {
            Log.d("trial", e.toString());
        }


        /* 获得MeidaPlayer对象 */
              mediaPlayer = new MediaPlayer();

              /* 得到文件路径 *//* 注：文件存放在SD卡的根目录，一定要进行prepare()方法，使硬件进行准备 */
              File file1 = new File(Environment.getExternalStorageDirectory(),"sky.mp3");
//        File file = new File("trial/sky1.mp3");

              try{
                  /* 为MediaPlayer 设置数据源 */
                  mediaPlayer.setDataSource(file1.getAbsolutePath());

                  /* 准备 */
                  mediaPlayer.prepare();

              }catch(Exception ex){
                  ex.printStackTrace();
              }


//
//        File file = new File("/raw/sky.mp3");
//        try {
//            FileInputStream in = new FileInputStream(file);
//
//            OutputStream out = new FileOutputStream("/raw/sky1.mp3");
//
//            IOUitls.copy(in, out);
//
//        } catch (Exception e) {
//            Log.d("trial", e.toString());
//        }
//
//
//        /* 从res/raw 资源中获取文件 */
//        String path = Environment.getExternalStorageState()
//
//
//        mediaPlayer = MediaPlayer.create(this,R.raw.sky1);

        /* 根据URI：创建 */
        //mediaPlayer = MediaPlayer.create(this, Uri.parse("/mnt/sdcard/aa.mp3"));

        /* 网络URI流 */
        //mediaPlayer = MediaPlayer.create(this, Uri.parse("http://www.sunzone.com/aa.mp3"));

    }

    public void newDirectory(String _path,String dirName){
        File file = new File(_path+"/"+dirName);
        try {
            if (!file.exists()) {
                file.mkdir();
            }

            File file1 = new File("/raw/sky.mp3");
            FileInputStream inputStream = new FileInputStream(file1);
            FileOutputStream outputStream = new FileOutputStream(file);
            IOUitls.copy(inputStream, outputStream);

        }catch (Exception e){
            e.printStackTrace();
        }


    }

    protected void onDestroy() {
        super.onDestroy();
        /* 释放MeidaPlayer 对象 */
        mediaPlayer.release();
    }

    /* 初始化组件对象 */
    public void init(){
        mediaStart = (Button) findViewById(R.id.media_start);
        mediaPause = (Button) findViewById(R.id.media_pause);
//        mediaStop = (Button) findViewById(R.id.media_stop);
    }
}
