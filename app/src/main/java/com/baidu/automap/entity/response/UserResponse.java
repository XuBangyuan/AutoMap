package com.baidu.automap.entity.response;


import com.baidu.automap.entity.User;

public class UserResponse {

    private String message;

    private User user;

    public UserResponse() {
        this.user = new User();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user.setUser(user);
    }

    @Override
    public String toString() {
        return "UserHttp{" +
                "message='" + message + '\'' +
                ", user=" + user +
                '}';
    }
}
