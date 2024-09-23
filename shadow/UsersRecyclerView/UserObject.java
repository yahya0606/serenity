package com.yahya.shadow.UsersRecyclerView;

public class UserObject {
    private String userName,userKey,userImage;

    public UserObject(String userName, String userKey, String userImage) {
        this.userName = userName;
        this.userKey = userKey;
        this.userImage = userImage;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserKey() {
        return userKey;
    }

    public String getUserImage() {
        return userImage;
    }
}
