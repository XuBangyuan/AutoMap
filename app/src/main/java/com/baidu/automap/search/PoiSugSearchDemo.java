package com.baidu.automap.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.automap.R;
import com.baidu.automap.entity.ResultEntity;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import java.util.ArrayList;
import java.util.List;


/**
 * 介绍地点检索输入提示
 */
public class PoiSugSearchDemo extends AppCompatActivity implements OnGetSuggestionResultListener {

    private SuggestionSearch mSuggestionSearch = null;

    private RecyclerView mResultRecyclerView;
    private ResultAdapter mAdapter;

    // 搜索关键字输入窗口
    private EditText mEditCity = null;
    private AutoCompleteTextView mKeyWordsView = null;
    private List<ResultEntity> resultList;
    private Intent result;

    private static final String KEY = "poiSug";
    private static final int SINGLE = 1;
    private static final int LIST = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(KEY, "start poiSug");

        setContentView(R.layout.activity_poisugsearch);

        // 初始化建议搜索模块，注册建议搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        // 初始化view
        mEditCity = (EditText) findViewById(R.id.city);
        String city = bundle.getString("city");
        mEditCity.setText(city);

        mResultRecyclerView = (RecyclerView) findViewById(R.id.sug_recycler_list);

        mResultRecyclerView.setLayoutManager(new LinearLayoutManager(PoiSugSearchDemo.this));

        mKeyWordsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
        mKeyWordsView.setThreshold(1);

        // 当输入关键字变化时，动态更新建议列表
        mKeyWordsView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                if (cs.length() <= 0) {
                    return;
                }

                // 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                        .keyword(cs.toString()) // 关键字
                        .city(mEditCity.getText().toString())); // 城市
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * 获取在线建议搜索结果，得到requestSuggestion返回的搜索结果
     *
     * @param suggestionResult    Sug检索结果
     */
    @Override
    public void onGetSuggestionResult(SuggestionResult suggestionResult) {
        if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
            return;
        }

//        List<HashMap<String, String>> suggest = new ArrayList<>();
        List<ResultEntity> suggest = new ArrayList<>();
        for (SuggestionResult.SuggestionInfo info : suggestionResult.getAllSuggestions()) {
            if (info.getKey() != null && info.getDistrict() != null && info.getCity() != null) {

                ResultEntity resultEntity = new ResultEntity(info.getUid(), info.getKey(), info.getCity(),
                        info.getDistrict(), info.getPt().latitude, info.getPt().longitude);
                suggest.add(resultEntity);
            }
        }

        Log.d("onGetSuggestionResult", "result.size :" + suggest.size());


        mResultRecyclerView.setAdapter(new ResultAdapter(suggest));

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSuggestionSearch.destroy();
    }

    private class ResultHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private ResultEntity mResult;

        private TextView mSugKey;
        private TextView mSugCity;
        private TextView mSugDis;

        public ResultHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_layout, parent, false));
            Log.d("holder", "begin build");
            itemView.setOnClickListener(this);

            mSugKey = (TextView) itemView.findViewById(R.id.sug_key);
            mSugCity = (TextView) itemView.findViewById(R.id.sug_city);
            mSugDis = (TextView) itemView.findViewById(R.id.sug_dis);
            Log.d("holder", "end build");
        }

        public void bind(ResultEntity Result) {
            Log.d("holder", "begin bind");

            mResult = Result;
            mSugKey.setText(mResult.getKey());
            mSugCity.setText(mResult.getCity());
            mSugDis.setText(mResult.getDis());

            Log.d("holder", "end bind");

        }

        @Override
        public void onClick(View view) {
            Log.d("holder", "begin click");
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putSerializable("result", mResult);
            intent.putExtras(bundle);
            result = intent;
            setResult(RESULT_OK, intent);
            finish();
            PoiSugSearchDemo.this.finish();
        }

    }


    @Override
    public void onBackPressed() {
        Log.d(KEY, "back pressed!");
        setResult(RESULT_CANCELED);
        finish();
    }

    private class ResultAdapter extends RecyclerView.Adapter<ResultHolder> {

        private List<ResultEntity> mResults;

        public ResultAdapter(List<ResultEntity> Results) {
            mResults = Results;
        }

        @Override
        public ResultHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(PoiSugSearchDemo.this);
            Log.d("adapter", "begin");
            setResult(RESULT_OK);

            return new ResultHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(ResultHolder holder, int position) {
            Log.d("adapter", "begin bind");

            ResultEntity Result = mResults.get(position);
            holder.bind(Result);
            setResult(RESULT_OK);


            Log.d("adapter", "end bind");
        }

        @Override
        public int getItemCount() {
            return mResults.size();
        }
    }
}
