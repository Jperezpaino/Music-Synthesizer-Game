package es.noa.rad.tracker;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Motor del sequencer: recorre las 16 columnas en bucle a un BPM dado,
 * reproduce las notas activas de cada columna y actualiza el cursor
 * visual del {@link TrackerPanel}.
 *
 * <p>Un paso equivale a una corchea (1/16) a la velocidad indicada.
 * Formula: stepMs = 60_000 / bpm / 4</p>
 */
public final class SequencerEngine {

  private static final int DEFAULT_BPM = 120;
  private static final int MIN_BPM     = 40;
  private static final int MAX_BPM     = 300;

  private int          bpm        = DEFAULT_BPM;
  private int          currentCol = -1;
  private Timer        timer;
  private TrackerPanel tracker;

  // -------------------------------------------------------------------------
  // Control
  // -------------------------------------------------------------------------

  /**
   * Inicia la reproduccion en bucle.
   * Si ya esta corriendo no hace nada.
   *
   * @param tracker panel cuyo contenido se reproducira
   */
  public void start(final TrackerPanel tracker) {
    if (this.timer != null && this.timer.isRunning()) return;
    this.tracker    = tracker;
    this.currentCol = -1;
    this.timer = new Timer(stepMs(), e -> tick());
    this.timer.setInitialDelay(0);
    this.timer.start();
  }

  /** Detiene la reproduccion y limpia el cursor visual. */
  public void stop() {
    if (this.timer != null) {
      this.timer.stop();
      this.timer = null;
    }
    if (this.tracker != null) {
      this.tracker.setPlayColumn(-1);
    }
    this.currentCol = -1;
  }

  /** @return true si el sequencer esta en marcha */
  public boolean isRunning() {
    return this.timer != null && this.timer.isRunning();
  }

  /**
   * Cambia el BPM. Si el sequencer esta corriendo actualiza el delay
   * del timer inmediatamente.
   *
   * @param bpm tempo deseado (se clampea entre {@value #MIN_BPM}
   *            y {@value #MAX_BPM})
   */
  public void setBpm(final int bpm) {
    this.bpm = Math.max(MIN_BPM, Math.min(MAX_BPM, bpm));
    if (isRunning()) {
      this.timer.setDelay(stepMs());
    }
  }

  public int getBpm() { return this.bpm; }

  // -------------------------------------------------------------------------
  // Tick interno
  // -------------------------------------------------------------------------

  private void tick() {
    this.currentCol = (this.currentCol + 1) % TrackerPanel.COLS;
    final int col = this.currentCol;

    // Actualizar cursor visual en el EDT (el Timer ya dispara en el EDT)
    this.tracker.setPlayColumn(col);

    // Reproducir todas las notas activas en esta columna
    for (int row = 0; row < TrackerPanel.ROWS; row++) {
      if (this.tracker.isRowActive(row) && this.tracker.isStepActive(row, col)) {
        final TrackerPanel.NoteInfo ni = this.tracker.getNoteInfo(row);
        NotePlayer.playNote(ni.semitone, ni.octave);
      }
    }
  }

  private int stepMs() {
    // Negras a bpm → corcheas = bpm*4 → ms por corchea = 60000/(bpm*4)
    return Math.max(1, 60_000 / this.bpm / 4);
  }
}