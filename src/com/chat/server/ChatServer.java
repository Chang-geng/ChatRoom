package com.chat.server;

import com.chat.common.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private static final int PORT=8888;

    public static Map<String,ClientHandler> onlineClients=new ConcurrentHashMap<>();

    public static void main(String[] args){
        try(ServerSocket serverSocket=new ServerSocket(PORT)){
            System.out.println("服务端已启动，端口："+PORT);
            while(true){
                Socket socket=serverSocket.accept();
                System.out.println("有客户端连接："+socket.getInetAddress());
                ClientHandler handler=new ClientHandler(socket);
                new Thread(handler).start();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void broadcastMessage(Message msg){
        for(ClientHandler handler:onlineClients.values()){
            handler.sendMessage(msg);
        }
    }

    public static void broadcastUserList(){
        Message listMsg=new Message(Message.TYPE_USER_LIST);
        List<String> userList=new ArrayList<>(onlineClients.keySet());
        listMsg.setOnlineUsers(userList);
        broadcastMessage(listMsg);
    }
}
