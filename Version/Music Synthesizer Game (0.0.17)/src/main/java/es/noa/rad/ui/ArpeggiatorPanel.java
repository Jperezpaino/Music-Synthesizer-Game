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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import es.noa.rad.arp.Arpeggiator;
import es.noa.rad.arp.Arpeggiator.Mode;

/**
 * Panel de control del Arpeggiador.
 *
 * <p>Permite activar/desactivar el arpeggiador y configurar sus parametros:
 * modo de reproduccion, velocidad y rango de octavas.</p>
 */
public final class ArpeggiatorPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final Color COLOR_BG      = new Color(22, 22, 30);
  private static final Color COLOR_SECTION = new Color(30, 35, 48);
  private static final Color COLOR_BORDER  = new Color(70, 100, 160);
  private static final Color COLOR_TITLE   = new Color(100, 160, 255);
  private static final Color COLOR_TEXT    = new Color(200, 200, 220);
  private static final Color COLOR_LABEL   = new Color(140, 140, 170);
  private static final Color COLOR_INACTIVE= new Color(70, 70, 90);

  /**
   * Crea el panel de arpeggiador.
   *
   * @param arp Arpeggiador a controlar
   */
  public ArpeggiatorPanel(final Arpeggiator arp) {
    this.setBackground(COLOR_BG);
    this.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));

    this.add(this.buildArpSection(arp));
  }

  // -------------------------------------------------------------------------
  // Seccion principal
  // -------------------------------------------------------------------------

  private JPanel buildArpSection(final Arpeggiator arp) {
    final JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(COLOR_SECTION);
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(COLOR_BORDER, 1),
        BorderFactory.createEmptyBorder(6, 10, 6, 10)
    ));
    panel.setPreferredSize(new Dimension(480, 110));

    final GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(2, 6, 2, 6);
    gbc.anchor = GridBagConstraints.WEST;

    // --- Fila 0: Checkbox de activacion ---
    final JCheckBox chkEnable = new JCheckBox("🎹  ARPEGGIADOR", arp.isEnabled());
    chkEnable.setBackground(COLOR_SECTION);
    chkEnable.setForeground(arp.isEnabled() ? COLOR_TITLE : COLOR_INACTIVE);
    chkEnable.setFont(new Font("SansSerif", Font.BOLD, 12));
    chkEnable.setFocusPainted(false);
    chkEnable.addActionListener(e -> {
      arp.setEnabled(chkEnable.isSelected());
      chkEnable.setForeground(chkEnable.isSelected() ? COLOR_TITLE : COLOR_INACTIVE);
    });
    gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 6;
    panel.add(chkEnable, gbc);
    gbc.gridwidth = 1;

    // --- Fila 1: Modo ---
    gbc.gridy = 1;
    gbc.gridx = 0;
    panel.add(makeLabel("Modo"), gbc);

    final JComboBox<Mode> cmbMode = new JComboBox<>(Mode.values());
    cmbMode.setSelectedItem(arp.getMode());
    cmbMode.setBackground(new Color(40, 40, 55));
    cmbMode.setForeground(COLOR_TEXT);
    cmbMode.setFont(new Font("SansSerif", Font.PLAIN, 11));
    cmbMode.setPreferredSize(new Dimension(110, 20));
    cmbMode.setFocusable(false);
    cmbMode.addActionListener(e ->
        arp.setMode((Mode) cmbMode.getSelectedItem()));
    gbc.gridx = 1; gbc.gridwidth = 2;
    panel.add(cmbMode, gbc);
    gbc.gridwidth = 1;

    // --- Fila 1: Velocidad (BPM) ---
    gbc.gridx = 3;
    panel.add(makeLabel("Velocidad"), gbc);

    final JSlider sldBpm = makeSlider(60, 300, arp.getBpm());
    sldBpm.addChangeListener(e -> arp.setBpm(sldBpm.getValue()));
    gbc.gridx = 4;
    panel.add(sldBpm, gbc);

    final JLabel lblBpmVal = new JLabel(arp.getBpm() + " bpm");
    lblBpmVal.setForeground(COLOR_LABEL);
    lblBpmVal.setFont(new Font("SansSerif", Font.PLAIN, 10));
    lblBpmVal.setPreferredSize(new Dimension(55, 16));
    sldBpm.addChangeListener(e -> lblBpmVal.setText(sldBpm.getValue() + " bpm"));
    gbc.gridx = 5;
    panel.add(lblBpmVal, gbc);

    // --- Fila 2: Rango de octavas ---
    gbc.gridy = 2;
    gbc.gridx = 0;
    panel.add(makeLabel("Octavas"), gbc);

    final JSlider sldOct = makeSlider(0, 2, arp.getOctaveRange());
    sldOct.setMajorTickSpacing(1);
    sldOct.setPaintTicks(true);
    sldOct.setSnapToTicks(true);
    sldOct.addChangeListener(e -> arp.setOctaveRange(sldOct.getValue()));
    gbc.gridx = 1; gbc.gridwidth = 2;
    panel.add(sldOct, gbc);
    gbc.gridwidth = 1;

    final String[] octLabels = { "+0 oct", "+1 oct", "+2 oct" };
    final JLabel lblOctVal = new JLabel(octLabels[arp.getOctaveRange()]);
    lblOctVal.setForeground(COLOR_LABEL);
    lblOctVal.setFont(new Font("SansSerif", Font.PLAIN, 10));
    lblOctVal.setPreferredSize(new Dimension(55, 16));
    sldOct.addChangeListener(e ->
        lblOctVal.setText(octLabels[Math.max(0, Math.min(2, sldOct.getValue()))]));
    gbc.gridx = 3;
    panel.add(lblOctVal, gbc);

    // Descripcion breve
    gbc.gridx = 4; gbc.gridy = 2; gbc.gridwidth = 2;
    final JLabel hint = new JLabel("Manten varias teclas y activa el arpeggiador");
    hint.setForeground(new Color(100, 100, 130));
    hint.setFont(new Font("SansSerif", Font.ITALIC, 10));
    panel.add(hint, gbc);

    return panel;
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private static JLabel makeLabel(final String text) {
    final JLabel lbl = new JLabel(text);
    lbl.setForeground(COLOR_LABEL);
    lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
    lbl.setHorizontalAlignment(SwingConstants.RIGHT);
    lbl.setPreferredSize(new Dimension(65, 16));
    return lbl;
  }

  private static JSlider makeSlider(final int min, final int max, final int value) {
    final JSlider s = new JSlider(SwingConstants.HORIZONTAL, min, max, value);
    s.setBackground(COLOR_SECTION);
    s.setForeground(COLOR_TEXT);
    s.setPreferredSize(new Dimension(110, 20));
    s.setPaintTicks(false);
    s.setPaintLabels(false);
    return s;
  }
}
