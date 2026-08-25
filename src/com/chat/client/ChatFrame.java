package com.chat.client;

import com.chat.common.Message;
import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.JScrollBar;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

public class ChatFrame extends JFrame{
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private String currentUsername;
    private DefaultListModel<String> userModel;
    private JList<String> userJList;
    private JTextField txtInput;
    private JButton btnSend;
    private JButton btnSendFile;
    private HTMLEditorKit htmlKit;
    private HTMLDocument htmlDoc;
    private JPanel chatBox;
    private JScrollPane chatScrollPane;
    private String lastSender = null;
    private static final Color PINK_PRIMARY=new Color(255,122,144);

    public ChatFrame(Socket socket,ObjectOutputStream oos,ObjectInputStream ois,String username){
        this.socket=socket;
        this.oos=oos;
        this.ois=ois;
        this.currentUsername=username;
        initUI();
        new Thread(new ClientReceiver()).start();
    }

    private void initUI(){
        setTitle("当前用户："+currentUsername);
        setSize(700,560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel contentPane=new JPanel(new BorderLayout(10,10));
        contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        contentPane.setBackground(new Color(245,246,250));
        setContentPane(contentPane);
        chatBox=new JPanel();
        chatBox.setLayout(new BoxLayout(chatBox,BoxLayout.Y_AXIS));
        chatBox.setBackground(new Color(241,244,248));
        JPanel topContainer=new JPanel(new BorderLayout());
        topContainer.setBackground(new Color(241,244,248));
        topContainer.add(chatBox,BorderLayout.NORTH);
        chatScrollPane=new JScrollPane(topContainer);
        chatScrollPane.setBorder(null);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentPane.add(chatScrollPane,BorderLayout.CENTER);
        userModel=new DefaultListModel<>();
        userJList=new JList<>(userModel);
        userJList.setCellRenderer(new OnlineUserCellRenderer());
        userJList.setFont(new Font("Microsoft YaHei UI",Font.PLAIN,13));
        userJList.setSelectionBackground(new Color(255,230,235));
        userJList.setSelectionForeground(PINK_PRIMARY);
        JPanel rightPanel=new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(180,0));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230,230,235),1,true),
                BorderFactory.createEmptyBorder(5,5,5,5)
        ));
        JLabel lblOnline=new JLabel("在线列表");
        lblOnline.setFont(new Font("Microsoft YaHei UI",Font.BOLD,13));
        lblOnline.setForeground(PINK_PRIMARY);
        lblOnline.setBorder(BorderFactory.createEmptyBorder(5,5,8,5));
        rightPanel.add(lblOnline,BorderLayout.NORTH);
        JScrollPane userScrollPane=new JScrollPane(userJList);
        userScrollPane.setBorder(null);
        rightPanel.add(userScrollPane,BorderLayout.CENTER);
        contentPane.add(rightPanel,BorderLayout.EAST);
        JPanel bottomPanel=new JPanel(new BorderLayout(8,0));
        bottomPanel.setOpaque(false);
        txtInput=new JTextField();
        txtInput.setFont(new Font("Microsoft YaHei UI",Font.PLAIN,14));
        txtInput.setPreferredSize(new Dimension(0,40));
        txtInput.putClientProperty("JComponent.roundRect",true);
        txtInput.putClientProperty("JTextField.placeholderText","发条消息吧...");
        JPanel btnGroup=new JPanel(new FlowLayout(FlowLayout.RIGHT,5,0));
        btnGroup.setOpaque(false);
        btnSendFile=new JButton("文件");
        btnSendFile.setPreferredSize(new Dimension(65,38));
        btnSendFile.putClientProperty("JButton.buttonType","roundRect");
        btnSend=new JButton("发送");
        btnSend.setPreferredSize(new Dimension(75,38));
        btnSend.setBackground(PINK_PRIMARY);
        btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font("Microsoft YaHei UI",Font.BOLD,13));
        btnSend.putClientProperty("JButton.buttonType","roundRect");
        btnGroup.add(btnSendFile);
        btnGroup.add(btnSend);
        bottomPanel.add(txtInput,BorderLayout.CENTER);
        bottomPanel.add(btnGroup,BorderLayout.EAST);
        contentPane.add(bottomPanel,BorderLayout.SOUTH);
        btnSend.addActionListener(e -> sendTextMessage());
        txtInput.addActionListener(e -> sendTextMessage());
        btnSendFile.addActionListener(e -> sendFileMessage());
    }

    private void sendTextMessage(){
        String content=txtInput.getText().trim();
        if(content.isEmpty()) return;
        try{
            Message msg=new Message(Message.TYPE_CHAT);
            msg.setSender(currentUsername);
            msg.setContent(content);
            oos.writeObject(msg);
            oos.flush();
            txtInput.setText("");
        }catch(IOException e){
            appendBubble(null,"消息发送失败！",false,true);
        }
    }

    private void sendFileMessage(){
        JFileChooser fileChooser=new JFileChooser();
        int result=fileChooser.showOpenDialog(this);
        if(result==JFileChooser.APPROVE_OPTION){
           File selectedFile=fileChooser.getSelectedFile();
           try(FileInputStream fis=new FileInputStream(selectedFile)){
               byte[] fileBytes=new byte[(int) selectedFile.length()];
               fis.read(fileBytes);
               Message msg=new Message(Message.TYPE_FILE);
               msg.setSender(currentUsername);
               msg.setFileName(selectedFile.getName());
               msg.setFileBytes(fileBytes);
               oos.writeObject(msg);
               oos.flush();
               appendBubble(null,"文件已上传 "+selectedFile.getName(),false,true);
           }catch(Exception e){
               JOptionPane.showMessageDialog(this,"上传失败！"+e.getMessage(),"错误",JOptionPane.ERROR_MESSAGE);
           }
        }
    }
    private class ClientReceiver implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Message msg = (Message) ois.readObject();
                    if (msg == null) break;
                    switch (msg.getType()) {
                        case Message.TYPE_CHAT:
                            boolean isSelf=currentUsername.equals(msg.getSender());
                            appendBubble(msg.getSender(),msg.getContent(),isSelf,false);
                            break;
                        case Message.TYPE_USER_LIST:
                            updateUserList(msg.getOnlineUsers());
                            break;
                        case Message.TYPE_FILE:
                            handleReceiveFile(msg);
                            break;
                        case Message.TYPE_SYSTEM:
                            appendBubble(null,msg.getContent(),false,true);
                            break;
                    }
                }
            } catch (Exception e) {
                appendBubble(null,"与服务器断开连接",false,true);
            }
        }
    }

    private void updateUserList(List<String> users){
        SwingUtilities.invokeLater(() -> {
            userModel.clear();
            if(users!=null && !users.isEmpty()){
                for(String user:users){
                    userModel.addElement(user);
                }
            }
            userJList.revalidate();
            userJList.repaint();
        });
    }

    private void handleReceiveFile(Message msg){
        if(currentUsername.equals(msg.getSender())) return;
        SwingUtilities.invokeLater(() -> {
            appendBubble(null,"收到来自 "+msg.getSender()+" 的文件："+msg.getFileName(),false,true);
            int choice=JOptionPane.showConfirmDialog(this,"收到来自 "+msg.getSender()+" 的文件:"+msg.getFileName()+" 是否下载？","文件接收提示",JOptionPane.YES_NO_OPTION);
            if(choice==JOptionPane.YES_OPTION){
                JFileChooser fileChooser=new JFileChooser();
                fileChooser.setSelectedFile(new File(msg.getFileName()));
                if(fileChooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){
                    File targetFile=fileChooser.getSelectedFile();
                    try(FileOutputStream fos=new FileOutputStream(targetFile)){
                        fos.write(msg.getFileBytes());
                        fos.flush();
                        JOptionPane.showMessageDialog(this,"文件保存成功！");
                    }catch(IOException e){
                        JOptionPane.showMessageDialog(this,"文件保存失败："+e.getMessage(),"错误",JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    private void initChatHtml(){
        try{
            htmlDoc.insertAfterStart(htmlDoc.getDefaultRootElement(),"<div id='chat-box'></div>");
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    private void appendBubble(String sender,String content,boolean isSelf,boolean isSystem){
        SwingUtilities.invokeLater(() -> {
            String time = new SimpleDateFormat("HH:mm").format(new Date());
            int topMargin = 8;
            if (!isSystem && sender != null && sender.equals(lastSender)) {
                topMargin = 2;
            }
            lastSender = isSystem ? null : sender;
            JPanel rowPanel = new JPanel(new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
            rowPanel.setOpaque(false);
            rowPanel.setBorder(BorderFactory.createEmptyBorder(topMargin, 0, 0, 0));

            if (isSystem) {
                JLabel lblSys = new JLabel("  " + content + "  ");
                lblSys.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
                lblSys.setForeground(new Color(110, 120, 130));
                lblSys.setOpaque(true);
                lblSys.setBackground(new Color(225, 230, 236));
                lblSys.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                rowPanel.add(lblSys);
            } else {
                BubblePanel bubble = new BubblePanel(sender, content, time, isSelf);
                rowPanel.add(bubble);
            }
            chatBox.add(rowPanel);
            chatBox.revalidate();
            chatBox.repaint();
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private String escapeHtml(String text){
        return text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\n","<br/>");
    }
}

class OnlineUserCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        String username = value.toString().replace("• ", "").trim();
        label.setText(username);
        label.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
        label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        label.setIcon(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(76, 228, 23));
                g2.fillOval(x, y + 2, 8, 8);
                g2.dispose();
            }
            @Override
            public int getIconWidth() {
                return 14;
            }
            @Override
            public int getIconHeight() {
                return 12;
            }
        });
        return label;
    }
}
