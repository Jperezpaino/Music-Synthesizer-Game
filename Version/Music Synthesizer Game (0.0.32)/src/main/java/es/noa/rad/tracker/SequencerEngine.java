package es.noa.rad.tracker;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Motor del sequencer: recorre las 16 columnas en bucle a un BPM dado,
 * reproduce las notas activas de cada columna en todos los canales activos
 * y actualiza el cursor visual de cada {@link TrackerPanel}.
 *
 * <p>Soporta hasta {@link NotePlayer#NUM_CHANNELS} trackers simultaneos,
 * cada uno con su propio canal MIDI e instrumento GM.</p>
 *
 * <p>Un paso equivale a una corchea (1/16) a la velocidad indicada.
 * Formula: stepMs = 60_000 / bpm / 4</p>
 */
public final class SequencerEngine {

  public static final int DEFAULT_BPM = 120;
  private static final int MIN_BPM    = 40;
  private static final int MAX_BPM    = 300;

  private int            bpm         = DEFAULT_BPM;
  private int            globalStep  = -1;   // paso global: bar * COLS + col
  private int            currentBar  = 0;
  private int            currentCol  = -1;
  private Timer          timer;
  private TrackerPanel[] trackers;

  // -------------------------------------------------------------------------
  // Control
  // -------------------------------------------------------------------------

  /**
   * Inicia la reproduccion en bucle sobre multiples canales.
   * Si ya esta corriendo no hace nada.
   *
   * @param trackers array de paneles a reproducir simultaneamente
   */
  public void start(final TrackerPanel[] trackers) {
    if (this.timer != null && this.timer.isRunning()) return;
    this.trackers   = trackers;
    this.globalStep = -1;
    this.currentBar = 0;
    this.currentCol = -1;
    this.timer = new Timer(stepMs(), e -> tick());
    this.timer.setInitialDelay(0);
    this.timer.start();
  }

  /**
   * Inicia la reproduccion sobre un unico canal (compatibilidad hacia atras).
   *
   * @param tracker panel a reproducir
   */
  public void start(final TrackerPanel tracker) {
    start(new TrackerPanel[]{ tracker });
  }

  /** Detiene la reproduccion y limpia el cursor visual de todos los paneles. */
  public void stop() {
    if (this.timer != null) {
      this.timer.stop();
      this.timer = null;
    }
    if (this.trackers != null) {
      for (final TrackerPanel t : this.trackers) {
        t.setPlayPosition(-1, -1);
      }
    }
    this.globalStep = -1;
    this.currentBar = 0;
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

  /** Devuelve el maximo numero de compases entre todos los trackers activos. */
  private int maxNumBars() {
    int max = 1;
    if (this.trackers != null) {
      for (final TrackerPanel t : this.trackers) {
        max = Math.max(max, t.getNumBars());
      }
    }
    return max;
  }

  private void tick() {
    final int totalSteps = maxNumBars() * TrackerPanel.COLS;
    this.globalStep = (this.globalStep + 1) % totalSteps;
    this.currentBar = this.globalStep / TrackerPanel.COLS;
    this.currentCol = this.globalStep % TrackerPanel.COLS;

    final int bar = this.currentBar;
    final int col = this.currentCol;

    if (this.trackers == null) return;

    for (final TrackerPanel tracker : this.trackers) {
      // Actualizar cursor visual (solo mueve el cursor si el bar existe en este tracker)
      if (bar < tracker.getNumBars()) {
        tracker.setPlayPosition(bar, col);
      } else {
        // Si este tracker tiene menos compases, ocultar cursor
        tracker.setPlayPosition(-1, -1);
      }

      // Reproducir notas del compas actual
      final int chIdx = tracker.getChannelIndex();
      for (int row = 0; row < TrackerPanel.ROWS; row++) {
        if (tracker.isRowActive(row) && tracker.isStepActiveAt(bar, row, col)) {
          final TrackerPanel.NoteInfo ni = tracker.getNoteInfo(row);
          NotePlayer.playNote(ni.semitone, ni.octave, chIdx);
        }
      }
    }
  }

  private int stepMs() {
    return Math.max(1, 60_000 / this.bpm / 4);
  }
}
