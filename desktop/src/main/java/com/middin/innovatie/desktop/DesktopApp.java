package com.middin.innovatie.desktop;

import com.middin.innovatie.desktop.ui.ChatPanel;
import com.middin.innovatie.desktop.ui.HomePanel;
import com.middin.innovatie.desktop.ui.MemoryPanel;
import com.middin.innovatie.desktop.ui.MiddinTheme;
import com.middin.innovatie.desktop.ui.MorePanel;
import com.middin.innovatie.desktop.ui.ThemeManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

public final class DesktopApp {
    private static final String CARD_HOME = "home";
    private static final String CARD_MEMORY = "memory";
    private static final String CARD_CHAT = "chat";
    private static final String CARD_PRODUCTS = "products";
    private static final String CARD_MORE = "more";

    private final File dataDir;
    private final File stateFile;
    private final DesktopChatRepository chatRepository;
    private final MemoryRepository memoryRepository;
    private final DesktopPreferences prefs;
    private JFrame frame;
    private List<Catalog.Product> products;
    private CardLayout cardLayout;
    private JPanel contentCards;
    private JButton navHome;
    private JButton navMemory;
    private JButton navChat;
    private JButton navProducts;
    private JButton navMore;
    private MorePanel morePanel;
    private String currentCard = CARD_HOME;
    private boolean suppressLogoutOnClose;

    public DesktopApp() {
        String local = System.getenv("LOCALAPPDATA");
        dataDir = local != null ? new File(local, "MiddinInnovatie") : new File(System.getProperty("user.home"), ".middin-innovatie");
        dataDir.mkdirs();
        stateFile = new File(dataDir, "state.properties");
        prefs = new DesktopPreferences(stateFile);
        ThemeManager.apply(prefs.getTheme());
        products = new ArrayList<>(Catalog.all());
        products.sort(Comparator.comparing(p -> p.name().toLowerCase()));
        chatRepository = new DesktopChatRepository(new File(dataDir, "chat-messages.dat"), products);
        memoryRepository = new MemoryRepository(new File(dataDir, "memory.dat"));
    }

    public void show() {
        if (!isLoggedIn() && !showLogin()) {
            return;
        }
        openMain();
    }

    private boolean isLoggedIn() {
        prefs.reload();
        return prefs.isLoggedIn();
    }

    private void saveState() {
        try {
            prefs.save();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Kon instellingen niet opslaan: " + e.getMessage());
        }
    }

    private ImageIcon loadBrandIcon(int size) {
        URL url = getClass().getResource("/brand/middin_app_icon.jpg");
        if (url == null) return null;
        Image img = new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private boolean showLogin() {
        prefs.reload();
        ThemeManager.apply(prefs.getTheme());

        JDialog login = new JDialog((JFrame) null, "Middin Innovatie", true);
        login.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        login.setSize(480, 560);
        login.setLocationRelativeTo(null);
        login.getContentPane().setBackground(MiddinTheme.BACKGROUND);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(MiddinTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(24, 32, 24, 32));

        JPanel card = MiddinTheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(0.5f);

        ImageIcon logo = loadBrandIcon(88);
        if (logo != null) {
            JLabel logoLbl = new JLabel(logo);
            logoLbl.setAlignmentX(0.5f);
            card.add(logoLbl);
            card.add(Box.createVerticalStrut(20));
        }

        JLabel title = MiddinTheme.titleLabel("Inloggen");
        title.setAlignmentX(0.5f);
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        JLabel sub = MiddinTheme.mutedLabel("Middin Innovatie · Den Haag");
        sub.setAlignmentX(0.5f);
        card.add(sub);
        card.add(Box.createVerticalStrut(24));

        JTextField user = MiddinTheme.outlinedField(24);
        user.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JPasswordField pass = MiddinTheme.passwordField(24);
        pass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        card.add(MiddinTheme.mutedLabel("Gebruikersnaam"));
        card.add(Box.createVerticalStrut(6));
        card.add(user);
        card.add(Box.createVerticalStrut(16));
        card.add(MiddinTheme.mutedLabel("Wachtwoord"));
        card.add(Box.createVerticalStrut(6));
        card.add(pass);
        card.add(Box.createVerticalStrut(12));
        JLabel hint = MiddinTheme.mutedLabel("Desktop: vul willekeurige gegevens in om verder te gaan.");
        hint.setAlignmentX(0.5f);
        card.add(hint);
        card.add(Box.createVerticalStrut(20));

        JButton loginBtn = MiddinTheme.primaryButton("Doorgaan");
        loginBtn.setAlignmentX(0.5f);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        card.add(loginBtn);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        root.add(card, gbc);
        login.setContentPane(root);

        final boolean[] ok = {false};
        Runnable attemptLogin = () -> {
            if (user.getText().isBlank() || pass.getPassword().length == 0) {
                JOptionPane.showMessageDialog(login, "Vul gebruikersnaam en wachtwoord in.", "Inloggen", JOptionPane.WARNING_MESSAGE);
                return;
            }
            prefs.setLoggedIn(true);
            prefs.setUsername(user.getText().trim());
            try {
                prefs.save();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(login, "Kon sessie niet opslaan: " + ex.getMessage());
            }
            ok[0] = true;
            login.dispose();
        };
        loginBtn.addActionListener(e -> attemptLogin.run());
        login.getRootPane().setDefaultButton(loginBtn);
        user.addActionListener(e -> pass.requestFocusInWindow());
        pass.addActionListener(e -> attemptLogin.run());
        login.setVisible(true);
        return ok[0];
    }

    private void clearSession() {
        prefs.setLoggedIn(false);
        saveState();
    }

    private void logoutAndShowLogin() {
        clearSession();
        suppressLogoutOnClose = true;
        frame.dispose();
        suppressLogoutOnClose = false;
        show();
    }

    private void openMain() {
        frame = new JFrame("Middin Innovatie");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (suppressLogoutOnClose) {
                    return;
                }
                clearSession();
                frame.dispose();
                System.exit(0);
            }
        });
        frame.setMinimumSize(new Dimension(960, 640));
        frame.setSize(1100, 780);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(MiddinTheme.BACKGROUND);
        ImageIcon icon = loadBrandIcon(32);
        if (icon != null) frame.setIconImage(icon.getImage());

        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setBackground(MiddinTheme.BACKGROUND);
        contentCards.add(homePanel(), CARD_HOME);
        contentCards.add(memoryPanel(), CARD_MEMORY);
        contentCards.add(chatPanel(), CARD_CHAT);
        contentCards.add(productsPanel(), CARD_PRODUCTS);
        morePanel = new MorePanel(prefs, () -> {
            int c = JOptionPane.showConfirmDialog(frame, "Weet je zeker dat je wilt uitloggen?", "Uitloggen", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                logoutAndShowLogin();
            }
        }, frame, this::reloadForTheme, dataDir);
        contentCards.add(morePanel, CARD_MORE);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.add(buildTopBar(), BorderLayout.NORTH);
        frame.add(contentCards, BorderLayout.CENTER);
        frame.add(buildBottomNav(), BorderLayout.SOUTH);
        showCard(currentCard);
        frame.setVisible(true);
        frame.toFront();
        frame.requestFocus();
        morePanel.checkForUpdateOnStartup();
    }

    private void reloadForTheme() {
        ThemeManager.apply(prefs.getTheme());
        suppressLogoutOnClose = true;
        frame.dispose();
        suppressLogoutOnClose = false;
        openMain();
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(MiddinTheme.BACKGROUND);
        top.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, MiddinTheme.BORDER),
            new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);
        ImageIcon logo = loadBrandIcon(40);
        if (logo != null) brand.add(new JLabel(logo));
        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        JLabel t = MiddinTheme.titleLabel("Middin Innovatie");
        t.setFont(MiddinTheme.FONT_TITLE.deriveFont(18f));
        titles.add(t);
        titles.add(MiddinTheme.mutedLabel("Den Haag"));
        brand.add(titles);
        top.add(brand, BorderLayout.WEST);

        JButton logout = MiddinTheme.textButton("Uitloggen");
        logout.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(frame, "Weet je zeker dat je wilt uitloggen?", "Uitloggen", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                logoutAndShowLogin();
            }
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(logout);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JPanel buildBottomNav() {
        JPanel bar = new JPanel(new GridBagLayout());
        bar.setBackground(MiddinTheme.BACKGROUND);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, MiddinTheme.BORDER));

        navHome = MiddinTheme.navButton("Home", true);
        navMemory = MiddinTheme.navButton("Geheugen", false);
        navChat = MiddinTheme.navButton("Chat", false);
        navProducts = MiddinTheme.navButton("Producten", false);
        navMore = MiddinTheme.navButton("Meer", false);

        navHome.addActionListener(e -> showCard(CARD_HOME));
        navMemory.addActionListener(e -> showCard(CARD_MEMORY));
        navChat.addActionListener(e -> showCard(CARD_CHAT));
        navProducts.addActionListener(e -> showCard(CARD_PRODUCTS));
        navMore.addActionListener(e -> showCard(CARD_MORE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridy = 0;
        gbc.gridx = 0;
        bar.add(navHome, gbc);
        gbc.gridx = 1;
        bar.add(navMemory, gbc);
        gbc.gridx = 2;
        bar.add(navChat, gbc);
        gbc.gridx = 3;
        bar.add(navProducts, gbc);
        gbc.gridx = 4;
        bar.add(navMore, gbc);
        return bar;
    }

    private void showCard(String card) {
        currentCard = card;
        cardLayout.show(contentCards, card);
        styleNav(navHome, card.equals(CARD_HOME));
        styleNav(navMemory, card.equals(CARD_MEMORY));
        styleNav(navChat, card.equals(CARD_CHAT));
        styleNav(navProducts, card.equals(CARD_PRODUCTS));
        styleNav(navMore, card.equals(CARD_MORE));
        if (card.equals(CARD_MORE)) {
            morePanel.showMenu();
        }
    }

    private void styleNav(JButton b, boolean selected) {
        MiddinTheme.updateNavButton(b, selected);
    }

    private JPanel homePanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.add(new HomePanel(products), BorderLayout.CENTER);
        return MiddinTheme.contentPad(root);
    }

    private JPanel memoryPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.add(new MemoryPanel(memoryRepository, prefs.getUsername(), frame), BorderLayout.CENTER);
        return MiddinTheme.contentPad(root);
    }

    private JPanel productsPanel() {
        DefaultListModel<Catalog.Product> model = new DefaultListModel<>();
        products.forEach(model::addElement);

        JList<Catalog.Product> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(MiddinTheme.FONT_BODY);
        list.setBackground(MiddinTheme.BACKGROUND);
        list.setBorder(new EmptyBorder(4, 4, 4, 4));
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                JList<?> l, Object value, int index, boolean isSelected, boolean cellHasFocus
            ) {
                JLabel c = (JLabel) super.getListCellRendererComponent(l, value, index, isSelected, cellHasFocus);
                c.setText(((Catalog.Product) value).name());
                c.setBorder(new EmptyBorder(10, 12, 10, 12));
                c.setFont(MiddinTheme.FONT_BODY);
                if (isSelected) {
                    c.setBackground(MiddinTheme.PRIMARY_LIGHT);
                    c.setForeground(MiddinTheme.TEXT);
                } else {
                    c.setBackground(MiddinTheme.BACKGROUND);
                    c.setForeground(MiddinTheme.TEXT);
                }
                return c;
            }
        });

        JTextArea detail = MiddinTheme.bodyArea();
        detail.setEditable(false);
        JPanel detailCard = MiddinTheme.cardPanel();
        detailCard.setLayout(new BorderLayout());
        detailCard.add(MiddinTheme.scroll(detail), BorderLayout.CENTER);

        JTextField search = MiddinTheme.outlinedField(20);
        JButton searchBtn = MiddinTheme.primaryButton("Zoeken");
        searchBtn.setBorder(new EmptyBorder(10, 16, 10, 16));

        Runnable filter = () -> {
            String q = search.getText().trim().toLowerCase();
            model.clear();
            products.stream()
                .filter(p -> q.isEmpty() || p.name().toLowerCase().contains(q) || p.description().toLowerCase().contains(q))
                .forEach(model::addElement);
        };
        searchBtn.addActionListener(e -> filter.run());
        search.addActionListener(e -> filter.run());

        list.addListSelectionListener(e -> {
            Catalog.Product p = list.getSelectedValue();
            if (p != null) {
                detail.setText(p.name() + "\n\n" + p.description());
                detail.setCaretPosition(0);
            }
        });
        if (!model.isEmpty()) list.setSelectedIndex(0);

        JPanel listCard = MiddinTheme.cardPanel();
        listCard.setLayout(new BorderLayout());
        listCard.add(new JScrollPane(list), BorderLayout.CENTER);
        listCard.setPreferredSize(new Dimension(300, 0));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        searchBar.add(MiddinTheme.mutedLabel("Zoeken"));
        searchBar.add(search);
        searchBar.add(searchBtn);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setOpaque(false);
        root.add(searchBar, BorderLayout.NORTH);
        JPanel split = new JPanel(new BorderLayout(16, 0));
        split.setOpaque(false);
        split.add(listCard, BorderLayout.WEST);
        split.add(detailCard, BorderLayout.CENTER);
        root.add(split, BorderLayout.CENTER);
        return MiddinTheme.contentPad(root);
    }

    private JPanel chatPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.add(new ChatPanel(chatRepository, frame), BorderLayout.CENTER);
        return MiddinTheme.contentPad(root);
    }
}
