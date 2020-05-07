package com.baidu.automap.entity.response;

import android.util.Base64;

import com.baidu.automap.entity.Mp3Entity;

public class MediumTran {

    private String file;

    private Integer id;

    private String desId;

    private String name;

    public MediumTran() {

    }

    public MediumTran(Mp3Entity entity) {
        file = Base64.encodeToString(entity.getFile(), Base64.DEFAULT);
        id = entity.getId();
        desId = entity.getDesId();
        name = entity.getName();
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDesId() {
        return desId;
    }

    public void setDesId(String desId) {
        this.desId = desId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

