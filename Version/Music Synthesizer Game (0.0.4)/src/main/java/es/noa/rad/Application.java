package es.noa.rad;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import es.noa.rad.synth.SoundEngine;
import es.noa.rad.ui.SynthesizerPanel;

  /**
   * Ventana principal de la aplicación Music Synthesizer Game.
   *
   * @see javax.swing.JFrame
   */
  public final class Application
      extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private Application() {
      this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      this.setResizable(false);
      this.setSize(1280, 720);
      this.setTitle("🎹 Music Synthesizer Game");
      this.setLocationRelativeTo(null);

      // Motor de audio compartido por toda la aplicación
      final SoundEngine soundEngine = new SoundEngine();

      // Panel principal del sintetizador
      final SynthesizerPanel synthPanel = new SynthesizerPanel(soundEngine);
      this.setLayout(new BorderLayout());
      this.add(synthPanel, BorderLayout.CENTER);

      this.setVisible(true);
    }

    /**
     *
     */
    public static void launch() {
      new Application();
    }

    /**
     * @param _arguments {@code String...}
     */
    public static void main(
        final String... _arguments) {
      SwingUtilities.invokeLater(
        Application::launch
      );
    }

  }
