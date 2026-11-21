
package br.edu.mic1.view;

import br.edu.mic1.controller.SimulatorController;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class SimulatorView extends JFrame {
    private static final long serialVersionUID = 1L;

    private final SimulatorController controller;
    private final RegisterPanel registerPanel;
    private final ControlUnitPanel controlUnitPanel;
    private final MemoryPanel memoryPanel;

    private final JLabel statusBadge = new JLabel("● PRONTO");
    private final JLabel mpcStatus = new JLabel();

    private final Timer timer;

    private JSplitPane mainSplit;

    private JButton stepBtn;
    private JButton runBtn;
    private JButton stopBtn;

    private JTextArea assemblyArea;
    private JTextArea assemblyStatus;
    private DefaultTableModel assemblyTableModel;

    public SimulatorView(SimulatorController controller) {
        super("Simulador MIC-1");

        Theme.applyGlobalLookAndFeel();

        this.controller = controller;
        this.registerPanel = new RegisterPanel();
        this.controlUnitPanel = new ControlUnitPanel();
        this.memoryPanel = new MemoryPanel(
                controller.getModel().getMemoria()
        );

        this.timer = new Timer(250, event -> controller.step());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));

        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout(8, 8));

        add(createToolbar(), BorderLayout.NORTH);

        mainSplit = createMainPanel();
        add(mainSplit, BorderLayout.CENTER);

        add(createStatusBar(), BorderLayout.SOUTH);

        controller.addStateListener(this::refresh);

        refresh();

        pack();
        setLocationRelativeTo(null);

        SwingUtilities.invokeLater(() -> {
            mainSplit.setDividerLocation(0.70);
        });
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 8)
        );

        toolbar.setBackground(Theme.BG_PANEL);
        toolbar.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, Theme.BORDER_COLOR
        ));

        stepBtn = Theme.createButton("⏭ Passo", Theme.ACCENT_BLUE);
        runBtn = Theme.createButton("▶ Executar", Theme.ACCENT_GREEN);
        stopBtn = Theme.createButton("⏸ Parar", Theme.ACCENT_RED);

        JButton resetBtn = Theme.createButton(
                "↺ Reset", Theme.TEXT_MUTED
        );

        JButton assembleBtn = Theme.createButton(
                "Montar Programa", Theme.ACCENT_AMBER
        );

        stopBtn.setEnabled(false);

        stepBtn.addActionListener(event -> controller.step());

        runBtn.addActionListener(event -> {
            timer.start();
            updateExecutionControls(true);
        });

        stopBtn.addActionListener(event -> {
            timer.stop();
            updateExecutionControls(false);
        });

        resetBtn.addActionListener(event -> {
            timer.stop();
            updateExecutionControls(false);
            controller.reset();
        });

        assembleBtn.addActionListener(event -> mountProgram());

        JLabel speedLabel = new JLabel("Clock (250 ms):");
        speedLabel.setForeground(Theme.TEXT_MUTED);

        JSlider speedSlider = new JSlider(50, 1000, 250);
        speedSlider.setBackground(Theme.BG_PANEL);
        speedSlider.setPreferredSize(new Dimension(140, 24));

        speedSlider.addChangeListener(e -> {
            int delay = speedSlider.getValue();
            timer.setDelay(delay);
            speedLabel.setText("Clock (" + delay + " ms):");
        });

        toolbar.add(stepBtn);
        toolbar.add(runBtn);
        toolbar.add(stopBtn);
        toolbar.add(resetBtn);
        toolbar.add(assembleBtn);
        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(speedLabel);
        toolbar.add(speedSlider);

        return toolbar;
    }

    private void updateExecutionControls(boolean running) {
        runBtn.setEnabled(!running);
        stepBtn.setEnabled(!running);
        stopBtn.setEnabled(running);
    }

    private JSplitPane createMainPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Theme.BG_MAIN);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);

        // Unidade de Controle: 70%
        gbc.gridy = 0;
        gbc.weighty = 0.1;
        right.add(controlUnitPanel, gbc);

        // Assembly: 30%
        gbc.gridy = 1;
        gbc.weighty = 0.9;
        right.add(createAssemblyPanel(), gbc);

        JSplitPane horizontal = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                registerPanel,
                right
        );

        horizontal.setResizeWeight(0.56);
        horizontal.setDividerSize(6);
        horizontal.setBorder(null);

        JSplitPane vertical = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                horizontal,
                memoryPanel
        );

        vertical.setResizeWeight(0.70);
        vertical.setDividerSize(8);
        vertical.setContinuousLayout(true);
        vertical.setBorder(
                BorderFactory.createEmptyBorder(4, 8, 0, 8)
        );

        memoryPanel.setMinimumSize(
                new Dimension(300, 180)
        );

        horizontal.setMinimumSize(
                new Dimension(500, 350)
        );

        return vertical;
    }

    private JPanel createAssemblyPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));

        panel.setBackground(Theme.BG_PANEL);
        panel.setBorder(
                Theme.createSectionBorder("Montagem Assembly")
        );

        assemblyArea = new JTextArea(
                "LOCO 10\n"
                + "STOD 20\n"
                + "LOCO 5\n"
                + "ADDD 20"
        );

        assemblyArea.setFont(Theme.FONT_MONO);
        assemblyArea.setForeground(Theme.TEXT_PRIMARY);
        assemblyArea.setBackground(Theme.BG_ELEVATED);
        assemblyArea.setLineWrap(false);

        assemblyStatus = new JTextArea("Aguardando montagem.");
        assemblyStatus.setEditable(false);
        assemblyStatus.setLineWrap(true);
        assemblyStatus.setWrapStyleWord(true);
        assemblyStatus.setFont(Theme.FONT_UI);
        assemblyStatus.setForeground(Theme.ACCENT_AMBER);
        assemblyStatus.setBackground(Theme.BG_PANEL);

        assemblyTableModel = new DefaultTableModel(
                new Object[]{
                        "Endereco",
                        "Macroinstrucao",
                        "Codigo binario"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable assemblyTable = new JTable(assemblyTableModel);
        Theme.styleTable(assemblyTable);

        JPanel sourcePanel = new JPanel(new BorderLayout(4, 4));

        sourcePanel.setBackground(Theme.BG_PANEL);

        sourcePanel.add(
                new JScrollPane(assemblyArea),
                BorderLayout.CENTER
        );

        sourcePanel.add(
                assemblyStatus,
                BorderLayout.SOUTH
        );

        sourcePanel.setPreferredSize(
                new Dimension(420, 180)
        );

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                sourcePanel,
                new JScrollPane(assemblyTable)
        );

        split.setResizeWeight(0.52);
        split.setDividerSize(5);
        split.setBorder(null);

        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private void mountProgram() {
        SimulatorController.AssemblyResult result =
                controller.montarECarregar(assemblyArea.getText());

        assemblyTableModel.setRowCount(0);

        if (result.isSuccess()) {
            assemblyStatus.setForeground(Theme.ACCENT_GREEN);
            assemblyStatus.setText(result.getMessage());

            for (SimulatorController.AssemblyLine line : result.getLines()) {
                assemblyTableModel.addRow(new Object[]{
                        line.getAddress(),
                        line.getSource(),
                        line.getBinary()
                });
            }

        } else {
            assemblyStatus.setForeground(Theme.ACCENT_RED);
            assemblyStatus.setText(result.getMessage());

            JOptionPane.showMessageDialog(
                    this,
                    result.getMessage(),
                    "Erro de montagem",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());

        statusBar.setBackground(Theme.BG_ELEVATED);

        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        1, 0, 0, 0, Theme.BORDER_COLOR
                ),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        statusBadge.setFont(Theme.FONT_UI_BOLD);
        statusBadge.setForeground(Theme.ACCENT_GREEN);

        mpcStatus.setFont(Theme.FONT_MONO);
        mpcStatus.setForeground(Theme.TEXT_PRIMARY);

        statusBar.add(statusBadge, BorderLayout.WEST);
        statusBar.add(mpcStatus, BorderLayout.EAST);

        return statusBar;
    }

    private void refresh() {
        registerPanel.refresh(
                controller.getModel().getUnidadeControle()
        );

        controlUnitPanel.refresh(
                controller.getModel().getUnidadeControle()
        );

        memoryPanel.refresh();

        boolean halted = controller.getModel().isHalted();

        if (halted) {
            timer.stop();
            updateExecutionControls(false);

            statusBadge.setText("● PROGRAMA FINALIZADO");
            statusBadge.setForeground(Theme.ACCENT_RED);

        } else if (timer.isRunning()) {
            statusBadge.setText("● EXECUTANDO");
            statusBadge.setForeground(Theme.ACCENT_GREEN);

        } else {
            statusBadge.setText("● AGUARDANDO");
            statusBadge.setForeground(Theme.ACCENT_AMBER);
        }

        mpcStatus.setText(
                "MPC Atual: "
                + controller.getModel().getUnidadeControle().getMpc()
        );
    }
}
