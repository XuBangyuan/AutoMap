package com.baidu.automap.entity;


import java.util.Date;

public class Comment {

    private Integer id;

    private Integer userId;

    private Integer journeyId;

    private Integer agree;

    private String detail;

    private Date createTime;

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

    public void setComment(Comment source) {
        id = source.getId();
        userId = source.getUserId();
        journeyId = source.getJourneyId();
        agree = source.getAgree();
        detail = source.getDetail();
        createTime = new Date(source.getCreateTime().getTime());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAgree() {
        return agree;
    }

    public void setAgree(Integer agree) {
        this.agree = agree;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", userId=" + userId +
                ", journeyId=" + journeyId +
                ", agree=" + agree +
                ", detail='" + detail + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
