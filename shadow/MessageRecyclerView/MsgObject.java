package com.yahya.shadow.MessageRecyclerView;

public class MsgObject {
    private String sender,receiver,msg;

    public MsgObject(String sender, String receiver, String msg) {
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMsg() {
        return msg;
    }
}
