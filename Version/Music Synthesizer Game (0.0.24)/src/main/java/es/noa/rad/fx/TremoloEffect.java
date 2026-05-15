package es.noa.rad.fx;

/**
 * Efecto Tremolo: modula el volumen con un Oscilador de Baja Frecuencia (LFO).
 *
 * <p>El LFO genera una onda seno lenta que sube y baja el volumen de forma
 * continua, produciendo el efecto de "pulso" o "vibrado en el volumen"
 * caracteristico de musica clasica y rock antiguo.</p>
 *
 * <p>Parametros:</p>
 * <ul>
 *   <li><b>Rate</b>: velocidad del pulso en Hz (0.1 = muy lento, 10 = muy rapido)</li>
 *   <li><b>Depth</b>: intensidad del efecto (0.0 = sin efecto, 1.0 = volumen
 *       oscila entre 0 % y 100 %)</li>
 * </ul>
 */
public final class TremoloEffect implements AudioEffect {

  private static final float SAMPLE_RATE = 44100f;

  private boolean enabled;
  private double  rate;   // Hz
  private double  depth;  // 0.0 – 1.0

  private long sampleIndex = 0L;

  /**
   * Crea el efecto tremolo.
   *
   * @param enabled Si empieza activo
   * @param rate    Frecuencia del LFO en Hz (0.1 - 10.0)
   * @param depth   Profundidad (0.0 - 1.0)
   */
  public TremoloEffect(final boolean enabled, final double rate, final double depth) {
    this.enabled = enabled;
    this.rate    = Math.max(0.1, Math.min(10.0, rate));
    this.depth   = Math.max(0.0, Math.min(1.0,  depth));
  }

  @Override
  public double process(final double sample) {
    if (!this.enabled) {
      return sample;
    }
    // LFO: seno centrado en 1.0, oscila en [1-depth, 1.0]
    final double lfo = 1.0 - this.depth * 0.5
        * (1.0 - Math.sin(2.0 * Math.PI * this.rate * this.sampleIndex / SAMPLE_RATE));
    this.sampleIndex++;
    return sample * lfo;
  }

  @Override
  public boolean isEnabled() { return this.enabled; }

  @Override
  public void setEnabled(final boolean enabled) { this.enabled = enabled; }

  @Override
  public String getName() { return "Tremolo"; }

  // -------------------------------------------------------------------------
  // Getters y setters de parametros
  // -------------------------------------------------------------------------

  /** @param rate Frecuencia del LFO en Hz (0.1 - 10.0) */
  public void setRate(final double rate) {
    this.rate = Math.max(0.1, Math.min(10.0, rate));
  }

  /** @param depth Profundidad del efecto (0.0 - 1.0) */
  public void setDepth(final double depth) {
    this.depth = Math.max(0.0, Math.min(1.0, depth));
  }

  /** @return Frecuencia actual del LFO en Hz */
  public double getRate()  { return this.rate; }

  /** @return Profundidad actual del efecto */
  public double getDepth() { return this.depth; }
}
