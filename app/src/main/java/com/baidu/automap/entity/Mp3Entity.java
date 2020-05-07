package com.baidu.automap.entity;

import com.baidu.automap.entity.response.MediumTran;

import android.util.Base64;

public class Mp3Entity {

    private Integer id;

    private String desId;

    private String name;

    private byte[] file;

    public Mp3Entity() {

    }

    public void setMp3(Mp3Entity entity) {
        id = entity.getId();
        desId = entity.getDesId();
        name = entity.getName();
        file = entity.getFile();
    }

    public Mp3Entity(MediumTran entity) {
        id = entity.getId();
        desId = entity.getDesId();
        if(entity.getFile() != null && entity.getFile().length() != 0) {
            file = Base64.decode(entity.getFile(), Base64.DEFAULT);
        }
        name = entity.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesId() {
        return desId;
    }

    public void setDesId(String desId) {
        this.desId = desId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }
}
