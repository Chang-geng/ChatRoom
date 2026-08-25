package com.chat.common;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable{
    private static final long serialVersionUID=1L;

    public static final String TYPE_LOGIN="LOGIN";
    public static final String TYPE_LOGIN_SUCCESS="LOGIN_SUCCESS";
    public static final String TYPE__LOGOUT="LOGOUT";
    public static final String TYPE_CHAT="CHAT";
    public static final String TYPE_USER_LIST="USER_LIST";
    public static final String TYPE_FILE="FILE";
    public static final String TYPE_SYSTEM = "SYSTEM";
    public static final String TYPE_LOGIN_FAIL = "LOGIN_FAIL";

    private String type;
    private String sender;
    private String content;
    private List<String> onlineUsers;
    private byte[] fileBytes;
    private String fileName;

    public Message(){}

    public Message(String type){
        this.type=type;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type=type;
    }

    public String getSender(){
        return sender;
    }

    public void setSender(String sender){
        this.sender=sender;
    }

    public String getContent(){
        return content;
    }

    public void setContent(String content){
        this.content=content;
    }

    public List<String> getOnlineUsers(){
        return onlineUsers;
    }

    public void setOnlineUsers(List<String> onlineUsers){
        this.onlineUsers=onlineUsers;
    }

    public byte[] getFileBytes(){
        return fileBytes;
    }

    public void setFileBytes(byte[] fileBytes){
        this.fileBytes=fileBytes;
    }

    public String getFileName(){
        return fileName;
    }

    public void setFileName(String fileName){
        this.fileName=fileName;
    }
}
