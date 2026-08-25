package com.chat.server;

import com.chat.common.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private Socket socket;
    private String username;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public ClientHandler(Socket socket){
        this.socket=socket;
    }

    @Override
    public void run(){
        try{
            oos=new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ois=new ObjectInputStream(socket.getInputStream());
            while(true){
                Message msg=(Message) ois.readObject();
                if(msg==null) break;
                switch(msg.getType()){
                    case Message.TYPE_LOGIN:
                        handleLogin(msg);
                        break;
                    case Message.TYPE_CHAT:
                    case Message.TYPE_FILE:
                        ChatServer.broadcastMessage(msg);
                        break;
                    case Message.TYPE__LOGOUT:
                        return;
                }
            }
        }catch(Exception e){
            System.out.println("客户端"+username+"断开连接");
        }finally{
            closeClient();
        }
    }

    public void handleLogin(Message msg){
        this.username=msg.getSender();
        if (ChatServer.onlineClients.containsKey(username)) {
            System.out.println("登录拒绝：用户 " + username + " 已在线");
            Message failMsg = new Message(Message.TYPE_LOGIN_FAIL);
            failMsg.setContent("该用户已在聊天室中，请更换用户名！");
            sendMessage(failMsg);
            return;
        }
        ChatServer.onlineClients.put(username,this);
        System.out.println("用户 "+username+" 登录成功！当前在线人数："+ChatServer.onlineClients.size());
        Message ack=new Message(Message.TYPE_LOGIN_SUCCESS);
        sendMessage(ack);
        Message sysMsg=new Message(Message.TYPE_SYSTEM);
        sysMsg.setContent(username+" 加入了聊天室");
        ChatServer.broadcastMessage(sysMsg);
        ChatServer.broadcastUserList();
    }

    public void sendMessage(Message msg){
        try{
            oos.writeObject(msg);
            oos.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void closeClient(){
        if(username!=null){
            ChatServer.onlineClients.remove(username);
            System.out.println("用户 "+username+" 已下线。当前在线人数："+ChatServer.onlineClients.size());
            Message sysMsg=new Message(Message.TYPE_SYSTEM);
            sysMsg.setContent(username+" 离开了");
            ChatServer.broadcastMessage(sysMsg);
            ChatServer.broadcastUserList();
        }
        try{
            if(socket!=null && !socket.isClosed()) socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
