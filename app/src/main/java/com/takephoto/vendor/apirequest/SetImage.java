package com.takephoto.vendor.apirequest;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class SetImage {

    @SerializedName("image")
    private String image;

    @SerializedName("userID")
    private String userID;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @NonNull
    @Override
    public String toString(){
        return "{image: " + this.image + ", userID: " + this.userID + " }";
    }
}
