package es.noa.rad.examples;

import es.noa.rad.synth.Note;
import es.noa.rad.synth.SoundEngine;
import es.noa.rad.synth.Waveform;

/**
 * Ejemplo 2: Melodía de "Cumpleaños Feliz" con onda sinusoidal.
 *
 * <p>Demuestra cómo encadenar notas con duraciones distintas
 * para reproducir una melodía reconocible.</p>
 */
public final class Example2Melody {

  /** Duración base (negra) en milisegundos */
  private static final int QUARTER = 350;

  /** Duración corchea */
  private static final int EIGHTH  = QUARTER / 2;

  /** Duración negra con puntillo (negra + corchea) */
  private static final int DOTTED  = QUARTER + EIGHTH;

  /** Duración blanca */
  private static final int HALF    = QUARTER * 2;

  /** Pausa entre notas */
  private static final int GAP     = 40;

  /**
   * Par {nota, duración} que representa cada sonido de la melodía.
   * null en la nota representa un silencio.
   */
  private static final Object[][] HAPPY_BIRTHDAY = {
    // "Cum-ple-a-ños fe-liz"
    { Note.C4, EIGHTH  }, { Note.C4, EIGHTH  }, { Note.D4, QUARTER },
    { Note.C4, QUARTER }, { Note.F4, QUARTER }, { Note.E4, HALF    },

    // "Cum-ple-a-ños fe-liz"
    { Note.C4, EIGHTH  }, { Note.C4, EIGHTH  }, { Note.D4, QUARTER },
    { Note.C4, QUARTER }, { Note.G4, QUARTER }, { Note.F4, HALF    },

    // "Cum-ple-a-ños que-ri-do..."
    { Note.C4, EIGHTH  }, { Note.C4, EIGHTH  }, { Note.C5, QUARTER },
    { Note.A4, QUARTER }, { Note.F4, QUARTER }, { Note.E4, QUARTER },
    { Note.D4, DOTTED  },

    // "Cum-ple-a-ños fe-liz"
    { Note.B4, EIGHTH  }, { Note.B4, EIGHTH  }, { Note.A4, QUARTER },
    { Note.F4, QUARTER }, { Note.G4, QUARTER }, { Note.F4, HALF    },
  };

  private Example2Melody() { }

  /**
   * @param _arguments argumentos de línea de comandos (no usados)
   * @throws InterruptedException si el hilo es interrumpido
   */
  public static void main(final String... _arguments) throws InterruptedException {
    final SoundEngine engine = new SoundEngine();
    engine.setWaveform(Waveform.SINE);
    engine.setVolume(0.8);

    System.out.println("=== Ejemplo 2: Cumpleaños Feliz ===");
    System.out.println("Onda: SINE | Volumen: 80%\n");

    for (final Object[] step : HAPPY_BIRTHDAY) {
      final Note note     = (Note)    step[0];
      final int  duration = (Integer) step[1];

      System.out.println("  ♪ " + note + " — " + duration + " ms");
      engine.startNote(note);
      Thread.sleep(duration);
      engine.stopNote();
      Thread.sleep(GAP);
    }

    System.out.println("\n✓ Ejemplo 2 finalizado.");
  }

}
