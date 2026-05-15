package es.noa.rad.melody;

import es.noa.rad.synth.SoundEngine;

/**
 * Reproduce una {@link Melody} grabada de forma fiel al original.
 *
 * <p>Usa un hilo daemon para no bloquear la UI.
 * Respeta los tiempos de inicio y duración de cada nota,
 * incluyendo silencios entre notas simultáneas o superpuestas.</p>
 */
public final class MelodyPlayer {

  /** Motor de audio que genera el sonido */
  private final SoundEngine soundEngine;

  /** Melodía actualmente en reproducción */
  private Thread playThread;

  /** Señal para detener la reproducción */
  private volatile boolean playing;

  /** Listener que recibe eventos de reproducción */
  private PlaybackListener listener;

  /**
   * Interfaz para recibir notificaciones de inicio/fin de reproducción.
   */
  public interface PlaybackListener {
    /** Llamado cuando empieza la reproducción */
    void onPlaybackStarted();
    /** Llamado cuando termina o se detiene la reproducción */
    void onPlaybackStopped();
  }

  /**
   * Crea el reproductor de melodías.
   *
   * @param soundEngine Motor de audio a usar
   */
  public MelodyPlayer(final SoundEngine soundEngine) {
    this.soundEngine = soundEngine;
  }

  /**
   * Asigna un listener de eventos de reproducción.
   *
   * @param listener Listener a notificar
   */
  public void setListener(final PlaybackListener listener) {
    this.listener = listener;
  }

  /**
   * Inicia la reproducción de la melodía.
   * Si ya hay una reproducción activa, la detiene primero.
   *
   * @param melody Melodía a reproducir
   */
  public void play(final Melody melody) {
    if (melody.isEmpty()) {
      return;
    }
    this.stop();
    this.playing  = true;
    this.playThread = new Thread(() -> this.playLoop(melody), "MelodyPlayer-Thread");
    this.playThread.setDaemon(true);
    this.playThread.start();
  }

  /**
   * Detiene la reproducción en curso.
   */
  public void stop() {
    this.playing = false;
    this.soundEngine.stopNote();
    if (this.playThread != null) {
      try {
        this.playThread.join(500);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      this.playThread = null;
    }
  }

  /**
   * @return true si hay una melodía reproduciéndose
   */
  public boolean isPlaying() {
    return this.playing;
  }

  /**
   * Bucle principal de reproducción.
   * Itera las notas en orden, esperando el tiempo adecuado entre ellas.
   *
   * @param melody Melodía a reproducir
   */
  private void playLoop(final Melody melody) {
    if (this.listener != null) {
      this.listener.onPlaybackStarted();
    }

    final long startTime = System.currentTimeMillis();

    for (final MelodyNote melodyNote : melody.getNotes()) {
      if (!this.playing) {
        break;
      }

      // Esperar hasta el momento en que empieza esta nota
      final long targetTime = startTime + melodyNote.getStartTimeMs();
      final long waitMs     = targetTime - System.currentTimeMillis();
      if (waitMs > 0) {
        try {
          Thread.sleep(waitMs);
        } catch (final InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }

      if (!this.playing) {
        break;
      }

      // Cambiar forma de onda y tocar la nota
      this.soundEngine.setWaveform(melodyNote.getWaveform());
      this.soundEngine.startNote(melodyNote.getNote());

      // Mantener la nota el tiempo grabado
      try {
        Thread.sleep(melodyNote.getDurationMs());
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }

      this.soundEngine.stopNote();
    }

    this.playing = false;
    if (this.listener != null) {
      this.listener.onPlaybackStopped();
    }
  }

}
