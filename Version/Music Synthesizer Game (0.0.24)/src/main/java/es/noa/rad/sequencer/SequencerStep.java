package es.noa.rad.sequencer;

import es.noa.rad.synth.Note;

/**
 * Representa un paso del secuenciador.
 *
 * <p>Cada paso tiene una nota asignada, una velocidad (0.0–1.0)
 * y un flag que indica si está activo (suena) o silenciado.</p>
 */
public final class SequencerStep {

  /** Nota por defecto al crear un paso */
  private static final Note    DEFAULT_NOTE     = Note.C4;
  private static final double  DEFAULT_VELOCITY = 0.8;

  private Note    note;
  private double  velocity;
  private boolean active;

  /**
   * Crea un paso inactivo con la nota y velocidad por defecto.
   */
  public SequencerStep() {
    this.note     = DEFAULT_NOTE;
    this.velocity = DEFAULT_VELOCITY;
    this.active   = false;
  }

  // -------------------------------------------------------------------------
  // Getters / Setters
  // -------------------------------------------------------------------------

  public Note    getNote()                        { return this.note; }
  public void    setNote(final Note note)         { this.note = note; }

  public double  getVelocity()                    { return this.velocity; }
  public void    setVelocity(final double v)      { this.velocity = Math.max(0.0, Math.min(1.0, v)); }

  public boolean isActive()                       { return this.active; }
  public void    setActive(final boolean active)  { this.active = active; }

  /** Alterna entre activo e inactivo. */
  public void toggle() {
    this.active = !this.active;
  }

}
