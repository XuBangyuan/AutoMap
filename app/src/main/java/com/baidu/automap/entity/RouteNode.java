package com.baidu.automap.entity;

public class RouteNode {

    private Integer nodeId;

    private Integer routeId;

    private String desId;

    private String desName;

    private Double latitude;

    private Double longitude;

    public void setRouteNode(RouteNode node) {
        this.nodeId = node.getNodeId();
        this.routeId = node.getRouteId();
        this.desId = node.getDesId();
        this.desName = node.getDesName();
        this.latitude = node.getLatitude();
        this.longitude = node.getlongitude();
    }

    public Integer getRouteId() {
        return routeId;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }

    public String getDesId() {
        return desId;
    }

    public void setDesId(String desId) {
        this.desId = desId;
    }


    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getlongitude() {
        return longitude;
    }

    public void setlongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDesName() {
        return desName;
    }

    public void setDesName(String desName) {
        this.desName = desName;
    }

    @Override
    public String toString() {
        return "RouteNode{" +
                "nodeId=" + nodeId +
                ", routeId=" + routeId +
                ", desId='" + desId + '\'' +
                ", desName='" + desName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
