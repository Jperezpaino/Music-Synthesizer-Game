package es.noa.rad.tracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Utilidad estatica para guardar y cargar melodias en formato texto (.tracker).
 *
 * <p>Formato del fichero (multi-canal):
 * <pre>
 * BPM=120
 * CHANNEL_0_PROGRAM=0
 * CHANNEL_0_SCALE=EASY_PLUS
 * CHANNEL_0_STEPS=0:3,1:5,...
 * CHANNEL_1_PROGRAM=25
 * CHANNEL_1_SCALE=MAJOR
 * CHANNEL_1_STEPS=...
 * ...
 * </pre>
 * Retrocompatible con el formato antiguo (SCALE= / STEPS= sin canal).</p>
 */
public final class MelodyFile {

  private static final String EXT         = "tracker";
  private static final String KEY_BPM     = "BPM";

  // Claves multi-canal
  private static final String KEY_CH_PFX     = "CHANNEL_";
  private static final String KEY_PROGRAM    = "_PROGRAM";
  private static final String KEY_SCALE      = "_SCALE";
  private static final String KEY_STEPS      = "_STEPS";       // legacy (bar 0)
  private static final String KEY_BARS       = "_BARS";
  private static final String KEY_BAR_PFX    = "_BAR_";        // _BAR_N_STEPS

  // Claves legacy (ficheros guardados con version anterior)
  private static final String KEY_LEGACY_SCALE = "SCALE";
  private static final String KEY_LEGACY_STEPS = "STEPS";

  private MelodyFile() {}

  // -------------------------------------------------------------------------
  // Guardar como (con dialogo) — multi-canal
  // -------------------------------------------------------------------------

  public static File saveAs(final JFrame parent,
                            final TrackerPanel[] trackers,
                            final SequencerEngine sequencer) {
    final JFileChooser fc = buildChooser();
    fc.setDialogTitle("Guardar melodia como...");
    if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return null;

    File file = fc.getSelectedFile();
    if (!file.getName().endsWith("." + EXT)) {
      file = new File(file.getAbsolutePath() + "." + EXT);
    }
    return writeToFile(file, trackers, sequencer, parent) ? file : null;
  }

  // -------------------------------------------------------------------------
  // Guardar (sobre fichero existente, sin dialogo) — multi-canal
  // -------------------------------------------------------------------------

  public static boolean saveToFile(final File file,
                                   final TrackerPanel[] trackers,
                                   final SequencerEngine sequencer,
                                   final JFrame parent) {
    return writeToFile(file, trackers, sequencer, parent);
  }

  // -------------------------------------------------------------------------
  // Escritura interna — multi-canal
  // -------------------------------------------------------------------------

  private static boolean writeToFile(final File file,
                                     final TrackerPanel[] trackers,
                                     final SequencerEngine sequencer,
                                     final JFrame parent) {
    try (final BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
      bw.write(KEY_BPM + "=" + sequencer.getBpm());
      bw.newLine();

      for (int i = 0; i < trackers.length; i++) {
        final TrackerPanel tp = trackers[i];
        final String pfx = KEY_CH_PFX + i;

        bw.write(pfx + KEY_PROGRAM + "=" + NotePlayer.getProgram(i));
        bw.newLine();
        bw.write(pfx + KEY_SCALE + "=" + tp.getCurrentScale().name());
        bw.newLine();

        final boolean[][][] allBars = tp.getAllBarSteps();
        bw.write(pfx + KEY_BARS + "=" + allBars.length);
        bw.newLine();

        for (int b = 0; b < allBars.length; b++) {
          final StringBuilder sb = new StringBuilder();
          for (int r = 0; r < TrackerPanel.ROWS; r++) {
            for (int c = 0; c < TrackerPanel.COLS; c++) {
              if (allBars[b][r][c]) {
                if (sb.length() > 0) sb.append(',');
                sb.append(r).append(':').append(c);
              }
            }
          }
          bw.write(pfx + KEY_BAR_PFX + b + KEY_STEPS + "=" + sb);
          bw.newLine();
        }
      }
      return true;
    } catch (final IOException ex) {
      JOptionPane.showMessageDialog(parent,
          "Error al guardar:\n" + ex.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  // -------------------------------------------------------------------------
  // Cargar — multi-canal (retrocompatible con formato antiguo)
  // -------------------------------------------------------------------------

  public static File load(final JFrame parent,
                          final TrackerPanel[] trackers,
                          final SequencerEngine sequencer,
                          final java.util.function.IntConsumer bpmUpdater,
                          final java.util.function.Consumer<TrackerScale> scaleUpdater) {
    final JFileChooser fc = buildChooser();
    fc.setDialogTitle("Cargar melodia");
    if (fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return null;

    final File file = fc.getSelectedFile();
    try (final BufferedReader br = new BufferedReader(new FileReader(file))) {

      // --- Estructuras temporales por canal ---
      final int n = trackers.length;
      final TrackerScale[] scales    = new TrackerScale[n];
      final int[]          programs  = new int[n];
      // barStepLists[ch][bar] -> lista de pasos de ese compas
      @SuppressWarnings("unchecked")
      final List<List<int[]>>[] barStepLists = new List[n];
      final int[] numBarsPerChannel = new int[n];
      for (int i = 0; i < n; i++) {
        scales[i]          = trackers[i].getCurrentScale();
        programs[i]        = NotePlayer.getProgram(i);
        barStepLists[i]    = new ArrayList<>();
        numBarsPerChannel[i] = 0;
      }
      int bpm = sequencer.getBpm();
      // Variables para retrocompatibilidad (canal 0)
      TrackerScale legacyScale = null;
      List<int[]>  legacySteps = null;

      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) continue;

        if (line.startsWith(KEY_BPM + "=")) {
          try { bpm = Integer.parseInt(line.substring(KEY_BPM.length() + 1)); }
          catch (final NumberFormatException ignored) { }

        } else if (line.startsWith(KEY_LEGACY_SCALE + "=")) {
          try {
            legacyScale = TrackerScale.valueOf(line.substring(KEY_LEGACY_SCALE.length() + 1));
          } catch (final IllegalArgumentException ignored) { }

        } else if (line.startsWith(KEY_LEGACY_STEPS + "=") && !line.startsWith(KEY_CH_PFX)) {
          legacySteps = parseSteps(line.substring(KEY_LEGACY_STEPS.length() + 1));

        } else if (line.startsWith(KEY_CH_PFX)) {
          for (int i = 0; i < n; i++) {
            final String pfx = KEY_CH_PFX + i;
            if (line.startsWith(pfx + KEY_PROGRAM + "=")) {
              try { programs[i] = Integer.parseInt(
                  line.substring((pfx + KEY_PROGRAM + "=").length())); }
              catch (final NumberFormatException ignored) { }
            } else if (line.startsWith(pfx + KEY_SCALE + "=")) {
              try { scales[i] = TrackerScale.valueOf(
                  line.substring((pfx + KEY_SCALE + "=").length())); }
              catch (final IllegalArgumentException ignored) { }
            } else if (line.startsWith(pfx + KEY_BARS + "=")) {
              try { numBarsPerChannel[i] = Integer.parseInt(
                  line.substring((pfx + KEY_BARS + "=").length())); }
              catch (final NumberFormatException ignored) { }
            } else if (line.startsWith(pfx + KEY_BAR_PFX)) {
              // CHANNEL_i_BAR_b_STEPS=...
              final String afterBar = line.substring((pfx + KEY_BAR_PFX).length());
              final int stepIdx = afterBar.indexOf(KEY_STEPS + "=");
              if (stepIdx >= 0) {
                try {
                  final int barIdx = Integer.parseInt(afterBar.substring(0, stepIdx));
                  final List<int[]> steps = parseSteps(
                      afterBar.substring(stepIdx + KEY_STEPS.length() + 1));
                  // Expandir la lista hasta el indice necesario
                  while (barStepLists[i].size() <= barIdx) {
                    barStepLists[i].add(new ArrayList<>());
                  }
                  barStepLists[i].set(barIdx, steps);
                } catch (final NumberFormatException ignored) { }
              }
            } else if (line.startsWith(pfx + KEY_STEPS + "=")) {
              // Formato viejo multi-canal sin barra -> bar 0
              final List<int[]> steps = parseSteps(
                  line.substring((pfx + KEY_STEPS + "=").length()));
              if (barStepLists[i].isEmpty()) barStepLists[i].add(steps);
              else barStepLists[i].set(0, steps);
            }
          }
        }
      }

      // Retrocompatibilidad
      if (legacyScale != null) scales[0] = legacyScale;
      if (legacySteps != null) {
        if (barStepLists[0].isEmpty()) barStepLists[0].add(legacySteps);
        else barStepLists[0].set(0, legacySteps);
      }

      // Aplicar a todos los canales
      for (int i = 0; i < n; i++) {
        trackers[i].clearAll();
        trackers[i].setScale(scales[i]);
        NotePlayer.setProgram(i, programs[i]);

        final int nb = Math.max(1, barStepLists[i].size());
        final boolean[][][] allBars = new boolean[nb][TrackerPanel.ROWS][TrackerPanel.COLS];
        for (int b = 0; b < barStepLists[i].size(); b++) {
          for (final int[] rc : barStepLists[i].get(b)) {
            allBars[b][rc[0]][rc[1]] = true;
          }
        }
        trackers[i].setAllBarSteps(allBars);
      }
      sequencer.setBpm(bpm);
      if (bpmUpdater   != null) bpmUpdater.accept(bpm);
      if (scaleUpdater != null) scaleUpdater.accept(scales[0]);
      return file;

    } catch (final IOException ex) {
      JOptionPane.showMessageDialog(parent,
          "Error al cargar:\n" + ex.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
      return null;
    }
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private static List<int[]> parseSteps(final String data) {
    final List<int[]> list = new ArrayList<>();
    if (data == null || data.isEmpty()) return list;
    for (final String token : data.split(",")) {
      final String[] parts = token.split(":");
      if (parts.length == 2) {
        try {
          final int r = Integer.parseInt(parts[0].trim());
          final int c = Integer.parseInt(parts[1].trim());
          if (r >= 0 && r < TrackerPanel.ROWS && c >= 0 && c < TrackerPanel.COLS) {
            list.add(new int[]{r, c});
          }
        } catch (final NumberFormatException ignored) { }
      }
    }
    return list;
  }

  private static JFileChooser buildChooser() {
    final JFileChooser fc = new JFileChooser();
    fc.setFileFilter(new FileNameExtensionFilter(
        "Melodia Tracker (*." + EXT + ")", EXT));
    fc.setAcceptAllFileFilterUsed(false);
    return fc;
  }
}