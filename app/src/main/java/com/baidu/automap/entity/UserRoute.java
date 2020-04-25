package com.baidu.automap.entity;

import java.util.List;

public class UserRoute {

    private Integer userId;

    private Integer routeId;

    /**
     * 路线描述
     */
    private String description;

    @Override
    public String toString() {
        return "UserRoute{" +
                "userId=" + userId +
                ", routeId=" + routeId +
                ", description='" + description + '\'' +
                '}';
    }

    public void setUserRoute(UserRoute route) {
        this.userId = route.getUserId();
        this.routeId = route.getRouteId();
        this.description = route.description;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getRouteId() {
        return routeId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void updateDescription(List<RouteNode> list){
        if(list != null && list.size() > 0) {
            this.description = "";
            for(RouteNode node : list) {
                this.description += node.getDesName() + "——";
            }

            description = description.substring(0, description.length() - 2);
        }
    }
}
