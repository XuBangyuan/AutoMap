package com.baidu.automap.entity;


public class User {

    private Integer userId;

    private String phone;

    private String password;

    private Boolean isAdministrator;

    public Boolean getAdministrator() {
        return isAdministrator;
    }

    public void setAdministrator(Boolean administrator) {
        isAdministrator = administrator;
    }

    public void setUser(User user) {
        this.setUserId(user.getUserId());
        this.setPhone(user.getPhone());
        this.setPassword(user.getPassword());
        this.setAdministrator(user.isAdministrator);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", phone='" + phone + '\'' +
                ", password='" + password + '\'' +
                ", isAdministrator=" + isAdministrator +
                '}';
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

