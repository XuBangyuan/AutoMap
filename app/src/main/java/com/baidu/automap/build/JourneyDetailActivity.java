package com.baidu.automap.build;

import android.content.Intent;
import android.os.Bundle;
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
import com.baidu.automap.entity.Comment;
import com.baidu.automap.entity.Journey;
import com.baidu.automap.entity.response.CommentResponse;
import com.baidu.automap.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class JourneyDetailActivity extends AppCompatActivity {

    private TextView title;
    private TextView author;
    private TextView createTime;
    private TextView agree;
    private TextView content;
    private Button createComment;
    private RecyclerView commentList;

    private Journey curJourney;
    private CommentResponse response;

    private static final String KEY = "journeyDetailActivity";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journey_detail);

        title = (TextView) findViewById(R.id.journey_detail_title);
        author = (TextView) findViewById(R.id.journey_detail_author);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        createTime = (TextView) findViewById(R.id.journey_detail_create_time);
        agree = (TextView) findViewById(R.id.journey_detail_agree_num);
        createComment = (Button) findViewById(R.id.create_comment);
        commentList = (RecyclerView)findViewById(R.id.comment_list);
        commentList.setLayoutManager(new LinearLayoutManager(JourneyDetailActivity.this));

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        curJourney = bundle.getParcelable("journey");
        Log.d(KEY, curJourney.toString());
        response = new CommentResponse();

        //设置攻略内容
        title.setText(curJourney.getTitle());
        author.setText(curJourney.getUserId() + "");
        createTime.setText(format.format(curJourney.getCreateTime()));
        agree.setText(curJourney.getAgree() + "");

    }

    //更新commentList
    private void updateCommentList() {
        ThreadComment thread = new ThreadComment("findCommentByCondition");
        thread.start();

        try {
            thread.join();

            if(thread.getIsSuccess()) {
                Log.d(KEY, "更新列表成功，下一步展示");

            } else {
                Log.d(KEY, response.getMessage());
                Toast.makeText(JourneyDetailActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } catch (InterruptedException e) {
            Log.d(KEY, e.toString());
        }
    }

    private class ThreadComment extends Thread {
        private boolean isSuccess;

        public boolean getIsSuccess() {
            return isSuccess;
        }

        private String url;

        public ThreadComment(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {

                Journey journey = new Journey();
                journey.setUserId(curJourney.getUserId());
                journey.setDesId(curJourney.getDesId());

                Log.d(KEY, "begin http connect " + journey.toString());

                byte[] data = HttpUtil.readJourneyParse(url , journey);
                String str = new String(data);
                JSONObject jsonObject = new JSONObject(str);

                response.setMessage(jsonObject.getString("message"));
                if(response != null && response.getMessage().compareTo("success!") == 0) {
                    Log.d(KEY, url + " get data from server success!");
                    isSuccess = true;
                    JSONArray jsonArray =new JSONArray(jsonObject.getString("commentList"));

                    response.getCommentList().clear();

                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        Comment cNode = new Comment();
                        cNode.setJourneyId(object.getInt("journeyId"));
                        cNode.setUserId(object.getInt("userId"));
                        cNode.setAgree(object.getInt("agree"));
                        cNode.setCreateTime(new Date(object.getLong("createTime")));
                        cNode.setDetail(object.getString("detail"));
                        cNode.setId(object.getInt("id"));

                        response.addComment(cNode);
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

    private void refreshCommentList() {
        CommentAdapter commentAdapter = new CommentAdapter(response.getCommentList());
        commentList.setAdapter(commentAdapter);
    }

    private class CommentHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Comment comment;

        private TextView content;
        private TextView author;
        private TextView createTime;
        private TextView agree;

        public CommentHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.journey_item, parent, false));
            Log.d(KEY, "begin build");
            itemView.setOnClickListener(this);

            content = (TextView) itemView.findViewById(R.id.comment_detail);
            author = (TextView) itemView.findViewById(R.id.comment_author);
            createTime = (TextView) itemView.findViewById(R.id.comment_create_time);
            agree = (TextView) itemView.findViewById(R.id.comment_agree_num);

            Log.d(KEY, "end build");
        }

        public void bind(Comment comment) {
            Log.d(KEY, "begin bind " + comment.toString());

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            this.comment = comment;
            content.setText(comment.getDetail());
            author.setText(comment.getUserId() + "");
            createTime.setText(format.format(comment.getCreateTime()));
            agree.setText(comment.getAgree() + "");

            Log.d(KEY, "end bind");

        }

        @Override
        public void onClick(View view) {
            Log.d("holder", "begin click");

//            Intent intent = new Intent(JourneyActivity.this, JourneyDetailActivity.class);
//            Bundle bundle = new Bundle();
//            bundle.putParcelable("journey", journey);
//            intent.putExtras(bundle);

//            startActivityForResult(intent, JOURNEY_DETAIL);
        }

    }


    private class CommentAdapter extends RecyclerView.Adapter<CommentHolder> {

        private List<Comment> list;

        public CommentAdapter(List<Comment> list) {
            this.list = list;
        }

        @Override
        public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(JourneyDetailActivity.this);

            return new CommentHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(CommentHolder holder, int position) {
            Comment comment = list.get(position);
            holder.bind(comment);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

}
