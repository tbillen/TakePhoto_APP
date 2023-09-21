package com.takephoto.vendor.apirequest;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class LoginInfo {


    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("create_at")
    private String createdAt;

    public String getId() {
        return id;
    }

    @NonNull
    @Override
    public String toString() {
        return "{id:" + this.id + ",name:" + this.email + ",create_at:" + this.createdAt + "}";
    }
}
