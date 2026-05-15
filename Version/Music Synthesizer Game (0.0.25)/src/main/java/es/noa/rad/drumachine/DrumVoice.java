package es.noa.rad.drumachine;

import java.awt.Color;
import java.util.Random;

/**
 * Instrumentos de percusión con síntesis DSP pura (sin samples).
 *
 * <p>Cada voz genera sus propias muestras PCM mediante síntesis:
 * <ul>
 *   <li><b>KICK</b>  — seno con pitch-bend descendente exponencial (150→40 Hz)</li>
 *   <li><b>SNARE</b> — mezcla de tono corto + ruido gaussiano</li>
 *   <li><b>HIHAT</b> — ruido blanco con decay muy rápido</li>
 *   <li><b>CLAP</b>  — tres ráfagas breves de ruido superpuestas</li>
 * </ul>
 * </p>
 */
public enum DrumVoice {

  // -------------------------------------------------------------------------
  KICK("KICK", new Color(220, 80, 60)) {
    @Override
    public double[] generateSamples(final float sr) {
      final int len = (int) (sr * 0.30); // 300 ms
      final double[] s = new double[len];
      double phase = 0.0;
      for (int i = 0; i < len; i++) {
        final double t    = i / sr;
        final double freq = 40.0 + 120.0 * Math.exp(-t * 20.0);
        final double amp  = Math.exp(-t * 10.0);
        s[i]  = amp * Math.sin(2.0 * Math.PI * phase);
        phase += freq / sr;
        if (phase > 1.0) phase -= 1.0;
      }
      return s;
    }
  },

  // -------------------------------------------------------------------------
  SNARE("SNARE", new Color(220, 160, 50)) {
    @Override
    public double[] generateSamples(final float sr) {
      final int    len = (int) (sr * 0.15); // 150 ms
      final double[] s = new double[len];
      final Random   rng = new Random();
      double phase = 0.0;
      for (int i = 0; i < len; i++) {
        final double t     = i / sr;
        final double amp   = Math.exp(-t * 28.0);
        final double tone  = Math.sin(2.0 * Math.PI * phase);
        final double noise = rng.nextGaussian() * 0.7;
        s[i] = clamp(amp * (0.35 * tone + 0.65 * noise));
        phase += 180.0 / sr;
        if (phase > 1.0) phase -= 1.0;
      }
      return s;
    }
  },

  // -------------------------------------------------------------------------
  HIHAT("HI-HAT", new Color(70, 200, 140)) {
    @Override
    public double[] generateSamples(final float sr) {
      final int    len = (int) (sr * 0.06); // 60 ms
      final double[] s = new double[len];
      final Random   rng = new Random();
      for (int i = 0; i < len; i++) {
        final double t   = i / sr;
        final double amp = Math.exp(-t * 70.0);
        s[i] = clamp(amp * rng.nextGaussian() * 0.9);
      }
      return s;
    }
  },

  // -------------------------------------------------------------------------
  CLAP("CLAP", new Color(150, 100, 220)) {
    @Override
    public double[] generateSamples(final float sr) {
      final int    len = (int) (sr * 0.10); // 100 ms
      final double[] s = new double[len];
      final Random   rng = new Random();
      for (int i = 0; i < len; i++) {
        final double t  = i / sr;
        final double b1 = Math.exp(-t * 300.0);
        final double b2 = t > 0.007 ? Math.exp(-(t - 0.007) * 300.0) : 0.0;
        final double b3 = t > 0.014 ? Math.exp(-(t - 0.014) * 300.0) : 0.0;
        final double amp = (b1 + b2 + b3) / 3.0;
        s[i] = clamp(amp * rng.nextGaussian());
      }
      return s;
    }
  };

  // -------------------------------------------------------------------------
  // Atributos comunes
  // -------------------------------------------------------------------------

  private final String name;
  private final Color  color;

  DrumVoice(final String name, final Color color) {
    this.name  = name;
    this.color = color;
  }

  /** @return Nombre legible para la UI */
  public String getName()  { return this.name; }

  /** @return Color identificativo para la UI */
  public Color  getColor() { return this.color; }

  /**
   * Genera las muestras PCM normalizadas [-1.0, 1.0] para esta voz.
   *
   * @param sampleRate Tasa de muestreo en Hz (normalmente 44100)
   * @return Array de muestras
   */
  public abstract double[] generateSamples(float sampleRate);

  // -------------------------------------------------------------------------
  private static double clamp(final double v) {
    return Math.max(-1.0, Math.min(1.0, v));
  }

}
