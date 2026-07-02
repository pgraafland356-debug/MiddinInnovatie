package com.middin.innovatie.desktop.ui;

import com.middin.innovatie.desktop.MemoryEntry;
import com.middin.innovatie.desktop.MemoryRepository;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public final class MemoryPanel extends JPanel {
    private final MemoryRepository repository;
    private final String authorName;
    private final java.awt.Window owner;
    private final JPanel listPanel;
    private final JTextArea draft;

    public MemoryPanel(MemoryRepository repository, String authorName, java.awt.Window owner) {
        this.repository = repository;
        this.authorName = authorName == null || authorName.isBlank() ? "Jij" : authorName;
        this.owner = owner;
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);

        JLabel title = MiddinTheme.titleLabel("Collectief geheugen");
        title.setFont(MiddinTheme.FONT_TITLE.deriveFont(18f));
        add(title, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createLineBorder(MiddinTheme.BORDER));
        scroll.getViewport().setBackground(MiddinTheme.BACKGROUND);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 0, 0, 0));
        draft = MiddinTheme.bodyArea();
        draft.setRows(3);
        draft.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MiddinTheme.BORDER_INPUT),
            new EmptyBorder(10, 12, 10, 12)
        ));
        bottom.add(MiddinTheme.mutedLabel("Nieuwe notitie"));
        bottom.add(Box.createVerticalStrut(6));
        bottom.add(draft);
        bottom.add(Box.createVerticalStrut(8));
        JButton add = MiddinTheme.primaryButton("Toevoegen");
        add.setAlignmentX(0f);
        add.setMaximumSize(new Dimension(160, 44));
        add.addActionListener(e -> addNote());
        bottom.add(add);
        add(bottom, BorderLayout.SOUTH);
        refresh();
    }

    private void addNote() {
        try {
            repository.add(draft.getText(), authorName);
            draft.setText("");
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage(), "Collectief geheugen", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refresh() {
        try {
            List<MemoryEntry> items = repository.listAll();
            listPanel.removeAll();
            if (items.isEmpty()) {
                listPanel.add(MiddinTheme.mutedLabel("Nog geen notities. Voeg de eerste toe."));
            } else {
                for (MemoryEntry e : items) listPanel.add(cardFor(e));
            }
            listPanel.revalidate();
            listPanel.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage(), "Collectief geheugen", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel cardFor(MemoryEntry e) {
        JPanel card = MiddinTheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(0f);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        JTextArea body = MiddinTheme.bodyArea();
        body.setText(e.content());
        body.setEditable(false);
        body.setBackground(MiddinTheme.PRIMARY_LIGHT);
        card.add(body);
        card.add(Box.createVerticalStrut(6));
        card.add(MiddinTheme.mutedLabel(e.authorName() + " · " + e.formattedTime()));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton edit = MiddinTheme.textButton("Bewerken");
        JButton del = MiddinTheme.textButton("Verwijderen");
        edit.addActionListener(ev -> editNote(e));
        del.addActionListener(ev -> deleteNote(e));
        actions.add(edit);
        actions.add(del);
        card.add(actions);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.CENTER);
        wrap.setBorder(new EmptyBorder(0, 0, 10, 0));
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));
        return wrap;
    }

    private void editNote(MemoryEntry e) {
        JTextArea area = MiddinTheme.bodyArea();
        area.setText(e.content());
        area.setRows(5);
        int c = JOptionPane.showConfirmDialog(owner, area, "Notitie bewerken", JOptionPane.OK_CANCEL_OPTION);
        if (c != JOptionPane.OK_OPTION) return;
        try {
            repository.update(e.id(), area.getText());
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage());
        }
    }

    private void deleteNote(MemoryEntry e) {
        int c = JOptionPane.showConfirmDialog(owner, "Deze notitie verwijderen?", "Verwijderen", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            repository.delete(e.id());
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage());
        }
    }
}
