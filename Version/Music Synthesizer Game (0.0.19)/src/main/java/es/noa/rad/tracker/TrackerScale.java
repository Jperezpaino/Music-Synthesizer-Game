package es.noa.rad.tracker;

import java.util.HashSet;
import java.util.Set;

/**
 * Escalas musicales del Tracker Visual (12 escalas).
 *
 * <p>Cada escala define los semitonos activos (0-11) dentro de una octava.
 * El metodo {@link #isRowActive(int)} comprueba si un semitono dado pertenece
 * a la escala usando {@code semitone % 12}.</p>
 */
public enum TrackerScale {

  /** Pentatonica mayor: C D E G A */
  EASY_PLUS(
      "Easy +", "Pentatonica mayor", "5 notas",
      new int[]{ 0, 2, 4, 7, 9 }),

  /** Pentatonica menor: C Eb F G Bb */
  EASY_MINUS(
      "Easy -", "Pentatonica menor", "5 notas",
      new int[]{ 0, 3, 5, 7, 10 }),

  /** Pelog / Gamelan: C Db F G Ab */
  ISLAND_PLUS(
      "Island +", "Pelog / Island", "Exotica 5",
      new int[]{ 0, 1, 5, 7, 8 }),

  /** Variante menor island: C E F A B */
  ISLAND_MINUS(
      "Island -", "Island menor", "Exotica 5",
      new int[]{ 0, 4, 5, 9, 11 }),

  /** Blues mayor: C D Eb E G A */
  BLUES_PLUS(
      "Blues +", "Blues mayor", "Hexatonica",
      new int[]{ 0, 2, 3, 4, 7, 9 }),

  /** Blues menor: C Eb F F# G Bb */
  BLUES_MINUS(
      "Blues -", "Blues menor", "Hexatonica",
      new int[]{ 0, 3, 5, 6, 7, 10 }),

  /** Escala mayor (Ionian): C D E F G A B */
  NORMAL_PLUS(
      "Normal +", "Mayor (Ionian)", "Diatonica",
      new int[]{ 0, 2, 4, 5, 7, 9, 11 }),

  /** Menor natural (Aeolian): C D Eb F G Ab Bb */
  NORMAL_MINUS(
      "Normal -", "Menor natural (Aeolian)", "Diatonica",
      new int[]{ 0, 2, 3, 5, 7, 8, 10 }),

  /** Doble armonica mayor: C Db E F G Ab B */
  DOUBLE_HARMONIC_PLUS(
      "Double +", "Doble armonica mayor", "Oriental 7",
      new int[]{ 0, 1, 4, 5, 7, 8, 11 }),

  /** Menor hungara (Doble armonica menor): C D Eb F# G Ab B */
  DOUBLE_HARMONIC_MINUS(
      "Double -", "Menor hungara", "Oriental 7",
      new int[]{ 0, 2, 3, 6, 7, 8, 11 }),

  /** Whole Tone (tonos enteros): C D E F# Ab Bb */
  STRANGE(
      "Strange", "Whole Tone (tonos enteros)", "Simetrica 6",
      new int[]{ 0, 2, 4, 6, 8, 10 }),

  /** Cromatica completa: las 12 notas */
  EXPERT(
      "Expert", "Cromatica completa", "12 notas",
      new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 });

  // -------------------------------------------------------------------------

  private final String       shortName;
  private final String       fullName;
  private final String       type;
  private final Set<Integer> semitones;

  TrackerScale(final String shortName,
               final String fullName,
               final String type,
               final int[]  semitoneArray) {
    this.shortName = shortName;
    this.fullName  = fullName;
    this.type      = type;
    this.semitones = new HashSet<>();
    for (final int s : semitoneArray) {
      this.semitones.add(s);
    }
  }

  /** Nombre corto para el selector (ej: "Easy +"). */
  public String getShortName() { return this.shortName; }

  /** Nombre musical completo. */
  public String getFullName()  { return this.fullName; }

  /** Tipo / clasificacion. */
  public String getType()      { return this.type; }

  /**
   * Indica si el semitono dado pertenece a esta escala.
   *
   * @param semitone Semitono (cualquier valor; se aplica % 12)
   * @return true si pertenece a la escala
   */
  public boolean isRowActive(final int semitone) {
    return this.semitones.contains(semitone % 12);
  }

  /** Numero de notas distintas de esta escala. */
  public int getNoteCount() { return this.semitones.size(); }
}