package es.noa.rad.fx;

/**
 * Interfaz para efectos de audio procesados sample a sample.
 *
 * <p>Cada efecto recibe un sample normalizado [-1.0, 1.0] y devuelve
 * el sample procesado en el mismo rango.</p>
 */
public interface AudioEffect {

  /**
   * Procesa un único sample de audio.
   *
   * @param sample Valor de entrada en rango [-1.0, 1.0]
   * @return Valor procesado en rango [-1.0, 1.0]
   */
  double process(double sample);

  /** @return {@code true} si el efecto está activo */
  boolean isEnabled();

  /**
   * Activa o desactiva el efecto.
   *
   * @param enabled {@code true} para activar
   */
  void setEnabled(boolean enabled);

  /** @return Nombre legible del efecto */
  String getName();

}
