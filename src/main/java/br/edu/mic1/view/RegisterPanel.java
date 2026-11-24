package br.edu.mic1.view;

import br.edu.mic1.model.UnidadeControle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegisterPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final String[] DATAPATH_NAMES = {
            "MAR", "MBR", "PC", "IR", "SP", "AC"
    };

    private final Map<String, String> previousValues = new HashMap<>();
    private final Set<Integer> changedRows = new HashSet<>();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Registrador", "Decimal", "Binário (16-bit)"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public RegisterPanel() {
        super(new BorderLayout(4, 4));
        setBackground(Theme.BG_PANEL);
        setBorder(Theme.createSectionBorder("Registradores"));

        JTable table = new JTable(tableModel);
        Theme.styleTable(table);
        table.setAutoCreateRowSorter(true);

        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(210);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                int modelRow = tbl.convertRowIndexToModel(row);

                if (!isSelected) {
                    if (changedRows.contains(modelRow)) {
                        c.setBackground(Theme.BG_HIGHLIGHT);
                    } else {
                        c.setBackground(row % 2 == 0 ? Theme.BG_PANEL : Theme.BG_ROW_ALT);
                    }
                }

                if (col == 0) {
                    setFont(Theme.FONT_UI_BOLD);
                    setForeground(Theme.ACCENT_BLUE);
                } else if (col == 1) {
                    setFont(Theme.FONT_MONO_BOLD);
                    setForeground(Theme.ACCENT_AMBER);
                } else {
                    setFont(Theme.FONT_MONO);
                    setForeground(Theme.TEXT_PRIMARY);
                }

                return Theme.wrapCellPadding(this, c);
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(javax.swing.BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh(UnidadeControle control) {
        tableModel.setRowCount(0);
        changedRows.clear();

        List<String> names = new ArrayList<>();
        List<int[]> values = new ArrayList<>();
        for (int i = 0; i < DATAPATH_NAMES.length; i++) {
            names.add(DATAPATH_NAMES[i]);
            values.add(control.getRegistrador(i));
        }

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            int[] bits = values.get(i);
            String formattedBits = Theme.formatBits(bits);

            String oldVal = previousValues.get(name);
            if (oldVal != null && !oldVal.equals(formattedBits)) {
                changedRows.add(i);
            }
            previousValues.put(name, formattedBits);

            tableModel.addRow(new Object[]{name, bitsToDecimal(bits), formattedBits});
        }
    }

    private static String bitsToDecimal(int[] bits) {
        long value = 0;
        for (int bit : bits) {
            value = (value << 1) | bit;
        }
        if (bits.length == 16 && (value & 0x8000L) != 0) {
            value -= 0x10000L;
        }
        return Long.toString(value);
    }
}