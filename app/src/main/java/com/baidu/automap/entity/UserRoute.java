package com.baidu.automap.entity;

public class UserRoute {

    private Integer userId;

    private Integer routeId;

    @Override
    public String toString() {
        return "UserRoute{" +
                "userId=" + userId +
                ", routeId=" + routeId +
                '}';
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }
}
