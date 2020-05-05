package com.baidu.automap.entity;

public class JourneyAgree {

    private Integer journeyId;

    private Integer userId;

    public Integer getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(Integer journeyId) {
        this.journeyId = journeyId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "JourneyAgree{" +
                "journeyId=" + journeyId +
                ", userId=" + userId +
                '}';
    }
}
