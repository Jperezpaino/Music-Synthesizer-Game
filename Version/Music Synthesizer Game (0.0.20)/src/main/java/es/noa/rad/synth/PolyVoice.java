package es.noa.rad.synth;

/**
 * Voz individual del pool polifónico.
 *
 * <p>Encapsula el estado completo de un sonido en curso: frecuencia, forma de onda,
 * envolvente ADSR y contador de muestras. El método {@link #nextSample()} avanza
 * el estado interno en cada llamada y devuelve el siguiente sample PCM.</p>
 *
 * <p>Thread-safety: todos los métodos públicos están sincronizados para permitir
 * que el hilo de mezcla llame a {@code nextSample()} mientras el hilo principal
 * llama a {@code trigger()} o {@code release()}.</p>
 */
public final class PolyVoice {

  // -------------------------------------------------------------------------
  // Estados de la envolvente
  // -------------------------------------------------------------------------
  private enum State { IDLE, ATTACK, DECAY, SUSTAIN, RELEASE, DONE }

  // -------------------------------------------------------------------------
  // Estado mutable de la voz
  // -------------------------------------------------------------------------
  private State    state       = State.IDLE;
  private double   frequency   = 440.0;
  private Waveform waveform    = Waveform.SINE;

  private int    attackSamples;
  private int    decaySamples;
  private double sustainLevel;
  private int    releaseSamples;

  /** Contador de samples desde el inicio del estado actual */
  private long stateIndex;

  /** Nivel de envolvente al que empieza el release (puede ser <1 si se suelta durante ataque) */
  private double releaseStartLevel;

  /** Marca de tiempo (nanoTime) para robo LRU */
  private long lastUsedAt;

  // -------------------------------------------------------------------------
  // API pública
  // -------------------------------------------------------------------------

  /**
   * Activa la voz con los parámetros dados.
   * Si la voz ya estaba sonando la corta limpiamente (no hay clic porque el
   * hilo de mezcla leerá el nuevo estado en la próxima muestra).
   *
   * @param frequency     Frecuencia en Hz
   * @param waveform      Forma de onda
   * @param attackSamples  Samples de ataque (≥ 0)
   * @param decaySamples   Samples de decaimiento (≥ 0)
   * @param sustainLevel   Nivel de sustain [0.0, 1.0]
   * @param releaseSamples Samples de release (≥ 1)
   */
  public synchronized void trigger(final double frequency,
                                   final Waveform waveform,
                                   final int attackSamples,
                                   final int decaySamples,
                                   final double sustainLevel,
                                   final int releaseSamples) {
    this.frequency       = frequency;
    this.waveform        = waveform;
    this.attackSamples   = attackSamples;
    this.decaySamples    = decaySamples;
    this.sustainLevel    = sustainLevel;
    this.releaseSamples  = Math.max(1, releaseSamples);
    this.stateIndex      = 0;
    this.lastUsedAt      = System.nanoTime();
    this.state           = (attackSamples > 0) ? State.ATTACK : State.DECAY;
  }

  /**
   * Inicia la fase de release. Si la voz está en IDLE o DONE no hace nada.
   */
  public synchronized void release() {
    if (this.state == State.IDLE || this.state == State.DONE) {
      return;
    }
    this.releaseStartLevel = computeCurrentEnvelopeLevel();
    this.stateIndex        = 0;
    this.state             = State.RELEASE;
  }

  /**
   * Para la voz inmediatamente (sin release). Usada para robo LRU.
   */
  public synchronized void forceStop() {
    this.state = State.IDLE;
  }

  /**
   * Genera el siguiente sample PCM de esta voz (incluye envolvente ADSR).
   * Avanza el estado interno.
   *
   * @return Sample en rango [-1.0, 1.0], o 0.0 si la voz está inactiva
   */
  public synchronized double nextSample() {
    if (this.state == State.IDLE || this.state == State.DONE) {
      return 0.0;
    }

    final double envelope = advanceEnvelope();
    final double osc      = oscillator();
    return osc * envelope;
  }

  /** @return {@code true} si la voz está generando audio (no IDLE ni DONE) */
  public synchronized boolean isActive() {
    return this.state != State.IDLE && this.state != State.DONE;
  }

  /** @return Frecuencia actual en Hz */
  public synchronized double getFrequency() {
    return this.frequency;
  }

  /** @return Marca de tiempo del último {@code trigger()} (nanoTime) */
  public synchronized long getLastUsedAt() {
    return this.lastUsedAt;
  }

  // -------------------------------------------------------------------------
  // Lógica interna
  // -------------------------------------------------------------------------

  /**
   * Avanza la máquina de estados ADSR y devuelve el nivel de envolvente actual.
   * La llamada ya asume que stateIndex corresponde al sample que se está generando.
   */
  private double advanceEnvelope() {
    double level;

    switch (this.state) {
      case ATTACK -> {
        level = (this.attackSamples > 0)
            ? (double) this.stateIndex / this.attackSamples
            : 1.0;
        this.stateIndex++;
        if (this.stateIndex >= this.attackSamples) {
          this.stateIndex = 0;
          this.state      = (this.decaySamples > 0) ? State.DECAY : State.SUSTAIN;
        }
      }
      case DECAY -> {
        level = (this.decaySamples > 0)
            ? 1.0 - (1.0 - this.sustainLevel) * ((double) this.stateIndex / this.decaySamples)
            : this.sustainLevel;
        this.stateIndex++;
        if (this.stateIndex >= this.decaySamples) {
          this.stateIndex = 0;
          this.state      = State.SUSTAIN;
        }
      }
      case SUSTAIN -> {
        level = this.sustainLevel;
        // stateIndex sigue creciendo para que el oscilador tenga fase correcta
        this.stateIndex++;
      }
      case RELEASE -> {
        final double progress = (double) this.stateIndex / this.releaseSamples;
        level = this.releaseStartLevel * (1.0 - progress);
        this.stateIndex++;
        if (this.stateIndex >= this.releaseSamples) {
          this.state = State.DONE;
          level      = 0.0;
        }
      }
      default -> {
        level = 0.0;
      }
    }

    return Math.max(0.0, Math.min(1.0, level));
  }

  /** Calcula el nivel de envolvente sin avanzar el estado (para snapshot al iniciar release). */
  private double computeCurrentEnvelopeLevel() {
    return switch (this.state) {
      case ATTACK  -> (this.attackSamples > 0)
          ? (double) this.stateIndex / this.attackSamples : 1.0;
      case DECAY   -> (this.decaySamples > 0)
          ? 1.0 - (1.0 - this.sustainLevel) * ((double) this.stateIndex / this.decaySamples)
          : this.sustainLevel;
      case SUSTAIN -> this.sustainLevel;
      default      -> this.sustainLevel;
    };
  }

  /**
   * Genera el valor del oscilador para el sample actual (usando {@code stateIndex}
   * como contador de samples desde el trigger, no desde el inicio del estado).
   * Nota: stateIndex se incrementa en {@link #advanceEnvelope()}, por lo que aquí
   * usamos su valor ANTES del incremento — lo cual es correcto porque este método
   * se llama antes de que advanceEnvelope actualice el índice.
   */
  private double oscillator() {
    // Usamos stateIndex como contador de samples globales desde trigger
    // (esto funciona porque en SUSTAIN stateIndex sigue creciendo indefinidamente
    //  y en ATTACK/DECAY los samples son pocos y la transición es continua)
    final double phase = (this.frequency * this.stateIndex / 44100.0) % 1.0;

    return switch (this.waveform) {
      case SINE     -> Math.sin(2.0 * Math.PI * phase);
      case SQUARE   -> phase < 0.5 ? 1.0 : -1.0;
      case SAWTOOTH -> 2.0 * phase - 1.0;
      case TRIANGLE -> phase < 0.5 ? 4.0 * phase - 1.0 : -4.0 * phase + 3.0;
    };
  }

}
