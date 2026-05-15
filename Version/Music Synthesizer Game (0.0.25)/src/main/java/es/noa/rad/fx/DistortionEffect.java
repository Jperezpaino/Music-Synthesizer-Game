package es.noa.rad.fx;

/**
 * Efecto de distorsión con saturación suave (soft clipping) y dura (hard clipping).
 *
 * <p>La cantidad de {@code drive} amplifica la señal antes del clipping,
 * produciendo desde una saturación suave hasta una distorsión agresiva.</p>
 */
public final class DistortionEffect implements AudioEffect {

  private boolean enabled;

  /** Ganancia de entrada antes del clipping (1.0 – 20.0) */
  private double drive;

  /** Mezcla wet/dry (0.0 – 1.0) */
  private double mix;

  /**
   * Crea un efecto de distorsión.
   *
   * @param enabled Si el efecto empieza activo
   * @param drive   Drive inicial (1.0 = limpio, 20.0 = distorsión máxima)
   * @param mix     Mezcla wet/dry inicial (0.0 – 1.0)
   */
  public DistortionEffect(final boolean enabled, final double drive, final double mix) {
    this.enabled = enabled;
    this.drive   = Math.max(1.0, Math.min(20.0, drive));
    this.mix     = Math.max(0.0, Math.min(1.0, mix));
  }

  @Override
  public double process(final double sample) {
    if (!this.enabled) {
      return sample;
    }

    // Amplificar con el drive
    final double amplified = sample * this.drive;

    // Soft clipping: tanh normalizado a [-1, 1]
    final double distorted = Math.tanh(amplified);

    return (1.0 - this.mix) * sample + this.mix * distorted;
  }

  @Override public boolean isEnabled()                  { return this.enabled; }
  @Override public void    setEnabled(final boolean e)  { this.enabled = e; }
  @Override public String  getName()                    { return "Distorsión"; }

  /**
   * Ajusta el nivel de drive.
   *
   * @param drive Drive (1.0 – 20.0)
   */
  public void setDrive(final double drive) {
    this.drive = Math.max(1.0, Math.min(20.0, drive));
  }

  /**
   * Ajusta la mezcla wet/dry.
   *
   * @param mix Mezcla (0.0 = sólo seco, 1.0 = sólo distorsión)
   */
  public void setMix(final double mix) {
    this.mix = Math.max(0.0, Math.min(1.0, mix));
  }

  public double getDrive() { return this.drive; }
  public double getMix()   { return this.mix; }

}
