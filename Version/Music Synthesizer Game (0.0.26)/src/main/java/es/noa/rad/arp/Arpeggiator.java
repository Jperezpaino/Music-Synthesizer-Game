package es.noa.rad.arp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import es.noa.rad.synth.Note;
import es.noa.rad.synth.SoundEngine;

/**
 * Arpeggiador: reproduce automaticamente las notas mantenidas pulsadas
 * en secuencia, creando un patron ritmico melodico.
 *
 * <p>Analogia: es como tener a alguien que, mientras tu mantienes pulsadas
 * varias teclas del piano, va tocando cada una de ellas de forma ordenada
 * y repetida al ritmo que tu elijas.</p>
 *
 * <h2>Modos de reproduccion</h2>
 * <ul>
 *   <li>{@link Mode#UP}       - De la nota mas grave a la mas aguda</li>
 *   <li>{@link Mode#DOWN}     - De la nota mas aguda a la mas grave</li>
 *   <li>{@link Mode#UP_DOWN}  - Sube hasta la mas aguda, baja hasta la mas grave, y repite</li>
 *   <li>{@link Mode#RANDOM}   - Elige notas al azar</li>
 * </ul>
 */
public final class Arpeggiator {

  /** Modos de reproduccion del arpeggiador */
  public enum Mode {
    UP("Arriba"),
    DOWN("Abajo"),
    UP_DOWN("Arriba-Abajo"),
    RANDOM("Aleatorio");

    private final String displayName;

    Mode(final String displayName) {
      this.displayName = displayName;
    }

    /** @return Nombre legible en castellano */
    public String getDisplayName() {
      return this.displayName;
    }

    @Override
    public String toString() {
      return this.displayName;
    }
  }

  // -------------------------------------------------------------------------
  // Estado
  // -------------------------------------------------------------------------

  private volatile boolean enabled     = false;
  private volatile Mode    mode        = Mode.UP;
  private volatile int     bpm         = 120;
  private volatile int     octaveRange = 0;   // 0 = solo octava actual, 1 = +1 oct, 2 = +2 oct

  private final SoundEngine soundEngine;

  /** Notas actualmente mantenidas pulsadas, ordenadas por frecuencia */
  private final TreeSet<Note> heldNotes =
      new TreeSet<>(Comparator.comparingDouble(Note::getFrequency));

  private Thread           arpThread;
  private volatile boolean running = false;

  /** Ultima nota que el arpeggiador disparo (para poder soltarla antes de la siguiente) */
  private volatile Note lastNote = null;

  /** Indice de paso interno para los modos UP / DOWN / UP_DOWN */
  private int stepIndex = 0;

  /** Direccion para el modo UP_DOWN (+1 subiendo, -1 bajando) */
  private int direction = 1;

  private final Random random = new Random();

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  /**
   * Crea el arpeggiador.
   *
   * @param soundEngine Motor de audio al que enviar las notas
   */
  public Arpeggiator(final SoundEngine soundEngine) {
    this.soundEngine = soundEngine;
  }

  // -------------------------------------------------------------------------
  // API de notas (llamada desde KeyboardPanel via SoundEngine)
  // -------------------------------------------------------------------------

  /**
   * Registra que una tecla ha sido pulsada.
   *
   * @param note Nota pulsada
   */
  public synchronized void noteOn(final Note note) {
    this.heldNotes.add(note);
  }

  /**
   * Registra que una tecla ha sido soltada.
   *
   * @param note Nota soltada
   */
  public synchronized void noteOff(final Note note) {
    this.heldNotes.remove(note);
    if (this.heldNotes.isEmpty() && this.lastNote != null) {
      this.soundEngine.stopNoteArp(this.lastNote);
      this.lastNote = null;
    }
  }

  // -------------------------------------------------------------------------
  // Control
  // -------------------------------------------------------------------------

  /**
   * Activa o desactiva el arpeggiador.
   *
   * @param enabled {@code true} para activar
   */
  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
    if (!enabled) {
      this.stopArpThread();
      if (this.lastNote != null) {
        this.soundEngine.stopNoteArp(this.lastNote);
        this.lastNote = null;
      }
      synchronized (this) {
        this.heldNotes.clear();
      }
    } else {
      this.startArpThread();
    }
  }

  /** @return {@code true} si el arpeggiador esta activo */
  public boolean isEnabled() {
    return this.enabled;
  }

  // -------------------------------------------------------------------------
  // Configuracion
  // -------------------------------------------------------------------------

  /** @param mode Modo de reproduccion */
  public void setMode(final Mode mode)      { this.mode = mode; }

  /** @param bpm Velocidad en pasos por minuto (60 - 300) */
  public void setBpm(final int bpm)         { this.bpm = Math.max(60, Math.min(300, bpm)); }

  /** @param range Octavas extra (0 = sin extension, 1 = +1 oct, 2 = +2 oct) */
  public void setOctaveRange(final int range) {
    this.octaveRange = Math.max(0, Math.min(2, range));
  }

  /** @return Modo actual */
  public Mode getMode()        { return this.mode; }

  /** @return Velocidad actual en BPM */
  public int getBpm()          { return this.bpm; }

  /** @return Rango de octavas extra */
  public int getOctaveRange()  { return this.octaveRange; }

  // -------------------------------------------------------------------------
  // Hilo de arpeggio
  // -------------------------------------------------------------------------

  private void startArpThread() {
    if (this.running) {
      return;
    }
    this.running   = true;
    this.stepIndex = 0;
    this.direction = 1;
    this.arpThread = new Thread(this::arpLoop, "Arpeggiator");
    this.arpThread.setDaemon(true);
    this.arpThread.start();
  }

  private void stopArpThread() {
    this.running = false;
    if (this.arpThread != null) {
      this.arpThread.interrupt();
    }
  }

  private void arpLoop() {
    while (this.running) {
      final long delayMs = 60_000L / this.bpm;
      final List<Note> notes = this.buildNoteList();

      if (!notes.isEmpty()) {
        final Note next = this.selectNextNote(notes);
        if (this.lastNote != null) {
          this.soundEngine.stopNoteArp(this.lastNote);
        }
        this.soundEngine.startNoteArp(next);
        this.lastNote = next;
      }

      try {
        Thread.sleep(delayMs);
      } catch (final InterruptedException e) {
        break;
      }
    }
  }

  // -------------------------------------------------------------------------
  // Construccion de la lista de notas (con extension de octavas)
  // -------------------------------------------------------------------------

  /** Construye la lista de notas a usar en el ciclo, incluyendo octavas extra */
  private synchronized List<Note> buildNoteList() {
    if (this.heldNotes.isEmpty()) {
      return Collections.emptyList();
    }

    final List<Note> notes = new ArrayList<>(this.heldNotes);

    if (this.octaveRange > 0) {
      final List<Note> extras = new ArrayList<>();
      for (final Note base : this.heldNotes) {
        for (int oct = 1; oct <= this.octaveRange; oct++) {
          final Note shifted = findOctaveUp(base, oct);
          if (shifted != null) {
            extras.add(shifted);
          }
        }
      }
      notes.addAll(extras);
      notes.sort(Comparator.comparingDouble(Note::getFrequency));
    }

    return notes;
  }

  /**
   * Busca en el enum Note la nota que esta {@code octaves} octavas por encima
   * de la nota base (la misma nota pero mas aguda).
   *
   * @param base    Nota base
   * @param octaves Numero de octavas a subir (1 o 2)
   * @return La nota encontrada, o {@code null} si no existe en el enum
   */
  private static Note findOctaveUp(final Note base, final int octaves) {
    final double target = base.getFrequency() * Math.pow(2, octaves);
    Note   best     = null;
    double bestDiff = Double.MAX_VALUE;
    for (final Note n : Note.values()) {
      final double diff = Math.abs(n.getFrequency() - target);
      if (diff < bestDiff) {
        bestDiff = diff;
        best     = n;
      }
    }
    return (bestDiff < 15.0) ? best : null;
  }

  // -------------------------------------------------------------------------
  // Seleccion de nota segun el modo
  // -------------------------------------------------------------------------

  private Note selectNextNote(final List<Note> notes) {
    final int size = notes.size();
    if (size == 1) {
      this.stepIndex = 0;
      return notes.get(0);
    }

    switch (this.mode) {

      case UP: {
        final Note n = notes.get(this.stepIndex % size);
        this.stepIndex++;
        return n;
      }

      case DOWN: {
        final int idx = (size - 1) - (this.stepIndex % size);
        this.stepIndex++;
        return notes.get(idx);
      }

      case UP_DOWN: {
        // Ciclo de longitud (2*size - 2): 0,1,...,size-1,size-2,...,1
        final int cycle = 2 * size - 2;
        int idx = this.stepIndex % cycle;
        if (idx >= size) {
          idx = cycle - idx;
        }
        this.stepIndex++;
        return notes.get(idx);
      }

      case RANDOM:
      default:
        return notes.get(this.random.nextInt(size));
    }
  }
}
