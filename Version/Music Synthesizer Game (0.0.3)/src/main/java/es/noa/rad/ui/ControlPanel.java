package es.noa.rad.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import es.noa.rad.synth.SoundEngine;
import es.noa.rad.synth.Waveform;

/**
 * Panel de controles del sintetizador.
 *
 * <p>Permite ajustar:
 * <ul>
 *   <li>Forma de onda (SINE, SQUARE, SAWTOOTH, TRIANGLE)</li>
 *   <li>Volumen (0 - 100%)</li>
 *   <li>Octava base del teclado (3, 4 o 5)</li>
 * </ul>
 * </p>
 */
public final class ControlPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  // --- Colores ---
  private static final Color COLOR_BACKGROUND  = new Color(28, 28, 35);
  private static final Color COLOR_PANEL        = new Color(45, 45, 55);
  private static final Color COLOR_TEXT         = new Color(220, 220, 230);
  private static final Color COLOR_ACCENT       = new Color(80, 160, 255);
  private static final Color COLOR_BUTTON       = new Color(60, 60, 75);
  private static final Color COLOR_BUTTON_HOVER = new Color(80, 160, 255);

  /** Volumen inicial (%) */
  private static final int DEFAULT_VOLUME = 70;

  /** Octava mínima */
  private static final int OCT_MIN = 3;

  /** Octava máxima */
  private static final int OCT_MAX = 5;

  /** Octava inicial */
  private static final int OCT_DEFAULT = 4;

  /** Motor de audio */
  private final SoundEngine soundEngine;

  /** Teclado al que notificar cambios de octava */
  private KeyboardPanel keyboardPanel;

  /** Label que muestra la octava actual */
  private JLabel octaveLabel;

  /** Octava actual */
  private int currentOctave = OCT_DEFAULT;

  /**
   * Crea el panel de controles.
   *
   * @param soundEngine Motor de audio a controlar
   */
  public ControlPanel(final SoundEngine soundEngine) {
    this.soundEngine = soundEngine;
    this.setBackground(COLOR_BACKGROUND);
    this.setLayout(new GridBagLayout());
    this.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    this.setPreferredSize(new Dimension(1280, 110));

    this.buildUI();
  }

  /**
   * Vincula el teclado para que responda a cambios de octava.
   *
   * @param keyboardPanel Panel del teclado
   */
  public void setKeyboardPanel(final KeyboardPanel keyboardPanel) {
    this.keyboardPanel = keyboardPanel;
  }

  /**
   * Construye todos los componentes del panel.
   */
  private void buildUI() {
    final GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets  = new Insets(6, 16, 6, 16);
    gbc.anchor  = GridBagConstraints.CENTER;
    gbc.fill    = GridBagConstraints.NONE;
    gbc.gridy   = 0;

    // --- Sección: Forma de onda ---
    gbc.gridx = 0;
    this.add(this.buildSection("FORMA DE ONDA", this.buildWaveformCombo()), gbc);

    // --- Sección: Volumen ---
    gbc.gridx = 1;
    this.add(this.buildSection("VOLUMEN", this.buildVolumeSlider()), gbc);

    // --- Sección: Octava ---
    gbc.gridx = 2;
    this.add(this.buildSection("OCTAVA", this.buildOctaveControl()), gbc);

    // --- Sección: Título ---
    gbc.gridx = 3;
    gbc.weightx = 1.0;
    gbc.anchor  = GridBagConstraints.EAST;
    this.add(this.buildTitle(), gbc);
  }

  /**
   * Crea un panel de sección con título y control.
   */
  private JPanel buildSection(final String title, final java.awt.Component control) {
    final JPanel panel = new JPanel(new java.awt.BorderLayout(0, 4));
    panel.setBackground(COLOR_PANEL);
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(COLOR_ACCENT.darker(), 1),
        BorderFactory.createEmptyBorder(8, 12, 8, 12)
    ));

    final JLabel label = new JLabel(title, SwingConstants.CENTER);
    label.setFont(new Font("SansSerif", Font.BOLD, 10));
    label.setForeground(COLOR_ACCENT);

    panel.add(label,   java.awt.BorderLayout.NORTH);
    panel.add(control, java.awt.BorderLayout.CENTER);
    return panel;
  }

  /**
   * Crea el selector de forma de onda.
   */
  private JComboBox<String> buildWaveformCombo() {
    final String[] items = { "SINE  ∿", "SQUARE  ⊓", "SAWTOOTH  ⊿", "TRIANGLE  △" };
    final JComboBox<String> combo = new JComboBox<>(items);
    combo.setBackground(COLOR_BUTTON);
    combo.setForeground(COLOR_TEXT);
    combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
    combo.setPreferredSize(new Dimension(170, 30));
    combo.setFocusable(false);

    combo.addActionListener(e -> {
      final Waveform wf = Waveform.values()[combo.getSelectedIndex()];
      this.soundEngine.setWaveform(wf);
    });

    return combo;
  }

  /**
   * Crea el slider de volumen.
   */
  private JPanel buildVolumeSlider() {
    final JSlider slider = new JSlider(0, 100, DEFAULT_VOLUME);
    slider.setBackground(COLOR_PANEL);
    slider.setForeground(COLOR_TEXT);
    slider.setPreferredSize(new Dimension(180, 40));
    slider.setPaintTicks(true);
    slider.setMajorTickSpacing(25);
    slider.setFocusable(false);

    final JLabel valueLabel = new JLabel(DEFAULT_VOLUME + "%", SwingConstants.CENTER);
    valueLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
    valueLabel.setForeground(COLOR_ACCENT);

    slider.addChangeListener(e -> {
      final double vol = slider.getValue() / 100.0;
      this.soundEngine.setVolume(vol);
      valueLabel.setText(slider.getValue() + "%");
    });

    final JPanel panel = new JPanel(new java.awt.BorderLayout(0, 2));
    panel.setBackground(COLOR_PANEL);
    panel.add(slider,     java.awt.BorderLayout.CENTER);
    panel.add(valueLabel, java.awt.BorderLayout.SOUTH);
    return panel;
  }

  /**
   * Crea los botones de control de octava.
   */
  private JPanel buildOctaveControl() {
    final JButton btnDown = this.buildIconButton("◀");
    final JButton btnUp   = this.buildIconButton("▶");

    this.octaveLabel = new JLabel("Oct " + this.currentOctave, SwingConstants.CENTER);
    this.octaveLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
    this.octaveLabel.setForeground(COLOR_TEXT);
    this.octaveLabel.setPreferredSize(new Dimension(70, 30));

    btnDown.addActionListener(e -> this.changeOctave(-1));
    btnUp.addActionListener(e   -> this.changeOctave(+1));

    final JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 6, 0));
    panel.setBackground(COLOR_PANEL);
    panel.add(btnDown);
    panel.add(this.octaveLabel);
    panel.add(btnUp);
    return panel;
  }

  /**
   * Cambia la octava actual y notifica al teclado.
   *
   * @param delta +1 o -1
   */
  private void changeOctave(final int delta) {
    final int newOctave = Math.max(OCT_MIN, Math.min(OCT_MAX - 1, this.currentOctave + delta));
    if (newOctave != this.currentOctave) {
      this.currentOctave = newOctave;
      this.octaveLabel.setText("Oct " + newOctave);
      if (this.keyboardPanel != null) {
        this.keyboardPanel.setOctave(newOctave);
      }
    }
  }

  /**
   * Crea un botón de icono con estilo oscuro.
   */
  private JButton buildIconButton(final String text) {
    final JButton btn = new JButton(text);
    btn.setBackground(COLOR_BUTTON);
    btn.setForeground(COLOR_TEXT);
    btn.setFont(new Font("SansSerif", Font.BOLD, 14));
    btn.setFocusable(false);
    btn.setBorderPainted(false);
    btn.setPreferredSize(new Dimension(36, 30));
    btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

    btn.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override public void mouseEntered(final java.awt.event.MouseEvent e) {
        btn.setBackground(COLOR_BUTTON_HOVER);
      }
      @Override public void mouseExited(final java.awt.event.MouseEvent e) {
        btn.setBackground(COLOR_BUTTON);
      }
    });

    return btn;
  }

  /**
   * Crea el título decorativo del sintetizador.
   */
  private JLabel buildTitle() {
    final JLabel label = new JLabel("🎹 Music Synthesizer");
    label.setFont(new Font("SansSerif", Font.BOLD, 22));
    label.setForeground(COLOR_ACCENT);
    return label;
  }

}
