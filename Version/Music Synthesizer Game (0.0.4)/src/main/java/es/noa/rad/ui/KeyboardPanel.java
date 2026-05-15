package es.noa.rad.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;

import es.noa.rad.synth.Note;
import es.noa.rad.synth.SoundEngine;

/**
 * Panel que representa un teclado de piano interactivo.
 *
 * <p>Muestra teclas blancas y negras para dos octavas.
 * Al pulsar una tecla reproduce la nota correspondiente
 * y la suelta al soltar el ratón.</p>
 */
public final class KeyboardPanel extends JPanel {

  /**
   * Listener que recibe notificaciones cuando se pulsa o suelta una tecla.
   * Usado por {@link MelodyPanel} para grabar notas.
   */
  public interface NoteListener {
    /** Llamado cuando el usuario pulsa una tecla. */
    void onNotePressed(Note note);
    /** Llamado cuando el usuario suelta una tecla. */
    void onNoteReleased(Note note);
  }

  private static final long serialVersionUID = 1L;

  // --- Dimensiones de las teclas ---
  private static final int WHITE_KEY_WIDTH  = 54;
  private static final int WHITE_KEY_HEIGHT = 200;
  private static final int BLACK_KEY_WIDTH  = 32;
  private static final int BLACK_KEY_HEIGHT = 125;
  private static final int KEY_BORDER       = 2;

  // --- Colores ---
  private static final Color COLOR_WHITE_KEY         = new Color(255, 255, 248);
  private static final Color COLOR_WHITE_KEY_PRESSED = new Color(180, 220, 255);
  private static final Color COLOR_BLACK_KEY         = new Color(30, 30, 30);
  private static final Color COLOR_BLACK_KEY_PRESSED = new Color(60, 130, 200);
  private static final Color COLOR_KEY_BORDER        = new Color(100, 100, 100);
  private static final Color COLOR_LABEL             = new Color(80, 80, 80);
  private static final Color COLOR_LABEL_BLACK       = new Color(200, 200, 200);
  private static final Color COLOR_BACKGROUND        = new Color(40, 40, 40);

  /** Octava actual (3, 4 o 5) */
  private int octave;

  /** Motor de audio */
  private final SoundEngine soundEngine;

  /** Nota que se está pulsando actualmente (null si ninguna) */
  private Note pressedNote;

  /** Listener opcional para grabar notas (MelodyPanel) */
  private NoteListener noteListener;

  /**
   * Orden de teclas blancas en una octava: Do Re Mi Fa Sol La Si
   */
  private static final String[] WHITE_NAMES = { "C", "D", "E", "F", "G", "A", "B" };

  /**
   * Posición relativa (en índice de tecla blanca) donde aparece cada sostenido.
   * C#=entre0-1, D#=entre1-2, F#=entre3-4, G#=entre4-5, A#=entre5-6
   */
  private static final int[] BLACK_POSITIONS = { 0, 1, 3, 4, 5 };

  /**
   * Sufijos de las teclas negras (sostenidos).
   */
  private static final String[] BLACK_NAMES = { "CS", "DS", "FS", "GS", "AS" };

  /**
   * Número de octavas a mostrar en el teclado.
   */
  private static final int OCTAVES = 2;

  /**
   * Mapa ordenado de área rectangular (x,y,w,h) → Note para cada tecla.
   * Se recalcula al cambiar de octava.
   */
  private final Map<int[], Note> whiteKeyAreas = new LinkedHashMap<>();
  private final Map<int[], Note> blackKeyAreas = new LinkedHashMap<>();

  /**
   * Crea el panel del teclado.
   *
   * @param soundEngine Motor de audio a usar
   * @param octave      Octava base inicial (3, 4 o 5)
   */
  public KeyboardPanel(final SoundEngine soundEngine, final int octave) {
    this.soundEngine = soundEngine;
    this.octave      = octave;

    final int totalWhiteKeys = WHITE_NAMES.length * OCTAVES;
    final int panelWidth     = totalWhiteKeys * WHITE_KEY_WIDTH + KEY_BORDER;
    this.setPreferredSize(new Dimension(panelWidth, WHITE_KEY_HEIGHT + 20));
    this.setBackground(COLOR_BACKGROUND);

    this.buildKeyAreas();
    this.addMouseListeners();
  }

  /**
   * Recalcula las áreas de todas las teclas según la octava actual.
   */
  private void buildKeyAreas() {
    this.whiteKeyAreas.clear();
    this.blackKeyAreas.clear();

    for (int oct = 0; oct < OCTAVES; oct++) {
      final int octaveNumber  = this.octave + oct;
      final int octaveXOffset = oct * WHITE_NAMES.length * WHITE_KEY_WIDTH;

      // Teclas blancas
      for (int i = 0; i < WHITE_NAMES.length; i++) {
        final String noteName = WHITE_NAMES[i] + octaveNumber;
        try {
          final Note note = Note.valueOf(noteName);
          final int x     = octaveXOffset + i * WHITE_KEY_WIDTH;
          this.whiteKeyAreas.put(new int[]{ x, 0, WHITE_KEY_WIDTH, WHITE_KEY_HEIGHT }, note);
        } catch (final IllegalArgumentException ignored) { }
      }

      // Teclas negras
      for (int i = 0; i < BLACK_POSITIONS.length; i++) {
        final String noteName = BLACK_NAMES[i] + octaveNumber;
        try {
          final Note note = Note.valueOf(noteName);
          final int x     = octaveXOffset + BLACK_POSITIONS[i] * WHITE_KEY_WIDTH
              + WHITE_KEY_WIDTH - BLACK_KEY_WIDTH / 2;
          this.blackKeyAreas.put(new int[]{ x, 0, BLACK_KEY_WIDTH, BLACK_KEY_HEIGHT }, note);
        } catch (final IllegalArgumentException ignored) { }
      }
    }
  }

  /**
   * Registra los listeners de ratón para pulsar/soltar teclas.
   */
  private void addMouseListeners() {
    this.addMouseListener(new MouseAdapter() {

      @Override
      public void mousePressed(final MouseEvent e) {
        final Note note = KeyboardPanel.this.getNoteAt(e.getX(), e.getY());
        if (note != null) {
          KeyboardPanel.this.pressedNote = note;
          KeyboardPanel.this.soundEngine.startNote(note);
          if (KeyboardPanel.this.noteListener != null) {
            KeyboardPanel.this.noteListener.onNotePressed(note);
          }
          KeyboardPanel.this.repaint();
        }
      }

      @Override
      public void mouseReleased(final MouseEvent e) {
        if (KeyboardPanel.this.pressedNote != null) {
          final Note released = KeyboardPanel.this.pressedNote;
          KeyboardPanel.this.soundEngine.stopNote();
          KeyboardPanel.this.pressedNote = null;
          if (KeyboardPanel.this.noteListener != null) {
            KeyboardPanel.this.noteListener.onNoteReleased(released);
          }
          KeyboardPanel.this.repaint();
        }
      }

    });
  }

  /**
   * Devuelve la nota correspondiente a la posición del ratón.
   * Las teclas negras tienen prioridad sobre las blancas.
   *
   * @param x Coordenada X del ratón
   * @param y Coordenada Y del ratón
   * @return Nota en esa posición, o null si no hay ninguna
   */
  private Note getNoteAt(final int x, final int y) {
    // Primero buscar en negras (prioridad)
    for (final Map.Entry<int[], Note> entry : this.blackKeyAreas.entrySet()) {
      final int[] area = entry.getKey();
      if (x >= area[0] && x <= area[0] + area[2]
          && y >= area[1] && y <= area[1] + area[3]) {
        return entry.getValue();
      }
    }
    // Luego en blancas
    for (final Map.Entry<int[], Note> entry : this.whiteKeyAreas.entrySet()) {
      final int[] area = entry.getKey();
      if (x >= area[0] && x <= area[0] + area[2]
          && y >= area[1] && y <= area[1] + area[3]) {
        return entry.getValue();
      }
    }
    return null;
  }

  /**
   * Cambia la octava base del teclado.
   *
   * @param octave Nueva octava (3, 4 o 5)
   */
  public void setOctave(final int octave) {
    this.octave = octave;
    this.buildKeyAreas();
    this.repaint();
  }

  /**
   * Asigna un listener para recibir eventos de pulsación de teclas.
   *
   * @param listener Listener a registrar (normalmente MelodyPanel)
   */
  public void setNoteListener(final NoteListener listener) {
    this.noteListener = listener;
  }

  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);
    final Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Dibujar teclas blancas
    for (final Map.Entry<int[], Note> entry : this.whiteKeyAreas.entrySet()) {
      final int[] area     = entry.getKey();
      final Note  note     = entry.getValue();
      final boolean pressed = note.equals(this.pressedNote);

      g2.setColor(pressed ? COLOR_WHITE_KEY_PRESSED : COLOR_WHITE_KEY);
      g2.fillRoundRect(area[0] + KEY_BORDER, area[1] + KEY_BORDER,
          area[2] - KEY_BORDER * 2, area[3] - KEY_BORDER, 4, 4);

      g2.setColor(COLOR_KEY_BORDER);
      g2.setStroke(new BasicStroke(1.2f));
      g2.drawRoundRect(area[0] + KEY_BORDER, area[1] + KEY_BORDER,
          area[2] - KEY_BORDER * 2, area[3] - KEY_BORDER, 4, 4);

      // Etiqueta de la nota
      g2.setColor(COLOR_LABEL);
      g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
      final String label = note.getDisplayName().replace(" ", "\n");
      final String[] parts = label.split(" ");
      g2.drawString(parts[0], area[0] + KEY_BORDER + 8, area[1] + area[3] - 28);
      if (parts.length > 1) {
        g2.drawString(parts[1], area[0] + KEY_BORDER + 10, area[1] + area[3] - 14);
      }
    }

    // Dibujar teclas negras encima
    for (final Map.Entry<int[], Note> entry : this.blackKeyAreas.entrySet()) {
      final int[] area     = entry.getKey();
      final Note  note     = entry.getValue();
      final boolean pressed = note.equals(this.pressedNote);

      g2.setColor(pressed ? COLOR_BLACK_KEY_PRESSED : COLOR_BLACK_KEY);
      g2.fillRoundRect(area[0], area[1], area[2], area[3], 4, 4);

      g2.setColor(COLOR_KEY_BORDER);
      g2.setStroke(new BasicStroke(1f));
      g2.drawRoundRect(area[0], area[1], area[2], area[3], 4, 4);

      // Etiqueta pequeña en tecla negra
      g2.setColor(COLOR_LABEL_BLACK);
      g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
      final String[] parts = note.getDisplayName().split(" ");
      g2.drawString(parts[0], area[0] + 4, area[1] + area[3] - 8);
    }
  }

}
