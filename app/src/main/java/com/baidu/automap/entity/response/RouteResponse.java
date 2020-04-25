package com.baidu.automap.entity.response;


import com.baidu.automap.entity.UserRoute;

import java.util.LinkedList;
import java.util.List;

public class RouteResponse {

    /**
     * 路线列表
     */
    private List<UserRoute> routeList;

    private String message;

    private Integer userId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public RouteResponse() {
        routeList = new LinkedList<>();
    }

    public List<UserRoute> getRouteList() {
        return routeList;
    }

    public void setRouteList(List<UserRoute> routeList) {
        for(UserRoute route : routeList) {
            UserRoute newRoute = new UserRoute();
            newRoute.setUserRoute(route);
            this.routeList.add(newRoute);
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "RouteResponse{" +
                ", message='" + message + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    public void addRoute(UserRoute userRoute) {
        UserRoute newRoute = new UserRoute();
        newRoute.setUserRoute(userRoute);
        routeList.add(newRoute);
    }
}
