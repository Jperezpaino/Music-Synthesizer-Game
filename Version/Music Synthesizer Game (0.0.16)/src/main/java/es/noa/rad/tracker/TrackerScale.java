package es.noa.rad.tracker;

import java.util.HashSet;
import java.util.Set;

/**
 * Escalas musicales disponibles en el Tracker Visual.
 *
 * <p>Cada escala define que notas (semitonos) estan activas dentro de una octava.
 * La comprobacion {@link #isRowActive(int)} mapea una fila del tracker (0-15)
 * al semitono correspondiente mediante {@code row % 12}, de modo que la logica
 * funciona tanto para la primera octava (C4-B4) como para la segunda parcial
 * (C5-D#5) sin duplicar datos.</p>
 *
 * <p>Organizacion por nivel de dificultad:</p>
 * <ul>
 *   <li><b>Basica</b>  — Pentatonica (5 notas): muy consonante, sin tensiones</li>
 *   <li><b>Blues</b>   — Hexatonica (6 notas): color blues/jazz</li>
 *   <li><b>Normal</b>  — Diatonica (7 notas): escala estandar mayor/menor</li>
 *   <li><b>Experta</b> — Cromatica (12 notas): libertad total</li>
 * </ul>
 */
public enum TrackerScale {

  // -------------------------------------------------------------------------
  // Escalas definidas — (nombre corto, nombre completo, tipo, semitonos)
  // -------------------------------------------------------------------------

  /** Pentatonica mayor: C D E G A */
  PENTATONICA_MAYOR(
      "Basica+",
      "Pentatonica mayor",
      "5 notas",
      new int[]{ 0, 2, 4, 7, 9 }),

  /** Pentatonica menor: C D# F G A# */
  PENTATONICA_MENOR(
      "Basica-",
      "Pentatonica menor",
      "5 notas",
      new int[]{ 0, 3, 5, 7, 10 }),

  /** Blues mayor: C D D# E G A */
  BLUES_MAYOR(
      "Blues+",
      "Blues mayor",
      "Hexatonica",
      new int[]{ 0, 2, 3, 4, 7, 9 }),

  /** Blues menor: C D# F F# G A# */
  BLUES_MENOR(
      "Blues-",
      "Blues menor",
      "Hexatonica",
      new int[]{ 0, 3, 5, 6, 7, 10 }),

  /** Escala mayor (modo Ionian): C D E F G A B */
  MAYOR(
      "Normal+",
      "Mayor (Ionian)",
      "Diatonica",
      new int[]{ 0, 2, 4, 5, 7, 9, 11 }),

  /** Escala menor natural (modo Aeolian): C D D# F G G# A# */
  MENOR_NATURAL(
      "Normal-",
      "Menor natural (Aeolian)",
      "Diatonica",
      new int[]{ 0, 2, 3, 5, 7, 8, 10 }),

  /** Escala cromatica completa: las 12 notas */
  CROMATICA(
      "Experta",
      "Cromatica completa",
      "12 notas",
      new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 });

  // -------------------------------------------------------------------------
  // Campos de cada constante
  // -------------------------------------------------------------------------

  /** Nombre corto para el boton del selector (ej: "Basica+") */
  private final String shortName;

  /** Nombre musical completo (ej: "Pentatonica mayor") */
  private final String fullName;

  /** Clasificacion por numero de notas (ej: "5 notas", "Diatonica") */
  private final String type;

  /** Conjunto de semitonos activos dentro de una octava (0-11) */
  private final Set<Integer> semitones;

  // -------------------------------------------------------------------------
  // Constructor (privado por ser enum)
  // -------------------------------------------------------------------------

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

  // -------------------------------------------------------------------------
  // Getters
  // -------------------------------------------------------------------------

  /**
   * Nombre corto para la interfaz (ej: "Basica+").
   *
   * @return Nombre corto de la escala
   */
  public String getShortName() {
    return this.shortName;
  }

  /**
   * Nombre musical completo (ej: "Pentatonica mayor").
   *
   * @return Nombre completo de la escala
   */
  public String getFullName() {
    return this.fullName;
  }

  /**
   * Tipo / clasificacion de la escala (ej: "5 notas", "Diatonica").
   *
   * @return Tipo de la escala
   */
  public String getType() {
    return this.type;
  }

  // -------------------------------------------------------------------------
  // Logica de escala
  // -------------------------------------------------------------------------

  /**
   * Indica si una fila del tracker pertenece a esta escala.
   *
   * <p>El calculo es {@code semitones.contains(row % 12)}, lo que hace que
   * funcione tanto para la primera octava (filas 0-11) como para la segunda
   * octava parcial (filas 12-15).</p>
   *
   * @param row Fila del tracker (0-15)
   * @return {@code true} si la nota de esa fila pertenece a la escala
   */
  public boolean isRowActive(final int row) {
    return this.semitones.contains(row % 12);
  }

  /**
   * Devuelve cuantas notas distintas (semitonos) tiene esta escala.
   *
   * @return Numero de notas de la escala
   */
  public int getNoteCount() {
    return this.semitones.size();
  }
}
