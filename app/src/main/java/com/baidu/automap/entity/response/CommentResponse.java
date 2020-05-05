package com.baidu.automap.entity.response;


import com.baidu.automap.entity.Comment;

import java.util.LinkedList;
import java.util.List;

public class CommentResponse {

    private String message;

    private Integer userId;

    private Integer journeyId;

    private List<Comment> commentList;

    public CommentResponse() {
        commentList = new LinkedList<>();
    }

    public void addComment(Comment comment) {
        commentList.add(comment);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(Integer journeyId) {
        this.journeyId = journeyId;
    }

    public List<Comment> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<Comment> source) {
        for(Comment comment : source) {
            commentList.add(comment);
        }
    }
}

