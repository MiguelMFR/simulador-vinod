package br.edu.mic1;

import br.edu.mic1.controller.SimulatorController;
import br.edu.mic1.model.SimulatorModel;
import br.edu.mic1.view.SimulatorView;

import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulatorModel model = new SimulatorModel();
            SimulatorView view = new SimulatorView();
            new SimulatorController(model, view);
            view.setVisible(true);
        });
    }
}
