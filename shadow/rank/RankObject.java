package com.yahya.shadow.rank;

public class RankObject {
    private String rank,user,user_pic_id,userID;
    private float points;

    public RankObject(String rank, String user, float points, String user_pic_id,String userID) {
        this.rank = rank;
        this.user = user;
        this.points = points;
        this.user_pic_id = user_pic_id;
        this.userID = userID;
    }

    public String getRank() {
        return rank;
    }

    public String getRankedUser() {
        return user;
    }

    public float getRankedPoints() {
        return points;
    }

    public String getRankedProfile() {
        return user_pic_id;
    }

    public String getUserId() {
        return userID;
    }
}
