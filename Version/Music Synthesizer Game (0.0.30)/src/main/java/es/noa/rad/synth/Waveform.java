package es.noa.rad.synth;

/**
 * Tipos de onda sonora que puede generar el sintetizador.
 */
public enum Waveform {

  /** Onda sinusoidal — sonido suave y puro */
  SINE,

  /** Onda cuadrada — sonido electrónico, hueco */
  SQUARE,

  /** Onda sierra — sonido brillante y rico en armónicos */
  SAWTOOTH,

  /** Onda triangular — más suave que la cuadrada */
  TRIANGLE

}
