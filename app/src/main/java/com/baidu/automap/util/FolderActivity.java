package com.baidu.automap.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.automap.R;
import com.baidu.automap.entity.ImgEntity;
import com.baidu.mapframework.commonlib.utils.IOUitls;

public class FolderActivity extends Activity implements OnItemClickListener,OnClickListener {

    private RecyclerView folderLv;
    private TextView foldernowTv;
    private FileAdapter sAdapter;
    private List<Map<String, Object>> aList;
    private String baseFile;

    LinearLayout makeSure;
    Button sureImg;
    Button cancelImg;

    private String choiceImgPath;
    private String choiceImgName;
    private boolean isShow;

    private TextView titleTv;

    private static final String KEY = "folderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mypage_folder);
        baseFile=GetFilesUtils.getInstance().getBasePath();

//        titleTv=(TextView) findViewById(R.id.mtitle);
//        titleTv.setText("本地文件");

        folderLv=(RecyclerView) findViewById(R.id.folder_list);
        foldernowTv=(TextView) findViewById(R.id.folder_now);
        makeSure = (LinearLayout) findViewById(R.id.make_sure_choice);
        sureImg = (Button) findViewById(R.id.sure_choice_img);
        cancelImg = (Button) findViewById(R.id.cancel_choice_img);
        makeSure.setVisibility(View.GONE);
//        Drawable folderBack = getResources().getDrawable(R.drawable.folder_back);
        Drawable folderBack = getResources().getDrawable(R.drawable.folder_back);

        folderBack.setBounds(0, 0, 50, 50);
        foldernowTv.setCompoundDrawables(folderBack, null, null, null);
        foldernowTv.setText(baseFile);
        foldernowTv.setOnClickListener(this);
        aList=new ArrayList<Map<String,Object>>();
        folderLv.setLayoutManager(new LinearLayoutManager(FolderActivity.this));
        sAdapter = new FileAdapter(aList);
        folderLv.setAdapter(sAdapter);
//        folderLv.setOnItemClickListener(this);
        try {
            loadFolderList(baseFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//        choiceImg = new ImgEntity();

        sureImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("path", choiceImgPath);
                bundle.putString("name", choiceImgName);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
//                Log.d(KEY, choiceImg.getName() + choiceImg.getData().length);
                finish();
            }
        });

        cancelImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                makeSure.setVisibility(View.GONE);
                isShow = false;
            }
        });
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if(isShow) {
            makeSure.setVisibility(View.GONE);
            isShow = true;
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void loadFolderList(String file) throws IOException{
        List<Map<String, Object>> list=GetFilesUtils.getInstance().getSonNode(file);
        if(list!=null){
            Collections.sort(list, GetFilesUtils.getInstance().defaultOrder());
            aList.clear();
            for( Map<String, Object> map:list ) {
                String fileType = (String) map.get(GetFilesUtils.FILE_INFO_TYPE);
                Map<String,Object> gMap = new HashMap<String, Object>();
                if( map.get(GetFilesUtils.FILE_INFO_ISFOLDER ).equals(true)){
                    gMap.put("fIsDir", true);
//                    Resources resources = getApplicationContext().getResources();
//                    Drawable drawable = resources.getDrawable(R.drawable.filetype_folder);
                    gMap.put("fImgDrawable", R.drawable.filetype_folder);
                    try {
                        int resId = R.drawable.class.getDeclaredField("filetype_folder").getInt(R.drawable.class);
                        Log.e(KEY, "resId : " + resId);
                    } catch (IllegalAccessException e) {
                        Log.e(KEY, e.toString());
                    } catch (NoSuchFieldException e) {
                        Log.e(KEY, e.toString());
                    }
                    Log.e(KEY, "drawableId " + R.drawable.filetype_folder );

                    gMap.put("fInfo", map.get(GetFilesUtils.FILE_INFO_NUM_SONDIRS)+"个文件夹和"+
                            map.get(GetFilesUtils.FILE_INFO_NUM_SONFILES)+"个文件");
                }else{
                    gMap.put("fIsDir", false);
                    if(fileType.equals("img")||fileType.equals("png") || fileType.equals("jpg")){
                        String path = (map.get(GetFilesUtils.FILE_INFO_PATH).toString());
//                        Bitmap img = null;
//                        try {
//                            img = getLocalBitmap(path);
//                            Log.d(KEY, "file path : " +  path);
//                        } catch (Exception e) {
//                            Log.e(KEY, e.toString());
//                        }
//                        BitmapDrawable bd = new BitmapDrawable(img);
                        gMap.put(GetFilesUtils.FILE_INFO_TYPE, fileType);
                        gMap.put("fImgBitmap", path);
                    }else{
                        Log.e(KEY, "drawableId " + R.drawable.filetype_folder );
                        gMap.put("fImgDrawable", R.drawable.filetype_unknown);
                    }
                    gMap.put("fInfo","文件大小:"+GetFilesUtils.getInstance().getFileSize(map.get(GetFilesUtils.FILE_INFO_PATH).toString()));
                }
                gMap.put("fName", map.get(GetFilesUtils.FILE_INFO_NAME));
                gMap.put("fPath", map.get(GetFilesUtils.FILE_INFO_PATH));
                Log.d(KEY, "map : " + gMap.toString());
                aList.add(gMap);
            }
        }else{
            aList.clear();
        }
        sAdapter.notifyDataSetChanged();
        foldernowTv.setText(file);
    }

    public Bitmap getLocalBitmap(String path) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//如此，无法decode bitmap
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 40, 40);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;//如此，方可decode bitmap

        return BitmapFactory.decodeFile(path, options);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub
        try {
            if(aList.get(position).get("fIsDir").equals(true)){
                loadFolderList(aList.get(position).get("fPath").toString());
            }else{
                Toast.makeText(this, "这是文件，处理程序待添加", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if(v.getId()==R.id.folder_now){
            try {
                String folder=GetFilesUtils.getInstance().getParentPath(foldernowTv.getText().toString());
                if(folder==null){
                    Toast.makeText(this, "无父目录，待处理", Toast.LENGTH_SHORT).show();
                }else{
                    loadFolderList(folder);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class FileHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Map<String, Object> mMap;

        private ImageView img;
        private TextView name;
        private TextView info;

        public FileHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.listitem_folder, parent, false));
            Log.d("holder", "begin build");
            itemView.setOnClickListener(this);

            img = (ImageView) itemView.findViewById(R.id.folder_img);
            name = (TextView) itemView.findViewById(R.id.folder_name);
            info = (TextView) itemView.findViewById(R.id.folder_info);

        }

        public void bind(Map<String, Object> map) {
            Log.d(KEY, "bind begin, map : " + map.toString());

            mMap = new HashMap<String, Object>();

            if(map.get("fImgBitmap") != null) {
                mMap.put("fImgBitmap", map.get("fImgBitmap"));
            } else if(map.get("fImgDrawable") != null) {
                mMap.put("fImgDrawable", map.get("fImgDrawable"));
            }
            mMap.put("fName", map.get("fName"));
            mMap.put("fInfo", map.get("fInfo"));
            mMap.put("fIsDir", map.get("fIsDir"));
            mMap.put("fPath", map.get("fPath"));
            mMap.put(GetFilesUtils.FILE_INFO_TYPE, map.get(GetFilesUtils.FILE_INFO_TYPE));

            Log.d(KEY, "mMap : " + mMap.toString());


            try {
                if(mMap.get("fImgBitmap") != null) {
                    Log.d(KEY, "bitmapDrawable bind");
                    String path = mMap.get("fImgBitmap").toString();
                    Bitmap bitmap = getLocalBitmap(path);
                    img.setImageBitmap(bitmap);
                } else if(mMap.get("fImgDrawable") != null){
                    Log.d(KEY, "drawable bind + " + (int)map.get("fImgDrawable"));
                    Resources resources = getResources();
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(resources.openRawResource((int)map.get("fImgDrawable")));
                    img.setImageBitmap(bitmapDrawable.getBitmap());
//                    Drawable drawable = resources.getDrawable((int)map.get("fImgDrawable"));
//                    img.setImageDrawable(drawable);
//                    img.setImage
                }
            } catch (Exception e) {
                Log.e(KEY, e.toString());
            }

            name.setText(mMap.get("fName").toString());
            info.setText(mMap.get("fInfo").toString());

            Log.d(KEY, name.getText().toString() + ", " + info.getText().toString());

            Log.d(KEY, "bind finish");

        }

        @Override
        public void onClick(View view) {
            Log.d("holder", "begin click");

            String fileType = (String) mMap.get(GetFilesUtils.FILE_INFO_TYPE);

            try {
                if(mMap.get("fIsDir").equals(true)){
                    loadFolderList(mMap.get("fPath").toString());
                } else if (fileType.equals("jpg") || fileType.equals("png")) {
                    File file = new File(mMap.get(GetFilesUtils.FILE_INFO_PATH).toString());
                    choiceImgName = mMap.get(GetFilesUtils.FILE_INFO_NAME).toString();
                    choiceImgPath = mMap.get(GetFilesUtils.FILE_INFO_PATH).toString();
                    makeSure.setVisibility(View.VISIBLE);
                    isShow = true;
                } else {
                    Toast.makeText(FolderActivity.this, "这是文件，处理程序待添加", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }


    private class FileAdapter extends RecyclerView.Adapter<FileHolder> {

        private List<Map<String, Object>> list;

        public FileAdapter(List<Map<String, Object>> list) {
            this.list = list;
        }

        @Override
        public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(FolderActivity.this);
            Log.d("adapter", "begin");

            return new FileHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(FileHolder holder, int position) {
            Log.d("adapter", "begin bind");

            Map<String, Object> map = list.get(position);
            holder.bind(map);

            Log.d("adapter", "end bind");
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

}
