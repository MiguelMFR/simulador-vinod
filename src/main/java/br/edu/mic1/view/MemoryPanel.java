package br.edu.mic1.view;

import br.edu.mic1.model.MemoriaPrincipal;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

public class MemoryPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final MemoriaPrincipal memory;
    private final JSpinner addressSpinner;
    private final JLabel selectedWord = new JLabel();
    private final JTable table;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Endereço", "Hex", "Decimal", "Bits"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public MemoryPanel(MemoriaPrincipal memory) {
        super(new BorderLayout(6, 6));
        this.memory = memory;
        setBackground(Theme.BG_PANEL);
        setBorder(Theme.createSectionBorder("Memória Principal"));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        controls.setBackground(Theme.BG_ELEVATED);
        controls.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true));

        addressSpinner = new JSpinner(new SpinnerNumberModel(0, 0, memory.size() - 1, 1));
        addressSpinner.setFont(Theme.FONT_MONO);
        addressSpinner.addChangeListener(e -> refreshSelectedWord());

        JButton inspectButton = Theme.createButton("Inspecionar", Theme.ACCENT_BLUE);
        inspectButton.addActionListener(event -> refreshSelectedWord());

        selectedWord.setFont(Theme.FONT_MONO_BOLD);
        selectedWord.setForeground(Theme.ACCENT_GREEN);

        controls.add(new JLabel("Endereço:"));
        controls.add(addressSpinner);
        controls.add(inspectButton);
        controls.add(selectedWord);

        table = new JTable(tableModel);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(240);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                int inspectedAddr = (Integer) addressSpinner.getValue();

                if (!isSelected) {
                    if (row == inspectedAddr) {
                        c.setBackground(Theme.BG_HIGHLIGHT);
                    } else {
                        c.setBackground(row % 2 == 0 ? Theme.BG_PANEL : Theme.BG_ROW_ALT);
                    }
                }

                if (col == 0) {
                    setForeground(Theme.TEXT_MUTED);
                } else if (col == 1) {
                    setForeground(Theme.ACCENT_BLUE);
                } else if (col == 2) {
                    setForeground(Theme.ACCENT_AMBER);
                } else {
                    setForeground(Theme.TEXT_PRIMARY);
                }

                return Theme.wrapCellPadding(this, c);
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));

        add(controls, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        int visibleWords = Math.min(memory.size(), 256);
        for (int address = 0; address < visibleWords; address++) {
            int[] word = memory.read(address);
            int dec = bitsToInt(word);
            int decimal = (dec & 0x8000) != 0 ? dec - 0x10000 : dec;
            String hex = String.format("0x%04X", dec & 0xFFFF);
            tableModel.addRow(new Object[]{
                    String.format("[%02d]", address),
                    hex,
                    decimal,
                    Theme.formatBits(word)
            });
        }
        refreshSelectedWord();
    }

    private void refreshSelectedWord() {
        int address = (Integer) addressSpinner.getValue();
        int[] word = memory.read(address);
        int dec = bitsToInt(word);
        if ((dec & 0x8000) != 0) {
            dec -= 0x10000;
        }
        selectedWord.setText(String.format("Palavra [%d]:  %s  (Dec: %d | 0x%04X)",
                address, Theme.formatBits(word), dec, dec & 0xFFFF));
        table.repaint();
    }

    private static int bitsToInt(int[] bits) {
        int value = 0;
        for (int bit : bits) {
            value = (value << 1) | bit;
        }
        return value;
    }
}