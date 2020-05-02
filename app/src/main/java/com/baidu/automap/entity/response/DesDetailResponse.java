package com.baidu.automap.entity.response;

import com.baidu.automap.entity.DesDetailIntroduction;

public class DesDetailResponse {

    private String message;

    private DesDetailIntroduction introduction;

    public DesDetailResponse() {
        this.introduction = new DesDetailIntroduction();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DesDetailIntroduction getIntroduction() {
        return introduction;
    }

    public void setIntroduction(DesDetailIntroduction introduction) {
        this.introduction.setDesDetailIntroduction(introduction);
    }

    @Override
    public String toString() {
        return "DesDetailResponse{" +
                "message='" + message + '\'' +
                ", introduction=" + introduction.toString() +
                '}';
    }
}