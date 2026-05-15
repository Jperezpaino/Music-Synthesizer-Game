package es.noa.rad.sequencer;

import java.util.ArrayList;
import java.util.List;

import es.noa.rad.synth.SoundEngine;

/**
 * Motor del secuenciador por pasos.
 *
 * <p>Gestiona una lista de {@link SequencerStep} y los reproduce en bucle
 * en un hilo de fondo a un tempo configurable (BPM).</p>
 *
 * <p>Notifica los cambios de paso a los {@link SequencerListener} registrados
 * para que la UI pueda resaltar el paso activo.</p>
 */
public final class Sequencer {

  /** Número de pasos del secuenciador */
  public static final int STEP_COUNT = 16;

  /** BPM mínimo / máximo / por defecto */
  public static final int BPM_MIN     = 40;
  public static final int BPM_MAX     = 240;
  public static final int BPM_DEFAULT = 120;

  // -------------------------------------------------------------------------
  // Interfaz de escucha
  // -------------------------------------------------------------------------

  /**
   * Escucha de eventos del secuenciador.
   */
  public interface SequencerListener {
    /**
     * Se llama en el hilo del secuenciador cada vez que se avanza al paso indicado.
     *
     * @param stepIndex Índice del paso activo (0 – STEP_COUNT-1)
     */
    void onStep(int stepIndex);

    /** Se llama cuando la reproducción se detiene. */
    void onStopped();
  }

  // -------------------------------------------------------------------------
  // Estado
  // -------------------------------------------------------------------------

  private final SequencerStep[] steps;
  private final SoundEngine     soundEngine;
  private final List<SequencerListener> listeners = new ArrayList<>();

  private int     bpm          = BPM_DEFAULT;
  private boolean running      = false;
  private Thread  playThread;

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  /**
   * Crea un secuenciador con {@value #STEP_COUNT} pasos vacíos.
   *
   * @param soundEngine Motor de audio compartido
   */
  public Sequencer(final SoundEngine soundEngine) {
    this.soundEngine = soundEngine;
    this.steps = new SequencerStep[STEP_COUNT];
    for (int i = 0; i < STEP_COUNT; i++) {
      this.steps[i] = new SequencerStep();
    }
  }

  // -------------------------------------------------------------------------
  // Control de reproducción
  // -------------------------------------------------------------------------

  /**
   * Inicia la reproducción en bucle desde el paso 0.
   * Si ya está en marcha, no hace nada.
   */
  public void start() {
    if (this.running) {
      return;
    }
    this.running   = true;
    this.playThread = new Thread(this::runLoop, "Sequencer-Thread");
    this.playThread.setDaemon(true);
    this.playThread.start();
  }

  /**
   * Detiene la reproducción y silencia la nota en curso.
   */
  public void stop() {
    this.running = false;
    this.soundEngine.stopNote();
    if (this.playThread != null) {
      try {
        this.playThread.join(500);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      this.playThread = null;
    }
    this.notifyListenersStopped();
  }

  /** @return {@code true} si el secuenciador está en reproducción */
  public boolean isRunning() {
    return this.running;
  }

  // -------------------------------------------------------------------------
  // Bucle principal
  // -------------------------------------------------------------------------

  private void runLoop() {
    int stepIndex = 0;
    while (this.running) {
      final SequencerStep step = this.steps[stepIndex];
      final long stepDurationMs = bpmToStepMs(this.bpm);

      // Notificar a la UI el paso actual
      final int currentStep = stepIndex;
      this.notifyListenersStep(currentStep);

      if (step.isActive()) {
        this.soundEngine.startNote(step.getNote());
        // Sonar el 70% de la duración del paso (gate)
        sleep((long) (stepDurationMs * 0.70));
        this.soundEngine.stopNote();
        sleep((long) (stepDurationMs * 0.30));
      } else {
        this.soundEngine.stopNote();
        sleep(stepDurationMs);
      }

      stepIndex = (stepIndex + 1) % STEP_COUNT;
    }
  }

  // -------------------------------------------------------------------------
  // Configuración
  // -------------------------------------------------------------------------

  /**
   * Cambia el tempo.
   *
   * @param bpm Tempo en pulsaciones por minuto ({@value #BPM_MIN}–{@value #BPM_MAX})
   */
  public void setBpm(final int bpm) {
    this.bpm = Math.max(BPM_MIN, Math.min(BPM_MAX, bpm));
  }

  /** @return Tempo actual en BPM */
  public int getBpm() {
    return this.bpm;
  }

  /**
   * Devuelve el paso en el índice dado.
   *
   * @param index Índice (0 – {@value #STEP_COUNT}-1)
   * @return El paso
   */
  public SequencerStep getStep(final int index) {
    return this.steps[index];
  }

  /**
   * Limpia todos los pasos (los desactiva).
   */
  public void clearAll() {
    for (final SequencerStep step : this.steps) {
      step.setActive(false);
    }
  }

  // -------------------------------------------------------------------------
  // Listeners
  // -------------------------------------------------------------------------

  /**
   * Registra un listener de eventos del secuenciador.
   *
   * @param listener Listener a registrar
   */
  public void addListener(final SequencerListener listener) {
    this.listeners.add(listener);
  }

  private void notifyListenersStep(final int step) {
    for (final SequencerListener l : this.listeners) {
      l.onStep(step);
    }
  }

  private void notifyListenersStopped() {
    for (final SequencerListener l : this.listeners) {
      l.onStopped();
    }
  }

  // -------------------------------------------------------------------------
  // Utilidades
  // -------------------------------------------------------------------------

  /**
   * Convierte BPM en duración de un paso de corchea (1/8) en milisegundos.
   * Un compás de 4/4 tiene 4 negras = 8 corcheas; a 120 BPM → 125 ms/paso.
   *
   * @param bpm Tempo en BPM
   * @return Duración de un paso en ms
   */
  private static long bpmToStepMs(final int bpm) {
    return (long) (60_000.0 / bpm / 2.0);  // corchea = negra / 2
  }

  private static void sleep(final long ms) {
    if (ms <= 0) {
      return;
    }
    try {
      Thread.sleep(ms);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}
