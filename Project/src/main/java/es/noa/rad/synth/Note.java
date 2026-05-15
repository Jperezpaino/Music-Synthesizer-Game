package es.noa.rad.synth;

/**
 * Notas musicales con sus frecuencias en Hz para cada octava.
 * Frecuencias basadas en el estándar A4 = 440 Hz (afinación igual temperamento).
 */
public enum Note {

  // Octava 3
  C3("Do 3",  130.81),
  D3("Re 3",  146.83),
  E3("Mi 3",  164.81),
  F3("Fa 3",  174.61),
  G3("Sol 3", 196.00),
  A3("La 3",  220.00),
  B3("Si 3",  246.94),

  // Octava 4 (central)
  C4("Do 4",  261.63),
  D4("Re 4",  293.66),
  E4("Mi 4",  329.63),
  F4("Fa 4",  349.23),
  G4("Sol 4", 392.00),
  A4("La 4",  440.00),
  B4("Si 4",  493.88),

  // Octava 5
  C5("Do 5",  523.25),
  D5("Re 5",  587.33),
  E5("Mi 5",  659.25),
  F5("Fa 5",  698.46),
  G5("Sol 5", 783.99),
  A5("La 5",  880.00),
  B5("Si 5",  987.77),

  // Sostenidos octava 3
  CS3("Do# 3", 138.59),
  DS3("Re# 3", 155.56),
  FS3("Fa# 3", 185.00),
  GS3("Sol# 3",207.65),
  AS3("La# 3", 233.08),

  // Sostenidos octava 4
  CS4("Do# 4", 277.18),
  DS4("Re# 4", 311.13),
  FS4("Fa# 4", 369.99),
  GS4("Sol# 4",415.30),
  AS4("La# 4", 466.16),

  // Sostenidos octava 5
  CS5("Do# 5", 554.37),
  DS5("Re# 5", 622.25),
  FS5("Fa# 5", 739.99),
  GS5("Sol# 5",830.61),
  AS5("La# 5", 932.33);

  /** Nombre legible de la nota */
  private final String displayName;

  /** Frecuencia en Hz */
  private final double frequency;

  Note(final String displayName, final double frequency) {
    this.displayName = displayName;
    this.frequency   = frequency;
  }

  /**
   * @return Frecuencia de la nota en Hz
   */
  public double getFrequency() {
    return this.frequency;
  }

  /**
   * @return Nombre legible de la nota
   */
  public String getDisplayName() {
    return this.displayName;
  }

  @Override
  public String toString() {
    return this.displayName + " (" + this.frequency + " Hz)";
  }

}
