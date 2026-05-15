package es.noa.rad.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import es.noa.rad.fx.DelayEffect;
import es.noa.rad.fx.DistortionEffect;
import es.noa.rad.fx.ReverbEffect;
import es.noa.rad.synth.SoundEngine;

/**
 * Panel de efectos de audio (Delay, Reverb, Distorsión).
 *
 * <p>Muestra tres secciones en fila, cada una con un checkbox de activación
 * y sliders para los parámetros de cada efecto.</p>
 */
public final class EffectsPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final Color COLOR_BG        = new Color(22, 22, 30);
  private static final Color COLOR_SECTION   = new Color(32, 32, 44);
  private static final Color COLOR_BORDER    = new Color(60, 60, 80);
  private static final Color COLOR_TEXT      = new Color(200, 200, 220);
  private static final Color COLOR_LABEL     = new Color(140, 140, 170);
  private static final Color COLOR_DELAY     = new Color(60,  140, 200);
  private static final Color COLOR_REVERB    = new Color(120,  60, 200);
  private static final Color COLOR_DIST      = new Color(200,  70,  50);
  private static final Color COLOR_INACTIVE  = new Color(70,   70,  90);

  /**
   * Crea el panel de efectos.
   *
   * @param soundEngine Motor de audio cuyos efectos se controlan
   */
  public EffectsPanel(final SoundEngine soundEngine) {
    this.setBackground(COLOR_BG);
    this.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));

    final DelayEffect       delay = soundEngine.getDelayEffect();
    final ReverbEffect      reverb = soundEngine.getReverbEffect();
    final DistortionEffect  dist  = soundEngine.getDistortionEffect();

    this.add(this.buildDelaySection(delay));
    this.add(this.buildReverbSection(reverb));
    this.add(this.buildDistortionSection(dist));
  }

  // -------------------------------------------------------------------------
  // Sección Delay
  // -------------------------------------------------------------------------

  private JPanel buildDelaySection(final DelayEffect fx) {
    final JPanel panel = this.makeSectionPanel("⏱  DELAY", COLOR_DELAY, fx.isEnabled(), fx::setEnabled);

    final JSlider sldTime = makeSlider(1, 100, (int) (fx.getDelayTime() * 100));
    final JSlider sldFb   = makeSlider(0, 95,  (int) (fx.getFeedback()  * 100));
    final JSlider sldMix  = makeSlider(0, 100, (int) (fx.getMix()       * 100));

    sldTime.addChangeListener(e -> fx.setDelayTime(sldTime.getValue() / 100.0));
    sldFb.addChangeListener(e   -> fx.setFeedback(sldFb.getValue()   / 100.0));
    sldMix.addChangeListener(e  -> fx.setMix(sldMix.getValue()       / 100.0));

    addRow(panel, "Tiempo",   sldTime, "ms×100");
    addRow(panel, "Feedback", sldFb,   "%");
    addRow(panel, "Mix",      sldMix,  "%");
    return panel;
  }

  // -------------------------------------------------------------------------
  // Sección Reverb
  // -------------------------------------------------------------------------

  private JPanel buildReverbSection(final ReverbEffect fx) {
    final JPanel panel = this.makeSectionPanel("🔊  REVERB", COLOR_REVERB, fx.isEnabled(), fx::setEnabled);

    final JSlider sldRoom = makeSlider(0, 100, (int) (fx.getRoomSize() * 100));
    final JSlider sldMix  = makeSlider(0, 100, (int) (fx.getMix()      * 100));

    sldRoom.addChangeListener(e -> fx.setRoomSize(sldRoom.getValue() / 100.0));
    sldMix.addChangeListener(e  -> fx.setMix(sldMix.getValue()      / 100.0));

    addRow(panel, "Sala",  sldRoom, "%");
    addRow(panel, "Mix",   sldMix,  "%");
    return panel;
  }

  // -------------------------------------------------------------------------
  // Sección Distorsión
  // -------------------------------------------------------------------------

  private JPanel buildDistortionSection(final DistortionEffect fx) {
    final JPanel panel = this.makeSectionPanel("🎸  DISTORSIÓN", COLOR_DIST, fx.isEnabled(), fx::setEnabled);

    final JSlider sldDrive = makeSlider(10, 200, (int) (fx.getDrive() * 10));
    final JSlider sldMix   = makeSlider(0,  100, (int) (fx.getMix()  * 100));

    sldDrive.addChangeListener(e -> fx.setDrive(sldDrive.getValue() / 10.0));
    sldMix.addChangeListener(e   -> fx.setMix(sldMix.getValue()    / 100.0));

    addRow(panel, "Drive", sldDrive, "×10");
    addRow(panel, "Mix",   sldMix,   "%");
    return panel;
  }

  // -------------------------------------------------------------------------
  // Helpers de construcción UI
  // -------------------------------------------------------------------------

  /**
   * Crea un panel de sección con título coloreado y checkbox de activación.
   */
  private JPanel makeSectionPanel(final String title,
                                   final Color  titleColor,
                                   final boolean initialEnabled,
                                   final java.util.function.Consumer<Boolean> enableSetter) {
    final JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(COLOR_SECTION);
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(COLOR_BORDER, 1),
        BorderFactory.createEmptyBorder(6, 10, 6, 10)
    ));
    panel.setPreferredSize(new Dimension(210, 110));

    final GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(2, 4, 2, 4);
    gbc.anchor = GridBagConstraints.WEST;

    // Fila 0: checkbox + título
    final JCheckBox chk = new JCheckBox(title, initialEnabled);
    chk.setBackground(COLOR_SECTION);
    chk.setForeground(initialEnabled ? titleColor : COLOR_INACTIVE);
    chk.setFont(new Font("SansSerif", Font.BOLD, 12));
    chk.setFocusPainted(false);
    chk.addActionListener(e -> {
      enableSetter.accept(chk.isSelected());
      chk.setForeground(chk.isSelected() ? titleColor : COLOR_INACTIVE);
    });

    gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
    panel.add(chk, gbc);
    gbc.gridwidth = 1;

    return panel;
  }

  /**
   * Añade una fila con etiqueta + slider + unidad al panel.
   */
  private static void addRow(final JPanel panel,
                              final String labelText,
                              final JSlider slider,
                              final String unit) {
    final GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets  = new Insets(1, 4, 1, 4);
    gbc.anchor  = GridBagConstraints.WEST;

    // Contar filas ya existentes
    final int row = panel.getComponentCount() / 3;

    final JLabel lbl = new JLabel(labelText);
    lbl.setForeground(COLOR_LABEL);
    lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
    lbl.setPreferredSize(new Dimension(55, 16));
    lbl.setHorizontalAlignment(SwingConstants.RIGHT);

    final JLabel unitLbl = new JLabel(unit);
    unitLbl.setForeground(COLOR_LABEL);
    unitLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));

    gbc.gridx = 0; gbc.gridy = row; panel.add(lbl,     gbc);
    gbc.gridx = 1;                  panel.add(slider,   gbc);
    gbc.gridx = 2;                  panel.add(unitLbl,  gbc);
  }

  /**
   * Crea un slider horizontal compacto con estilo oscuro.
   */
  private static JSlider makeSlider(final int min, final int max, final int value) {
    final JSlider s = new JSlider(SwingConstants.HORIZONTAL, min, max, value);
    s.setBackground(COLOR_SECTION);
    s.setForeground(COLOR_TEXT);
    s.setPreferredSize(new Dimension(100, 20));
    s.setPaintTicks(false);
    s.setPaintLabels(false);
    return s;
  }

}
