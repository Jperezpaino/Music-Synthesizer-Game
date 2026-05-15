package es.noa.rad.synth;

/**
 * Presets de instrumento para el sintetizador.
 *
 * <p>Cada preset define la forma de onda y los parámetros ADSR:</p>
 * <ul>
 *   <li><b>Attack</b>  — muestras para pasar de 0 a amplitud máxima</li>
 *   <li><b>Decay</b>   — muestras para bajar de máxima a nivel de sustain</li>
 *   <li><b>Sustain</b> — nivel de amplitud mientras la tecla está pulsada (0.0–1.0)</li>
 *   <li><b>Release</b> — muestras para apagar la nota al soltar la tecla</li>
 * </ul>
 *
 * <p>Los valores están en samples a 44 100 Hz.</p>
 */
public enum InstrumentPreset {

  /**
   * Piano — ataque rápido, decaimiento largo, sustain bajo.
   * Simula el martillo percutiendo la cuerda y su resonancia.
   */
  PIANO("🎹 Piano", Waveform.SINE,
      50,    // attack  (~1 ms)
      8_000, // decay   (~180 ms)
      0.35,  // sustain
      5_000  // release (~113 ms)
  ),

  /**
   * Órgano — ataque y release instantáneos, sustain pleno.
   * Característica "on/off" del órgano Hammond.
   */
  ORGAN("🎸 Órgano", Waveform.SQUARE,
      80,    // attack
      0,     // decay (sin decaimiento)
      1.0,   // sustain pleno
      300    // release muy corto
  ),

  /**
   * Flauta — ataque suave y largo, sustain pleno.
   * Simula el soplo gradual sobre la embocadura.
   */
  FLUTE("🪈 Flauta", Waveform.SINE,
      3_500, // attack  (~80 ms)
      0,     // decay
      1.0,   // sustain
      2_500  // release (~57 ms)
  ),

  /**
   * Bajo eléctrico — ataque muy rápido, decay medio, sustain medio.
   */
  BASS("🎸 Bajo", Waveform.SAWTOOTH,
      80,    // attack
      6_000, // decay  (~136 ms)
      0.5,   // sustain
      800    // release
  ),

  /**
   * Lead synth — respuesta inmediata, sustain pleno.
   * Típico del sintetizador monofónico de melodía.
   */
  LEAD("🎛 Lead Synth", Waveform.SAWTOOTH,
      60,    // attack
      0,     // decay
      1.0,   // sustain
      400    // release
  ),

  /**
   * Cuerdas — ataque y release largos, sustain alto.
   * Simula un pad de cuerdas orquestales.
   */
  STRINGS("🎻 Cuerdas", Waveform.TRIANGLE,
      9_000, // attack  (~200 ms)
      2_000, // decay
      0.8,   // sustain
      10_000 // release (~227 ms)
  );

  // -------------------------------------------------------------------------

  private final String   displayName;
  private final Waveform waveform;
  private final int      attackSamples;
  private final int      decaySamples;
  private final double   sustainLevel;
  private final int      releaseSamples;

  InstrumentPreset(final String displayName,
                   final Waveform waveform,
                   final int attackSamples,
                   final int decaySamples,
                   final double sustainLevel,
                   final int releaseSamples) {
    this.displayName    = displayName;
    this.waveform       = waveform;
    this.attackSamples  = attackSamples;
    this.decaySamples   = decaySamples;
    this.sustainLevel   = sustainLevel;
    this.releaseSamples = releaseSamples;
  }

  /** @return Nombre para mostrar en la UI */
  public String getDisplayName() { return this.displayName; }

  /** @return Forma de onda del preset */
  public Waveform getWaveform() { return this.waveform; }

  /** @return Samples de ataque */
  public int getAttackSamples() { return this.attackSamples; }

  /** @return Samples de decaimiento */
  public int getDecaySamples() { return this.decaySamples; }

  /** @return Nivel de sustain (0.0–1.0) */
  public double getSustainLevel() { return this.sustainLevel; }

  /** @return Samples de release */
  public int getReleaseSamples() { return this.releaseSamples; }

  @Override
  public String toString() { return this.displayName; }
}
