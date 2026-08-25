package com.chat.client;

import com.chat.common.Message;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LoginFrame extends JFrame{
    private JTextField txtUsername;
    private JButton btnLogin;
    private JTextField txtIp;
    private JTextField txtPort;

    public LoginFrame(){
        setTitle("聊天客户端登录");
        setSize(340,340);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        JPanel mainPanel=new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20,30,20,30));
        mainPanel.setBackground(new Color(250,250,252));
        JLabel lblTitle=new JLabel("在线聊天室");
        lblTitle.setFont(new Font("Microsoft YaHei UI",Font.BOLD,22));
        lblTitle.setForeground(new Color(255,122,144));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblSub=new JLabel("欢迎加入聊天室");
        lblSub.setFont(new Font("Microsoft YaHei UI",Font.PLAIN,12));
        lblSub.setForeground(Color.GRAY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtIp=new JTextField("127.0.0.1");
        txtIp.setMaximumSize(new Dimension(280,36));
        txtIp.setFont(new Font("Miscrosoft YaHei UI",Font.PLAIN,13));
        txtIp.putClientProperty("JComponent.roundRect",true);
        txtIp.putClientProperty("JTextField.placeholderText","服务器IP");
        txtPort=new JTextField("8888");
        txtPort.setMaximumSize(new Dimension(280,36));
        txtPort.setFont(new Font("Miscrosoft YaHei UI",Font.PLAIN,13));
        txtPort.putClientProperty("JComponent.roundRect",true);
        txtPort.putClientProperty("JTextField.placeholderText","端口号");
        txtUsername=new JTextField();
        txtUsername.setMaximumSize(new Dimension(280,38));
        txtUsername.setFont(new Font("Microsoft YaHei UI",Font.PLAIN,13));
        txtUsername.putClientProperty("JComponent.roundRect",true);
        txtUsername.putClientProperty("JTextField.placeholderText","请输入用户名");
        btnLogin=new JButton("加入聊天");
        btnLogin.setMaximumSize(new Dimension(280,40));
        btnLogin.setFont(new Font("Microsoft YaHei UI",Font.BOLD,14));
        btnLogin.setBackground(new Color(255,122,144));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.putClientProperty("JButton.buttonType","roundRect");
        mainPanel.add(lblTitle);
        mainPanel.add(lblSub);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(txtIp);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(txtPort);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(txtUsername);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(btnLogin);
        add(mainPanel);
        btnLogin.addActionListener(e -> doLogin());
        txtUsername.addActionListener(e -> doLogin());
    }

    public void doLogin(){
        String ip=txtIp.getText().trim();
        String postStr=txtPort.getText().trim();
        String username=txtUsername.getText().trim();
        if(username.isEmpty() || ip.isEmpty() || postStr.isEmpty()){
            JOptionPane.showMessageDialog(this,"用户名或服务器IP或端口不正确！","提示",JOptionPane.WARNING_MESSAGE);
            return;
        }
        try{
            int port=Integer.parseInt(postStr);
            Socket socket=new Socket(ip,port);
            ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
            Message loginMsg=new Message(Message.TYPE_LOGIN);
            loginMsg.setSender(username);
            oos.writeObject(loginMsg);
            oos.flush();
            Message response=(Message) ois.readObject();
            if(Message.TYPE_LOGIN_SUCCESS.equals(response.getType())){
                this.dispose();
                new ChatFrame(socket,oos,ois,username).setVisible(true);
            }else if(Message.TYPE_LOGIN_FAIL.equals(response.getType())){
                JOptionPane.showMessageDialog(this,response.getContent(),"登录失败",JOptionPane.WARNING_MESSAGE);
                socket.close();
            }
        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"无法连接到服务器，请检查服务端是否启动！");
        }
    }

    public static void main(String[] args){
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
