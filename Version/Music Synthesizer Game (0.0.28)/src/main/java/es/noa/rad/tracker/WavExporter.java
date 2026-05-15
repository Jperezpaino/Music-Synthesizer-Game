package es.noa.rad.tracker;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Exporta la melodia actual del TrackerPanel a un fichero WAV de 44100 Hz / 16 bits / mono.
 *
 * <p>La sintesis es completamente software (sin MIDI): para cada columna activa
 * se suman ondas sinusoidales de las notas activas, con un envolvente ADSR
 * minimo (attack + release) para evitar clicks. La amplitud se normaliza
 * automaticamente segun el numero de notas simultaneas.</p>
 *
 * <p>La exportacion se realiza en un hilo de fondo ({@link SwingWorker}) para
 * no bloquear la interfaz durante la generacion.</p>
 */
public final class WavExporter {

  /** Frecuencia de muestreo del audio exportado. */
  private static final int    SAMPLE_RATE   = 44100;

  /** Amplitud maxima por nota (fraccion de rango full-scale 16-bit). */
  private static final double AMPLITUDE     = 0.6;

  /** Duracion del attack (rampa de subida) en milisegundos. */
  private static final int    ATTACK_MS     = 8;

  /** Duracion del release (rampa de bajada) en milisegundos. */
  private static final int    RELEASE_MS    = 40;

  private WavExporter() {}

  // -------------------------------------------------------------------------
  // API publica
  // -------------------------------------------------------------------------

  /**
   * Muestra un dialogo para elegir el destino y lanza la exportacion en segundo plano.
   *
   * @param parent    ventana padre para dialogos
   * @param tracker   panel cuya melodia se exportara
   * @param sequencer motor del sequencer (para leer el BPM actual)
   */
  public static void export(final JFrame parent,
                            final TrackerPanel tracker,
                            final SequencerEngine sequencer) {
    final JFileChooser fc = new JFileChooser();
    fc.setDialogTitle("Exportar melodia como WAV");
    fc.setFileFilter(new FileNameExtensionFilter("Archivo de audio WAV (*.wav)", "wav"));
    fc.setAcceptAllFileFilterUsed(false);
    if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

    File file = fc.getSelectedFile();
    if (!file.getName().toLowerCase().endsWith(".wav")) {
      file = new File(file.getAbsolutePath() + ".wav");
    }

    // Capturar snapshot del estado actual antes de salir del hilo EDT
    final boolean[][] steps   = tracker.getSteps();
    final TrackerPanel.NoteInfo[] notes = snapshotNotes(tracker);
    final int bpm             = sequencer.getBpm();
    final File dest           = file;

    new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() throws Exception {
        synthesize(dest, steps, notes, bpm);
        return null;
      }
      @Override
      protected void done() {
        try {
          get();
          JOptionPane.showMessageDialog(parent,
              "WAV exportado correctamente:\n" + dest.getName(),
              "Exportacion completada", JOptionPane.INFORMATION_MESSAGE);
        } catch (final Exception ex) {
          final Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
          JOptionPane.showMessageDialog(parent,
              "Error al exportar WAV:\n" + cause.getMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }.execute();
  }

  // -------------------------------------------------------------------------
  // Sintesis PCM
  // -------------------------------------------------------------------------

  /**
   * Genera el audio PCM y lo escribe en {@code dest}.
   *
   * <p>Algoritmo:
   * <ol>
   *   <li>Calcula la duracion de cada paso en muestras: {@code 44100 * 60 / bpm / 4}</li>
   *   <li>Para cada columna, suma ondas sinusoidales de las notas activas.</li>
   *   <li>Aplica envolvente ataque/release para evitar artefactos.</li>
   *   <li>Normaliza la amplitud en funcion del numero de notas simultaneas.</li>
   *   <li>Convierte a 16-bit signed little-endian y escribe el fichero WAVE.</li>
   * </ol>
   * </p>
   */
  private static void synthesize(final File dest,
                                  final boolean[][] steps,
                                  final TrackerPanel.NoteInfo[] notes,
                                  final int bpm) throws Exception {
    final double stepSec       = 60.0 / bpm / 4.0;
    final int    samplesPerStep = (int) Math.round(SAMPLE_RATE * stepSec);
    final int    totalSamples  = samplesPerStep * TrackerPanel.COLS;

    final double[] pcm          = new double[totalSamples];
    final int      attackSmp    = Math.max(1, (int) (SAMPLE_RATE * ATTACK_MS  / 1000.0));
    final int      releaseSmp   = Math.max(1, (int) (SAMPLE_RATE * RELEASE_MS / 1000.0));

    for (int col = 0; col < TrackerPanel.COLS; col++) {
      // Collect frequencies active in this column
      final List<Double> freqList = new ArrayList<>();
      for (int row = 0; row < TrackerPanel.ROWS; row++) {
        if (steps[row][col] && notes[row] != null) {
          freqList.add(midiFrequency(notes[row].semitone, notes[row].octave));
        }
      }
      if (freqList.isEmpty()) continue;

      // Normalize gain: louder when few notes, quieter when many
      final double gain = AMPLITUDE / Math.sqrt(freqList.size());
      final int colBase = col * samplesPerStep;

      for (int s = 0; s < samplesPerStep; s++) {
        // ADSR envelope (attack + sustain + release)
        final double env;
        if (s < attackSmp) {
          env = (double) s / attackSmp;
        } else if (s >= samplesPerStep - releaseSmp) {
          env = (double) (samplesPerStep - 1 - s) / releaseSmp;
        } else {
          env = 1.0;
        }

        double sample = 0.0;
        final double t = (double) s / SAMPLE_RATE;
        for (final double freq : freqList) {
          sample += Math.sin(2.0 * Math.PI * freq * t);
        }
        pcm[colBase + s] += sample * gain * env;
      }
    }

    // Convert double PCM [-1..1] to 16-bit signed little-endian bytes
    final byte[] bytes = new byte[totalSamples * 2];
    for (int i = 0; i < totalSamples; i++) {
      final int val = (int) Math.max(-32768, Math.min(32767, pcm[i] * 32767.0));
      bytes[i * 2]     = (byte) ( val        & 0xFF);
      bytes[i * 2 + 1] = (byte) ((val >> 8)  & 0xFF);
    }

    // Write WAV via javax.sound.sampled
    final AudioFormat fmt = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        SAMPLE_RATE,  // sample rate
        16,           // bits per sample
        1,            // channels (mono)
        2,            // frame size (bytes) = channels * bits/8
        SAMPLE_RATE,  // frame rate = sample rate for uncompressed PCM
        false);       // little-endian

    try (final AudioInputStream ais = new AudioInputStream(
            new ByteArrayInputStream(bytes), fmt, totalSamples)) {
      AudioSystem.write(ais, AudioFileFormat.Type.WAVE, dest);
    }
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  /** Toma un snapshot de todas las NoteInfo por fila en el EDT. */
  private static TrackerPanel.NoteInfo[] snapshotNotes(final TrackerPanel tracker) {
    final TrackerPanel.NoteInfo[] arr = new TrackerPanel.NoteInfo[TrackerPanel.ROWS];
    for (int r = 0; r < TrackerPanel.ROWS; r++) {
      arr[r] = tracker.getNoteInfo(r);
    }
    return arr;
  }

  /**
   * Convierte semitono + octava al numero MIDI correspondiente y luego a Hz.
   * Usa la misma formula que {@link NotePlayer#midiNote(int, int)}.
   *
   * <p>Frecuencia = 440 Hz * 2^((midiNote - 69) / 12)</p>
   */
  private static double midiFrequency(final int semitone, final int octave) {
    final int midi = NotePlayer.midiNote(semitone, octave);
    return 440.0 * Math.pow(2.0, (midi - 69) / 12.0);
  }
}