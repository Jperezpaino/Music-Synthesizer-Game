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
 * <p>Formato del fichero:
 * <pre>
 * SCALE=EASY_PLUS
 * BPM=120
 * STEPS=0:3,1:5,2:7,...
 * </pre>
 * Cada entrada de STEPS es "fila:columna" de las celdas activas.</p>
 */
public final class MelodyFile {

  private static final String EXT         = "tracker";
  private static final String KEY_SCALE   = "SCALE";
  private static final String KEY_BPM     = "BPM";
  private static final String KEY_STEPS   = "STEPS";

  private MelodyFile() {}

  // -------------------------------------------------------------------------
  // Guardar
  // -------------------------------------------------------------------------

  /**
   * Muestra un dialogo para elegir destino y guarda la melodia actual.
   *
   * @param parent    ventana padre para el dialogo
   * @param tracker   panel con los datos de la melodia
   * @param sequencer motor del sequencer (para leer el BPM actual)
   */
  public static void save(final JFrame parent,
                          final TrackerPanel tracker,
                          final SequencerEngine sequencer) {
    final JFileChooser fc = buildChooser();
    fc.setDialogTitle("Guardar melodia");
    if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

    File file = fc.getSelectedFile();
    if (!file.getName().endsWith("." + EXT)) {
      file = new File(file.getAbsolutePath() + "." + EXT);
    }

    try (final BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
      bw.write(KEY_SCALE + "=" + tracker.getCurrentScale().name());
      bw.newLine();
      bw.write(KEY_BPM + "=" + sequencer.getBpm());
      bw.newLine();

      final StringBuilder sb = new StringBuilder();
      final boolean[][] steps = tracker.getSteps();
      for (int r = 0; r < TrackerPanel.ROWS; r++) {
        for (int c = 0; c < TrackerPanel.COLS; c++) {
          if (steps[r][c]) {
            if (sb.length() > 0) sb.append(',');
            sb.append(r).append(':').append(c);
          }
        }
      }
      bw.write(KEY_STEPS + "=" + sb);
      bw.newLine();

      JOptionPane.showMessageDialog(parent,
          "Melodia guardada en:\n" + file.getName(),
          "Guardado", JOptionPane.INFORMATION_MESSAGE);

    } catch (final IOException ex) {
      JOptionPane.showMessageDialog(parent,
          "Error al guardar:\n" + ex.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  // -------------------------------------------------------------------------
  // Cargar
  // -------------------------------------------------------------------------

  /**
   * Muestra un dialogo para elegir un fichero y carga la melodia en el tracker.
   *
   * @param parent    ventana padre para el dialogo
   * @param tracker   panel donde se cargaran los datos
   * @param sequencer motor del sequencer (para restaurar el BPM)
   * @param bpmUpdater callback que recibe el BPM leido para actualizar la UI
   * @param scaleUpdater callback que recibe la escala leida para actualizar el combobox
   */
  public static void load(final JFrame parent,
                          final TrackerPanel tracker,
                          final SequencerEngine sequencer,
                          final java.util.function.IntConsumer bpmUpdater,
                          final java.util.function.Consumer<TrackerScale> scaleUpdater) {
    final JFileChooser fc = buildChooser();
    fc.setDialogTitle("Cargar melodia");
    if (fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return;

    final File file = fc.getSelectedFile();
    try (final BufferedReader br = new BufferedReader(new FileReader(file))) {
      TrackerScale scale = tracker.getCurrentScale();
      int bpm = sequencer.getBpm();
      final List<int[]> steps = new ArrayList<>();

      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.startsWith(KEY_SCALE + "=")) {
          try { scale = TrackerScale.valueOf(line.substring(KEY_SCALE.length() + 1)); }
          catch (final IllegalArgumentException ignored) { /* mantener actual */ }
        } else if (line.startsWith(KEY_BPM + "=")) {
          try { bpm = Integer.parseInt(line.substring(KEY_BPM.length() + 1)); }
          catch (final NumberFormatException ignored) { /* mantener actual */ }
        } else if (line.startsWith(KEY_STEPS + "=")) {
          final String data = line.substring(KEY_STEPS.length() + 1).trim();
          if (!data.isEmpty()) {
            for (final String token : data.split(",")) {
              final String[] parts = token.split(":");
              if (parts.length == 2) {
                try {
                  final int r = Integer.parseInt(parts[0]);
                  final int c = Integer.parseInt(parts[1]);
                  if (r >= 0 && r < TrackerPanel.ROWS && c >= 0 && c < TrackerPanel.COLS) {
                    steps.add(new int[]{r, c});
                  }
                } catch (final NumberFormatException ignored) { /* saltar */ }
              }
            }
          }
        }
      }

      // Aplicar al tracker
      tracker.clearAll();
      final TrackerScale finalScale = scale;
      tracker.setScale(finalScale);
      if (scaleUpdater != null) scaleUpdater.accept(finalScale);
      for (final int[] rc : steps) {
        tracker.setStep(rc[0], rc[1], true);
      }
      sequencer.setBpm(bpm);
      if (bpmUpdater != null) bpmUpdater.accept(bpm);

    } catch (final IOException ex) {
      JOptionPane.showMessageDialog(parent,
          "Error al cargar:\n" + ex.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private static JFileChooser buildChooser() {
    final JFileChooser fc = new JFileChooser();
    fc.setFileFilter(new FileNameExtensionFilter(
        "Melodia Tracker (*." + EXT + ")", EXT));
    fc.setAcceptAllFileFilterUsed(false);
    return fc;
  }
}