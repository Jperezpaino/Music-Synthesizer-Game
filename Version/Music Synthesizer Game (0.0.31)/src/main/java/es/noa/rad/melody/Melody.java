package es.noa.rad.melody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Secuencia de notas grabadas que forman una melodía.
 *
 * <p>Registra cada nota con su tiempo de inicio y duración,
 * permitiendo reproducirla de forma fiel al original.</p>
 */
public final class Melody {

  /** Lista de notas grabadas en orden cronológico */
  private final List<MelodyNote> notes = new ArrayList<>();

  /** Duración total de la melodía en ms */
  private long totalDurationMs;

  /**
   * Añade una nota a la melodía.
   *
   * @param note Nota grabada
   */
  public void addNote(final MelodyNote note) {
    this.notes.add(note);
    final long end = note.getStartTimeMs() + note.getDurationMs();
    if (end > this.totalDurationMs) {
      this.totalDurationMs = end;
    }
  }

  /**
   * Elimina todas las notas (reinicia la grabación).
   */
  public void clear() {
    this.notes.clear();
    this.totalDurationMs = 0;
  }

  /**
   * @return Lista inmutable de notas grabadas
   */
  public List<MelodyNote> getNotes() {
    return Collections.unmodifiableList(this.notes);
  }

  /**
   * @return Número de notas grabadas
   */
  public int size() {
    return this.notes.size();
  }

  /**
   * @return true si no hay notas grabadas
   */
  public boolean isEmpty() {
    return this.notes.isEmpty();
  }

  /**
   * @return Duración total de la melodía en ms
   */
  public long getTotalDurationMs() {
    return this.totalDurationMs;
  }

}
