package com.chat.client;

import javax.swing.*;
import java.awt.*;

public class BubblePanel extends JPanel {
    private boolean isSelf;
    private Color bgColor;

    public BubblePanel(String sender, String text, String time, boolean isSelf) {
        this.isSelf = isSelf;
        this.bgColor = isSelf ? new Color(238, 255, 222) : Color.WHITE;
        setOpaque(false);
        setLayout(new BorderLayout(8, 2));
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        if (!isSelf && sender != null) {
            JLabel lblSender = new JLabel(sender);
            lblSender.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 12));
            lblSender.setForeground(new Color(43, 130, 202));
            contentPanel.add(lblSender);
            contentPanel.add(Box.createVerticalStrut(2));
        }
        JLabel lblText = new JLabel(text);
        lblText.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
        lblText.setForeground(new Color(34, 34, 34));
        contentPanel.add(lblText);
        JLabel lblTime = new JLabel(time);
        lblTime.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 10));
        lblTime.setForeground(new Color(150, 160, 170));
        lblTime.setHorizontalAlignment(SwingConstants.RIGHT);
        setBorder(BorderFactory.createEmptyBorder(6, 10, 5, 10));
        add(contentPanel, BorderLayout.CENTER);
        add(lblTime, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        if (!isSelf) {
            g2.setColor(new Color(225, 230, 235));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        }
        g2.dispose();
        super.paintComponent(g2);
    }
}