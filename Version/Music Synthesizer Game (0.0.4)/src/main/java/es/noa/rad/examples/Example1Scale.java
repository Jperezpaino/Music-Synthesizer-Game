package es.noa.rad.examples;

import es.noa.rad.synth.Note;
import es.noa.rad.synth.SoundEngine;
import es.noa.rad.synth.Waveform;

/**
 * Ejemplo 1: Escala de Do mayor (C4 a C5) con cada tipo de onda.
 *
 * <p>Toca la escala completa de Do Mayor probando las 4 formas de onda:
 * SINE, SQUARE, SAWTOOTH y TRIANGLE.</p>
 */
public final class Example1Scale {

  /** Duración de cada nota en milisegundos */
  private static final int NOTE_DURATION_MS = 400;

  /** Pausa entre notas en milisegundos */
  private static final int NOTE_GAP_MS = 50;

  /** Pausa entre formas de onda en milisegundos */
  private static final int WAVEFORM_GAP_MS = 800;

  /** Notas de la escala de Do Mayor */
  private static final Note[] DO_MAJOR_SCALE = {
    Note.C4, Note.D4, Note.E4, Note.F4,
    Note.G4, Note.A4, Note.B4, Note.C5
  };

  private Example1Scale() { }

  /**
   * @param _arguments argumentos de línea de comandos (no usados)
   * @throws InterruptedException si el hilo es interrumpido
   */
  public static void main(final String... _arguments) throws InterruptedException {
    final SoundEngine engine = new SoundEngine();

    System.out.println("=== Ejemplo 1: Escala de Do Mayor ===\n");

    for (final Waveform waveform : Waveform.values()) {
      engine.setWaveform(waveform);
      System.out.println("▶ Forma de onda: " + waveform);

      for (final Note note : DO_MAJOR_SCALE) {
        System.out.println("  ♪ " + note);
        engine.startNote(note);
        Thread.sleep(NOTE_DURATION_MS);
        engine.stopNote();
        Thread.sleep(NOTE_GAP_MS);
      }

      System.out.println();
      Thread.sleep(WAVEFORM_GAP_MS);
    }

    System.out.println("✓ Ejemplo 1 finalizado.");
  }

}
