package es.noa.rad.synth;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import es.noa.rad.fx.DelayEffect;
import es.noa.rad.fx.DistortionEffect;
import es.noa.rad.fx.EffectChain;
import es.noa.rad.fx.ReverbEffect;

/**
 * Motor de audio polifónico del sintetizador.
 *
 * <p>Mantiene un pool de {@value #VOICE_COUNT} voces {@link PolyVoice}.
 * Cuando se solicita una nota nueva se busca una voz libre; si no hay,
 * se roba la que lleva más tiempo activa (estrategia LRU).
 * Todas las voces activas se mezclan y la señal resultante pasa por la
 * cadena de efectos antes de escribirse en la línea de salida.</p>
 *
 * <p>Un único hilo daemon mezcla el audio continuamente en bloques de
 * {@value #BUFFER_SAMPLES} samples. La línea de salida permanece abierta
 * mientras el motor exista, lo que minimiza la latencia.</p>
 */
public final class SoundEngine {

  /** Número de voces del pool polifónico */
  public static final int VOICE_COUNT = 8;

  /** Tasa de muestreo: 44 100 Hz */
  private static final float SAMPLE_RATE = 44100f;

  /** Samples por buffer de mezcla */
  private static final int BUFFER_SAMPLES = 512;

  /** Bytes por buffer (16 bits, mono) */
  private static final int BUFFER_BYTES = BUFFER_SAMPLES * 2;

  /** Tamaño de la línea de salida en bytes */
  private static final int LINE_BUFFER = BUFFER_BYTES * 4;

  /** Volumen por defecto */
  private static final double DEFAULT_VOLUME = 0.7;

  /** Formato de audio: 16 bits, mono, con signo, big-endian */
  private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
      SAMPLE_RATE, 16, 1, true, true
  );

  /** Tamaño del buffer circular del osciloscopio */
  private static final int OSCILLOSCOPE_SIZE = 512;

  // -------------------------------------------------------------------------
  // Pool de voces
  // -------------------------------------------------------------------------
  private final PolyVoice[] voices = new PolyVoice[VOICE_COUNT];

  // -------------------------------------------------------------------------
  // Parametros de sintesis
  // -------------------------------------------------------------------------
  private InstrumentPreset preset;
  private Waveform          waveform;
  private double            volume;

  private int    attackSamples;
  private int    decaySamples;
  private double sustainLevel;
  private int    releaseSamples;

  // -------------------------------------------------------------------------
  // Audio output
  // -------------------------------------------------------------------------
  private SourceDataLine   line;
  private Thread           mixThread;
  private volatile boolean running = false;

  // -------------------------------------------------------------------------
  // Osciloscopio
  // -------------------------------------------------------------------------
  private final double[] oscilloscopeBuffer = new double[OSCILLOSCOPE_SIZE];
  private int oscilloscopeIndex;

  // -------------------------------------------------------------------------
  // Efectos
  // -------------------------------------------------------------------------
  private final EffectChain      effectChain;
  private final DelayEffect      delayEffect;
  private final ReverbEffect     reverbEffect;
  private final DistortionEffect distortionEffect;

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  /**
   * Crea el motor de audio. Abre la linea de salida e inicia el hilo de mezcla.
   */
  public SoundEngine() {
    this.volume = DEFAULT_VOLUME;
    this.applyPreset(InstrumentPreset.PIANO);

    for (int i = 0; i < VOICE_COUNT; i++) {
      this.voices[i] = new PolyVoice();
    }

    this.delayEffect      = new DelayEffect(false, 0.35, 0.4, 0.35);
    this.reverbEffect     = new ReverbEffect(false, 0.6, 0.35);
    this.distortionEffect = new DistortionEffect(false, 4.0, 0.6);

    this.effectChain = new EffectChain();
    this.effectChain.add(this.distortionEffect);
    this.effectChain.add(this.delayEffect);
    this.effectChain.add(this.reverbEffect);

    this.startMixThread();
  }

  // -------------------------------------------------------------------------
  // API publica
  // -------------------------------------------------------------------------

  /**
   * Activa una nueva voz para la nota dada.
   * Si no hay voces libres, roba la que lleva mas tiempo activa (LRU).
   *
   * @param note Nota a reproducir
   */
  public void startNote(final Note note) {
    final PolyVoice voice = allocateVoice();
    voice.trigger(
        note.getFrequency(),
        this.waveform,
        this.attackSamples,
        this.decaySamples,
        this.sustainLevel,
        this.releaseSamples
    );
  }

  /**
   * Inicia el release de la voz que esta reproduciendo la nota dada.
   *
   * @param note Nota a soltar
   */
  public void stopNote(final Note note) {
    final double freq = note.getFrequency();
    for (final PolyVoice v : this.voices) {
      if (v.isActive() && Math.abs(v.getFrequency() - freq) < 0.01) {
        v.release();
        return;
      }
    }
  }

  /**
   * Inicia el release de todas las voces activas.
   * Compatibilidad con la API monofonica anterior.
   */
  public void stopNote() {
    for (final PolyVoice v : this.voices) {
      if (v.isActive()) {
        v.release();
      }
    }
  }

  // -------------------------------------------------------------------------
  // Configuracion
  // -------------------------------------------------------------------------

  /** @param waveform Nueva forma de onda */
  public void setWaveform(final Waveform waveform) {
    this.waveform = waveform;
  }

  /** @param volume Volumen (0.0 - 1.0) */
  public void setVolume(final double volume) {
    this.volume = Math.max(0.0, Math.min(1.0, volume));
  }

  /** @param preset Preset de instrumento */
  public void setPreset(final InstrumentPreset preset) {
    this.applyPreset(preset);
  }

  // -------------------------------------------------------------------------
  // Getters
  // -------------------------------------------------------------------------

  /** @return Forma de onda actual */
  public Waveform getWaveform() { return this.waveform; }

  /** @return Volumen actual (0.0 - 1.0) */
  public double getVolume()     { return this.volume; }

  /** @return Preset activo */
  public InstrumentPreset getPreset() { return this.preset; }

  /** @return Efecto de delay */
  public DelayEffect getDelayEffect()           { return this.delayEffect; }

  /** @return Efecto de reverberacion */
  public ReverbEffect getReverbEffect()         { return this.reverbEffect; }

  /** @return Efecto de distorsion */
  public DistortionEffect getDistortionEffect() { return this.distortionEffect; }

  /**
   * Devuelve una copia del buffer del osciloscopio ordenada cronologicamente.
   *
   * @return Array de {@value #OSCILLOSCOPE_SIZE} samples en [-1.0, 1.0]
   */
  public double[] getOscilloscopeBuffer() {
    final double[] copy  = new double[OSCILLOSCOPE_SIZE];
    final int      start = this.oscilloscopeIndex;
    for (int i = 0; i < OSCILLOSCOPE_SIZE; i++) {
      copy[i] = this.oscilloscopeBuffer[(start + i) % OSCILLOSCOPE_SIZE];
    }
    return copy;
  }

  /** @return Tamano del buffer del osciloscopio */
  public static int getOscilloscopeSize() { return OSCILLOSCOPE_SIZE; }

  // -------------------------------------------------------------------------
  // Hilo de mezcla
  // -------------------------------------------------------------------------

  private void startMixThread() {
    try {
      this.line = AudioSystem.getSourceDataLine(AUDIO_FORMAT);
      this.line.open(AUDIO_FORMAT, LINE_BUFFER);
      this.line.start();
    } catch (final LineUnavailableException e) {
      System.err.println("[SoundEngine] No se pudo abrir la linea de audio: " + e.getMessage());
      return;
    }

    this.running   = true;
    this.mixThread = new Thread(this::mixLoop, "SoundEngine-MixThread");
    this.mixThread.setDaemon(true);
    this.mixThread.start();
  }

  /**
   * Bucle de mezcla: genera {@value #BUFFER_SAMPLES} samples por iteracion,
   * mezclando todas las voces activas antes de aplicar los FX.
   */
  private void mixLoop() {
    final byte[] buf = new byte[BUFFER_BYTES];

    while (this.running) {
      for (int i = 0; i < BUFFER_SAMPLES; i++) {
        double mix         = 0.0;
        int    activeCount = 0;
        for (final PolyVoice v : this.voices) {
          if (v.isActive()) {
            mix += v.nextSample();
            activeCount++;
          }
        }

        if (activeCount > 1) {
          mix /= Math.sqrt(activeCount);
        }

        final double processed = this.effectChain.process(mix * this.volume);

        this.oscilloscopeBuffer[this.oscilloscopeIndex] = processed;
        this.oscilloscopeIndex = (this.oscilloscopeIndex + 1) % OSCILLOSCOPE_SIZE;

        final short pcm = (short) Math.max(Short.MIN_VALUE,
                                  Math.min(Short.MAX_VALUE,
                                           (int) (processed * Short.MAX_VALUE)));
        buf[i * 2]     = (byte) (pcm >> 8);
        buf[i * 2 + 1] = (byte) (pcm & 0xFF);
      }

      this.line.write(buf, 0, BUFFER_BYTES);
    }
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  /**
   * Busca una voz libre en el pool. Si no hay ninguna, roba la mas antigua (LRU).
   *
   * @return Voz lista para ser disparada
   */
  private PolyVoice allocateVoice() {
    for (final PolyVoice v : this.voices) {
      if (!v.isActive()) {
        return v;
      }
    }
    PolyVoice oldest = this.voices[0];
    for (int i = 1; i < VOICE_COUNT; i++) {
      if (this.voices[i].getLastUsedAt() < oldest.getLastUsedAt()) {
        oldest = this.voices[i];
      }
    }
    oldest.forceStop();
    return oldest;
  }

  /** Aplica un preset al motor (waveform + ADSR). */
  private void applyPreset(final InstrumentPreset preset) {
    this.preset         = preset;
    this.waveform       = preset.getWaveform();
    this.attackSamples  = preset.getAttackSamples();
    this.decaySamples   = preset.getDecaySamples();
    this.sustainLevel   = preset.getSustainLevel();
    this.releaseSamples = preset.getReleaseSamples();
  }

}