package es.noa.rad.tracker;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;

/**
 * Utilidad para emitir notas MIDI con soporte de multiples canales independientes.
 *
 * <p>Gestiona hasta {@value #NUM_CHANNELS} canales MIDI simultaneos, cada uno
 * con su propio instrumento GM. El canal 0 es el principal; los canales 1-3
 * corresponden a los canales adicionales del tracker multicanal.</p>
 *
 * <p>El sintetizador se inicializa de forma perezosa. La nota suena durante
 * {@value #DURATION_MS} ms en un hilo daemon para no bloquear el EDT.</p>
 */
public final class NotePlayer {

  /** Numero de canales MIDI gestionados simultaneamente. */
  public static final int NUM_CHANNELS = 4;

  /** Duracion de la nota en milisegundos. */
  private static final int DURATION_MS = 250;

  /** Velocidad (intensidad) MIDI 0-127. */
  private static final int VELOCITY = 90;

  private static Synthesizer   synth;
  private static final MidiChannel[] channels   = new MidiChannel[NUM_CHANNELS];
  private static final int[]         programs   = new int[NUM_CHANNELS]; // 0=Piano por defecto
  private static boolean             initFailed = false;

  private NotePlayer() {}

  /**
   * Inicializa el sintetizador MIDI anticipadamente en un hilo daemon.
   * Llamar una vez al arrancar la aplicacion para evitar retardo en la primera nota.
   */
  public static void warmUp() {
    final Thread t = new Thread(NotePlayer::ensureInit, "midi-warmup");
    t.setDaemon(true);
    t.start();
  }

  /**
   * Inicializa el sintetizador y los {@value #NUM_CHANNELS} canales MIDI.
   * Silencia errores de inicializacion para no romper la UI.
   */
  private static synchronized void ensureInit() {
    if (channels[0] != null || initFailed) return;
    try {
      synth = MidiSystem.getSynthesizer();
      synth.open();
      if (synth.getDefaultSoundbank() != null) {
        synth.loadAllInstruments(synth.getDefaultSoundbank());
      }
      final MidiChannel[] all = synth.getChannels();
      if (all == null || all.length < NUM_CHANNELS) {
        initFailed = true;
        return;
      }
      for (int i = 0; i < NUM_CHANNELS; i++) {
        channels[i] = all[i];
        channels[i].programChange(programs[i]);
      }
    } catch (final Exception ex) {
      initFailed = true;
    }
  }

  /**
   * Cambia el instrumento MIDI (programa GM 0-127) de un canal.
   *
   * @param channelIdx indice del canal (0 a {@value #NUM_CHANNELS}-1)
   * @param program    numero de programa GM (0=Piano, 25=Guitarra acustica, etc.)
   */
  public static synchronized void setProgram(final int channelIdx, final int program) {
    if (channelIdx < 0 || channelIdx >= NUM_CHANNELS) return;
    programs[channelIdx] = program;
    ensureInit();
    if (!initFailed && channels[channelIdx] != null) {
      channels[channelIdx].programChange(program);
    }
  }

  /**
   * Devuelve el programa GM asignado actualmente al canal indicado.
   *
   * @param channelIdx indice del canal (0 a {@value #NUM_CHANNELS}-1)
   * @return numero de programa GM (0-127)
   */
  public static int getProgram(final int channelIdx) {
    if (channelIdx < 0 || channelIdx >= NUM_CHANNELS) return 0;
    return programs[channelIdx];
  }

  /**
   * Emite una nota en el canal 0 (compatibilidad hacia atras).
   *
   * @param semitone 0-11
   * @param octave   octava numerica (ej. 2, 3, 4, 5)
   */
  public static void playNote(final int semitone, final int octave) {
    playNote(semitone, octave, 0);
  }

  /**
   * Emite una nota en el canal MIDI indicado.
   * La reproduccion se realiza en un hilo daemon para no bloquear el EDT.
   *
   * @param semitone   0-11
   * @param octave     octava numerica
   * @param channelIdx indice del canal (0 a {@value #NUM_CHANNELS}-1)
   */
  public static void playNote(final int semitone, final int octave, final int channelIdx) {
    ensureInit();
    if (initFailed || channelIdx < 0 || channelIdx >= NUM_CHANNELS) return;
    final MidiChannel ch = channels[channelIdx];
    if (ch == null) return;
    final int midi = midiNote(semitone, octave);
    if (midi < 0 || midi > 127) return;

    final Thread t = new Thread(() -> {
      try {
        ch.noteOn(midi, VELOCITY);
        Thread.sleep(DURATION_MS);
        ch.noteOff(midi);
      } catch (final InterruptedException ie) {
        ch.noteOff(midi);
        Thread.currentThread().interrupt();
      }
    });
    t.setDaemon(true);
    t.start();
  }

  /**
   * Calcula el numero de nota MIDI a partir del semitono y la octava.
   *
   * <p>Convencion MIDI: C-1 = 0, C0 = 12, C1 = 24, C2 = 36, C5 = 72.</p>
   *
   * @param semitone semitono dentro de la octava (0=C, 1=C#, ..., 11=B)
   * @param octave   numero de octava (2-5 en este tracker)
   * @return numero MIDI (0-127)
   */
  public static int midiNote(final int semitone, final int octave) {
    return 12 + octave * 12 + semitone;
  }
}
