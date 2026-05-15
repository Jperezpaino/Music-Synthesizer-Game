package es.noa.rad.fx;

/**
 * Efecto de delay (eco) con control de tiempo, feedback y mezcla wet/dry.
 *
 * <p>Usa un buffer circular de muestras. Cada sample de entrada se mezcla
 * con la señal retardada multiplicada por el factor de feedback.</p>
 */
public final class DelayEffect implements AudioEffect {

  private static final float SAMPLE_RATE = 44100f;

  /** Tiempo máximo de delay soportado: 1 segundo */
  private static final int MAX_DELAY_SAMPLES = (int) SAMPLE_RATE;

  private boolean enabled;

  /** Tiempo de delay en segundos (0.0 – 1.0) */
  private double delayTime;

  /** Retroalimentación: cuánto del eco vuelve a entrar (0.0 – 0.95) */
  private double feedback;

  /** Mezcla entre señal original (0.0) y señal con eco (1.0) */
  private double mix;

  /** Buffer circular para el delay */
  private final double[] buffer = new double[MAX_DELAY_SAMPLES];

  /** Índice de escritura en el buffer */
  private int writeIndex;

  /**
   * Crea un efecto de delay con parámetros por defecto.
   *
   * @param enabled   Si el efecto empieza activo
   * @param delayTime Tiempo de delay inicial en segundos (0.0 – 1.0)
   * @param feedback  Factor de feedback inicial (0.0 – 0.95)
   * @param mix       Mezcla wet/dry inicial (0.0 – 1.0)
   */
  public DelayEffect(final boolean enabled,
                     final double delayTime,
                     final double feedback,
                     final double mix) {
    this.enabled   = enabled;
    this.delayTime = delayTime;
    this.feedback  = feedback;
    this.mix       = mix;
  }

  @Override
  public double process(final double sample) {
    if (!this.enabled) {
      return sample;
    }

    final int delaySamples = (int) (this.delayTime * SAMPLE_RATE);
    final int readIndex    = Math.floorMod(this.writeIndex - delaySamples, MAX_DELAY_SAMPLES);
    final double delayed   = this.buffer[readIndex];

    // Escribir en el buffer: señal directa + feedback del eco
    this.buffer[this.writeIndex] = sample + delayed * this.feedback;
    this.writeIndex = (this.writeIndex + 1) % MAX_DELAY_SAMPLES;

    // Mezcla wet/dry
    return clamp((1.0 - this.mix) * sample + this.mix * delayed);
  }

  @Override public boolean isEnabled()                  { return this.enabled; }
  @Override public void    setEnabled(final boolean e)  { this.enabled = e; }
  @Override public String  getName()                    { return "Delay"; }

  /**
   * Establece el tiempo de delay.
   *
   * @param seconds Tiempo en segundos (0.0 – 1.0)
   */
  public void setDelayTime(final double seconds) {
    this.delayTime = Math.max(0.01, Math.min(1.0, seconds));
  }

  /**
   * Establece el factor de feedback.
   *
   * @param feedback Feedback (0.0 – 0.95)
   */
  public void setFeedback(final double feedback) {
    this.feedback = Math.max(0.0, Math.min(0.95, feedback));
  }

  /**
   * Establece la mezcla wet/dry.
   *
   * @param mix Mezcla (0.0 = sólo seco, 1.0 = sólo eco)
   */
  public void setMix(final double mix) {
    this.mix = Math.max(0.0, Math.min(1.0, mix));
  }

  public double getDelayTime() { return this.delayTime; }
  public double getFeedback()  { return this.feedback; }
  public double getMix()       { return this.mix; }

  private static double clamp(final double v) {
    return Math.max(-1.0, Math.min(1.0, v));
  }

}
