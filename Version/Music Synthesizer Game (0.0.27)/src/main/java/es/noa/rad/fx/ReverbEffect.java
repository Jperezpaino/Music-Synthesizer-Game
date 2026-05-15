package es.noa.rad.fx;

/**
 * Efecto de reverberación basado en el modelo de Schroeder.
 *
 * <p>Arquitectura clásica: 4 filtros comb en paralelo seguidos de
 * 2 filtros allpass en serie. Produce una reverberación densa y natural.</p>
 */
public final class ReverbEffect implements AudioEffect {

  private static final float SAMPLE_RATE = 44100f;

  // ---- Tiempos de los filtros comb (en ms) — primos entre sí ----
  private static final double[] COMB_TIMES_MS = { 29.7, 37.1, 41.1, 43.7 };

  // ---- Tiempos de los filtros allpass (en ms) ----
  private static final double[] ALLPASS_TIMES_MS = { 5.0, 1.7 };

  private boolean enabled;

  /** Tiempo de caída: controla el feedback de los combs (0.0 – 1.0) */
  private double roomSize;

  /** Mezcla wet/dry (0.0 = sólo seco, 1.0 = sólo reverb) */
  private double mix;

  // Buffers de los filtros comb
  private final double[][] combBuffers;
  private final int[]      combLengths;
  private final int[]      combIndices;
  private final double[]   combFilters;   // filtro paso bajo integrado en cada comb

  // Buffers de los filtros allpass
  private final double[][] allpassBuffers;
  private final int[]      allpassLengths;
  private final int[]      allpassIndices;

  /** Coeficiente del filtro paso bajo integrado en los combs */
  private static final double DAMP = 0.4;

  /**
   * Crea un efecto de reverberación.
   *
   * @param enabled  Si el efecto empieza activo
   * @param roomSize Tamaño de sala inicial (0.0 – 1.0)
   * @param mix      Mezcla wet/dry inicial (0.0 – 1.0)
   */
  public ReverbEffect(final boolean enabled, final double roomSize, final double mix) {
    this.enabled  = enabled;
    this.roomSize = Math.max(0.0, Math.min(1.0, roomSize));
    this.mix      = Math.max(0.0, Math.min(1.0, mix));

    // Inicializar filtros comb
    this.combBuffers  = new double[COMB_TIMES_MS.length][];
    this.combLengths  = new int[COMB_TIMES_MS.length];
    this.combIndices  = new int[COMB_TIMES_MS.length];
    this.combFilters  = new double[COMB_TIMES_MS.length];
    for (int i = 0; i < COMB_TIMES_MS.length; i++) {
      this.combLengths[i]  = (int) (COMB_TIMES_MS[i] * SAMPLE_RATE / 1000.0);
      this.combBuffers[i]  = new double[this.combLengths[i]];
      this.combIndices[i]  = 0;
      this.combFilters[i]  = 0.0;
    }

    // Inicializar filtros allpass
    this.allpassBuffers  = new double[ALLPASS_TIMES_MS.length][];
    this.allpassLengths  = new int[ALLPASS_TIMES_MS.length];
    this.allpassIndices  = new int[ALLPASS_TIMES_MS.length];
    for (int i = 0; i < ALLPASS_TIMES_MS.length; i++) {
      this.allpassLengths[i] = (int) (ALLPASS_TIMES_MS[i] * SAMPLE_RATE / 1000.0);
      this.allpassBuffers[i] = new double[this.allpassLengths[i]];
      this.allpassIndices[i] = 0;
    }
  }

  @Override
  public double process(final double sample) {
    if (!this.enabled) {
      return sample;
    }

    // Feedback de los combs a partir del roomSize
    final double feedback = 0.5 + this.roomSize * 0.45;

    // Suma de las salidas de los 4 filtros comb (en paralelo)
    double output = 0.0;
    for (int i = 0; i < this.combLengths.length; i++) {
      final double delayed = this.combBuffers[i][this.combIndices[i]];
      // Filtro paso-bajo integrado (amortiguación de altas frecuencias)
      this.combFilters[i] = delayed * (1.0 - DAMP) + this.combFilters[i] * DAMP;
      this.combBuffers[i][this.combIndices[i]] = sample + this.combFilters[i] * feedback;
      this.combIndices[i] = (this.combIndices[i] + 1) % this.combLengths[i];
      output += delayed;
    }
    output /= this.combLengths.length;

    // 2 filtros allpass en serie
    for (int i = 0; i < this.allpassLengths.length; i++) {
      final double bufOut = this.allpassBuffers[i][this.allpassIndices[i]];
      final double newIn  = output + bufOut * 0.5;
      this.allpassBuffers[i][this.allpassIndices[i]] = newIn;
      this.allpassIndices[i] = (this.allpassIndices[i] + 1) % this.allpassLengths[i];
      output = bufOut - output;
    }

    return clamp((1.0 - this.mix) * sample + this.mix * output);
  }

  @Override public boolean isEnabled()                  { return this.enabled; }
  @Override public void    setEnabled(final boolean e)  { this.enabled = e; }
  @Override public String  getName()                    { return "Reverb"; }

  /**
   * Ajusta el tamaño de sala (controla el tiempo de caída).
   *
   * @param roomSize Tamaño (0.0 = sala muy pequeña, 1.0 = gran recinto)
   */
  public void setRoomSize(final double roomSize) {
    this.roomSize = Math.max(0.0, Math.min(1.0, roomSize));
  }

  /**
   * Ajusta la mezcla wet/dry.
   *
   * @param mix Mezcla (0.0 = sólo seco, 1.0 = sólo reverb)
   */
  public void setMix(final double mix) {
    this.mix = Math.max(0.0, Math.min(1.0, mix));
  }

  public double getRoomSize() { return this.roomSize; }
  public double getMix()      { return this.mix; }

  private static double clamp(final double v) {
    return Math.max(-1.0, Math.min(1.0, v));
  }

}
