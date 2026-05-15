package es.noa.rad.ui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import es.noa.rad.synth.SoundEngine;

/**
 * Panel principal del sintetizador.
 *
 * <p>Combina el panel de controles (arriba), el teclado piano (centro)
 * y el panel de grabación/reproducción de melodías (abajo).</p>
 */
public final class SynthesizerPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final Color COLOR_BACKGROUND = new Color(28, 28, 35);

  /**
   * Crea el panel principal del sintetizador.
   *
   * @param soundEngine Motor de audio compartido
   */
  public SynthesizerPanel(final SoundEngine soundEngine) {
    this.setLayout(new BorderLayout());
    this.setBackground(COLOR_BACKGROUND);

    // Crear componentes
    final KeyboardPanel keyboardPanel = new KeyboardPanel(soundEngine, 4);
    final ControlPanel  controlPanel  = new ControlPanel(soundEngine);
    final MelodyPanel   melodyPanel   = new MelodyPanel(soundEngine);

    // Vincular octava y listener de notas
    controlPanel.setKeyboardPanel(keyboardPanel);
    keyboardPanel.setNoteListener(melodyPanel);

    // Scroll horizontal por si la ventana es pequeña
    final JScrollPane scrollPane = new JScrollPane(keyboardPanel,
        JScrollPane.VERTICAL_SCROLLBAR_NEVER,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setBackground(COLOR_BACKGROUND);
    scrollPane.getViewport().setBackground(COLOR_BACKGROUND);
    scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 75), 1));

    this.add(controlPanel, BorderLayout.NORTH);
    this.add(scrollPane,   BorderLayout.CENTER);
    this.add(melodyPanel,  BorderLayout.SOUTH);
  }

}

