package es.noa.rad.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import es.noa.rad.drumachine.DrumMachine;
import es.noa.rad.drumachine.DrumVoice;

/**
 * Panel visual de la caja de ritmos.
 *
 * <ul>
 *   <li>4 filas (una por {@link DrumVoice}) × 16 columnas de botones iluminados.</li>
 *   <li>Columna izquierda: nombre de la voz con su color + slider de volumen.</li>
 *   <li>Fila inferior: controles de transporte y BPM.</li>
 *   <li>El paso actual se resalta en amarillo durante la reproducción.</li>
 * </ul>
 */
public final class DrumMachinePanel extends JPanel implements DrumMachine.DrumMachineListener {

  private static final long serialVersionUID = 1L;

  // -------------------------------------------------------------------------
  // Colores y fuentes de la UI
  // -------------------------------------------------------------------------
  private static final Color BG_COLOR       = new Color(20, 20, 30);
  private static final Color CELL_OFF       = new Color(45, 45, 65);
  private static final Color CELL_ACTIVE    = new Color(70, 70, 100);  // encendido pero no paso actual
  private static final Color CELL_CURRENT   = new Color(230, 200, 40); // paso actual
  private static final Color LABEL_FG       = new Color(200, 200, 210);
  private static final Font  LABEL_FONT     = new Font("Monospaced", Font.BOLD, 11);
  private static final Font  TITLE_FONT     = new Font("Monospaced", Font.BOLD, 13);
  private static final Font  BTN_FONT       = new Font("SansSerif",  Font.BOLD, 12);

  // -------------------------------------------------------------------------
  // Modelo
  // -------------------------------------------------------------------------
  private final DrumMachine machine;
  private int currentStep = -1;

  // -------------------------------------------------------------------------
  // Widgets de la cuadrícula
  // -------------------------------------------------------------------------
  /** stepButtons[track][step] */
  private final StepButton[][] stepButtons = new StepButton[DrumMachine.TRACK_COUNT][DrumMachine.STEP_COUNT];

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  /**
   * Construye el panel y lo conecta al motor dado.
   *
   * @param machine Motor de la caja de ritmos (nunca {@code null})
   */
  public DrumMachinePanel(final DrumMachine machine) {
    this.machine = machine;
    machine.addListener(this);

    setBackground(BG_COLOR);
    setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(new Color(80, 80, 120), 1),
        "🥁  DRUM MACHINE",
        0, 0, TITLE_FONT, new Color(160, 140, 220)
    ));

    setLayout(new BorderLayout(6, 6));
    add(buildGridPanel(),    BorderLayout.CENTER);
    add(buildControlPanel(), BorderLayout.SOUTH);
  }

  // =========================================================================
  // Construcción UI
  // =========================================================================

  private JPanel buildGridPanel() {
    final DrumVoice[] voices = DrumVoice.values();

    // Panel exterior: fila de etiqueta + grid por cada voz
    final JPanel outer = new JPanel();
    outer.setBackground(BG_COLOR);
    outer.setLayout(new GridLayout(DrumMachine.TRACK_COUNT, 1, 2, 2));

    for (int t = 0; t < DrumMachine.TRACK_COUNT; t++) {
      final int track = t;
      outer.add(buildTrackRow(voices[t], track));
    }
    return outer;
  }

  private JPanel buildTrackRow(final DrumVoice voice, final int track) {
    final JPanel row = new JPanel(new BorderLayout(4, 0));
    row.setBackground(BG_COLOR);

    // ---- Etiqueta de voz (izquierda, ancho fijo) ----
    final JLabel lbl = new JLabel(voice.getName(), SwingConstants.CENTER);
    lbl.setFont(LABEL_FONT);
    lbl.setForeground(voice.getColor());
    lbl.setPreferredSize(new Dimension(64, 26));
    row.add(lbl, BorderLayout.WEST);

    // ---- 16 botones de paso ----
    final JPanel stepsPanel = new JPanel(new GridLayout(1, DrumMachine.STEP_COUNT, 2, 0));
    stepsPanel.setBackground(BG_COLOR);
    for (int s = 0; s < DrumMachine.STEP_COUNT; s++) {
      final int step = s;
      final StepButton btn = new StepButton(voice.getColor());
      btn.addActionListener(e -> {
        machine.toggleStep(track, step);
        btn.setActive(machine.getStep(track, step));
      });
      // Separador visual entre grupos de 4
      if (s > 0 && s % 4 == 0) {
        stepsPanel.add(Box.createHorizontalStrut(4));
      }
      stepButtons[track][step] = btn;
      stepsPanel.add(btn);
    }
    row.add(stepsPanel, BorderLayout.CENTER);

    // ---- Slider de volumen (derecha) ----
    final JSlider volSlider = new JSlider(0, 100, (int) (machine.getVolume(track) * 100));
    volSlider.setBackground(BG_COLOR);
    volSlider.setForeground(LABEL_FG);
    volSlider.setPreferredSize(new Dimension(70, 26));
    volSlider.setToolTipText("Volumen: " + voice.getName());
    volSlider.addChangeListener(e -> machine.setVolume(track, volSlider.getValue() / 100.0));
    row.add(volSlider, BorderLayout.EAST);

    return row;
  }

  private JPanel buildControlPanel() {
    final JPanel panel = new JPanel();
    panel.setBackground(BG_COLOR);
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

    // --- Botón PLAY ---
    final JButton btnPlay = new JButton("▶ PLAY");
    styleButton(btnPlay, new Color(50, 180, 80));
    btnPlay.addActionListener(e -> machine.start());

    // --- Botón STOP ---
    final JButton btnStop = new JButton("⏹ STOP");
    styleButton(btnStop, new Color(200, 60, 60));
    btnStop.addActionListener(e -> machine.stop());

    // --- Botón CLEAR ---
    final JButton btnClear = new JButton("🗑 CLEAR");
    styleButton(btnClear, new Color(100, 80, 40));
    btnClear.addActionListener(e -> {
      machine.clearAll();
      refreshAllButtons();
    });

    // --- Slider BPM ---
    final JLabel bpmLabel = new JLabel("BPM: " + machine.getBpm());
    bpmLabel.setFont(LABEL_FONT);
    bpmLabel.setForeground(LABEL_FG);

    final JSlider bpmSlider = new JSlider(
        DrumMachine.BPM_MIN, DrumMachine.BPM_MAX, machine.getBpm()
    );
    bpmSlider.setBackground(BG_COLOR);
    bpmSlider.setForeground(LABEL_FG);
    bpmSlider.setPreferredSize(new Dimension(120, 26));
    bpmSlider.setMaximumSize(new Dimension(130, 30));
    bpmSlider.addChangeListener(e -> {
      machine.setBpm(bpmSlider.getValue());
      bpmLabel.setText("BPM: " + bpmSlider.getValue());
    });

    panel.add(Box.createHorizontalStrut(4));
    panel.add(btnPlay);
    panel.add(Box.createHorizontalStrut(4));
    panel.add(btnStop);
    panel.add(Box.createHorizontalStrut(4));
    panel.add(btnClear);
    panel.add(Box.createHorizontalStrut(12));
    panel.add(bpmLabel);
    panel.add(Box.createHorizontalStrut(4));
    panel.add(bpmSlider);
    panel.add(Box.createHorizontalGlue());

    return panel;
  }

  // =========================================================================
  // Helpers UI
  // =========================================================================

  private static void styleButton(final JButton btn, final Color accent) {
    btn.setFont(BTN_FONT);
    btn.setBackground(accent.darker());
    btn.setForeground(Color.WHITE);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setOpaque(true);
  }

  private void refreshAllButtons() {
    for (int t = 0; t < DrumMachine.TRACK_COUNT; t++) {
      for (int s = 0; s < DrumMachine.STEP_COUNT; s++) {
        if (stepButtons[t][s] != null) {
          stepButtons[t][s].setActive(machine.getStep(t, s));
        }
      }
    }
  }

  // =========================================================================
  // DrumMachineListener
  // =========================================================================

  @Override
  public void onBeat(final int stepIndex) {
    SwingUtilities.invokeLater(() -> {
      this.currentStep = stepIndex;
      // Actualizar aspecto de todos los botones de columna anterior y actual
      repaint();
    });
  }

  @Override
  public void onStopped() {
    SwingUtilities.invokeLater(() -> {
      this.currentStep = -1;
      repaint();
    });
  }

  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);
    // Resaltar columna del paso actual en todos los botones
    for (int t = 0; t < DrumMachine.TRACK_COUNT; t++) {
      for (int s = 0; s < DrumMachine.STEP_COUNT; s++) {
        if (stepButtons[t][s] != null) {
          stepButtons[t][s].setCurrent(s == currentStep);
        }
      }
    }
  }

  // =========================================================================
  // Botón de paso personalizado
  // =========================================================================

  /**
   * Botón cuadrado iluminado para la cuadrícula de la caja de ritmos.
   */
  private static final class StepButton extends JButton {

    private static final long serialVersionUID = 1L;

    private boolean active  = false;
    private boolean current = false;
    private final Color accentColor;

    StepButton(final Color accentColor) {
      this.accentColor = accentColor;
      setPreferredSize(new Dimension(26, 26));
      setMinimumSize(new Dimension(18, 18));
      setContentAreaFilled(false);
      setBorderPainted(false);
      setFocusPainted(false);
      setOpaque(false);
    }

    void setActive(final boolean active) {
      this.active = active;
      repaint();
    }

    void setCurrent(final boolean current) {
      if (this.current != current) {
        this.current = current;
        repaint();
      }
    }

    @Override
    protected void paintComponent(final Graphics g) {
      final Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      final int w = getWidth();
      final int h = getHeight();
      final int arc = 4;

      if (current) {
        // Paso actual: fondo amarillo brillante
        g2.setPaint(new GradientPaint(0, 0, CELL_CURRENT, 0, h, CELL_CURRENT.darker()));
        g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
        // Borde luminoso
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
      } else if (active) {
        // Paso activo: color de la voz
        g2.setPaint(new GradientPaint(0, 0, accentColor, 0, h, accentColor.darker().darker()));
        g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
        g2.setColor(accentColor.brighter());
        g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
      } else {
        // Paso inactivo
        g2.setColor(CELL_OFF);
        g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
        g2.setColor(CELL_ACTIVE);
        g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
      }

      // Indicador de hover del modelo
      if (getModel().isRollover()) {
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
      }

      // Dibuja el número del paso (pequeño, centrado)
      g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
      g2.setColor(current ? Color.BLACK : new Color(120, 120, 150));
      final FontMetrics fm  = g2.getFontMetrics();
      final String      txt = String.valueOf(getActionCommand().isEmpty() ? "" : "");
      final int         tx  = (w - fm.stringWidth(txt)) / 2;
      final int         ty  = (h + fm.getAscent() - fm.getDescent()) / 2;
      g2.drawString(txt, tx, ty);

      g2.dispose();
    }
  }
}
