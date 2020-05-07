package com.baidu.automap.entity.response;


import com.baidu.automap.entity.Mp3Entity;

import java.util.LinkedList;
import java.util.List;

public class Mp3Response {

    private List<Mp3Entity> list;

    private String message;

    public Mp3Response() {
        list = new LinkedList<>();
    }

    public void addEntity(Mp3Entity entity) {
        list.add(entity);
    }

    public List<Mp3Entity> getList() {
        return list;
    }

    public void setList(List<Mp3Entity> list) {
        this.list = list;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

