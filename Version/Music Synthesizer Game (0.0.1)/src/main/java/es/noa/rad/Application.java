package es.noa.rad;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

  /**
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
      this.setTitle("Music Synthesizer Game");
      this.setLocationRelativeTo(null);
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
