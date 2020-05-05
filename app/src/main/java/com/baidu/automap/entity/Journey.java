package com.baidu.automap.entity;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Journey implements Parcelable {

    private Integer id;

    private Integer userId;

    private String desId;

    private String title;

    private Date createTime;

    private Integer agree;

    private String detail;

    protected Journey(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        if (in.readByte() == 0) {
            userId = null;
        } else {
            userId = in.readInt();
        }
        desId = in.readString();
        title = in.readString();
        if (in.readByte() == 0) {
            agree = null;
        } else {
            agree = in.readInt();
        }
        detail = in.readString();
    }

    public Journey() {

    }

    public static final Creator<Journey> CREATOR = new Creator<Journey>() {
        @Override
        public Journey createFromParcel(Parcel in) {
            return new Journey(in);
        }

        @Override
        public Journey[] newArray(int size) {
            return new Journey[size];
        }
    };

    public void setJourney(Journey source) {
        id = source.getId();
        userId = source.getUserId();
        desId = source.getDesId();
        title = source.getTitle();
        createTime = new Date(source.getCreateTime().getTime());
        agree = source.getAgree();
        detail = source.getDetail();
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getDesId() {
        return desId;
    }

    public void setDesId(String desId) {
        this.desId = desId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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

    @Override
    public String toString() {
        return "Journey{" +
                "id=" + id +
                ", userId=" + userId +
                ", desId=" + desId +
                ", title='" + title + '\'' +
                ", createTime=" + createTime +
                ", agree=" + agree +
                ", detail='" + detail + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        if (userId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(userId);
        }
        dest.writeString(desId);
        dest.writeString(title);
        if (agree == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(agree);
        }
        dest.writeString(detail);
    }
}

