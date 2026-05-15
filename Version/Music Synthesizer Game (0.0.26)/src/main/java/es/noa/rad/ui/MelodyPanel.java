package es.noa.rad.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import es.noa.rad.melody.Melody;
import es.noa.rad.melody.MelodyNote;
import es.noa.rad.melody.MelodyPlayer;
import es.noa.rad.melody.MelodySerializer;
import es.noa.rad.synth.Note;
import es.noa.rad.synth.SoundEngine;

/**
 * Panel de grabación y reproducción de melodías.
 *
 * <p>Permite al usuario:
 * <ul>
 *   <li>⏺ Grabar lo que toca en el teclado</li>
 *   <li>⏹ Detener la grabación</li>
 *   <li>▶ Reproducir la melodía grabada</li>
 *   <li>⏹ Detener la reproducción</li>
 *   <li>🗑 Borrar la melodía</li>
 * </ul>
 * </p>
 *
 * <p>Se integra con {@link KeyboardPanel} interceptando las pulsaciones
 * de teclas mientras el modo grabación está activo.</p>
 */
public final class MelodyPanel extends JPanel implements KeyboardPanel.NoteListener {

  private static final long serialVersionUID = 1L;

  // --- Colores ---
  private static final Color COLOR_BACKGROUND   = new Color(22, 22, 30);
  private static final Color COLOR_PANEL         = new Color(38, 38, 50);
  private static final Color COLOR_TEXT          = new Color(220, 220, 230);
  private static final Color COLOR_ACCENT        = new Color(80, 160, 255);
  private static final Color COLOR_RECORD        = new Color(220, 60,  60);
  private static final Color COLOR_RECORD_ACTIVE = new Color(255, 80,  80);
  private static final Color COLOR_PLAY          = new Color(60,  180, 60);
  private static final Color COLOR_PLAY_ACTIVE   = new Color(80,  220, 80);
  private static final Color COLOR_DELETE        = new Color(160, 60,  60);
  private static final Color COLOR_DISABLED      = new Color(70,  70,  85);
  private static final Color COLOR_SAVE          = new Color(60,  120, 200);
  private static final Color COLOR_LOAD          = new Color(90,  160, 90);

  // --- Estado ---
  private enum State { IDLE, RECORDING, PLAYING }
  private State state = State.IDLE;

  // --- Modelo ---
  private final Melody       melody;
  private final MelodyPlayer player;
  private final SoundEngine  soundEngine;

  // --- Grabación ---
  private long recordStartMs;
  private final Map<Note, MelodyNote> activeNotes = new HashMap<>();

  // --- UI ---
  private JButton  btnRecord;
  private JButton  btnPlay;
  private JButton  btnStop;
  private JButton  btnClear;
  private JButton  btnSave;
  private JButton  btnLoad;
  private JLabel   statusLabel;
  private JLabel   notesLabel;

  /** Último directorio usado en el JFileChooser */
  private File lastDirectory = new File(System.getProperty("user.home"));

  /**
   * Crea el panel de grabación/reproducción.
   *
   * @param soundEngine Motor de audio compartido
   */
  public MelodyPanel(final SoundEngine soundEngine) {
    this.soundEngine = soundEngine;
    this.melody      = new Melody();
    this.player      = new MelodyPlayer(soundEngine);

    this.player.setListener(new MelodyPlayer.PlaybackListener() {
      @Override public void onPlaybackStarted() {
        SwingUtilities.invokeLater(() -> MelodyPanel.this.updateUI(State.PLAYING));
      }
      @Override public void onPlaybackStopped() {
        SwingUtilities.invokeLater(() -> MelodyPanel.this.updateUI(State.IDLE));
      }
    });

    this.setBackground(COLOR_BACKGROUND);
    this.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
    this.setPreferredSize(new Dimension(1280, 60));
    this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 80)));

    this.buildUI();
    this.updateUI(State.IDLE);
  }

  /**
   * Construye los botones y etiquetas del panel.
   */
  private void buildUI() {
    this.btnRecord = this.makeButton("⏺  GRABAR",  COLOR_RECORD);
    this.btnPlay   = this.makeButton("▶  PLAY",    COLOR_PLAY);
    this.btnStop   = this.makeButton("⏹  STOP",    COLOR_DISABLED);
    this.btnClear  = this.makeButton("🗑  BORRAR", COLOR_DELETE);
    this.btnSave   = this.makeButton("💾  GUARDAR", COLOR_SAVE);
    this.btnLoad   = this.makeButton("📂  ABRIR",  COLOR_LOAD);

    this.statusLabel = new JLabel("Listo para grabar", SwingConstants.LEFT);
    this.statusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    this.statusLabel.setForeground(COLOR_ACCENT);

    this.notesLabel = new JLabel("0 notas | 0 ms");
    this.notesLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
    this.notesLabel.setForeground(COLOR_TEXT);

    this.btnRecord.addActionListener(e -> this.startRecording());
    this.btnPlay.addActionListener(e   -> this.startPlayback());
    this.btnStop.addActionListener(e   -> this.stopCurrent());
    this.btnClear.addActionListener(e  -> this.clearMelody());
    this.btnSave.addActionListener(e   -> this.saveMelody());
    this.btnLoad.addActionListener(e   -> this.loadMelody());

    final JLabel sep1 = new JLabel("  |  ");
    sep1.setForeground(new Color(70, 70, 90));
    final JLabel sep2 = new JLabel("  |  ");
    sep2.setForeground(new Color(70, 70, 90));

    this.add(this.btnRecord);
    this.add(this.btnPlay);
    this.add(this.btnStop);
    this.add(this.btnClear);
    this.add(sep1);
    this.add(this.btnSave);
    this.add(this.btnLoad);
    this.add(sep2);
    this.add(this.statusLabel);
    this.add(new JLabel("   "));
    this.add(this.notesLabel);
  }

  // -------------------------------------------------------------------------
  // Acciones
  // -------------------------------------------------------------------------

  /** Inicia la grabación. */
  private void startRecording() {
    this.melody.clear();
    this.activeNotes.clear();
    this.recordStartMs = System.currentTimeMillis();
    this.updateUI(State.RECORDING);
  }

  /** Inicia la reproducción de la melodía grabada. */
  private void startPlayback() {
    if (!this.melody.isEmpty()) {
      this.player.play(this.melody);
    }
  }

  /** Detiene la grabación o reproducción en curso. */
  private void stopCurrent() {
    if (this.state == State.RECORDING) {
      // Cerrar notas activas que no se soltaron
      this.activeNotes.forEach((note, mn) -> {
        mn.setDurationMs(System.currentTimeMillis() - this.recordStartMs - mn.getStartTimeMs());
        this.melody.addNote(mn);
      });
      this.activeNotes.clear();
    } else if (this.state == State.PLAYING) {
      this.player.stop();
    }
    this.updateUI(State.IDLE);
  }

  /** Borra la melodía grabada. */
  private void clearMelody() {
    this.player.stop();
    this.melody.clear();
    this.activeNotes.clear();
    this.updateUI(State.IDLE);
  }

  // -------------------------------------------------------------------------
  // NoteListener — recibe eventos del teclado
  // -------------------------------------------------------------------------

  @Override
  public void onNotePressed(final Note note) {
    if (this.state != State.RECORDING) {
      return;
    }
    final long relativeMs = System.currentTimeMillis() - this.recordStartMs;
    final MelodyNote mn   = new MelodyNote(note, this.soundEngine.getWaveform(), relativeMs);
    this.activeNotes.put(note, mn);
    this.refreshNotesLabel();
  }

  @Override
  public void onNoteReleased(final Note note) {
    if (this.state != State.RECORDING) {
      return;
    }
    final MelodyNote mn = this.activeNotes.remove(note);
    if (mn != null) {
      final long durationMs = System.currentTimeMillis() - this.recordStartMs - mn.getStartTimeMs();
      mn.setDurationMs(durationMs);
      this.melody.addNote(mn);
      this.refreshNotesLabel();
    }
  }

  // -------------------------------------------------------------------------
  // UI helpers
  // -------------------------------------------------------------------------

  /**
   * Actualiza todos los controles según el estado actual.
   *
   * @param newState Nuevo estado
   */
  private void updateUI(final State newState) {
    this.state = newState;

    switch (newState) {
      case IDLE -> {
        this.btnRecord.setBackground(COLOR_RECORD);
        this.btnRecord.setEnabled(true);
        this.btnPlay.setEnabled(!this.melody.isEmpty());
        this.btnPlay.setBackground(this.melody.isEmpty() ? COLOR_DISABLED : COLOR_PLAY);
        this.btnStop.setEnabled(false);
        this.btnStop.setBackground(COLOR_DISABLED);
        this.btnClear.setEnabled(!this.melody.isEmpty());
        this.btnClear.setBackground(this.melody.isEmpty() ? COLOR_DISABLED : COLOR_DELETE);
        this.btnSave.setEnabled(!this.melody.isEmpty());
        this.btnSave.setBackground(this.melody.isEmpty() ? COLOR_DISABLED : COLOR_SAVE);
        this.btnLoad.setEnabled(true);
        this.btnLoad.setBackground(COLOR_LOAD);
        this.statusLabel.setText("Listo");
        this.statusLabel.setForeground(COLOR_ACCENT);
      }
      case RECORDING -> {
        this.btnRecord.setBackground(COLOR_RECORD_ACTIVE);
        this.btnRecord.setEnabled(false);
        this.btnPlay.setEnabled(false);
        this.btnPlay.setBackground(COLOR_DISABLED);
        this.btnStop.setEnabled(true);
        this.btnStop.setBackground(new Color(200, 100, 100));
        this.btnClear.setEnabled(false);
        this.btnClear.setBackground(COLOR_DISABLED);
        this.btnSave.setEnabled(false);
        this.btnSave.setBackground(COLOR_DISABLED);
        this.btnLoad.setEnabled(false);
        this.btnLoad.setBackground(COLOR_DISABLED);
        this.statusLabel.setText("● Grabando...");
        this.statusLabel.setForeground(COLOR_RECORD_ACTIVE);
      }
      case PLAYING -> {
        this.btnRecord.setEnabled(false);
        this.btnRecord.setBackground(COLOR_DISABLED);
        this.btnPlay.setBackground(COLOR_PLAY_ACTIVE);
        this.btnPlay.setEnabled(false);
        this.btnStop.setEnabled(true);
        this.btnStop.setBackground(new Color(100, 180, 100));
        this.btnClear.setEnabled(false);
        this.btnClear.setBackground(COLOR_DISABLED);
        this.btnSave.setEnabled(false);
        this.btnSave.setBackground(COLOR_DISABLED);
        this.btnLoad.setEnabled(false);
        this.btnLoad.setBackground(COLOR_DISABLED);
        this.statusLabel.setText("▶ Reproduciendo...");
        this.statusLabel.setForeground(COLOR_PLAY_ACTIVE);
      }
    }

    this.refreshNotesLabel();
    this.repaint();
  }

  /** Actualiza el contador de notas y duración. */
  private void refreshNotesLabel() {
    this.notesLabel.setText(
        this.melody.size() + " notas | " + this.melody.getTotalDurationMs() + " ms"
    );
  }

  /**
   * Guarda la melodía actual en un fichero .synth elegido por el usuario.
   */
  private void saveMelody() {
    final JFileChooser chooser = new JFileChooser(this.lastDirectory);
    chooser.setDialogTitle("Guardar melodía");
    chooser.setFileFilter(new FileNameExtensionFilter(MelodySerializer.FILE_DESCRIPTION, "synth"));
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      this.lastDirectory = file.getParentFile();
      if (!file.getName().endsWith(MelodySerializer.FILE_EXTENSION)) {
        file = new File(file.getAbsolutePath() + MelodySerializer.FILE_EXTENSION);
      }
      try {
        MelodySerializer.save(this.melody, file);
        JOptionPane.showMessageDialog(this, "Melodía guardada en:\n" + file.getName(),
            "Guardado", JOptionPane.INFORMATION_MESSAGE);
      } catch (final IOException ex) {
        JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  /**
   * Carga una melodía desde un fichero .synth elegido por el usuario.
   */
  private void loadMelody() {
    final JFileChooser chooser = new JFileChooser(this.lastDirectory);
    chooser.setDialogTitle("Abrir melodía");
    chooser.setFileFilter(new FileNameExtensionFilter(MelodySerializer.FILE_DESCRIPTION, "synth"));
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      this.lastDirectory = chooser.getSelectedFile().getParentFile();
      try {
        final Melody loaded = MelodySerializer.load(chooser.getSelectedFile());
        this.player.stop();
        this.melody.clear();
        loaded.getNotes().forEach(this.melody::addNote);
        this.updateUI(State.IDLE);
      } catch (final IOException ex) {
        JOptionPane.showMessageDialog(this, "Error al cargar: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  /**
   * Crea un botón con estilo oscuro.
   */
  private JButton makeButton(final String text, final Color bg) {
    final JButton btn = new JButton(text);
    btn.setBackground(bg);
    btn.setForeground(Color.WHITE);
    btn.setFont(new Font("SansSerif", Font.BOLD, 12));
    btn.setFocusable(false);
    btn.setBorderPainted(false);
    btn.setPreferredSize(new Dimension(130, 36));
    btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    return btn;
  }

}
