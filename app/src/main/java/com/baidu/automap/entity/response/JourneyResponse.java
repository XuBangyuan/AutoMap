package com.baidu.automap.entity.response;

import com.baidu.automap.entity.Journey;

import java.util.LinkedList;
import java.util.List;

public class JourneyResponse {

    private String desId;

    private String message;

    private List<Journey> journeyList;

    public String getdesId() {
        return desId;
    }

    public void setdesId(String desId) {
        this.desId = desId;
    }

    public JourneyResponse() {
        journeyList = new LinkedList<>();
    }

    public void addJourney(Journey journey) {
        journeyList.add(journey);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Journey> getJourneyList() {
        return journeyList;
    }

    public void setJourneyList(List<Journey> source) {
        journeyList.addAll(source);
    }

    @Override
    public String toString() {
        return "JourneyResponse{" +
                "desId='" + desId + '\'' +
                ", message='" + message + '\'' +
                ", journeyList=" + journeyList.size() +
                '}';
    }
}
