package com.example.smartgym1;

import androidx.annotation.NonNull;

public class User {
    private String name, email, phone, uid;

    public User (){}
    public User (String name, String email, String phone, String uid) {
        this.name=name;
        this.email=email;
        this.phone=phone;
        this.uid=uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name=name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email=email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone=phone;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid=uid;
    }

    public void copyUser(User user){
        this.name = user.getName();
        this.email = user.getEmail();
        this.uid = user.getUid();
        this.phone = user.getPhone();
    }

    @NonNull
    @Override
    public String toString() {
        return ("Name: "+this.name + "\n"+
                "Email: "+this.email + "\n"+
                "Phone: "+this.phone);
    }
}