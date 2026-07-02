package com.middin.innovatie.desktop.ui;

import com.middin.innovatie.desktop.Catalog;
import com.middin.innovatie.desktop.InnovationNewsClient;
import com.middin.innovatie.desktop.NewsItem;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.net.URI;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public final class HomePanel extends JPanel {
    private final List<Catalog.Product> products;
    private final JPanel newsList;
    private final JLabel newsStatus;

    public HomePanel(List<Catalog.Product> products) {
        this.products = products;
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        content.add(MiddinTheme.titleLabel("Welkom"));
        content.add(Box.createVerticalStrut(8));
        content.add(MiddinTheme.bodyLabel("Middin Innovatie · Den Haag"));
        content.add(Box.createVerticalStrut(16));

        content.add(section("Innovatienieuws"));
        JPanel newsHeader = new JPanel(new BorderLayout());
        newsHeader.setOpaque(false);
        newsStatus = MiddinTheme.mutedLabel("Laden…");
        JButton refreshNews = MiddinTheme.textButton("Vernieuwen");
        refreshNews.addActionListener(e -> loadNews());
        newsHeader.add(newsStatus, BorderLayout.WEST);
        newsHeader.add(refreshNews, BorderLayout.EAST);
        content.add(newsHeader);
        content.add(Box.createVerticalStrut(8));
        newsList = new JPanel();
        newsList.setLayout(new BoxLayout(newsList, BoxLayout.Y_AXIS));
        newsList.setOpaque(false);
        content.add(newsList);
        content.add(Box.createVerticalStrut(16));

        content.add(section("Top producten"));
        int limit = Math.min(3, products.size());
        for (int i = 0; i < limit; i++) {
            Catalog.Product p = products.get(i);
            JPanel card = MiddinTheme.cardPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setAlignmentX(0f);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
            JLabel name = MiddinTheme.bodyLabel(p.name());
            name.setFont(MiddinTheme.FONT_BODY.deriveFont(java.awt.Font.BOLD));
            card.add(name);
            String desc = p.description().split("\\.")[0];
            if (desc.length() > 100) desc = desc.substring(0, 97) + "…";
            card.add(MiddinTheme.mutedLabel(desc + "."));
            content.add(card);
            content.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createLineBorder(MiddinTheme.BORDER));
        scroll.getViewport().setBackground(MiddinTheme.BACKGROUND);
        add(scroll, BorderLayout.CENTER);
        loadNews();
    }

    private JLabel section(String text) {
        JLabel l = MiddinTheme.bodyLabel(text);
        l.setFont(MiddinTheme.FONT_BODY.deriveFont(java.awt.Font.BOLD));
        l.setAlignmentX(0f);
        return l;
    }

    private void loadNews() {
        newsStatus.setText("Laden…");
        new SwingWorker<List<NewsItem>, Void>() {
            @Override
            protected List<NewsItem> doInBackground() {
                return InnovationNewsClient.load();
            }

            @Override
            protected void done() {
                try {
                    renderNews(get());
                    newsStatus.setText("");
                } catch (Exception ex) {
                    newsStatus.setText("Nieuws laden mislukt — offline overzicht.");
                    renderNews(InnovationNewsClient.load());
                }
            }
        }.execute();
    }

    private void renderNews(List<NewsItem> items) {
        newsList.removeAll();
        for (NewsItem item : items) {
            JPanel card = MiddinTheme.cardPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setAlignmentX(0f);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            JTextArea title = MiddinTheme.bodyArea();
            title.setText(item.title());
            title.setEditable(false);
            title.setBackground(MiddinTheme.PRIMARY_LIGHT);
            card.add(title);
            card.add(MiddinTheme.mutedLabel(item.source()));
            if (item.url() != null && !item.url().isBlank() && Desktop.isDesktopSupported()) {
                JButton open = MiddinTheme.textButton("Open link");
                open.setAlignmentX(0f);
                open.addActionListener(e -> {
                    try {
                        Desktop.getDesktop().browse(URI.create(item.url()));
                    } catch (Exception ignored) {
                    }
                });
                card.add(open);
            }
            newsList.add(card);
            newsList.add(Box.createVerticalStrut(8));
        }
        newsList.revalidate();
        newsList.repaint();
    }
}
