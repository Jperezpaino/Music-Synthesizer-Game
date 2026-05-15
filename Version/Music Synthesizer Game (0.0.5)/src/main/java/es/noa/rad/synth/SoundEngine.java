package es.noa.rad.synth;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Motor de audio del sintetizador.
 * Genera señales de audio digitalmente a partir de frecuencias y formas de onda.
 *
 * <p>Usa {@code javax.sound.sampled} (incluido en el JDK, sin dependencias externas).</p>
 */
public final class SoundEngine {

  /** Tasa de muestreo estándar: 44100 Hz (calidad CD) */
  private static final float SAMPLE_RATE = 44100f;

  /** Tamaño del buffer de audio en bytes */
  private static final int BUFFER_SIZE = 1024;

  /** Volumen por defecto (0.0 - 1.0) */
  private static final double DEFAULT_VOLUME = 0.7;

  /** Duración de ataque/decaimiento en samples para evitar clicks */
  private static final int ENVELOPE_SAMPLES = 200;

  /** Formato de audio: 16 bits, mono, con signo, big-endian */
  private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
      SAMPLE_RATE,
      16,       // bits por sample
      1,        // canales (mono)
      true,     // con signo
      true      // big-endian
  );

  /** Forma de onda actual */
  private Waveform waveform;

  /** Volumen actual (0.0 - 1.0) */
  private double volume;

  /** Hilo de reproducción en curso */
  private Thread playThread;

  /** Señal para detener la nota actual */
  private volatile boolean playing;

  /** Línea de salida de audio */
  private SourceDataLine line;

  /** Tamaño del buffer del osciloscopio (número de samples double) */
  private static final int OSCILLOSCOPE_SIZE = 512;

  /** Buffer circular de samples para el osciloscopio [-1.0, 1.0] */
  private final double[] oscilloscopeBuffer = new double[OSCILLOSCOPE_SIZE];

  /** Índice de escritura en el buffer circular */
  private int oscilloscopeIndex;

  /**
   * Crea un motor de audio con onda sinusoidal y volumen por defecto.
   */
  public SoundEngine() {
    this.waveform = Waveform.SINE;
    this.volume   = DEFAULT_VOLUME;
    this.playing  = false;
  }

  /**
   * Inicia la reproducción continua de una nota.
   * Si ya hay una nota sonando, la detiene primero.
   *
   * @param note Nota a reproducir
   */
  public void startNote(final Note note) {
    this.stopNote();
    this.playing  = true;
    this.playThread = new Thread(() -> this.playLoop(note.getFrequency()), "SoundEngine-Thread");
    this.playThread.setDaemon(true);
    this.playThread.start();
  }

  /**
   * Detiene la nota en reproducción.
   */
  public void stopNote() {
    this.playing = false;
    if (this.playThread != null) {
      try {
        this.playThread.join(300);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      this.playThread = null;
    }
    if (this.line != null) {
      this.line.drain();
      this.line.close();
      this.line = null;
    }
  }

  /**
   * Bucle principal de generación y envío de audio.
   *
   * @param frequency Frecuencia en Hz
   */
  private void playLoop(final double frequency) {
    try {
      this.line = AudioSystem.getSourceDataLine(AUDIO_FORMAT);
      this.line.open(AUDIO_FORMAT, BUFFER_SIZE * 4);
      this.line.start();

      final byte[] buffer = new byte[BUFFER_SIZE];
      long sampleIndex    = 0;

      while (this.playing) {
        for (int i = 0; i < BUFFER_SIZE; i += 2) {
          final double sample = this.generateSample(frequency, sampleIndex);

          // Envolvente de ataque/decaimiento para evitar clicks al inicio
          final double envelope = (sampleIndex < ENVELOPE_SAMPLES)
              ? (double) sampleIndex / ENVELOPE_SAMPLES
              : 1.0;

          final double finalSample = sample * envelope * this.volume;

          // Guardar en buffer circular del osciloscopio
          this.oscilloscopeBuffer[this.oscilloscopeIndex] = finalSample;
          this.oscilloscopeIndex = (this.oscilloscopeIndex + 1) % OSCILLOSCOPE_SIZE;

          final short sampleShort = (short) (finalSample * Short.MAX_VALUE);

          // Big-endian: byte alto primero
          buffer[i]     = (byte) (sampleShort >> 8);
          buffer[i + 1] = (byte) (sampleShort & 0xFF);

          sampleIndex++;
        }
        this.line.write(buffer, 0, buffer.length);
      }

      // Envolvente de decaimiento al soltar la tecla
      this.applyRelease(frequency, sampleIndex);

    } catch (final LineUnavailableException e) {
      System.err.println("[SoundEngine] Error al abrir la línea de audio: " + e.getMessage());
    }
  }

  /**
   * Aplica un decaimiento suave al soltar la tecla para evitar clicks.
   *
   * @param frequency   Frecuencia en Hz
   * @param sampleIndex Índice de sample actual
   */
  private void applyRelease(final double frequency, final long sampleIndex) {
    if (this.line == null) {
      return;
    }
    final byte[] buffer = new byte[ENVELOPE_SAMPLES * 2];
    for (int i = 0; i < ENVELOPE_SAMPLES; i++) {
      final double sample   = this.generateSample(frequency, sampleIndex + i);
      final double fadeOut  = 1.0 - ((double) i / ENVELOPE_SAMPLES);
      final short sampleShort = (short) (sample * fadeOut * this.volume * Short.MAX_VALUE);
      buffer[i * 2]     = (byte) (sampleShort >> 8);
      buffer[i * 2 + 1] = (byte) (sampleShort & 0xFF);
    }
    this.line.write(buffer, 0, buffer.length);
  }

  /**
   * Genera un sample de audio según la forma de onda y frecuencia.
   *
   * @param frequency   Frecuencia en Hz
   * @param sampleIndex Índice del sample
   * @return Valor del sample en rango [-1.0, 1.0]
   */
  private double generateSample(final double frequency, final long sampleIndex) {
    // Fase normalizada entre 0.0 y 1.0
    final double phase = (frequency * sampleIndex / SAMPLE_RATE) % 1.0;

    return switch (this.waveform) {
      case SINE     -> Math.sin(2.0 * Math.PI * phase);
      case SQUARE   -> phase < 0.5 ? 1.0 : -1.0;
      case SAWTOOTH -> 2.0 * phase - 1.0;
      case TRIANGLE -> phase < 0.5
          ? 4.0 * phase - 1.0
          : -4.0 * phase + 3.0;
    };
  }

  /**
   * Cambia la forma de onda del sintetizador.
   *
   * @param waveform Nueva forma de onda
   */
  public void setWaveform(final Waveform waveform) {
    this.waveform = waveform;
  }

  /**
   * Cambia el volumen del sintetizador.
   *
   * @param volume Volumen entre 0.0 (silencio) y 1.0 (máximo)
   */
  public void setVolume(final double volume) {
    this.volume = Math.max(0.0, Math.min(1.0, volume));
  }

  /**
   * @return Forma de onda actual
   */
  public Waveform getWaveform() {
    return this.waveform;
  }

  /**
   * @return Volumen actual (0.0 - 1.0)
   */
  public double getVolume() {
    return this.volume;
  }

  /**
   * Devuelve una copia del buffer del osciloscopio con los últimos samples generados.
   * Los valores están en rango [-1.0, 1.0].
   * El buffer está ordenado cronológicamente: el índice 0 es el sample más antiguo.
   *
   * @return Array de {@value #OSCILLOSCOPE_SIZE} samples
   */
  public double[] getOscilloscopeBuffer() {
    final double[] copy = new double[OSCILLOSCOPE_SIZE];
    final int start = this.oscilloscopeIndex;
    for (int i = 0; i < OSCILLOSCOPE_SIZE; i++) {
      copy[i] = this.oscilloscopeBuffer[(start + i) % OSCILLOSCOPE_SIZE];
    }
    return copy;
  }

  /**
   * @return Tamaño del buffer del osciloscopio
   */
  public static int getOscilloscopeSize() {
    return OSCILLOSCOPE_SIZE;
  }

}
