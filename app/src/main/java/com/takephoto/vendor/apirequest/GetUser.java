package com.takephoto.vendor.apirequest;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetUser {
    @SerializedName("success")
    private boolean success;

    @SerializedName("res")
    private List<LoginInfo> users;

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess(){
        return this.success;
    }

    public void setUsers(List<LoginInfo> users){
        this.users = users;
    }

    public List<LoginInfo> getUsers(){
        return this.users;
    }

    @NonNull
    @Override
    public String toString(){
        return "{success: " + this.success + ", res: " + this.users + " }";
    }
}

