package es.noa.rad.drumachine;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Motor de la caja de ritmos.
 *
 * <p>Gestiona un patrón de {@value #TRACK_COUNT} pistas × {@value #STEP_COUNT} pasos.
 * Cada hit de percusión se reproduce en su propio hilo daemon para permitir
 * poliritmia simultánea sin bloquear el bucle de tempo.</p>
 */
public final class DrumMachine {

  /** Número de pistas (una por {@link DrumVoice}) */
  public static final int TRACK_COUNT = DrumVoice.values().length; // 4

  /** Pasos por pista */
  public static final int STEP_COUNT  = 16;

  public static final int BPM_MIN     = 40;
  public static final int BPM_MAX     = 240;
  public static final int BPM_DEFAULT = 120;

  private static final float SAMPLE_RATE = 44100f;
  private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
      SAMPLE_RATE, 16, 1, true, true
  );

  // -------------------------------------------------------------------------
  // Interfaz de escucha
  // -------------------------------------------------------------------------

  /**
   * Escucha de eventos de la caja de ritmos.
   */
  public interface DrumMachineListener {
    /**
     * Se invoca justo antes de reproducir el paso indicado.
     *
     * @param stepIndex Índice del paso actual (0–{@value #STEP_COUNT}-1)
     */
    void onBeat(int stepIndex);

    /** Se invoca cuando la reproducción se detiene completamente. */
    void onStopped();
  }

  // -------------------------------------------------------------------------
  // Estado
  // -------------------------------------------------------------------------

  /** Patrón de pasos: [pista][paso] */
  private final boolean[][] pattern = new boolean[TRACK_COUNT][STEP_COUNT];

  /** Volumen por pista (0.0 – 1.0) */
  private final double[] volumes = new double[TRACK_COUNT];

  /** Muestras PCM pre-generadas por voz */
  private final double[][] samples;

  private int     bpm     = BPM_DEFAULT;
  private boolean running = false;
  private Thread  playThread;

  private final List<DrumMachineListener> listeners = new ArrayList<>();

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  /**
   * Crea la caja de ritmos y pre-genera las muestras de cada instrumento.
   */
  public DrumMachine() {
    final DrumVoice[] voices = DrumVoice.values();
    this.samples = new double[TRACK_COUNT][];
    for (int i = 0; i < TRACK_COUNT; i++) {
      this.volumes[i] = 0.8;
      this.samples[i] = voices[i].generateSamples(SAMPLE_RATE);
    }
  }

  // -------------------------------------------------------------------------
  // Control de reproducción
  // -------------------------------------------------------------------------

  /**
   * Inicia la reproducción en bucle.
   */
  public void start() {
    if (this.running) {
      return;
    }
    this.running    = true;
    this.playThread = new Thread(this::runLoop, "DrumMachine-Thread");
    this.playThread.setDaemon(true);
    this.playThread.start();
  }

  /**
   * Detiene la reproducción.
   */
  public void stop() {
    this.running = false;
    if (this.playThread != null) {
      try {
        this.playThread.join(500);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      this.playThread = null;
    }
    this.listeners.forEach(DrumMachineListener::onStopped);
  }

  /** @return {@code true} si está en reproducción */
  public boolean isRunning() {
    return this.running;
  }

  // -------------------------------------------------------------------------
  // Bucle principal
  // -------------------------------------------------------------------------

  private void runLoop() {
    int step = 0;
    while (this.running) {
      final long stepMs = (long) (60_000.0 / this.bpm / 2.0); // corchea
      final int  cur    = step;

      // Notificar UI
      this.listeners.forEach(l -> l.onBeat(cur));

      // Lanzar hits activos en paralelo
      for (int t = 0; t < TRACK_COUNT; t++) {
        if (this.pattern[t][cur]) {
          fireHit(this.samples[t], this.volumes[t]);
        }
      }

      sleep(stepMs);
      step = (step + 1) % STEP_COUNT;
    }
  }

  /**
   * Reproduce un array de muestras en un hilo daemon independiente.
   * Permite solapamiento de varios hits simultáneos.
   *
   * @param samples Muestras PCM [-1.0, 1.0]
   * @param volume  Volumen de reproducción (0.0 – 1.0)
   */
  private static void fireHit(final double[] samples, final double volume) {
    final Thread t = new Thread(() -> {
      try {
        final SourceDataLine line = AudioSystem.getSourceDataLine(AUDIO_FORMAT);
        line.open(AUDIO_FORMAT, samples.length * 2);
        line.start();

        final byte[] buf = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
          final short s = (short) (samples[i] * volume * Short.MAX_VALUE);
          buf[i * 2]     = (byte) (s >> 8);
          buf[i * 2 + 1] = (byte) (s & 0xFF);
        }
        line.write(buf, 0, buf.length);
        line.drain();
        line.close();
      } catch (final LineUnavailableException ex) {
        // Ignorar fallos de audio puntuales en hits
      }
    });
    t.setDaemon(true);
    t.start();
  }

  // -------------------------------------------------------------------------
  // Configuración del patrón
  // -------------------------------------------------------------------------

  /**
   * Alterna el estado de un paso.
   *
   * @param track Índice de pista (0–{@value #TRACK_COUNT}-1)
   * @param step  Índice de paso  (0–{@value #STEP_COUNT}-1)
   */
  public void toggleStep(final int track, final int step) {
    this.pattern[track][step] = !this.pattern[track][step];
  }

  /**
   * @param track Índice de pista
   * @param step  Índice de paso
   * @return {@code true} si el paso está activo
   */
  public boolean getStep(final int track, final int step) {
    return this.pattern[track][step];
  }

  /** Desactiva todos los pasos de todas las pistas. */
  public void clearAll() {
    for (int t = 0; t < TRACK_COUNT; t++) {
      for (int s = 0; s < STEP_COUNT; s++) {
        this.pattern[t][s] = false;
      }
    }
  }

  /** Desactiva todos los pasos de una pista. */
  public void clearTrack(final int track) {
    for (int s = 0; s < STEP_COUNT; s++) {
      this.pattern[track][s] = false;
    }
  }

  // -------------------------------------------------------------------------
  // BPM y volumen
  // -------------------------------------------------------------------------

  /**
   * @param bpm Tempo ({@value #BPM_MIN}–{@value #BPM_MAX})
   */
  public void setBpm(final int bpm) {
    this.bpm = Math.max(BPM_MIN, Math.min(BPM_MAX, bpm));
  }

  /** @return Tempo actual en BPM */
  public int getBpm() {
    return this.bpm;
  }

  /**
   * @param track  Índice de pista
   * @param volume Volumen (0.0 – 1.0)
   */
  public void setVolume(final int track, final double volume) {
    this.volumes[track] = Math.max(0.0, Math.min(1.0, volume));
  }

  /** @return Volumen de la pista */
  public double getVolume(final int track) {
    return this.volumes[track];
  }

  // -------------------------------------------------------------------------
  // Listeners
  // -------------------------------------------------------------------------

  /**
   * Registra un listener de eventos.
   *
   * @param listener Listener a añadir
   */
  public void addListener(final DrumMachineListener listener) {
    this.listeners.add(listener);
  }

  // -------------------------------------------------------------------------
  private static void sleep(final long ms) {
    try {
      Thread.sleep(ms);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}
