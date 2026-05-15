package es.noa.rad;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import es.noa.rad.synth.Note;
import es.noa.rad.synth.SoundEngine;
import es.noa.rad.ui.KeyboardPanel;
import es.noa.rad.ui.SynthesizerPanel;

  /**
   * Ventana principal de la aplicación Music Synthesizer Game.
   *
   * @see javax.swing.JFrame
   */
  public final class Application
      extends JFrame {

    private static final long serialVersionUID = 1L;

    /** Mapeo tecla PC → nota (octava 4). Se ajusta con los botones de octava. */
    private static final Map<Integer, Note> KEY_NOTE_MAP = Map.ofEntries(
        // Teclas blancas: A S D F G H J K → C D E F G A B C5
        Map.entry(KeyEvent.VK_A, Note.C4),
        Map.entry(KeyEvent.VK_S, Note.D4),
        Map.entry(KeyEvent.VK_D, Note.E4),
        Map.entry(KeyEvent.VK_F, Note.F4),
        Map.entry(KeyEvent.VK_G, Note.G4),
        Map.entry(KeyEvent.VK_H, Note.A4),
        Map.entry(KeyEvent.VK_J, Note.B4),
        Map.entry(KeyEvent.VK_K, Note.C5),
        // Teclas negras: W E → C# D#,  T Y U → F# G# A#
        Map.entry(KeyEvent.VK_W, Note.CS4),
        Map.entry(KeyEvent.VK_E, Note.DS4),
        Map.entry(KeyEvent.VK_T, Note.FS4),
        Map.entry(KeyEvent.VK_Y, Note.GS4),
        Map.entry(KeyEvent.VK_U, Note.AS4)
    );

    private Application() {
      this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      this.setResizable(false);
      this.setSize(1280, 720);
      this.setTitle("🎹 Music Synthesizer Game");
      this.setLocationRelativeTo(null);

      final SoundEngine      soundEngine = new SoundEngine();
      final SynthesizerPanel synthPanel  = new SynthesizerPanel(soundEngine);
      this.setLayout(new BorderLayout());
      this.add(synthPanel, BorderLayout.CENTER);

      // Mapeo de teclado PC
      final KeyboardPanel kbPanel = synthPanel.getKeyboardPanel();
      final Set<Integer> pressedKeys = new HashSet<>();
      this.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(final KeyEvent e) {
          final int code = e.getKeyCode();
          if (pressedKeys.contains(code)) {
            return; // ignorar auto-repeat
          }
          pressedKeys.add(code);
          final Note note = KEY_NOTE_MAP.get(code);
          if (note != null) {
            kbPanel.pressNote(note);
          }
        }

        @Override
        public void keyReleased(final KeyEvent e) {
          final int code = e.getKeyCode();
          pressedKeys.remove(code);
          final Note note = KEY_NOTE_MAP.get(code);
          if (note != null) {
            kbPanel.releaseNote(note);
          }
        }
      });
      this.setFocusable(true);
      this.requestFocusInWindow();

      this.setVisible(true);
    }

    public static void launch() {
      new Application();
    }

    public static void main(final String... _arguments) {
      SwingUtilities.invokeLater(Application::launch);

    }

  }
