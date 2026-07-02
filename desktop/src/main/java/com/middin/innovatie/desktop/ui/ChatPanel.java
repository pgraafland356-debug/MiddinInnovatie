package com.middin.innovatie.desktop.ui;

import com.middin.innovatie.desktop.ChatMessage;
import com.middin.innovatie.desktop.DesktopChatRepository;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.net.URL;
import java.util.List;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/** Chat UI aligned with Android ChatScreen + LocalProductChatRepository. */
public final class ChatPanel extends JPanel {
    private final DesktopChatRepository repository;
    private final java.awt.Window owner;
    private final JPanel messageList;
    private final JLabel errorLabel;
    private final JTextArea input;
    private final JButton send;
    private final JButton clearHistory;
    private final JButton refresh;
    private final ImageIcon assistantIcon;

    public ChatPanel(DesktopChatRepository repository, java.awt.Window owner) {
        this.repository = repository;
        this.owner = owner;
        assistantIcon = loadBrandIcon(40);
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = MiddinTheme.titleLabel("Productassistent");
        title.setFont(MiddinTheme.FONT_TITLE.deriveFont(18f));
        header.add(title, BorderLayout.WEST);
        JPanel headerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerBtns.setOpaque(false);
        clearHistory = MiddinTheme.textButton("Geschiedenis wissen");
        refresh = MiddinTheme.textButton("Vernieuwen");
        headerBtns.add(clearHistory);
        headerBtns.add(refresh);
        header.add(headerBtns, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        messageList = new JPanel();
        messageList.setLayout(new BoxLayout(messageList, BoxLayout.Y_AXIS));
        messageList.setOpaque(false);
        JScrollPane scroll = new JScrollPane(messageList);
        scroll.setBorder(BorderFactory.createLineBorder(MiddinTheme.BORDER));
        scroll.getViewport().setBackground(MiddinTheme.BACKGROUND);
        add(scroll, BorderLayout.CENTER);

        errorLabel = MiddinTheme.mutedLabel("");
        errorLabel.setForeground(new java.awt.Color(0xB0, 0x00, 0x20));

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 0, 0, 0));
        bottom.add(errorLabel);
        input = MiddinTheme.bodyArea();
        input.setRows(2);
        input.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MiddinTheme.BORDER_INPUT),
            new EmptyBorder(10, 12, 10, 12)
        ));
        JPanel inputRow = new JPanel(new BorderLayout(8, 8));
        inputRow.setOpaque(false);
        inputRow.add(MiddinTheme.mutedLabel("Bericht"), BorderLayout.NORTH);
        inputRow.add(input, BorderLayout.CENTER);
        send = MiddinTheme.primaryButton("→");
        send.setPreferredSize(new Dimension(52, 48));
        send.setToolTipText("Versturen");
        JPanel sendWrap = new JPanel(new BorderLayout());
        sendWrap.setOpaque(false);
        sendWrap.add(send, BorderLayout.SOUTH);
        inputRow.add(sendWrap, BorderLayout.EAST);
        bottom.add(inputRow);
        add(bottom, BorderLayout.SOUTH);

        Runnable doRefresh = this::refreshMessages;
        Runnable doSend = this::sendMessage;
        send.addActionListener(e -> doSend.run());
        input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send");
        input.getActionMap().put("send", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doSend.run();
            }
        });
        refresh.addActionListener(e -> doRefresh.run());
        clearHistory.addActionListener(e -> {
            int c = JOptionPane.showOptionDialog(
                owner,
                "Alle berichten worden van dit apparaat verwijderd. Na vernieuwen verschijnt het welkomstbericht opnieuw.",
                "Alle chatberichten wissen?",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[] {"Wissen", "Annuleren"},
                "Annuleren"
            );
            if (c != 0) return;
            try {
                repository.clearHistory();
                doRefresh.run();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
        doRefresh.run();
    }

    private void refreshMessages() {
        errorLabel.setText("");
        try {
            List<ChatMessage> messages = repository.listMessages();
            messageList.removeAll();
            for (ChatMessage msg : messages) {
                messageList.add(buildMessageRow(msg));
                messageList.add(Box.createVerticalStrut(8));
            }
            clearHistory.setEnabled(!messages.isEmpty());
            messageList.revalidate();
            messageList.repaint();
            SwingUtilities.invokeLater(() -> {
                javax.swing.JScrollPane sp = (javax.swing.JScrollPane) javax.swing.SwingUtilities
                    .getAncestorOfClass(javax.swing.JScrollPane.class, messageList);
                if (sp != null) {
                    javax.swing.JScrollBar bar = sp.getVerticalScrollBar();
                    bar.setValue(bar.getMaximum());
                }
            });
        } catch (Exception ex) {
            showError(ex.getMessage() == null ? "Kon berichten niet laden." : ex.getMessage());
        }
    }

    private void sendMessage() {
        String text = input.getText().trim();
        if (text.isEmpty()) {
            showError("Leeg bericht.");
            return;
        }
        errorLabel.setText("");
        send.setEnabled(false);
        input.setEnabled(false);
        try {
            repository.sendMessage(text);
            input.setText("");
            refreshMessages();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError(ex.getMessage() == null ? "Versturen is mislukt." : ex.getMessage());
        } finally {
            send.setEnabled(true);
            input.setEnabled(true);
            input.requestFocusInWindow();
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg == null ? "" : msg);
    }

    private JPanel buildMessageRow(ChatMessage msg) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        JPanel bubble = buildBubble(msg);
        if (msg.isAssistant()) {
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);
            if (assistantIcon != null) left.add(new JLabel(assistantIcon));
            left.add(bubble);
            row.add(left, BorderLayout.WEST);
        } else {
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            right.setOpaque(false);
            right.add(bubble);
            row.add(right, BorderLayout.EAST);
        }
        return row;
    }

    private JPanel buildBubble(ChatMessage msg) {
        boolean assistant = msg.isAssistant();
        java.awt.Color bg = assistant ? MiddinTheme.SURFACE_VARIANT : MiddinTheme.PRIMARY_LIGHT;
        java.awt.Color fg = MiddinTheme.TEXT;
        java.awt.Color meta = MiddinTheme.TEXT_LIGHT;

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MiddinTheme.BORDER),
            new EmptyBorder(12, 12, 12, 12)
        ));
        card.setMaximumSize(new Dimension(320, Integer.MAX_VALUE));

        JTextArea body = new JTextArea(msg.text());
        body.setFont(MiddinTheme.FONT_BODY);
        body.setForeground(fg);
        body.setBackground(bg);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setEditable(false);
        body.setBorder(null);
        card.add(body);

        card.add(Box.createVerticalStrut(4));
        JLabel metaLbl = MiddinTheme.mutedLabel(msg.authorName() + " · " + msg.formattedTime());
        metaLbl.setForeground(meta);
        card.add(metaLbl);
        return card;
    }

    private static ImageIcon loadBrandIcon(int size) {
        URL url = ChatPanel.class.getResource("/brand/middin_app_icon.jpg");
        if (url == null) return null;
        Image img = new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}
