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
 * Exporta todos los canales activos del Tracker a un fichero WAV stereo 44100 Hz / 16-bit.
 *
 * <p>Panorama fijo por canal: 0=izq, 1=centro-izq, 2=centro-der, 3=der.</p>
 */
public final class WavExporter {

  private static final int    SAMPLE_RATE = 44100;
  private static final double AMPLITUDE   = 0.55;
  private static final int    ATTACK_MS   = 8;
  private static final int    RELEASE_MS  = 40;

  /** Panorama L/R por canal (0.5 = centro para todos, sin diferenciacion estereo). */
  private static final double[] PAN = { 0.5, 0.5, 0.5, 0.5 };

  private WavExporter() {}

  // -----------------------------------------------------------------------
  // API publica
  // -----------------------------------------------------------------------

  public static void export(final JFrame parent,
                            final TrackerPanel[] trackers,
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

    final int n = trackers.length;
    final boolean[][][][]           allBars  = new boolean[n][][][];
    final TrackerPanel.NoteInfo[][] allNotes = new TrackerPanel.NoteInfo[n][];
    for (int i = 0; i < n; i++) {
      allBars[i]  = trackers[i].getAllBarSteps();
      allNotes[i] = snapshotNotes(trackers[i]);
    }
    final int  bpm  = sequencer.getBpm();
    final File dest = file;

    new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() throws Exception {
        synthesizeStereo(dest, allBars, allNotes, bpm);
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

  // -----------------------------------------------------------------------
  // Sintesis PCM stereo
  // -----------------------------------------------------------------------

  private static void synthesizeStereo(final File dest,
                                        final boolean[][][][] allBars,
                                        final TrackerPanel.NoteInfo[][] allNotes,
                                        final int bpm) throws Exception {
    final double stepSec        = 60.0 / bpm / 4.0;
    final int    samplesPerStep = (int) Math.round(SAMPLE_RATE * stepSec);

    // Calcular el maximo de compases entre todos los canales
    int maxBars = 1;
    for (final boolean[][][] chBars : allBars) {
      if (chBars.length > maxBars) maxBars = chBars.length;
    }
    final int totalSamples = samplesPerStep * TrackerPanel.COLS * maxBars;

    final double[] pcmL      = new double[totalSamples];
    final double[] pcmR      = new double[totalSamples];
    final int      attackSmp = Math.max(1, (int) (SAMPLE_RATE * ATTACK_MS  / 1000.0));
    final int      relSmp    = Math.max(1, (int) (SAMPLE_RATE * RELEASE_MS / 1000.0));

    final int n = allBars.length;

    for (int ch = 0; ch < n; ch++) {
      final double pan   = ch < PAN.length ? PAN[ch] : 0.5;
      final double gainL = AMPLITUDE * Math.sqrt(1.0 - pan);
      final double gainR = AMPLITUDE * Math.sqrt(pan);

      final int chBars = allBars[ch].length;
      for (int bar = 0; bar < chBars; bar++) {
        for (int col = 0; col < TrackerPanel.COLS; col++) {
          final List<Double> freqs = new ArrayList<>();
          for (int row = 0; row < TrackerPanel.ROWS; row++) {
            if (allBars[ch][bar][row][col] && allNotes[ch][row] != null) {
              freqs.add(midiFrequency(allNotes[ch][row].semitone, allNotes[ch][row].octave));
            }
          }
          if (freqs.isEmpty()) continue;

          final double noteGain = 1.0 / Math.sqrt(freqs.size());
          final int    colBase  = (bar * TrackerPanel.COLS + col) * samplesPerStep;

        for (int s = 0; s < samplesPerStep; s++) {
          final double env;
          if (s < attackSmp) {
            env = (double) s / attackSmp;
          } else if (s >= samplesPerStep - relSmp) {
            env = (double) (samplesPerStep - 1 - s) / relSmp;
          } else {
            env = 1.0;
          }
          double sample = 0.0;
          final double t = (double) s / SAMPLE_RATE;
          for (final double freq : freqs) {
            sample += Math.sin(2.0 * Math.PI * freq * t);
          }
          final double out = sample * noteGain * env;
          pcmL[colBase + s] += out * gainL;
          pcmR[colBase + s] += out * gainR;
        }
        } // end for col
      } // end for bar
    } // end for ch

    // Normalizar conjuntamente
    double peak = 0.0;
    for (int i = 0; i < totalSamples; i++) {
      peak = Math.max(peak, Math.abs(pcmL[i]));
      peak = Math.max(peak, Math.abs(pcmR[i]));
    }
    final double norm = (peak > 0.001) ? (0.92 / peak) : 1.0;

    // Intercalado stereo L,R (16-bit LE)
    final byte[] bytes = new byte[totalSamples * 4];
    for (int i = 0; i < totalSamples; i++) {
      final int vL = (int) Math.max(-32768, Math.min(32767, pcmL[i] * norm * 32767.0));
      final int vR = (int) Math.max(-32768, Math.min(32767, pcmR[i] * norm * 32767.0));
      bytes[i * 4]     = (byte) ( vL       & 0xFF);
      bytes[i * 4 + 1] = (byte) ((vL >> 8) & 0xFF);
      bytes[i * 4 + 2] = (byte) ( vR       & 0xFF);
      bytes[i * 4 + 3] = (byte) ((vR >> 8) & 0xFF);
    }

    final AudioFormat fmt = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        SAMPLE_RATE, 16, 2, 4, SAMPLE_RATE, false);

    try (final AudioInputStream ais = new AudioInputStream(
            new ByteArrayInputStream(bytes), fmt, totalSamples)) {
      AudioSystem.write(ais, AudioFileFormat.Type.WAVE, dest);
    }
  }

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------

  private static TrackerPanel.NoteInfo[] snapshotNotes(final TrackerPanel tracker) {
    final TrackerPanel.NoteInfo[] arr = new TrackerPanel.NoteInfo[TrackerPanel.ROWS];
    for (int r = 0; r < TrackerPanel.ROWS; r++) {
      arr[r] = tracker.getNoteInfo(r);
    }
    return arr;
  }

  private static double midiFrequency(final int semitone, final int octave) {
    final int midi = NotePlayer.midiNote(semitone, octave);
    return 440.0 * Math.pow(2.0, (midi - 69) / 12.0);
  }
}