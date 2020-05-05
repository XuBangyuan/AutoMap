package com.baidu.automap.build;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.baidu.automap.entity.response.JourneyResponse;
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
    private Button agree;
    private TextView detail;
    private Button createComment;
    private EditText commentEdit;
    private LinearLayout makeSureLayout;
    private Button sureButton;
    private Button cancelButton;
    private RecyclerView commentList;

    private int curUserId;
    private Journey curJourney;
    private CommentResponse response;

    private static final String KEY = "journeyDetailActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journey_detail);

        title = (TextView) findViewById(R.id.journey_detail_title);
        author = (TextView) findViewById(R.id.journey_detail_author);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        createTime = (TextView) findViewById(R.id.journey_detail_create_time);
        agree = (Button) findViewById(R.id.journey_detail_agree_num);
        detail = (TextView) findViewById(R.id.journey_detail_content);
        createComment = (Button) findViewById(R.id.create_comment);
        commentEdit = (EditText) findViewById(R.id.content_edit);
        commentEdit.setVisibility(View.GONE);
        makeSureLayout = (LinearLayout) findViewById(R.id.make_sure_layout);
        sureButton = (Button) findViewById(R.id.send_comment);
        cancelButton = (Button) findViewById(R.id.cancel_comment);
        makeSureLayout.setVisibility(View.GONE);
        commentList = (RecyclerView)findViewById(R.id.comment_list);
        commentList.setLayoutManager(new LinearLayoutManager(JourneyDetailActivity.this));

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        curJourney = bundle.getParcelable("journey");
        curUserId = bundle.getInt("userId");
        curJourney.setCreateTime(new Date(bundle.getLong("createTime")));
        Log.d(KEY, "curUserId " + curUserId + ", " + curJourney.toString());
        response = new CommentResponse();

        //设置攻略内容
        title.setText(curJourney.getTitle());
        author.setText(curJourney.getUserId() + "");
        detail.setText(curJourney.getDetail());
        createTime.setText(format.format(curJourney.getCreateTime()));
        agree.setText("赞 ：" + curJourney.getAgree() + "");

        updateCommentList();

        //创建评论按钮点击
        createComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentEdit.setText("");
                commentEdit.setVisibility(View.VISIBLE);
                makeSureLayout.setVisibility(View.VISIBLE);
                createComment.setVisibility(View.GONE);
            }
        });

        //确认发布按钮点击
        sureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = commentEdit.getText().toString();
                if(content == null || content.length() == 0) {
                    Toast.makeText(JourneyDetailActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
                } else {

                    Comment comment = new Comment();
                    comment.setJourneyId(curJourney.getId());
                    comment.setDetail(commentEdit.getText().toString());
                    comment.setCreateTime(new Date());
                    comment.setUserId(curUserId);

                    ThreadCreateComment threadCreateComment = new ThreadCreateComment("insertComment", comment);
                    threadCreateComment.start();

                    try {
                        threadCreateComment.join();

                        if(threadCreateComment.getIsSuccess()) {
                            Log.d(KEY, "createComment success");
                            Toast.makeText(JourneyDetailActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                            updateCommentList();
                        } else {
                            Log.d(KEY, threadCreateComment.getResponse().getMessage());
                            Toast.makeText(JourneyDetailActivity.this, threadCreateComment.getResponse().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                    } catch (InterruptedException e) {
                        Log.d(KEY, e.toString());
                    }

                    commentEdit.setVisibility(View.GONE);
                    makeSureLayout.setVisibility(View.GONE);
                    createComment.setVisibility(View.VISIBLE);
                }

            }
        });

        //取消发布按钮点击
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentEdit.setVisibility(View.GONE);
                makeSureLayout.setVisibility(View.GONE);
                createComment.setVisibility(View.VISIBLE);
            }
        });

        //攻略点赞按钮点击
        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Journey journey = new Journey();
                journey.setId(curJourney.getId());
                journey.setUserId(curUserId);

                ThreadCreateJourney threadCreateJourney = new ThreadCreateJourney("addAgree", journey);
                threadCreateJourney.start();

                try {
                    threadCreateJourney.join();

                    if(threadCreateJourney.getIsSuccess()) {
                        Log.d(KEY, "addAgree success");
                        curJourney.setAgree(curJourney.getAgree() + 1);
                        agree.setText("赞 ：" + curJourney.getAgree() + "");

                        Toast.makeText(JourneyDetailActivity.this, "点赞成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(KEY, threadCreateJourney.getResponse().getMessage());
                        Toast.makeText(JourneyDetailActivity.this, threadCreateJourney.getResponse().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                } catch (InterruptedException e) {
                    Log.d(KEY, e.toString());
                }
            }
        });

    }

    //更新commentList
    private void updateCommentList() {
        ThreadUpdateCommentList thread = new ThreadUpdateCommentList("findCommentByCondition");
        thread.start();

        try {
            thread.join();

            if(thread.getIsSuccess()) {
                Log.d(KEY, "更新列表成功，下一步展示");
                refreshCommentList();
            } else {
                Log.d(KEY, response.getMessage());
                Toast.makeText(JourneyDetailActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } catch (InterruptedException e) {
            Log.d(KEY, e.toString());
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

        public ThreadCreateJourney(String url, Journey journey) {
            this.url = url;
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
                    Log.d(KEY, response.getMessage());
                }
                Log.d(KEY, str);
            } catch (Exception e) {
                Log.e(KEY, e.toString());
            }
        }
    }


    private class ThreadCreateComment extends Thread {
        private boolean isSuccess;
        private Comment comment;
        private CommentResponse mCommentResponse;

        public CommentResponse getResponse() {
            return mCommentResponse;
        }

        public boolean getIsSuccess() {
            return isSuccess;
        }

        private String url;

        public ThreadCreateComment(String url, Comment comment) {
            this.url = url;
            this.comment = comment;
            mCommentResponse = new CommentResponse();
        }

        @Override
        public void run() {
            try {
                Log.d(KEY, "begin http connect " + comment.toString());

                byte[] data = HttpUtil.readCommentParse(url , comment);
                String str = new String(data);
                JSONObject jsonObject = new JSONObject(str);

                mCommentResponse.setMessage(jsonObject.getString("message"));
                if(mCommentResponse != null && mCommentResponse.getMessage().compareTo("success!") == 0) {
                    Log.d(KEY, url + " get data from server success!");
                    isSuccess = true;
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

    private class ThreadUpdateCommentList extends Thread {
        private boolean isSuccess;

        public boolean getIsSuccess() {
            return isSuccess;
        }

        private String url;

        public ThreadUpdateCommentList(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {

                Comment comment = new Comment();
                comment.setJourneyId(curJourney.getId());

                Log.d(KEY, "begin http connect " + comment.toString());

                byte[] data = HttpUtil.readCommentParse(url , comment);
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
            super(inflater.inflate(R.layout.comment_item, parent, false));
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

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.comment = comment;
            content.setText(comment.getDetail());
            author.setText(comment.getUserId() + "");
            createTime.setText(format.format(comment.getCreateTime()));
            agree.setText("赞 ：" + comment.getAgree() + "");

            Log.d(KEY, "end bind");

        }

        @Override
        public void onClick(View view) {
            Log.d("holder", "begin click");
            Comment comment = new Comment();
            comment.setId(this.comment.getId());
            comment.setUserId(curUserId);

            ThreadCreateComment threadCreateComment = new ThreadCreateComment("addAgree", comment);
            threadCreateComment.start();

            try {
                threadCreateComment.join();

                if(threadCreateComment.getIsSuccess()) {
                    Log.d(KEY, "addAgree success");
                    this.comment.setAgree(this.comment.getAgree() + 1);
                    agree.setText("赞 ：" + this.comment.getAgree() + "");

                    Toast.makeText(JourneyDetailActivity.this, "点赞成功", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(KEY, threadCreateComment.getResponse().getMessage());
                    Toast.makeText(JourneyDetailActivity.this, threadCreateComment.getResponse().getMessage(), Toast.LENGTH_SHORT).show();

                }
            } catch (InterruptedException e) {
                Log.d(KEY, e.toString());
            }

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
