package com.baidu.automap.entity.response;


import com.baidu.automap.entity.RouteNode;

import java.util.LinkedList;
import java.util.List;

public class RouteNodeResponse {

    private Integer routeId;

    private Integer nodeId;

    /**
     * 节点列表
     */
    private List<RouteNode> nodeList;

    private String message;

    public RouteNodeResponse() {
        this.nodeList = new LinkedList<>();
    }

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }

    public List<RouteNode> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<RouteNode> nodeList) {
        for(RouteNode node : nodeList) {
            RouteNode newNode = new RouteNode();
            newNode.setRouteNode(node);
            this.nodeList.add(newNode);
        }
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void addRouteNode(RouteNode node) {
        RouteNode newNode = new RouteNode();
        newNode.setRouteNode(node);
        this.nodeList.add(newNode);
    }

    @Override
    public String toString() {
        return "RouteNodeResponse{" +
                "routeId=" + routeId +
                ", nodeId='" + nodeId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
