package es.noa.rad.melody;

import es.noa.rad.synth.Note;
import es.noa.rad.synth.Waveform;

/**
 * Representa una nota grabada dentro de una melodía.
 *
 * <p>Almacena la nota, la forma de onda activa en ese momento,
 * el instante en que se pulsó (relativo al inicio de la grabación)
 * y la duración que estuvo sonando.</p>
 */
public final class MelodyNote {

  /** Nota musical */
  private final Note     note;

  /** Forma de onda con la que se tocó */
  private final Waveform waveform;

  /** Tiempo de inicio en ms relativo al comienzo de la grabación */
  private final long startTimeMs;

  /** Duración de la nota en ms */
  private long durationMs;

  /**
   * Crea una nota grabada (sin duración aún, se cierra con {@link #setDurationMs}).
   *
   * @param note        Nota musical
   * @param waveform    Forma de onda activa
   * @param startTimeMs Tiempo de inicio relativo (ms)
   */
  public MelodyNote(final Note note, final Waveform waveform, final long startTimeMs) {
    this.note        = note;
    this.waveform    = waveform;
    this.startTimeMs = startTimeMs;
    this.durationMs  = 0;
  }

  /**
   * Fija la duración de la nota al soltarla.
   *
   * @param durationMs Duración en ms
   */
  public void setDurationMs(final long durationMs) {
    this.durationMs = Math.max(50, durationMs);
  }

  /** @return Nota musical */
  public Note getNote() { return this.note; }

  /** @return Forma de onda */
  public Waveform getWaveform() { return this.waveform; }

  /** @return Tiempo de inicio relativo en ms */
  public long getStartTimeMs() { return this.startTimeMs; }

  /** @return Duración en ms */
  public long getDurationMs() { return this.durationMs; }

  @Override
  public String toString() {
    return this.note.getDisplayName()
        + " [" + this.waveform + "] "
        + "@" + this.startTimeMs + "ms "
        + "dur=" + this.durationMs + "ms";
  }

}
