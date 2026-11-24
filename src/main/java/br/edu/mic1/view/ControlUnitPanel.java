package br.edu.mic1.view;

import br.edu.mic1.model.Microinstruction;
import br.edu.mic1.model.UnidadeControle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class ControlUnitPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JLabel mpcExecValue = createValueLabel();
    private final JLabel mpcNextValue = createValueLabel();
    private final JLabel mirValue = createValueLabel();
    private final JLabel aluValue = createValueLabel();
    private final JLabel condAddrValue = createValueLabel();
    private final JLabel busValue = createValueLabel();
    private final JLabel instructionCacheValue = createValueLabel();
    private final JLabel dataCacheValue = createValueLabel();

    public ControlUnitPanel() {
        super(new GridLayout(4, 2, 8, 8));
        setBackground(Theme.BG_PANEL);
        setBorder(Theme.createSectionBorder("Unidade de Controle"));

        add(createInfoCard("MPC Executado", mpcExecValue));
        add(createInfoCard("Próximo MPC", mpcNextValue));
        add(createInfoCard("Operação ALU", aluValue));
        add(createInfoCard("Condição / Próximo End.", condAddrValue));
        add(createInfoCard("MIR (Microinstrução)", mirValue));
        add(createInfoCard("Barramento C", busValue));
        add(createInfoCard("Cache de Instruções", instructionCacheValue));
        add(createInfoCard("Cache de Dados", dataCacheValue));
    }

    private JPanel createInfoCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(2, 4));
        card.setBackground(Theme.BG_ELEVATED);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(Theme.FONT_UI_BOLD.deriveFont(10.5f));
        titleLabel.setForeground(Theme.TEXT_MUTED);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private static JLabel createValueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(Theme.FONT_MONO_BOLD);
        label.setForeground(Theme.ACCENT_AMBER);
        return label;
    }

    public void refresh(UnidadeControle control) {
        Microinstruction instruction = control.getMicroinstruction();
        mpcExecValue.setText(String.valueOf(control.getMicroprogramCounterExecutado()));
        mpcNextValue.setText(String.valueOf(control.getMpc()));
        mirValue.setText(Theme.formatBits(control.getMir()));
        aluValue.setText(String.valueOf(instruction.getAlu()));
        condAddrValue.setText(instruction.getCondition() + "  →  " + instruction.getAddress());
        busValue.setText(Theme.formatBits(control.getBarramentoC()));
        instructionCacheValue.setText("Hits: " + control.getInstructionCacheHits()
                + " | Misses: " + control.getInstructionCacheMisses());
        dataCacheValue.setText("Hits: " + control.getDataCacheHits()
                + " | Misses: " + control.getDataCacheMisses());
    }
}