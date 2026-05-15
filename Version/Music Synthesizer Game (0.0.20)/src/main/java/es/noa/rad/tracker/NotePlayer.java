package es.noa.rad.tracker;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;

/**
 * Utilidad estatica para emitir notas MIDI usando el sintetizador del JDK.
 *
 * <p>El sintetizador se inicializa de forma perezosa la primera vez que se
 * llama a {@link #playNote}. El instrumento por defecto es Piano (program 0).
 * La nota suena durante {@value #DURATION_MS} ms en un hilo separado para
 * no bloquear el EDT.</p>
 */
public final class NotePlayer {

  /** Duracion de la nota en milisegundos. */
  private static final int DURATION_MS = 250;

  /** Velocidad (intensidad) MIDI 0-127. */
  private static final int VELOCITY = 90;

  /** Canal MIDI que se usara. */
  private static final int CHANNEL = 0;

  private static Synthesizer  synth;
  private static MidiChannel  channel;
  private static boolean      initFailed = false;

  private NotePlayer() {}

  /**
   * Inicializa el sintetizador MIDI si no se ha hecho todavia.
   * Silencia cualquier error de inicializacion para no romper la UI.
   */
  private static synchronized void ensureInit() {
    if (channel != null || initFailed) return;
    try {
      synth = MidiSystem.getSynthesizer();
      synth.open();
      // Cargar el banco de sonidos por defecto si hace falta
      if (synth.getDefaultSoundbank() != null) {
        synth.loadAllInstruments(synth.getDefaultSoundbank());
      }
      final MidiChannel[] channels = synth.getChannels();
      if (channels == null || channels.length == 0) {
        initFailed = true;
        return;
      }
      channel = channels[CHANNEL];
      channel.programChange(0); // Piano acustico
    } catch (final Exception ex) {
      initFailed = true;
    }
  }

  /**
   * Calcula el numero de nota MIDI a partir de la octava y el semitono.
   *
   * <p>Convencion MIDI: C-1 = 0, C0 = 12, C1 = 24, C2 = 36, C5 = 72.</p>
   *
   * @param semitone semitono dentro de la octava (0=C, 1=C#, ..., 11=B)
   * @param octave   numero de octava (2–5 en este tracker)
   * @return numero MIDI (0-127)
   */
  public static int midiNote(final int semitone, final int octave) {
    return 12 + octave * 12 + semitone;
  }

  /**
   * Emite la nota correspondiente al semitono y octava indicados.
   * La reproduccion se realiza en un hilo daemon para no bloquear el EDT.
   *
   * @param semitone 0–11
   * @param octave   octava numerica (ej. 2, 3, 4, 5)
   */
  public static void playNote(final int semitone, final int octave) {
    ensureInit();
    if (initFailed || channel == null) return;
    final int midi = midiNote(semitone, octave);
    if (midi < 0 || midi > 127) return;

    final Thread t = new Thread(() -> {
      try {
        channel.noteOn(midi, VELOCITY);
        Thread.sleep(DURATION_MS);
        channel.noteOff(midi);
      } catch (final InterruptedException ie) {
        channel.noteOff(midi);
        Thread.currentThread().interrupt();
      }
    });
    t.setDaemon(true);
    t.start();
  }
}