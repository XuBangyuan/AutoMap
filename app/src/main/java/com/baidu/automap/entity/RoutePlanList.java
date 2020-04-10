package com.baidu.automap.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class RoutePlanList implements Parcelable {
    String id;
    List<RoutePlanNode> list;

    static Random random;

    static {
        random = new Random(System.currentTimeMillis());
    }

    public RoutePlanList() {
        id = Double.valueOf(random.nextDouble()).toString();
        this.list = new LinkedList<>();
    }


    protected RoutePlanList(Parcel in) {
        id = in.readString();
        list = in.createTypedArrayList(RoutePlanNode.CREATOR);
    }

    public static final Creator<RoutePlanList> CREATOR = new Creator<RoutePlanList>() {
        @Override
        public RoutePlanList createFromParcel(Parcel in) {
            return new RoutePlanList(in);
        }

        @Override
        public RoutePlanList[] newArray(int size) {
            return new RoutePlanList[size];
        }
    };

    public void addNode(RoutePlanNode node) {
        list.add(node);
    }

    public void setList(List<RoutePlanNode> list) {
        this.list.clear();
        for(RoutePlanNode node : list) {
            RoutePlanNode newNode = new RoutePlanNode(node.mLatLng, node.name);
            this.addNode(newNode);
        }
    }


    public List<RoutePlanNode> getList() {
        return list;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeTypedList(list);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void add(RoutePlanNode node) {
        list.add(node);
    }
}
