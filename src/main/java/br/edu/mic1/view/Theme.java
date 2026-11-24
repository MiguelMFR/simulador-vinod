package br.edu.mic1.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class Theme {
    // Paleta Dark Moderna
    public static final Color BG_MAIN       = new Color(24, 26, 31);
    public static final Color BG_PANEL      = new Color(33, 37, 43);
    public static final Color BG_ELEVATED   = new Color(40, 44, 52);
    public static final Color BG_ROW_ALT    = new Color(37, 41, 49);
    public static final Color BG_HIGHLIGHT  = new Color(46, 67, 110); // Destaque de alteração

    public static final Color TEXT_PRIMARY  = new Color(230, 234, 240);
    public static final Color TEXT_MUTED    = new Color(148, 156, 171);
    public static final Color ACCENT_BLUE   = new Color(82, 139, 255);
    public static final Color ACCENT_GREEN  = new Color(73, 182, 117);
    public static final Color ACCENT_RED    = new Color(224, 93, 93);
    public static final Color ACCENT_AMBER  = new Color(229, 192, 123);
    public static final Color BORDER_COLOR  = new Color(56, 61, 71);

    // Tipografia
    public static final Font FONT_UI        = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_UI_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_MONO      = new Font("Consolas", Font.PLAIN, 13);
    public static final Font FONT_MONO_BOLD = new Font("Consolas", Font.BOLD, 13);

    private Theme() {}

    public static void applyGlobalLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Panel.background", BG_PANEL);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("Label.font", FONT_UI);
        UIManager.put("ScrollPane.background", BG_PANEL);
        UIManager.put("Viewport.background", BG_PANEL);
        UIManager.put("SplitPane.background", BG_MAIN);
        UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
        UIManager.put("TextField.background", BG_ELEVATED);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", TEXT_PRIMARY);
        UIManager.put("Spinner.background", BG_ELEVATED);
        UIManager.put("Spinner.foreground", TEXT_PRIMARY);
    }

    public static Border createSectionBorder(String title) {
        TitledBorder titled = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                "  " + title + "  "
        );
        titled.setTitleFont(FONT_TITLE);
        titled.setTitleColor(ACCENT_BLUE);
        return BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(4, 4, 4, 4),
                BorderFactory.createCompoundBorder(titled, BorderFactory.createEmptyBorder(8, 10, 10, 10))
        );
    }

    public static JButton createButton(String text, Color accent) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_UI_BOLD);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BG_ELEVATED);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 1, true),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(accent.darker());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(BG_ELEVATED);
            }
        });
        return btn;
    }

    public static void styleTable(JTable table) {
        table.setBackground(BG_PANEL);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(26);
        table.setFont(FONT_MONO);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setSelectionBackground(BG_HIGHLIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_ELEVATED);
        header.setForeground(TEXT_MUTED);
        header.setFont(FONT_UI_BOLD);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(DefaultTableCellRenderer.LEFT);
    }

    public static String formatBits(int[] bits) {
        StringBuilder sb = new StringBuilder(bits.length + (bits.length / 4));
        for (int i = 0; i < bits.length; i++) {
            if (i > 0 && i % 4 == 0) {
                sb.append(' '); // Agrupa de 4 em 4 bits para facilitar leitura
            }
            sb.append(bits[i]);
        }
        return sb.toString();
    }

    public static Component wrapCellPadding(DefaultTableCellRenderer renderer, Component c) {
        if (c instanceof javax.swing.JLabel lbl) {
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        }
        return c;
    }
}