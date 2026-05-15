package es.noa.rad.ui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import es.noa.rad.arp.Arpeggiator;
import es.noa.rad.drumachine.DrumMachine;
import es.noa.rad.synth.SoundEngine;

/**
 * Panel principal del sintetizador.
 *
 * <p>Combina el panel de controles (arriba), el teclado piano (centro)
 * y el panel de grabación/reproducción de melodías (abajo).</p>
 */
public final class SynthesizerPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final Color COLOR_BACKGROUND = new Color(28, 28, 35);

  private final KeyboardPanel    keyboardPanel;
  private final OscilloscopePanel oscilloscopePanel;

  /**
   * Crea el panel principal del sintetizador.
   *
   * @param soundEngine Motor de audio compartido
   */
  public SynthesizerPanel(final SoundEngine soundEngine) {
    this.setLayout(new BorderLayout());
    this.setBackground(COLOR_BACKGROUND);

    // Crear componentes
    this.keyboardPanel     = new KeyboardPanel(soundEngine, 4);
    this.oscilloscopePanel = new OscilloscopePanel(soundEngine);
    final ControlPanel    controlPanel    = new ControlPanel(soundEngine);
    final EffectsPanel    effectsPanel    = new EffectsPanel(soundEngine);
    final MelodyPanel     melodyPanel     = new MelodyPanel(soundEngine);
    final SequencerPanel  sequencerPanel  = new SequencerPanel(soundEngine);
    final DrumMachinePanel drumMachinePanel = new DrumMachinePanel(new DrumMachine());

    // Arpeggiador: crearlo y vincularlo al motor
    final Arpeggiator      arp            = new Arpeggiator(soundEngine);
    final ArpeggiatorPanel arpPanel       = new ArpeggiatorPanel(arp);
    soundEngine.setArpeggiator(arp);

    // Vincular octava y listener de notas
    controlPanel.setKeyboardPanel(this.keyboardPanel);
    this.keyboardPanel.setNoteListener(melodyPanel);

    // Scroll horizontal por si la ventana es pequeña
    final JScrollPane scrollPane = new JScrollPane(this.keyboardPanel,
        JScrollPane.VERTICAL_SCROLLBAR_NEVER,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setBackground(COLOR_BACKGROUND);
    scrollPane.getViewport().setBackground(COLOR_BACKGROUND);
    scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 75), 1));

    // Panel central: osciloscopio + teclado apilados
    final JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setBackground(COLOR_BACKGROUND);
    centerPanel.add(this.oscilloscopePanel, BorderLayout.NORTH);
    centerPanel.add(scrollPane,             BorderLayout.CENTER);

    // Panel norte: controles + efectos + arpeggiador apilados verticalmente
    final JPanel northPanel = new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
    northPanel.setBackground(COLOR_BACKGROUND);
    northPanel.add(controlPanel);
    northPanel.add(effectsPanel);
    northPanel.add(arpPanel);

    this.add(northPanel,   BorderLayout.NORTH);
    this.add(centerPanel,  BorderLayout.CENTER);

    // Panel sur: melody + secuenciador + drum machine apilados
    final JPanel southPanel = new JPanel(new BorderLayout());
    southPanel.setBackground(COLOR_BACKGROUND);
    southPanel.add(melodyPanel,      BorderLayout.NORTH);
    southPanel.add(sequencerPanel,   BorderLayout.CENTER);
    southPanel.add(drumMachinePanel, BorderLayout.SOUTH);

    this.add(southPanel, BorderLayout.SOUTH);
  }

  /** @return el panel de teclado (para el mapeo de teclas PC). */
  public KeyboardPanel getKeyboardPanel() {
    return this.keyboardPanel;
  }

}

