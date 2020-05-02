package com.baidu.automap.entity;

public class DesDetailIntroduction {

    private Integer desId;

    private String uId;

    private String introduction;

    public void setDesDetailIntroduction(DesDetailIntroduction introduction) {
        this.desId = introduction.getDesId();
        this.uId = introduction.getuId();
        this.introduction = introduction.getIntroduction();
    }

    @Override
    public String toString() {
        return "DesDetailIntroduction{" +
                "desId=" + desId +
                ", uId='" + uId + '\'' +
                ", introduction='" + introduction + '\'' +
                '}';
    }

    public Integer getDesId() {
        return desId;
    }

    public void setDesId(Integer desId) {
        this.desId = desId;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
}
