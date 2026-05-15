package es.noa.rad.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import es.noa.rad.sequencer.Sequencer;
import es.noa.rad.sequencer.Sequencer.SequencerListener;
import es.noa.rad.sequencer.SequencerStep;
import es.noa.rad.synth.Note;
import es.noa.rad.synth.SoundEngine;

/**
 * Panel de secuenciador por pasos (16 steps).
 *
 * <p>Muestra una fila de 16 botones toggle. El usuario puede activar/desactivar
 * cada paso y asignarle una nota mediante un combo contextual.
 * Un indicador luminoso avanza en tiempo real mostrando el paso en reproducción.</p>
 */
public final class SequencerPanel extends JPanel implements SequencerListener {

  private static final long serialVersionUID = 1L;

  // Colores
  private static final Color COLOR_BG         = new Color(18, 18, 26);
  private static final Color COLOR_SECTION    = new Color(28, 28, 40);
  private static final Color COLOR_STEP_OFF   = new Color(45, 45, 65);
  private static final Color COLOR_STEP_ON    = new Color(70, 140, 220);
  private static final Color COLOR_STEP_CUR   = new Color(255, 210,  50);
  private static final Color COLOR_STEP_BOTH  = new Color(100, 200, 255);
  private static final Color COLOR_BTN_PLAY   = new Color(50,  160,  80);
  private static final Color COLOR_BTN_STOP   = new Color(180,  60,  60);
  private static final Color COLOR_BTN_CLEAR  = new Color(80,   80, 100);
  private static final Color COLOR_TEXT       = new Color(200, 200, 220);
  private static final Color COLOR_ACCENT     = new Color(100, 180, 255);
  private static final Color COLOR_BORDER     = new Color(55,  55,  75);

  private final Sequencer   sequencer;
  private final StepButton[] stepButtons = new StepButton[Sequencer.STEP_COUNT];

  private int  currentStep = -1;

  // Controles
  private JButton   btnPlay;
  private JButton   btnStop;
  private JButton   btnClear;
  private JSlider   bpmSlider;
  private JLabel    bpmLabel;

  // Selector de nota global (se aplica a los pasos que se activan a partir de ahora)
  private JComboBox<Note> noteCombo;

  /**
   * Crea el panel del secuenciador.
   *
   * @param soundEngine Motor de audio compartido
   */
  public SequencerPanel(final SoundEngine soundEngine) {
    this.sequencer = new Sequencer(soundEngine);
    this.sequencer.addListener(this);

    this.setBackground(COLOR_BG);
    this.setLayout(new BorderLayout(0, 4));
    this.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER),
        BorderFactory.createEmptyBorder(6, 8, 6, 8)
    ));

    this.add(this.buildTitleBar(),  BorderLayout.NORTH);
    this.add(this.buildStepGrid(),  BorderLayout.CENTER);
    this.add(this.buildControls(),  BorderLayout.SOUTH);
  }

  // -------------------------------------------------------------------------
  // Construcción UI
  // -------------------------------------------------------------------------

  private JPanel buildTitleBar() {
    final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    p.setBackground(COLOR_BG);

    final JLabel title = new JLabel("◼  SECUENCIADOR  16 PASOS");
    title.setFont(new Font("SansSerif", Font.BOLD, 13));
    title.setForeground(COLOR_ACCENT);
    p.add(title);

    // Selector de nota para asignar a pasos
    final JLabel noteLbl = new JLabel("Nota:");
    noteLbl.setForeground(COLOR_TEXT);
    noteLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));

    this.noteCombo = new JComboBox<>(Note.values());
    this.noteCombo.setSelectedItem(Note.C4);
    this.noteCombo.setBackground(COLOR_SECTION);
    this.noteCombo.setForeground(COLOR_TEXT);
    this.noteCombo.setFont(new Font("SansSerif", Font.BOLD, 11));
    this.noteCombo.setPreferredSize(new Dimension(90, 24));
    this.noteCombo.setFocusable(false);

    p.add(noteLbl);
    p.add(this.noteCombo);
    return p;
  }

  private JPanel buildStepGrid() {
    final JPanel grid = new JPanel(new GridLayout(1, Sequencer.STEP_COUNT, 4, 0));
    grid.setBackground(COLOR_BG);
    grid.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

    for (int i = 0; i < Sequencer.STEP_COUNT; i++) {
      final int idx = i;
      final SequencerStep step = this.sequencer.getStep(i);

      final StepButton btn = new StepButton(i);
      btn.setActive(step.isActive());
      btn.addActionListener(e -> this.onStepClicked(idx, btn, step));

      this.stepButtons[i] = btn;
      grid.add(btn);
    }
    return grid;
  }

  private JPanel buildControls() {
    final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
    p.setBackground(COLOR_BG);

    // Botones de transporte
    this.btnPlay  = makeButton("▶  PLAY",  COLOR_BTN_PLAY);
    this.btnStop  = makeButton("⏹  STOP",  COLOR_BTN_STOP);
    this.btnClear = makeButton("🗑  LIMPIAR", COLOR_BTN_CLEAR);

    this.btnPlay.addActionListener(e  -> this.onPlay());
    this.btnStop.addActionListener(e  -> this.onStop());
    this.btnClear.addActionListener(e -> this.onClear());

    this.btnStop.setEnabled(false);

    // BPM slider
    final JLabel bpmTitle = new JLabel("BPM:");
    bpmTitle.setForeground(COLOR_TEXT);
    bpmTitle.setFont(new Font("SansSerif", Font.BOLD, 12));

    this.bpmSlider = new JSlider(Sequencer.BPM_MIN, Sequencer.BPM_MAX, Sequencer.BPM_DEFAULT);
    this.bpmSlider.setBackground(COLOR_BG);
    this.bpmSlider.setForeground(COLOR_TEXT);
    this.bpmSlider.setPreferredSize(new Dimension(160, 24));
    this.bpmSlider.setPaintTicks(false);
    this.bpmSlider.setPaintLabels(false);
    this.bpmSlider.setFocusable(false);
    this.bpmSlider.addChangeListener(e -> this.onBpmChanged());

    this.bpmLabel = new JLabel(Sequencer.BPM_DEFAULT + " BPM");
    this.bpmLabel.setForeground(COLOR_ACCENT);
    this.bpmLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
    this.bpmLabel.setPreferredSize(new Dimension(70, 20));

    p.add(this.btnPlay);
    p.add(this.btnStop);
    p.add(this.btnClear);

    final JLabel sep = new JLabel("  |  ");
    sep.setForeground(new Color(70, 70, 90));
    p.add(sep);

    p.add(bpmTitle);
    p.add(this.bpmSlider);
    p.add(this.bpmLabel);
    return p;
  }

  // -------------------------------------------------------------------------
  // Manejadores de eventos
  // -------------------------------------------------------------------------

  private void onStepClicked(final int idx, final StepButton btn, final SequencerStep step) {
    step.toggle();
    // Asignar la nota seleccionada en el combo al activar el paso
    if (step.isActive()) {
      final Note selected = (Note) this.noteCombo.getSelectedItem();
      if (selected != null) {
        step.setNote(selected);
      }
    }
    btn.setActive(step.isActive());
    btn.repaint();
  }

  private void onPlay() {
    this.sequencer.start();
    this.btnPlay.setEnabled(false);
    this.btnStop.setEnabled(true);
  }

  private void onStop() {
    this.sequencer.stop();
    // onStopped() se llamará vía listener
  }

  private void onClear() {
    this.sequencer.clearAll();
    for (final StepButton btn : this.stepButtons) {
      btn.setActive(false);
      btn.repaint();
    }
  }

  private void onBpmChanged() {
    final int bpm = this.bpmSlider.getValue();
    this.sequencer.setBpm(bpm);
    this.bpmLabel.setText(bpm + " BPM");
  }

  // -------------------------------------------------------------------------
  // SequencerListener
  // -------------------------------------------------------------------------

  @Override
  public void onStep(final int stepIndex) {
    SwingUtilities.invokeLater(() -> {
      // Quitar resaltado del paso anterior
      if (this.currentStep >= 0 && this.currentStep < Sequencer.STEP_COUNT) {
        this.stepButtons[this.currentStep].setCurrent(false);
        this.stepButtons[this.currentStep].repaint();
      }
      this.currentStep = stepIndex;
      this.stepButtons[stepIndex].setCurrent(true);
      this.stepButtons[stepIndex].repaint();
    });
  }

  @Override
  public void onStopped() {
    SwingUtilities.invokeLater(() -> {
      // Apagar todos los indicadores
      if (this.currentStep >= 0 && this.currentStep < Sequencer.STEP_COUNT) {
        this.stepButtons[this.currentStep].setCurrent(false);
        this.stepButtons[this.currentStep].repaint();
      }
      this.currentStep = -1;
      this.btnPlay.setEnabled(true);
      this.btnStop.setEnabled(false);
    });
  }

  // -------------------------------------------------------------------------
  // Clases internas
  // -------------------------------------------------------------------------

  /**
   * Botón de un paso del secuenciador: pintado a mano para control total del aspecto.
   */
  private static final class StepButton extends JButton {

    private static final long serialVersionUID = 1L;

    private final int index;
    private boolean active;
    private boolean current;

    StepButton(final int index) {
      this.index = index;
      this.setFocusable(false);
      this.setBorderPainted(false);
      this.setContentAreaFilled(false);
      this.setPreferredSize(new Dimension(38, 52));
      this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    void setActive(final boolean active)   { this.active  = active; }
    void setCurrent(final boolean current) { this.current = current; }

    @Override
    protected void paintComponent(final Graphics g) {
      final Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      final int w = this.getWidth();
      final int h = this.getHeight();
      final int arc = 8;

      // Fondo del botón
      Color bg;
      if (this.current && this.active) {
        bg = COLOR_STEP_BOTH;
      } else if (this.current) {
        bg = COLOR_STEP_CUR;
      } else if (this.active) {
        bg = COLOR_STEP_ON;
      } else {
        bg = COLOR_STEP_OFF;
      }

      g2.setColor(bg);
      g2.fillRoundRect(0, 0, w, h, arc, arc);

      // Borde
      g2.setColor(this.current ? COLOR_STEP_CUR.brighter() : COLOR_BORDER);
      g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

      // Número del paso
      g2.setFont(new Font("SansSerif", Font.BOLD, 10));
      g2.setColor(this.active || this.current ? Color.WHITE : new Color(120, 120, 150));
      final String num = String.valueOf(this.index + 1);
      final int strW = g2.getFontMetrics().stringWidth(num);
      g2.drawString(num, (w - strW) / 2, h - 6);

      // Punto indicador en la parte superior
      if (this.active) {
        g2.setColor(this.current ? Color.WHITE : COLOR_STEP_ON.brighter());
        g2.fillOval((w - 8) / 2, 6, 8, 8);
      }

      g2.dispose();
    }
  }

  // -------------------------------------------------------------------------
  // Helper
  // -------------------------------------------------------------------------

  private static JButton makeButton(final String text, final Color bg) {
    final JButton btn = new JButton(text);
    btn.setBackground(bg);
    btn.setForeground(Color.WHITE);
    btn.setFont(new Font("SansSerif", Font.BOLD, 12));
    btn.setFocusable(false);
    btn.setBorderPainted(false);
    btn.setPreferredSize(new Dimension(120, 32));
    btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    return btn;
  }

}
