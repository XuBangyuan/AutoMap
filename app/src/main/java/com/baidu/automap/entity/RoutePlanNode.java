package com.baidu.automap.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.mapapi.model.LatLng;

public class RoutePlanNode implements Parcelable {
    /**
     * 坐标信息
     */
    LatLng mLatLng;

    /**
     * 名称
     */
    String name;

    public RoutePlanNode(LatLng latLng, String name) {
        mLatLng = new LatLng(latLng.latitude, latLng.longitude);
        this.name = name;
    }

    public RoutePlanNode() {
        super();
    }

    protected RoutePlanNode(Parcel in) {
        mLatLng = in.readParcelable(LatLng.class.getClassLoader());
        name = in.readString();
    }

    public static final Creator<RoutePlanNode> CREATOR = new Creator<RoutePlanNode>() {
        @Override
        public RoutePlanNode createFromParcel(Parcel in) {
            return new RoutePlanNode(in);
        }

        @Override
        public RoutePlanNode[] newArray(int size) {
            return new RoutePlanNode[size];
        }
    };

    public LatLng getLatLng() {
        return mLatLng;
    }

    public void setLatLng(LatLng latLng) {
        mLatLng = latLng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mLatLng, flags);
        dest.writeString(name);
    }
}
