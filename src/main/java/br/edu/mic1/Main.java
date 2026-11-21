package br.edu.mic1;

import br.edu.mic1.controller.SimulatorController;
import br.edu.mic1.model.Mic1Model;
import br.edu.mic1.view.SimulatorView;

import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Mic1Model model = new Mic1Model(4096, 256);
            SimulatorController controller = new SimulatorController(model);
            new SimulatorView(controller).setVisible(true);
        });
    }

}
