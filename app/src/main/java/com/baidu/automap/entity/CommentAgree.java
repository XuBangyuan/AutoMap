package com.baidu.automap.entity;

public class CommentAgree {

    private Integer commentId;

    private Integer userId;

    public void setCommentAgree(CommentAgree source) {
        commentId = source.getCommentId();
        userId = source.getUserId();
    }

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "CommentAgree{" +
                "commentId=" + commentId +
                ", userId=" + userId +
                '}';
    }
}

