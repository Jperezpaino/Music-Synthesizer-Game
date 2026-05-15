package es.noa.rad.fx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cadena de efectos de audio.
 *
 * <p>Los efectos se aplican en el orden en que fueron añadidos.
 * Solo los efectos habilitados procesan la señal.</p>
 */
public final class EffectChain {

  private final List<AudioEffect> effects = new ArrayList<>();

  /**
   * Añade un efecto al final de la cadena.
   *
   * @param effect Efecto a añadir
   */
  public void add(final AudioEffect effect) {
    this.effects.add(effect);
  }

  /**
   * Procesa un sample pasándolo por todos los efectos habilitados en orden.
   *
   * @param sample Sample de entrada [-1.0, 1.0]
   * @return Sample procesado [-1.0, 1.0]
   */
  public double process(final double sample) {
    double s = sample;
    for (final AudioEffect fx : this.effects) {
      s = fx.process(s);
    }
    return s;
  }

  /** @return Lista inmutable de efectos */
  public List<AudioEffect> getEffects() {
    return Collections.unmodifiableList(this.effects);
  }

}
