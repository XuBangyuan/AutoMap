package com.baidu.automap.search;

import java.io.Serializable;

public class ResultEntity implements Serializable {

    private static final long serialVersionUID = 7247714666080613254L;

    private String uId;
    private String key;
    private String city;
    private String dis;
    //    private LatLng latLng;
    private double latitude;
    private double longitude;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public ResultEntity(String uId, String key, String city, String dis, double latitude, double longitude) {
        this.uId = uId;
        this.key = key;
        this.city = city;
        this.dis = dis;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDis() {
        return dis;
    }

    public void setDis(String dis) {
        this.dis = dis;
    }

    public ResultEntity(String key, String city, String dis) {
        this.key = key;
        this.city = city;
        this.dis = dis;
    }


}
